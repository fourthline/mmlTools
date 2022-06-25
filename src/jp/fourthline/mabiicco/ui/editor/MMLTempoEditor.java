/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.IViewTargetMarker;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTempoConverter;
import jp.fourthline.mmlTools.MMLTempoEvent;

/**
 * MMLTempo Editor
 *   edit.insert_tempo
 *   edit.edit_tempo
 *   edit.delete_tempo
 *   edit.label_tempo
 * @see AbstractMarkerEditor
 */
public final class MMLTempoEditor extends AbstractMarkerEditor<MMLTempoEvent> {

	private final Frame parentFrame;
	private final JMenuItem tempoConvertMenu;
	private final String tempoConvertCommand = "tempoConvert";

	public MMLTempoEditor(Frame parentFrame, IMMLManager mmlManager, IEditAlign editAlign, IViewTargetMarker viewTargetMarker) {
		super("tempo", mmlManager, editAlign, viewTargetMarker);
		this.parentFrame = parentFrame;
		this.tempoConvertMenu = newMenuItem(AppResource.appText("edit."+tempoConvertCommand));
		this.tempoConvertMenu.setActionCommand(tempoConvertCommand);
	}

	private int showTempoInputDialog(String title, int tempo) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(AppResource.appText("edit.label_"+suffix)));
		JSpinner spinner = NumberSpinner.createSpinner(tempo, 32, 255, 1);
		spinner.setFocusable(false);
		panel.add(spinner);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);

		UIUtils.setDefaultFocus(spinner);
		int status = JOptionPane.showConfirmDialog(this.parentFrame, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
			return ((Integer) spinner.getValue()).intValue();
		}

		return -1;
	}

	@Override
	protected List<MMLTempoEvent> getEventList() {
		return mmlManager.getMMLScore().getTempoEventList();
	}

	@Override
	protected void insertAction() {
		int tempo = mmlManager.getMMLScore().getTempoOnTick(targetTick);
		tempo = showTempoInputDialog(AppResource.appText("edit."+insertCommand), tempo);
		if (tempo < 0) {
			return;
		}

		// tempo align
		MMLTempoEvent insertTempo = new MMLTempoEvent(tempo, targetTick);
		insertTempo.appendToListElement(getEventList());
		System.out.println("insert tempo." + tempo);
	}

	@Override
	protected void editAction() {
		int tempo = showTempoInputDialog(AppResource.appText("edit."+editCommand), targetEvent.getTempo());
		if (tempo < 0) {
			return;
		}
		targetEvent.setTempo(tempo);
	}

	@Override
	protected void deleteAction() {
		getEventList().remove(targetEvent);
		System.out.println("delete tempo.");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String actionCommand = event.getActionCommand();
		if (actionCommand.equals(tempoConvertCommand)) {
			tempoConvertAction();
		} else {
			super.actionPerformed(event);
		}
	}

	private void tempoConvertAction() {
		int tempo = (targetEvent != null) ? targetEvent.getTempo() : mmlManager.getMMLScore().getTempoOnTick(targetTick);
		int targetTick = (targetEvent != null) ? targetEvent.getTickOffset() : this.targetTick;
		tempo = showTempoInputDialog(AppResource.appText("edit."+tempoConvertCommand), tempo);
		if (tempo < 0) {
			return;
		}

		// 複製データに対して変換実施.
		long diff = tempoConvert(tempo, targetTick, mmlManager.getMMLScore().clone());
		String title = AppResource.appText("edit.tempoConvert.result");
		String message = AppResource.appText("edit.tempoConvert.result_label") + " = " + diff;
		int ret = JOptionPane.showConfirmDialog(parentFrame, message, title, JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			// 実際のデータに対して変換実施.
			tempoConvert(tempo, targetTick, mmlManager.getMMLScore());
			mmlManager.updateActivePart(true);
		}
	}

	long tempoConvert(int tempo, int targetTick, MMLScore score) {
		MMLTempoEvent insertTempo = new MMLTempoEvent(tempo, targetTick);

		// 指定Tickより後ろのテンポを消して、新たなテンポイベントにするリストを作成する
		var tempoList = score.getTempoEventList();
		ArrayList<MMLTempoEvent> newTempoList = new ArrayList<>();
		for (var tempoEvent : tempoList) {
			if (tempoEvent.getTickOffset() < targetTick) {
				newTempoList.add(tempoEvent);
			}
		}
		newTempoList.add(insertTempo);

		MMLTempoConverter converter = new MMLTempoConverter(newTempoList);
		converter.convert(score);
		return converter.getConversionDiff();
	}

	public JMenuItem getTempoConvertMenu() {
		return tempoConvertMenu;
	}
}

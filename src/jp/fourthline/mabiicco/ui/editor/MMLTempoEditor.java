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
	private final JMenuItem tempoProcMenu;
	private final String tempoProcCommand = "tempoProc";

	public MMLTempoEditor(Frame parentFrame, IMMLManager mmlManager, IEditAlign editAlign, IViewTargetMarker viewTargetMarker) {
		super("tempo", mmlManager, editAlign, viewTargetMarker);
		this.parentFrame = parentFrame;
		this.tempoProcMenu = newMenuItem(AppResource.appText("edit."+tempoProcCommand));
		this.tempoProcMenu.setActionCommand(tempoProcCommand);
	}

	private int showTempoInputDialog(String title, int tempo) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(AppResource.appText("edit.label_"+suffix)));
		JSpinner spinner = NumberSpinner.createSpinner(tempo, 32, 255, 1);
		spinner.setFocusable(false);
		panel.add(spinner);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);

		setDefaultFocus(((JSpinner.NumberEditor)spinner.getEditor()).getTextField());
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
		super.actionPerformed(event);
		String actionCommand = event.getActionCommand();
		if (actionCommand.equals(tempoProcCommand)) {
			tempoProcAction();
			mmlManager.updateActivePart(true);
		}
	}

	private void tempoProcAction() {
		System.out.println("tempoProc " + targetEvent);
		int tempo = (targetEvent != null) ? targetEvent.getTempo() : mmlManager.getMMLScore().getTempoOnTick(targetTick);
		int targetTick = (targetEvent != null) ? targetEvent.getTickOffset() : this.targetTick;
		tempo = showTempoInputDialog(AppResource.appText("edit."+tempoProcCommand), tempo);
		if (tempo < 0) {
			return;
		}
		tempoProc(tempo, targetTick);
	}

	void tempoProc(int tempo, int targetTick) {
		MMLTempoEvent insertTempo = new MMLTempoEvent(tempo, targetTick);

		// 指定Tickより後ろのテンポを消して、新たなテンポイベントにするリストを作成する
		var tempoList = mmlManager.getMMLScore().getTempoEventList();
		ArrayList<MMLTempoEvent> newTempoList = new ArrayList<>();
		for (var tempoEvent : tempoList) {
			if (tempoEvent.getTickOffset() < targetTick) {
				newTempoList.add(tempoEvent);
			}
		}
		newTempoList.add(insertTempo);

		// 変換する
		for (var track : mmlManager.getMMLScore().getTrackList()) {
			for (var eventList : track.getMMLEventList()) {
				for (var noteEvent : eventList.getMMLNoteEventList()) {
					long endTick = MMLTempoEvent.getTickOffsetOnTime(newTempoList,
							MMLTempoEvent.getTimeOnTickOffset(tempoList, noteEvent.getEndTick()));
					long tickOffset = MMLTempoEvent.getTickOffsetOnTime(newTempoList,
							MMLTempoEvent.getTimeOnTickOffset(tempoList, noteEvent.getTickOffset()));
					noteEvent.setTickOffset((int)tickOffset);
					noteEvent.setTick((int)(endTick - tickOffset));
				}
			}
		}
		tempoList.clear();
		tempoList.addAll(newTempoList);
	}
	
	public JMenuItem getTempoProcMenu() {
		return tempoProcMenu;
	}
}

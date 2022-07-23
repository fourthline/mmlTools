/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.IViewTargetMarker;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.MMLTempoConverter;
import jp.fourthline.mmlTools.MMLTempoEvent;
import jp.fourthline.mmlTools.core.UndefinedTickException;

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

	public MMLTempoEditor(Frame parentFrame, IMMLManager mmlManager, IEditAlign editAlign, IViewTargetMarker viewTargetMarker) {
		super("tempo", mmlManager, editAlign, viewTargetMarker);
		this.parentFrame = parentFrame;
	}


	@Override
	protected List<MMLTempoEvent> getEventList() {
		return mmlManager.getMMLScore().getTempoEventList();
	}

	@Override
	protected boolean insertAction() {
		boolean ret = true;
		int tempo = mmlManager.getMMLScore().getTempoOnTick(targetTick);
		var dialog = new TempoInputDialog(parentFrame, tempo, true);
		do {
			tempo = dialog.showTempoInputDialog(AppResource.appText("edit."+insertCommand), AppResource.appText("edit.label_"+suffix));
			if (tempo < 0) {
				return false;
			}

			// tempo align
			List<MMLTempoEvent> newTempoList = new ArrayList<>(getEventList());
			new MMLTempoEvent(tempo, targetTick).appendToListElement(newTempoList);
			ret = updateTempoList(newTempoList, targetTick, true, dialog.isConvert(), dialog.isDeleteSubseq());
		} while (ret == false);
		return true;
	}

	@Override
	protected boolean editAction() {
		boolean ret = true;
		int tempo = targetEvent.getTempo();
		var dialog = new TempoInputDialog(parentFrame, tempo, true);
		do {
			tempo = dialog.showTempoInputDialog(AppResource.appText("edit."+editCommand), AppResource.appText("edit.label_"+suffix));
			if (tempo < 0) {
				return false;
			}
			List<MMLTempoEvent> newTempoList = new ArrayList<>(getEventList());
			new MMLTempoEvent(tempo, targetEvent.getTickOffset()).appendToListElement(newTempoList);
			ret = updateTempoList(newTempoList, targetEvent.getTickOffset(), true, dialog.isConvert(), dialog.isDeleteSubseq());
		} while (ret == false);
		return true;
	}

	@Override
	protected boolean deleteAction() {
		if (!MabiIccoProperties.getInstance().enableTempoDeleteWithConvert.get()) {
			getEventList().remove(targetEvent);
		} else {
			// テンポ削除時にもTick変換のダイアログ表示をする
			boolean ret = true;
			int tempo = targetEvent.getTempo();
			var dialog = new TempoInputDialog(parentFrame, tempo, false);
			do {
				tempo = dialog.showTempoInputDialog(AppResource.appText("edit."+deleteCommand), AppResource.appText("edit.label_"+suffix));
				if (tempo < 0) {
					return false;
				}
				List<MMLTempoEvent> newTempoList = new ArrayList<>(getEventList());
				newTempoList.remove(targetEvent);
				ret = updateTempoList(newTempoList, targetEvent.getTickOffset(), true, dialog.isConvert(), dialog.isDeleteSubseq());
			} while (ret == false);
		}
		return true;
	}

	/**
	 * テンポリストの更新を行う
	 * @param newTempoList  新しいテンポリスト
	 * @param targetTick    ターゲットTick
	 * @param confirm       確認ダイアログ表示オプション
	 * @param convertTick   tick変換オプション
	 * @param deleteSubseq  targetTick以降のテンポを削除するオプション
	 */
	boolean updateTempoList(List<MMLTempoEvent> newTempoList, int targetTick, boolean confirm, boolean convertTick, boolean deleteSubseq) {
		List<MMLTempoEvent> list = new ArrayList<>(newTempoList);
		if (deleteSubseq) {
			list.removeIf(t -> t.getTickOffset() > targetTick);
		}
		if (!convertTick) {
			var t = mmlManager.getMMLScore().getTempoEventList();
			t.clear();
			t.addAll(list);
			return true;
		}

		// 複製データに対して変換実施.
		int ret = JOptionPane.OK_OPTION;
		if (confirm) {
			var preScore = mmlManager.getMMLScore().clone();
			var converter = MMLTempoConverter.convert(preScore, list);
			try {
				preScore.generateAll();
			} catch (UndefinedTickException e) {
				JOptionPane.showMessageDialog(parentFrame, e.getMessage(), AppResource.getAppTitle(), JOptionPane.WARNING_MESSAGE);
				return false;
			}

			String title = AppResource.appText("edit.tempoConvert.result");
			String message = AppResource.appText("edit.tempoConvert.result_label") + " = " + converter.getConversionDiff();
			ret = JOptionPane.showConfirmDialog(parentFrame, message, title, JOptionPane.OK_CANCEL_OPTION);
		}
		if (ret == JOptionPane.OK_OPTION) {
			// 実際のデータに対して変換実施.
			MMLTempoConverter.convert(mmlManager.getMMLScore(), list);
			return true;
		}
		return false;
	}

	/**
	 * テンポ入力用ダイアログ
	 */
	private static class TempoInputDialog {
		private final Frame parentFrame;
		private final JCheckBox convertBox = new JCheckBox(AppResource.appText("edit.tempoConvert"));
		private final JCheckBox deleteSubseqBox = new JCheckBox(AppResource.appText("edit.delete_subseq_tempo"));
		private final JSpinner spinner;

		private TempoInputDialog(Frame parentFrame, int tempo, boolean tempoBox) {
			this.parentFrame = parentFrame;
			this.spinner = NumberSpinner.createSpinner(tempo, 32, 255, 1);
			spinner.setFocusable(false);
			spinner.setEnabled(tempoBox);
			UIUtils.setDefaultFocus(spinner);
			convertBox.setSelected(false);
			convertBox.setToolTipText(AppResource.appText("edit.tempoConvert.detail"));
			deleteSubseqBox.setSelected(false);
		}

		private int showTempoInputDialog(String title, String message) {
			JPanel panel = new JPanel();
			panel.add(new JLabel(message));
			panel.add(spinner);

			JPanel p1 = new JPanel();
			p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
			p1.add(convertBox);
			p1.add(deleteSubseqBox);
			JPanel cPanel = new JPanel(new BorderLayout());
			cPanel.add(panel, BorderLayout.CENTER);
			cPanel.add(p1, BorderLayout.SOUTH);

			int status = JOptionPane.showConfirmDialog(this.parentFrame, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
			if (status == JOptionPane.OK_OPTION) {
				return ((Integer) spinner.getValue()).intValue();
			}

			return -1;
		}

		private boolean isConvert() {
			return convertBox.isSelected();
		}

		private boolean isDeleteSubseq() {
			return deleteSubseqBox.isSelected();
		}
	}
}

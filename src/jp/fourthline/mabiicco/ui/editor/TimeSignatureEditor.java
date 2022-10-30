/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Frame;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.IViewTargetMarker;
import jp.fourthline.mmlTools.Measure;
import jp.fourthline.mmlTools.TimeSignature;
import jp.fourthline.mmlTools.core.UndefinedTickException;

/**
 * TimeSignature Editor
 *   edit.insert_timeSign
 *   edit.edit_timeSign
 *   edit.delete_timeSign
 * @see AbstractMarkerEditor
 */
public final class TimeSignatureEditor extends AbstractMarkerEditor<TimeSignature> {

	private final Frame parentFrame;
	private final JComboBox<String> timeCount = new JComboBox<>();
	private final JComboBox<String> timeBase = new JComboBox<>();
	private final JLabel sepLabel = new JLabel("/");

	public TimeSignatureEditor(Frame parentFrame, IMMLManager mmlManager, IEditAlign editAlign, IViewTargetMarker viewTargetMarker) {
		super("timeSign", mmlManager, editAlign, viewTargetMarker);
		this.parentFrame = parentFrame;
		initialComboBox();
	}

	private void initialComboBox() {
		String[] timeBaseList = { "1", "2", "4", "8", "16", "32", "64" };

		for (int i = 1; i <= 32; i++) {
			timeCount.addItem(Integer.toString(i));
		}
		timeCount.addItem(Integer.toString(64));

		for (String s : timeBaseList ) {
			timeBase.addItem(s);
		}
		timeBase.setMaximumRowCount(2);   // JComboBoxの性能劣化対策
	}

	@Override
	protected int targetTickAlign(int baseTick) {
		return new Measure(mmlManager.getMMLScore(), baseTick).measuredTick();
	}

	private TimeSignature showTimeSignatureInputDialog(String title) {
		JPanel panel = new JPanel();
		panel.add(timeCount);
		panel.add(sepLabel);
		panel.add(timeBase);

		var measure = new Measure(mmlManager.getMMLScore(), targetTick);
		timeCount.setSelectedItem(measure.timeCount());
		try {
			timeBase.setSelectedItem(measure.timeBase());
		} catch (UndefinedTickException e) {
			e.printStackTrace();
			return null;
		}

		int status = JOptionPane.showConfirmDialog(this.parentFrame, panel, title, JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
			try {
				String s1 = timeCount.getItemAt(timeCount.getSelectedIndex());
				String s2 = timeBase.getItemAt(timeBase.getSelectedIndex());
				return new TimeSignature(mmlManager.getMMLScore(), targetTick, s1, s2);
			} catch (UndefinedTickException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected List<TimeSignature> getEventList() {
		return mmlManager.getMMLScore().getTimeSignatureList();
	}

	@Override
	protected boolean insertAction() {
		TimeSignature ts = showTimeSignatureInputDialog(AppResource.appText("edit."+insertCommand));
		if (ts == null) {
			return false;
		}

		mmlManager.getMMLScore().addTimeSignature(ts);
		return true;
	}

	@Override
	protected boolean editAction() {
		TimeSignature ts = showTimeSignatureInputDialog(AppResource.appText("edit."+editCommand));
		if (ts == null) {
			return false;
		}

		mmlManager.getMMLScore().addTimeSignature(ts);
		return true;
	}

	@Override
	protected boolean deleteAction() {
		mmlManager.getMMLScore().removeTimeSignature(targetEvent);
		return true;
	}
}

/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.IViewTargetMarker;
import fourthline.mmlTools.MMLTempoEvent;

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
}

/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
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

	public MMLTempoEditor(IMMLManager mmlManager, IEditAlign editAlign) {
		super("tempo", mmlManager, editAlign);
	}

	private int showTempoInputDialog(String title, int tempo) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(AppResource.appText("edit.label_"+suffix)));
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(tempo, 32, 255, 1));
		spinner.setFocusable(false);
		panel.add(spinner);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);

		int status = JOptionPane.showConfirmDialog(null, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
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
		int tempo = showTempoInputDialog(AppResource.appText("edit."+insertCommand), 120);
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

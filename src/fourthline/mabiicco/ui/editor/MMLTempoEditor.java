/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

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

	private final IEditAlign editAlign;
	private final IMMLManager mmlManager;

	public MMLTempoEditor(IMMLManager mmlManager, IEditAlign editAlign) {
		super("tempo");
		this.editAlign = editAlign;
		this.mmlManager = mmlManager;
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
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals(insertCommand)) {
			int tempo = showTempoInputDialog(AppResource.appText("edit."+insertCommand), 120);
			if (tempo < 0) {
				return;
			}

			// tempo align
			int tick = targetTick - (targetTick % this.editAlign.getEditAlign());
			MMLTempoEvent insertTempo = new MMLTempoEvent(tempo, tick);
			insertTempo.appendToListElement(eventList);
			System.out.println("insert tempo." + tempo);
		} else if (actionCommand.equals(editCommand)) {
			int tempo = showTempoInputDialog(AppResource.appText("edit."+editCommand), targetEvent.getTempo());
			if (tempo < 0) {
				return;
			}
			targetEvent.setTempo(tempo);
		} else if (actionCommand.equals(deleteCommand)) {
			eventList.remove(targetEvent);
			System.out.println("delete tempo.");
		}

		mmlManager.updateActivePart();
	}
}

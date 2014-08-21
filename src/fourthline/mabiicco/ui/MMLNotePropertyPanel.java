/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

import fourthline.mabiicco.AppResource;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


/**
 * ノートプロパティを編集するためのダイアログ表示で用いるPanelです.
 */
public class MMLNotePropertyPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 646262293010195918L;

	private JSpinner velocityValueField;
	private JCheckBox velocityCheckBox;
	private JCheckBox tuningNoteCheckBox;
	private MMLNoteEvent noteEvent[];
	private MMLEventList eventList;

	public void showDialog() {
		int status = JOptionPane.showConfirmDialog(null, 
				this,
				AppResource.getText("note.properties"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (status == JOptionPane.OK_OPTION) {
			applyProperty();
		}
	}

	/**
	 * Create the panel.
	 */
	public MMLNotePropertyPanel() {
		this(null, null);
	}

	/**
	 * Create the panel.
	 */
	public MMLNotePropertyPanel(MMLNoteEvent noteEvent[], MMLEventList eventList) {
		super();
		setLayout(null);

		velocityValueField = new JSpinner();
		velocityValueField.setModel(new SpinnerNumberModel(8, 0, 15, 1));
		velocityValueField.setBounds(209, 37, 72, 19);
		add(velocityValueField);

		velocityCheckBox = new JCheckBox(AppResource.getText("note.properties.velocity"));
		velocityCheckBox.setBounds(42, 36, 150, 21);
		add(velocityCheckBox);

		tuningNoteCheckBox = new JCheckBox(AppResource.getText("note.properties.tuning"));
		tuningNoteCheckBox.setBounds(43, 99, 220, 21);
		add(tuningNoteCheckBox);

		this.noteEvent = noteEvent;
		this.eventList = eventList;
		setNoteEvent();
	}

	private void setNoteEvent() {
		if (noteEvent == null) {
			return;
		}

		tuningNoteCheckBox.setSelected( noteEvent[0].isTuningNote() );

		MMLNoteEvent prevNote = eventList.searchPrevNoteOnTickOffset(noteEvent[0].getTickOffset());
		int velocity = noteEvent[0].getVelocity();
		velocityValueField.setValue(velocity);
		if (prevNote.getVelocity() != velocity) {
			velocityCheckBox.setSelected(true);
		} else {
			velocityCheckBox.setSelected(false);
		}
	}

	/**
	 * パネルの情報をノートに反映します.
	 */
	public void applyProperty() {
		for (MMLNoteEvent targetNote : noteEvent) {
			if (tuningNoteCheckBox.isSelected()) {
				targetNote.setTuningNote(true);
			} else {
				targetNote.setTuningNote(false);
			}

			Integer value = (Integer) velocityValueField.getValue();
			int prevVelocity = targetNote.getVelocity();
			if (!velocityCheckBox.isSelected()) {
				targetNote.setVelocity(value.intValue());
			} else {
				for (MMLNoteEvent note : eventList.getMMLNoteEventList()) {
					if (note.getTickOffset() < targetNote.getTickOffset()) {
						continue;
					}
					if (prevVelocity == note.getVelocity()) {
						note.setVelocity(value);
					} else {
						break;
					}
				}
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 150);
	}
}

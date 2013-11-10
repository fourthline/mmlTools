/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

import fourthline.mmlTools.MMLNoteEvent;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


/**
 * ノートプロパティを編集するためのダイアログ表示で用いるPanelです.
 */
public class MMLNotePropertyPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 646262293010195918L;

	private JSpinner velocityValueField;
	private JCheckBox velocityCheckBox;
	private JCheckBox tuningNoteCheckBox;
	private MMLNoteEvent noteEvent;

	public void showDialog() {
		int status = JOptionPane.showConfirmDialog(null, 
				this,
				"ノートのプロパティ", 
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
		this(null);
	}

	/**
	 * Create the panel.
	 */
	public MMLNotePropertyPanel(MMLNoteEvent noteEvent) {
		super();
		setLayout(null);

		velocityValueField = new JSpinner();
		velocityValueField.setModel(new SpinnerNumberModel(8, 0, 15, 1));
		velocityValueField.setBounds(209, 37, 72, 19);
		add(velocityValueField);

		velocityCheckBox = new JCheckBox("音量コマンド（0～15）");
		velocityCheckBox.setBounds(42, 36, 127, 21);
		velocityCheckBox.addActionListener(this);
		add(velocityCheckBox);

		tuningNoteCheckBox = new JCheckBox("調律音符（L64を使って連結します）");
		tuningNoteCheckBox.setBounds(43, 99, 207, 21);
		add(tuningNoteCheckBox);

		setNoteEvent(noteEvent);
	}

	private void setNoteEvent(MMLNoteEvent noteEvent) {
		this.noteEvent = noteEvent;
		if (noteEvent == null) {
			return;
		}

		tuningNoteCheckBox.setSelected( noteEvent.isTuningNote() );

		int velocity = noteEvent.getVelocity();
		if (velocity >= 0) {
			velocityCheckBox.setSelected(true);
			velocityValueField.setEnabled(true);
			velocityValueField.setValue(velocity);
		} else {
			velocityCheckBox.setSelected(false);
			velocityValueField.setEnabled(false);
		}
	}

	/**
	 * パネルの情報をノートに反映します.
	 */
	public void applyProperty() {
		if (tuningNoteCheckBox.isSelected()) {
			noteEvent.setTuningNote(true);
		} else {
			noteEvent.setTuningNote(false);
		}

		if (velocityCheckBox.isSelected()) {
			Integer value = (Integer) velocityValueField.getValue();
			noteEvent.setVelocity(value.intValue());
		} else {
			noteEvent.setVelocity(MMLNoteEvent.NO_VEL);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 150);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == velocityCheckBox) {
			velocityValueField.setEnabled( velocityCheckBox.isSelected() );
		}
	}
}

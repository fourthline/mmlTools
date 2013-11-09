/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JCheckBox;

import fourthline.mmlTools.MMLNoteEvent;


/**
 * ノートプロパティを編集するためのダイアログ表示で用いるPanelです.
 */
public class MMLNotePropertyPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 646262293010195918L;

	private JFormattedTextField formattedTextField;
	private JCheckBox checkBox;
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

		formattedTextField = new JFormattedTextField();
		formattedTextField.setEnabled(false);
		formattedTextField.setBounds(209, 37, 72, 19);
		add(formattedTextField);

		checkBox = new JCheckBox("音量コマンド（0～15）");
		checkBox.setEnabled(false);
		checkBox.setBounds(42, 36, 127, 21);
		add(checkBox);

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
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 150);
	}
}

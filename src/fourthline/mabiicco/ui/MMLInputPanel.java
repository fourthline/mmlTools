/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLTrack;

public final class MMLInputPanel extends JPanel {
	private static final long serialVersionUID = -2517820687250637949L;
	private JTextField textField;
	private JComboBox<InstClass> comboBox;

	JRadioButton overrideButton;
	JRadioButton newTrackButton;

	MMLSeqView parent;
	MMLTrack track;

	public MMLInputPanel() {
		this(null);
	}

	/**
	 * @param parent
	 */
	public MMLInputPanel(MMLSeqView parent) {
		this.parent = parent;

		setLayout(null);

		JPanel panel1 = new JPanel();
		panel1.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), AppResource.appText("mml.input.method"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel1.setBounds(26, 28, 275, 87);
		add(panel1);
		panel1.setLayout(null);

		overrideButton = new JRadioButton(AppResource.appText("mml.input.method.override"));
		overrideButton.setBounds(8, 51, 174, 21);
		panel1.add(overrideButton);

		newTrackButton = new JRadioButton(AppResource.appText("mml.input.method.new"));
		newTrackButton.setBounds(8, 23, 144, 21);
		panel1.add(newTrackButton);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(overrideButton);
		buttonGroup.add(newTrackButton);
		newTrackButton.setSelected(true);

		JPanel panel2 = new JPanel();
		panel2.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), AppResource.appText("mml.input.instrument"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel2.setBounds(26, 140, 275, 75);
		add(panel2);
		panel2.setLayout(null);

		InstClass insts[] = null;
		try {
			insts = MabiDLS.getInstance().getInsts();
		} catch (NullPointerException e) {}
		if (insts == null) {
			comboBox = new JComboBox<>();
		} else {
			comboBox = new JComboBox<>(insts);
		}
		comboBox.setBounds(25, 33, 193, 19);
		comboBox.setMaximumRowCount(30);
		panel2.add(comboBox);

		JPanel panel3 = new JPanel();
		panel3.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), AppResource.appText("mml.input.trackname"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel3.setBounds(26, 249, 275, 75);
		add(panel3);
		panel3.setLayout(null);

		textField = new JTextField(20);
		textField.setBounds(27, 35, 193, 19);
		// TODO: 日本語を入力したとき、再生時にCPU負荷がかかる問題あり.
		// textField.setEditable(false);
		panel3.add(textField);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 350);
	}

	/**
	 * トラック名を指定して、ダイアログを表示する.
	 * @param trackName トラック名
	 */
	public void showDialog(Frame parentFrame, String trackName) {
		textField.setText(trackName);

		String mml = getClipboardString();

		track = new MMLTrack(mml);
		// 各パートが全て空であれば、ダイアログ表示しない。
		if ( (track.getMelody().length() == 0) &&
				(track.getChord1().length() == 0) &&
				(track.getChord2().length() == 0) &&
				(track.getSongEx().length() == 0) ) {
			return;
		}

		int status = JOptionPane.showConfirmDialog(parentFrame, 
				this,
				AppResource.appText("mml.input"), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (status == JOptionPane.OK_OPTION) {
			applyMMLTrack();
		}
	}

	private void applyMMLTrack() {
		InstClass inst = (InstClass)comboBox.getSelectedItem();
		track.setProgram(inst.getProgram());
		track.setTrackName(textField.getText());

		if (overrideButton.isSelected()) {
			parent.setMMLselectedTrack(track);
		} else {
			parent.addMMLTrack(track);
		}
	}

	private String getClipboardString() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();

		try {
			return (String) clip.getData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
}

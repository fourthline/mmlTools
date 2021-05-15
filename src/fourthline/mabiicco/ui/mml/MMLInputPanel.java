/*
 * Copyright (C) 2013-2021 たんらる
 */

package fourthline.mabiicco.ui.mml;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLTrack;

public final class MMLInputPanel extends JPanel {
	private static final long serialVersionUID = -2517820687250637949L;

	private final JDialog dialog;
	private JTextField textField;
	private JComboBox<InstClass> comboBox;

	private JRadioButton overrideButton;
	private JRadioButton newTrackButton;

	private final IMMLManager mmlManager;
	private MMLTrack track;
	private final Window parentFrame;

	/**
	 * @param mmlManager
	 */
	public MMLInputPanel(Frame parentFrame, String trackName, IMMLManager mmlManager) {
		this.mmlManager = mmlManager;
		this.dialog = new JDialog(parentFrame, AppResource.appText("mml.input"), true);
		this.parentFrame = parentFrame;

		initializePanel(trackName);
	}

	private void initializePanel(String trackName) {
		setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(null);

		JPanel panel1 = new JPanel();
		panel1.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), AppResource.appText("mml.input.method"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel1.setBounds(26, 28, 275, 87);
		contentPanel.add(panel1);
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
		contentPanel.add(panel2);
		panel2.setLayout(null);

		comboBox = new JComboBox<>( MabiDLS.getInstance().getAvailableInstByInstType(InstType.MAIN_INST_LIST) );
		comboBox.setBounds(25, 33, 193, 19);
		comboBox.setMaximumRowCount(30);
		panel2.add(comboBox);

		JPanel panel3 = new JPanel();
		panel3.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), AppResource.appText("mml.input.trackname"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel3.setBounds(26, 249, 275, 75);
		contentPanel.add(panel3);
		panel3.setLayout(null);

		textField = new JTextField(20);
		textField.setText(trackName);
		textField.setBounds(27, 35, 193, 19);
		panel3.add(textField);

		/* button */
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton(AppResource.appText("mml.input.okButton"));
		okButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(okButton);
		okButton.addActionListener((event) -> {
			applyMMLTrack();
			dialog.setVisible(false);
		});

		JButton cancelButton = new JButton(AppResource.appText("mml.input.cancelButton"));
		cancelButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(cancelButton);
		cancelButton.setFocusable(false);
		cancelButton.addActionListener((event) -> {
			dialog.setVisible(false);
		});

		add(buttonPanel, BorderLayout.SOUTH);
		add(contentPanel, BorderLayout.CENTER);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 380);
	}

	/**
	 * トラック名を指定して、ダイアログを表示する.
	 * @param trackName トラック名
	 */
	public void showDialog() {
		String mml = getClipboardString();

		track = new MMLTrack().setMML(mml);
		// 各パートが全て空であれば、ダイアログ表示しない。
		if ( track.isEmpty() ) {
			return;
		}

		dialog.getContentPane().add(this);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parentFrame);
		dialog.setVisible(true);
	}

	private void applyMMLTrack() {
		InstClass inst = (InstClass)comboBox.getSelectedItem();
		track.setProgram(inst.getProgram());
		track.setTrackName(textField.getText());

		if (overrideButton.isSelected()) {
			mmlManager.setMMLselectedTrack(track);
		} else {
			mmlManager.addMMLTrack(track);
		}
	}

	public static String getClipboardString() {
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

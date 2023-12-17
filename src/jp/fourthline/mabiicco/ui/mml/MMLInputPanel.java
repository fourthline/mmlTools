/*
 * Copyright (C) 2013-2023 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.InstType;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLText;

public final class MMLInputPanel extends JPanel {
	private static final long serialVersionUID = -2517820687250637949L;

	private final JDialog dialog;
	private JTextField textField;
	private JComboBox<InstClass> comboBox;

	private JRadioButton overrideButton;
	private JRadioButton newTrackButton;
	private final JPanel buttonPanel = new JPanel();
	private final JButton okButton = new JButton("OK" /*AppResource.appText("mml.input.okButton")*/);
	private final JButton cancelButton = new JButton("Cancel" /*AppResource.appText("mml.input.cancelButton")*/);
	private final JLabel mmlInfo = new JLabel();
	private final JCheckBox excludeSongCheckBox = new JCheckBox(AppResource.appText("instrument.excludeSongPart"));

	private final IMMLManager mmlManager;
	private MMLTrack track;
	private final Window parentFrame;

	private final Dimension prefSize = new Dimension(350, 380);

	private final MMLText mmlText = new MMLText();

	/**
	 * @param mmlManager
	 */
	public MMLInputPanel(Frame parentFrame, String trackName, IMMLManager mmlManager) {
		this(parentFrame, trackName, mmlManager, false);
	}

	public MMLInputPanel(Frame parentFrame, String trackName, IMMLManager mmlManager, boolean excludeSongPart) {
		this.mmlManager = mmlManager;
		this.dialog = new JDialog(parentFrame, AppResource.appText("mml.input"), true);
		this.parentFrame = parentFrame;

		UIUtils.dialogCloseAction(dialog);

		initializePanel(trackName);
		excludeSongCheckBox.setSelected(excludeSongPart);
		updateInfo();
	}

	private void initializePanel(String trackName) {
		setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(null);

		JPanel mmlPanel = UIUtils.createTitledPanel("mml.input.info");
		mmlPanel.setBounds(26, 10, 275, 80);
		mmlPanel.add(mmlInfo);
		mmlInfo.setBounds(26, 20, 230, 21);
		excludeSongCheckBox.setBounds(26, 45, 230, 21);
		excludeSongCheckBox.addActionListener(t -> updateInfo());
		mmlPanel.add(excludeSongCheckBox);
		contentPanel.add(mmlPanel);

		JPanel panel1 = UIUtils.createTitledPanel("mml.input.method");
		panel1.setBounds(26, 100, 275, 87);
		contentPanel.add(panel1);

		overrideButton = new JRadioButton(AppResource.appText("mml.input.method.override"));
		overrideButton.setBounds(26, 51, 174, 21);
		panel1.add(overrideButton);

		newTrackButton = new JRadioButton(AppResource.appText("mml.input.method.new"));
		newTrackButton.setBounds(26, 23, 144, 21);
		panel1.add(newTrackButton);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(overrideButton);
		buttonGroup.add(newTrackButton);
		newTrackButton.setSelected(true);

		JPanel panel2 = UIUtils.createTitledPanel("mml.input.instrument");
		panel2.setBounds(26, 196, 275, 65);
		contentPanel.add(panel2);
		panel2.setLayout(null);

		comboBox = new JComboBox<>( MabiDLS.getInstance().getAvailableInstByInstType(InstType.MAIN_INST_LIST) );
		comboBox.setBounds(25, 26, 193, 19);
		comboBox.setMaximumRowCount(30);
		comboBox.addActionListener(t -> updateInfo());
		panel2.add(comboBox);

		JPanel panel3 = UIUtils.createTitledPanel("mml.input.trackname");
		panel3.setBounds(26, 270, 275, 65);
		contentPanel.add(panel3);
		panel3.setLayout(null);

		textField = new JTextField(20);
		textField.setText(trackName);
		textField.setBounds(27, 30, 193, 19);
		panel3.add(textField);

		/* button */
		okButton.setMargin(new Insets(5, 10, 5, 10));
		okButton.setFocusPainted(true);
		buttonPanel.add(okButton);
		okButton.addActionListener((event) -> {
			applyMMLTrack();
			dialog.setVisible(false);
		});

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
		return prefSize;
	}

	private void updateInfo() {
		var b = ((InstClass)(comboBox.getSelectedItem())).getType().allowExcludeSongPart();
		excludeSongCheckBox.setVisible(b);

		mmlText.setExcludeSongPart(b && excludeSongCheckBox.isSelected());
		mmlInfo.setText(mmlText.mmlRankFormat());
	}

	/**
	 * トラック名を指定して、ダイアログを表示する.
	 */
	public void showDialog() {
		showDialog(getClipboardString());
	}

	public void showDialog(String mml) {
		track = new MMLTrack().setMML(mml);
		// 各パートが全て空であれば、ダイアログ表示しない。
		if ( track.isEmpty() ) {
			return;
		}
		mmlText.setMMLText(mml);
		updateInfo();

		dialog.getContentPane().add(this);
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parentFrame);
		dialog.setVisible(true);
	}

	void applyMMLTrack() {
		InstClass inst = (InstClass)comboBox.getSelectedItem();
		track.setProgram(inst.getProgram());
		track.setTrackName(textField.getText());
		track.setSongProgram(mmlText.isExcludeSongPart() ? MMLTrack.EXCLUDE_SONG : MMLTrack.NO_CHORUS);

		if (overrideButton.isSelected()) {
			mmlManager.setMMLselectedTrack(track);
		} else {
			mmlManager.addMMLTrack(track);
		}
	}

	void setMMLTrack(MMLTrack track) {
		this.track = track;
	}

	void setOverride(boolean b) {
		overrideButton.setSelected(b);
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

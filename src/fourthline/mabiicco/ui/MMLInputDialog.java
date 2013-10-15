/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.parser.MMLTrack;

public class MMLInputDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2517820687250637949L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JComboBox<InstClass> comboBox;

	JRadioButton overrideButton;
	JRadioButton newTrackButton;

	MMLTrack track;

	MMLSeqView parent;



	public MMLInputDialog() {
		this(null, null);
	}

	/**
	 * 現在のトラック名を指定してダイアログを作成する。
	 * @param trackName
	 */
	public MMLInputDialog(MMLSeqView parent, MMLTrack track) {
		this.parent = parent;
		setModal(true);
		setResizable(false);

		setTitle("クリップボードからのMML入力");
		setBounds(100, 100, 352, 417);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JPanel panel1 = new JPanel();
		panel1.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "\u5165\u529B\u65B9\u6CD5", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel1.setBounds(26, 28, 275, 87);
		contentPanel.add(panel1);
		panel1.setLayout(null);

		overrideButton = new JRadioButton("現在のトラックに上書き");
		overrideButton.setBounds(8, 51, 174, 21);
		panel1.add(overrideButton);

		newTrackButton = new JRadioButton("新しいトラックを作成");
		newTrackButton.setBounds(8, 23, 144, 21);
		panel1.add(newTrackButton);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(overrideButton);
		buttonGroup.add(newTrackButton);
		newTrackButton.setSelected(true);

		JPanel panel2 = new JPanel();
		panel2.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "\u697D\u5668\u8A2D\u5B9A", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel2.setBounds(26, 140, 275, 75);
		contentPanel.add(panel2);
		panel2.setLayout(null);

		InstClass insts[] = null;
		try {
			insts = MabiDLS.getInstance().getInsts();
		} catch (NullPointerException e) {}
		if (insts == null) {
			comboBox = new JComboBox<InstClass>();
		} else {
			comboBox = new JComboBox<InstClass>(insts);
		}
		comboBox.setBounds(25, 33, 193, 19);
		panel2.add(comboBox);



		JPanel panel3 = new JPanel();
		panel3.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "\u30C8\u30E9\u30C3\u30AF\u540D", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel3.setBounds(26, 249, 275, 75);
		contentPanel.add(panel3);
		panel3.setLayout(null);

		textField = new JTextField();
		if (track != null) {
			textField.setText(track.getName());
		}
		textField.setBounds(27, 35, 193, 19);
		panel3.add(textField);
		textField.setColumns(10);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						applyMMLTrack();
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
				InputMap imap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
				getRootPane().getActionMap().put("escape", new AbstractAction(){
					private static final long serialVersionUID = 8365149917383455221L;

					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				}); 
			}
		}
	}


	@Override
	public void setVisible(boolean visible) {
		String mml = getClipboardString();

		track = new MMLTrack(mml);
		// 各パートが全て空であれば、ダイアログ表示しない。
		if ( (track.getMelody().length() == 0) &&
				(track.getChord1().length() == 0) &&
				(track.getChord2().length() == 0) ) {
			return;
		}

		super.setVisible(visible);
	}


	private void applyMMLTrack() {
		track.setName(textField.getText());
		InstClass inst = (InstClass)comboBox.getSelectedItem();
		track.setProgram(inst.getProgram());

		if (overrideButton.isSelected()) {
			parent.setMMLselectedTrack(track);
		} else {
			parent.addMMLTrack("");
			parent.setMMLselectedTrack(track);
		}
	}


	private String getClipboardString() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();

		try {
			return (String) clip.getData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}

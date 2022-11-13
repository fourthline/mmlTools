/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.IFileState;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.TimeSignature;

import javax.swing.JTextField;
import javax.swing.JComboBox;



public final class MMLScorePropertyPanel extends JPanel {
	private static final long serialVersionUID = -3976816581383137814L;

	private final JTextField titleField = new JTextField();
	private final JTextField authorField = new JTextField();
	private final JComboBox<String> timeCount = new JComboBox<>(TimeSignature.TIME_COUNT_LIST);
	private final JComboBox<String> timeBase = new JComboBox<>(TimeSignature.TIME_BASE_LIST);

	private final Dimension prefSize = new Dimension(300, 170);

	public MMLScorePropertyPanel() {
		initializePanel();
	}

	private void initializePanel() {
		setBounds(100, 100, 300, 170);
		setLayout(null);

		JLabel label1 = new JLabel(AppResource.appText("score_property.name"));
		label1.setBounds(12, 40, 90, 14);
		add(label1);

		JLabel label2 = new JLabel(AppResource.appText("score_property.author"));
		label2.setBounds(12, 73, 90, 14);
		add(label2);

		JLabel label3 = new JLabel(AppResource.appText("score_property.measure"));
		label3.setBounds(12, 105, 90, 14);
		add(label3);

		JLabel label4 = new JLabel("/");
		label4.setBounds(198, 105, 15, 14);
		add(label4);

		titleField.setBounds(125, 37, 152, 19);
		add(titleField);
		titleField.setColumns(10);

		authorField.setColumns(10);
		authorField.setBounds(125, 70, 152, 19);

		timeCount.setBounds(125, 102, 63, 19);
		timeBase.setBounds(214, 102, 63, 19);

		timeBase.setMaximumRowCount(4);   // JComboBoxの性能劣化対策

		add(timeCount);
		add(timeBase);
		add(authorField);
	}

	@Override
	public Dimension getPreferredSize() {
		return prefSize;
	}

	private void initialComboBox(String baseTime) {

		timeCount.setSelectedItem("4");
		timeBase.setSelectedItem("4");

		try {
			String[] base = baseTime.split("/");
			timeCount.setSelectedItem(base[0]);
			timeBase.setSelectedItem(base[1]);
		} catch (ArrayIndexOutOfBoundsException e) {}
	}

	/**
	 * ダイアログを表示する.
	 */
	public void showDialog(JFrame parent, MMLScore score, IFileState fileState) {
		titleField.setText(score.getTitle());
		authorField.setText(score.getAuthor());
		initialComboBox(score.getBaseTime());

		int status = JOptionPane.showConfirmDialog(parent, 
				this,
				AppResource.appText("score_property"), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (status == JOptionPane.OK_OPTION) {
			score.setTitle(titleField.getText());
			score.setAuthor(authorField.getText());

			String time = timeCount.getSelectedItem()+"/"+timeBase.getSelectedItem();
			score.setBaseTime(time);
			fileState.saveState();
		}
	}
}

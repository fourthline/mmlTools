/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.mml;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.IFileState;
import fourthline.mmlTools.MMLScore;

import javax.swing.JTextField;
import javax.swing.JComboBox;



public final class MMLScorePropertyPanel extends JPanel {
	private static final long serialVersionUID = -3976816581383137814L;

	private JTextField titleField;
	private JTextField authorField;
	private JComboBox<String> timeCount;
	private JComboBox<String> timeBase;

	public MMLScorePropertyPanel() {
		initializePanel();
	}

	private void initializePanel() {
		setBounds(100, 100, 300, 170);
		setLayout(null);

		JLabel label1 = new JLabel(AppResource.appText("score_property.name"));
		label1.setBounds(12, 40, 90, 13);
		add(label1);

		JLabel label2 = new JLabel(AppResource.appText("score_property.author"));
		label2.setBounds(12, 73, 90, 13);
		add(label2);

		JLabel label3 = new JLabel(AppResource.appText("score_property.measure"));
		label3.setBounds(12, 105, 90, 13);
		add(label3);

		JLabel label4 = new JLabel("/");
		label4.setBounds(198, 105, 15, 13);
		add(label4);

		titleField = new JTextField();
		titleField.setBounds(125, 37, 152, 19);
		add(titleField);
		titleField.setColumns(10);

		authorField = new JTextField();
		authorField.setColumns(10);
		authorField.setBounds(125, 70, 152, 19);
		add(authorField);

		timeCount = new JComboBox<String>();
		timeCount.setBounds(125, 102, 63, 19);
		add(timeCount);

		timeBase = new JComboBox<String>();
		timeBase.setBounds(214, 102, 63, 19);
		add(timeBase);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(300, 170);
	}

	private void initialComboBox(String baseTime) {
		String timeBaseList[]  = { "1", "2", "4", "8", "16", "32", "64" };

		for (int i = 1; i <= 32; i++) {
			timeCount.addItem(Integer.toString(i));
		}
		timeCount.addItem(Integer.toString(64));
		for (String s : timeBaseList) {
			timeBase.addItem(s);
		}

		timeCount.setSelectedItem("4");
		timeBase.setSelectedItem("4");

		try {
			String base[] = baseTime.split("/");
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

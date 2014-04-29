/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import fourthline.mabiicco.AppResource;
import fourthline.mmlTools.MMLScore;

import javax.swing.JTextField;
import javax.swing.JComboBox;



public class MMLScorePropertyPanel extends JPanel {
	private static final long serialVersionUID = -3976816581383137814L;

	private JTextField titleField;
	private JTextField authorField;
	private JComboBox<String> timeCount;
	private JComboBox<String> timeBase;

	public MMLScorePropertyPanel() {
		initializePanel();
	}

	private void initializePanel() {
		setBounds(100, 100, 260, 170);
		setLayout(null);

		JLabel label1 = new JLabel(AppResource.getText("score_property.name"));
		label1.setBounds(12, 40, 50, 13);
		add(label1);

		JLabel label2 = new JLabel(AppResource.getText("score_property.author"));
		label2.setBounds(12, 73, 50, 13);
		add(label2);

		JLabel label3 = new JLabel(AppResource.getText("score_property.measure"));
		label3.setBounds(12, 105, 50, 13);
		add(label3);

		JLabel label4 = new JLabel("/");
		label4.setBounds(156, 105, 15, 13);
		add(label4);

		titleField = new JTextField();
		titleField.setBounds(85, 37, 152, 19);
		add(titleField);
		titleField.setColumns(10);

		authorField = new JTextField();
		authorField.setColumns(10);
		authorField.setBounds(85, 70, 152, 19);
		add(authorField);

		timeCount = new JComboBox<String>();
		timeCount.setBounds(85, 102, 63, 19);
		add(timeCount);

		timeBase = new JComboBox<String>();
		timeBase.setBounds(174, 102, 63, 19);
		add(timeBase);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(260, 170);
	}

	private void initialComboBox(String baseTime) {
		String timeCountList[] = { "1", "2", "3", "4", "5", "6", "7", "8" };
		String timeBaseList[]  = { "1", "2", "4", "8", "16" };

		for (String s : timeCountList) {
			timeCount.addItem(s);
		}
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
	public void showDialog(JFrame parent, MMLScore score) {
		titleField.setText(score.getTitle());
		authorField.setText(score.getAuthor());
		initialComboBox(score.getBaseTime());

		int status = JOptionPane.showConfirmDialog(parent, 
				this,
				AppResource.getText("score_property"), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (status == JOptionPane.OK_OPTION) {
			score.setTitle(titleField.getText());
			score.setAuthor(authorField.getText());

			String time = timeCount.getSelectedItem()+"/"+timeBase.getSelectedItem();
			score.setBaseTime(time);
		}
	}
}

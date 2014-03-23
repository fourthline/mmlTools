/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui;

import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class Startup extends JDialog {
	private static final long serialVersionUID = 4112879755056918382L;

	private JTextArea textArea;

	/**
	 * Create the application.
	 */
	public Startup() {
		super();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		getContentPane().setBackground(Color.WHITE);
		setResizable(false);
		setBounds(100, 100, 400, 300);
		getContentPane().setLayout(null);
		setLocationRelativeTo(null);
		//		setAlwaysOnTop(true);
		setUndecorated(true);

		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(12, 10, 376, 280);
		getContentPane().add(panel);
		panel.setLayout(null);

		textArea = new JTextArea();
		textArea.setBounds(12, 150, 352, 120);
		panel.add(textArea);
		textArea.setBackground(UIManager.getColor("Panel.background"));
		textArea.setEditable(false);
	}

	public void printStatus(String s) {
		String str = textArea.getText() + s;
		textArea.setText(str);
	}
}

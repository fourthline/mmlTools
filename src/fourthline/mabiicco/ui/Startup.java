/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import fourthline.mabiicco.midi.MabiDLS;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class Startup {

	private JDialog frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.LookAndFeelInfo infos[] = UIManager.getInstalledLookAndFeels();

			System.out.println("getClassName:");

			for(int i = 0 ; i < infos.length ; i++){
				System.out.println(infos[i].getClassName());
			}
	    
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {}
		
		final Startup window = new Startup();
		window.frame.setVisible(true);
		
		try {
			MabiDLS.getInstance().initialize(MabiDLS.DEFALUT_DLS_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			window.frame.setVisible(false);
			System.exit(1);
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame mainFrame = new MainFrame();
					mainFrame.setVisible(true);
					window.frame.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Startup() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JDialog();
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setResizable(false);
		frame.setBounds(100, 100, 400, 300);
		frame.getContentPane().setLayout(null);
		frame.setLocationRelativeTo(null);
//		frame.setAlwaysOnTop(true);
		frame.setUndecorated(true);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(12, 10, 376, 280);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(12, 232, 352, 38);
		panel.add(textArea);
		textArea.setBackground(UIManager.getColor("Panel.background"));
		textArea.setEditable(false);
		textArea.setText("初期化中...");
	}
}

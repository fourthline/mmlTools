/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import fourthline.mabiicco.midi.MabiDLS;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.Font;

public class Startup extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4112879755056918382L;
	
	private JTextArea textArea;

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

		Startup window = new Startup();
		window.setVisible(true);
		
		try {
			window.printStatus("MIDI初期化中...");
			MabiDLS.getInstance().initializeMIDI();
			window.printStatus("OK\n");
			
			window.printStatus("DLSファイル読み込み中...");
			MabiDLS.getInstance().initializeSound();
			window.printStatus("OK\n");
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			window.setVisible(false);
			System.exit(1);
		}
		
		MainFrame mainFrame = new MainFrame();
		mainFrame.setVisible(true);
		window.setVisible(false);
	}

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
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
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

/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mabiicco;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import jp.fourthline.mabiicco.MabiIcco.ISplash;


public final class Splash2 extends JDialog implements ISplash {
	private static final long serialVersionUID = 6454976933389100203L;
	private final SplashPanel splashPanel = new SplashPanel();
	public Splash2() {
		getContentPane().add(splashPanel);
		setUndecorated(true);
		setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
		pack();
		setLocationRelativeTo(null);
	}

	@Override
	public void updateProgress(String s, int v) {
		splashPanel.textArea.setText(splashPanel.textArea.getText()+s);
		splashPanel.progress.setValue(v);
		if (v == 100) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}
	}

	private static final class SplashPanel extends JPanel {
		private static final long serialVersionUID = -3042011501769635754L;
		private static final int WIDTH = 318;
		private static final int HEIGHT = 433; 
		private final ImageIcon img;
		private final JProgressBar progress = new JProgressBar();
		private final JLabel version = new JLabel("", SwingConstants.RIGHT);
		private final JTextArea textArea = new JTextArea();
		private SplashPanel() {
			super();
			setLayout(null);
			img = AppResource.getImageIcon("/img/MabiIcco_loading.png");
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			progress.setMaximum(100);
			add(progress);
			progress.setBounds(14, 406, 290, 12);
			add(version);
			version.setText("Version: "+AppResource.getVersionText());
			version.setBounds(100, 290, 180, 14);
			version.setForeground(Color.BLACK);
			add(textArea);
			textArea.setEditable(false);
			textArea.setBounds(20, 313, WIDTH-40, 80);
			textArea.setBorder(new Splash.RoundBorder(20, 10));
			textArea.setOpaque(false);
			textArea.setForeground(Color.BLACK);
			setOpaque(false);
		}

		@Override
		public void paint(Graphics g) {
			g.drawImage(img.getImage(), 0, 0, this);
			super.paint(g);
		}
	}
}

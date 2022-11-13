/*
 * Copyright (C) 2021 たんらる
 */

package jp.fourthline.mabiicco;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;

import jp.fourthline.mabiicco.MabiIcco.ISplash;


public final class Splash extends JDialog implements ISplash {
	private static final long serialVersionUID = -4276613664298174063L;
	private final SplashPanel splashPanel = new SplashPanel();

	public Splash() {
		getContentPane().add(splashPanel);
		setUndecorated(true);
		pack();
		setLocationRelativeTo(null);
	}

	@Override
	public void updateProgress(String s, int v) {
		splashPanel.textArea.setText(splashPanel.textArea.getText() + s);
		splashPanel.progress.setValue(v);
	}

	public static final class SplashPanel extends JPanel {
		private static final long serialVersionUID = 2210455372955295858L;
		private static final int WIDTH = 300;
		private static final int HEIGHT = 220;
		private final ImageIcon img;
		private final JProgressBar progress = new JProgressBar();
		private final JLabel version = new JLabel();
		private final JTextArea textArea = new JTextArea();

		private SplashPanel() {
			super();
			setLayout(null);
			img = AppResource.getImageIcon("/img/title.png");
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			progress.setMaximum(100);
			add(progress);
			progress.setBounds(4, 206, 292, 12);
			add(version);
			version.setText("Version: " + AppResource.getVersionText());
			version.setBounds(160, 60, 120, 14);
			add(textArea);
			textArea.setEditable(false);
			textArea.setBounds(20, 100, WIDTH - 40, 80);
			textArea.setBorder(new RoundBorder(20, 10));
			setOpaque(false);
			setBorder(new LineBorder(Color.GRAY, 1, false));
		}

		@Override
		public void paint(Graphics g) {
			g.drawImage(img.getImage(), 0, 0, this);
			super.paint(g);
		}
	}

	static final class RoundBorder extends AbstractBorder {
		private static final long serialVersionUID = 429150943820077975L;
		private final int r;
		private final Insets insets;

		RoundBorder(int r, int m) {
			this.r = r;
			this.insets = new Insets(m, m, m, m);
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(0.6f, 0.6f, 0.6f));
			g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);
			g2.dispose();
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return insets;
		}
	}
}

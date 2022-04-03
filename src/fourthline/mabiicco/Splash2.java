/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mabiicco;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.border.AbstractBorder;

import fourthline.mabiicco.MabiIcco.ISplash;


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

	private final class SplashPanel extends JPanel {
		private static final long serialVersionUID = -3042011501769635754L;
		private static final int WIDTH = 318;
		private static final int HEIGHT = 433; 
		private final ImageIcon img;
		private final JProgressBar progress = new JProgressBar();
		private final JLabel version = new JLabel();
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
			version.setBounds(200, 290, 120, 14);
			add(textArea);
			textArea.setEditable(false);
			textArea.setBounds(20, 313, WIDTH-40, 80);
			textArea.setBorder(new RoundBorder(20, 10));
			textArea.setOpaque(false);
			setOpaque(false);
		}

		@Override
		public void paint(Graphics g) {
			g.drawImage(img.getImage(), 0, 0, this);
			super.paint(g);
		}

		private final class RoundBorder extends AbstractBorder {
			private static final long serialVersionUID = 429150943820077975L;
			private final int r;
			private final Insets insets;
			private RoundBorder(int r, int m) {
				this.r = r;
				this.insets = new Insets(m, m, m, m);
			}

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(new Color(0.7f, 0.7f, 0.7f));
				g2.drawRoundRect(0, 0, w-1, h-1, r, r);
				g2.setColor(new Color(0.6f, 0.6f, 0.6f));
				g2.drawRoundRect(1, 1, w-3, h-3, r, r);
				g2.dispose();
			}

			@Override
			public Insets getBorderInsets(Component c) {
				return insets;
			}
		}
	}
}

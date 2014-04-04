/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;

import fourthline.mabiicco.midi.MabiDLS;

public class KeyboardView extends JPanel implements IMMLView {
	private static final long serialVersionUID = -3850112420986284800L;

	private int playNote = -1;
	private final int width = 60;
	private final int PLAY_CHANNEL = 0;
	private final IMMLManager mmlManager;

	/**
	 * Create the panel.
	 */
	public KeyboardView(IMMLManager manager, final PianoRollView pianoRollView) {
		setPreferredSize(new Dimension(width, 649));
		this.mmlManager = manager;

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int note = pianoRollView.convertY2Note( e.getY() );
				playNote( note );
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				offNote();
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int note = pianoRollView.convertY2Note( e.getY() );
				playNote( note );
			}
		});
	}



	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g.create();
		for (int i = 0; i < OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+OCTNUM-i-1));
		}

		g2.setColor(Color.BLUE);
		g2.drawLine(width-1, 0, width-1, getHeight());

		paintPlayNote(g2);

		g2.dispose();
	}

	private boolean isWhiteKey(int note) {
		switch (note%12) {
		case 0:
		case 2:
		case 4:
		case 5:
		case 7:
		case 9:
		case 11:
			return true;
		default:
			return false;
		}
	}

	private void paintPlayNote(Graphics2D g) {
		int yAdd[] = { -2, -2, -1, -2, 1, -3, -2, -2, -1, 0, 0, 2 }; // 補正値
		if (playNote < 0) {
			return;
		}

		int x = 15;
		if ( isWhiteKey(playNote) ) {
			x += 20;
		}
		int y = getHeight() - ((playNote -11) * HEIGHT_C) + yAdd[playNote%12];
		g.setColor(Color.RED);
		g.fillOval(x, y, 4, 4);
	}

	private void paintOctPianoLine(Graphics2D g, int pos, char posText) {
		int white_wigth[] = { 10, 10, 10, 11, 10, 10, 11 };
		// ド～シのしろ鍵盤
		g.setColor(new Color(0.3f, 0.3f, 0.3f));

		int startY = 12 * HEIGHT_C * pos;
		int y = startY;
		for (int i = 0; i < white_wigth.length; i++) {
			g.drawRect(0, y, 40, white_wigth[i]);
			y += white_wigth[i];
		}
		// 黒鍵盤
		int black_posIndex[] = { 
				0, // A#
				1, // G#
				2, // F#
				4, // D#
				5  // C#
		};
		int posOffset[] = { 1, 2, 3, 1, 3 };

		for (int i = 0; i < black_posIndex.length; i++) {
			y = (black_posIndex[i]*10+5)+startY+posOffset[i];

			g.setColor(new Color(0.0f, 0.0f, 0.0f));
			g.fillRect(0, y, 20, HEIGHT_C);

			g.setColor(new Color(0.3f, 0.3f, 0.3f));
			g.drawRect(0, y, 20, HEIGHT_C);
		}

		// グリッド
		y = startY;
		g.setColor(new Color(0.3f, 0.3f, 0.6f));
		g.drawLine(40, y, width, y);

		// オクターブ
		char o_char[] = { 'o', posText };
		g.setFont(new Font("Arial", Font.PLAIN, 12));
		y = startY + (12 * HEIGHT_C);
		g.drawChars(o_char, 0, o_char.length, 42, y);
		g.drawLine(40, y, width, y);
	}


	public void playNote(int note) {
		if (note < 0) {
			offNote();
			return;
		}
		playNote = note + 12;

		int program = mmlManager.getActivePartProgram();
		MabiDLS.getInstance().playNote(playNote, program, PLAY_CHANNEL);

		repaint();
	}

	public void offNote() {
		playNote = -1;
		MabiDLS.getInstance().playNote(playNote, 0, PLAY_CHANNEL);

		repaint();
	}
}

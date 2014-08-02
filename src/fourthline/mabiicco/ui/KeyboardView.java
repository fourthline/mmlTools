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

public class KeyboardView extends JPanel {
	private static final long serialVersionUID = -3850112420986284800L;

	private int playNote = -1;
	private final int width = 60;
	private final int PLAY_CHANNEL = 15;
	private final IMMLManager mmlManager;

	private final PianoRollView pianoRollView;

	/**
	 * Create the panel.
	 */
	public KeyboardView(IMMLManager manager, final PianoRollView pianoRollView) {
		this.mmlManager = manager;
		this.pianoRollView = pianoRollView;
		updateHeight();

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

	public void updateHeight() {
		setPreferredSize(new Dimension(width, 12*PianoRollView.OCTNUM*pianoRollView.getNoteHeight()+1));
	}

	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g.create();
		for (int i = 0; i < PianoRollView.OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+PianoRollView.OCTNUM-i-1));
		}

		g2.setColor(Color.BLUE);
		g2.drawLine(width-1, 0, width-1, pianoRollView.getTotalHeight()-1);

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
		int y = pianoRollView.getTotalHeight() - ((playNote +1) * pianoRollView.getNoteHeight()) + yAdd[playNote%12];
		g.setColor(Color.RED);
		g.fillOval(x, y, 4, 4);
	}

	private void paintOctPianoLine(Graphics2D g, int pos, char posText) {
		int octHeight = pianoRollView.getNoteHeight() * 12;
		// ド～シのしろ鍵盤
		g.setColor(new Color(0.3f, 0.3f, 0.3f));

		int startY = octHeight * pos;
		for (int i = 0; i < 7; i++) {
			double y1 = octHeight * i / 7;
			double y2 = octHeight * (i+1) / 7;
			g.drawRect(0, (int)(startY+y1), 40, (int)(y2-y1));
		}

		// 黒鍵盤
		int black_posIndex[] = { 
				1, // A#
				2, // G#
				3, // F#
				5, // D#
				6  // C#
		};

		for (int i = 0; i < black_posIndex.length; i++) {
			int y = octHeight * black_posIndex[i] / 7 - pianoRollView.getNoteHeight() / 2-1;
			y += startY;

			g.setColor(new Color(0.0f, 0.0f, 0.0f));
			g.fillRect(0, y, 20, pianoRollView.getNoteHeight());

			g.setColor(new Color(0.3f, 0.3f, 0.3f));
			g.drawRect(0, y, 20, pianoRollView.getNoteHeight());
		}

		// グリッド
		g.setColor(new Color(0.3f, 0.3f, 0.6f));
		g.drawLine(40, startY, width, startY);

		// オクターブ
		char o_char[] = { 'o', posText };
		g.setFont(new Font("Arial", Font.PLAIN, 12));
		int y = startY + octHeight;
		g.drawChars(o_char, 0, o_char.length, 42, y);
		g.drawLine(40, y, width, y);
	}

	public void playNote(int note) {
		if (note < 0) {
			offNote();
			return;
		}
		playNote = note;

		int program = mmlManager.getActivePartProgram();
		MabiDLS.getInstance().setMute(PLAY_CHANNEL, false);
		MabiDLS.getInstance().playNote(playNote, program, PLAY_CHANNEL);

		repaint();
	}

	public void offNote() {
		playNote = -1;
		MabiDLS.getInstance().playNote(playNote, 0, PLAY_CHANNEL);

		repaint();
	}
}

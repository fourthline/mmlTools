/*
 * Copyright (C) 2013-2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;

import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.midi.IPlayNote;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mabiicco.midi.MMLMidiTrack.OverlapMode;
import jp.fourthline.mmlTools.MMLNoteEvent;

public final class KeyboardView extends JPanel implements IPlayNote {
	private static final long serialVersionUID = -3850112420986284800L;

	private int[] playNote = null;
	private final int width = 60;
	private final int DEFAULT_VELOCITY = 11;
	private final IMMLManager mmlManager;

	private final PianoRollView pianoRollView;

	private InstClass relativeInst = null;

	// オクターブ変化量
	private final int octDelta;

	private final Color borderColor = new Color(0.3f, 0.3f, 0.3f);
	private final Color octaveBorderColor = new Color(0.3f, 0.3f, 0.6f);

	/**
	 * Create the panel.
	 * @param manager 関連付けるIMMLManager
	 * @param pianoRollView 関連付けるPianoRollView
	 */
	public KeyboardView(IMMLManager manager, final PianoRollView pianoRollView) {
		this.mmlManager = manager;
		this.pianoRollView = pianoRollView;
		updateHeight();

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int note = pianoRollView.convertY2Note( e.getY() );
				playNote( note, DEFAULT_VELOCITY );
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
				playNote( note, DEFAULT_VELOCITY );
			}
		});

		octDelta = MabiIccoProperties.getInstance().soundEnv.get().pianoRollOctDelta();
	}

	public void updateHeight() {
		setPreferredSize(new Dimension(width, pianoRollView.getTotalHeight()));
	}

	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int height = pianoRollView.getTotalHeight();

		Graphics2D g2 = (Graphics2D)g.create();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, width, height);

		for (int i = 0; i <= PianoRollView.OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+PianoRollView.OCTNUM-i-1 + octDelta));
		}

		paintOverlapRange(g2);
		paintPlayNote(g2);

		// 残りの領域があれば塗りつぶす
		if (height < getHeight()) {
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRect(0, height+1, width, getHeight() - height);
		}

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
		case -1:
			return true;
		default:
			return false;
		}
	}

	private void paintPlayNote(Graphics2D g) {
		int[] yAdd = { -2, -2, -1, -2, 1, -3, -2, -2, -1, 0, 0, 2 }; // 補正値
		if (playNote == null) {
			return;
		}

		for (int note : playNote) {
			int x = 15;
			if (isWhiteKey(note)) {
				x += 20;
			}

			int y = pianoRollView.convertNote2Y(note) + yAdd[(note + 12) % 12];
			g.setColor(Color.RED);
			g.fillOval(x, y, 4, 4);
		}
	}

	private void paintOctPianoLine(Graphics2D g, int pos, char posText) {
		int height = pianoRollView.getNoteHeight();
		int octHeight = pianoRollView.getNoteHeight() * 12;
		int yLimit = pianoRollView.getTotalHeight();

		// ド～シのしろ鍵盤
		g.setColor(borderColor);

		int startY = octHeight * pos;
		for (int i = 0; i < 7; i++) {
			double y1 = octHeight * i / 7;
			double y2 = octHeight * (i+1) / 7;
			if ((startY+y1) > yLimit) break;
			g.drawRect(0, (int)(startY+y1), 40, (int)(y2-y1));
		}

		// 黒鍵盤
		int[] black_posIndex = {
				1, // A#
				2, // G#
				3, // F#
				5, // D#
				6  // C#
		};

		for (int i : black_posIndex) {
			int y = octHeight * i / 7 - height / 2 - 1;
			y += startY;
			if (y + height > yLimit) break;

			g.setColor(Color.BLACK);
			g.fillRect(0, y, 20, height);

			g.setColor(borderColor);
			g.drawRect(0, y, 20, height);
		}

		// グリッド
		g.setColor(octaveBorderColor);
		g.drawLine(40, startY, width, startY);

		// オクターブ
		int y = startY + octHeight;
		if (y < yLimit) {
			char[] o_char = { 'o', posText };
			g.setFont(new Font("Arial", Font.PLAIN, 12));
			g.drawChars(o_char, 0, o_char.length, 42, y);
			g.drawLine(40, y, width, y);
		}
	}

	private void paintOverlapRange(Graphics2D g) {
		if (relativeInst == null) {
			return;
		}

		if (!MabiIccoProperties.getInstance().enableEdit.get()) {
			// 編集モードでなければ表示しない
		} else if (MabiIccoProperties.getInstance().instAttr.get()) {
			int totalHeight = pianoRollView.getTotalHeight();
			int height = pianoRollView.getNoteHeight();
			int x = width - 1;
			OverlapMode overlapMode = MabiIccoProperties.getInstance().overlapMode.get();
			for (int i = 0; i < totalHeight; i += height) {
				int note = pianoRollView.convertY2Note(i);
				if (!relativeInst.isValid(note)) {
					g.setColor(Color.RED);
				} else if (overlapMode.isOverlap(relativeInst, note)) {
					g.setColor(Color.BLUE);
				} else {
					g.setColor(Color.GREEN);
				}
				g.drawLine(x, i, x, i+height);
			}
		}
	}

	@Override
	public void playNote(int note, int velocity) {
		if (note < -1) {
			offNote();
			return;
		}
		playNote(new int[]{ note }, velocity);
	}

	@Override
	public void playNote(int[] note, int velocity) {
		if (note == null) {
			offNote();
			return;
		}
		playNote = note;
		MMLNoteEvent[] noteEvent = new MMLNoteEvent[note.length];
		for (int i = 0; i < note.length; i++) {
			noteEvent[i] = new MMLNoteEvent(note[i], 0, 0, velocity);
		}

		MabiDLS dls = MabiDLS.getInstance();
		if (!dls.getSequencer().isRunning()) {
			int program = mmlManager.getActivePartProgram();
			dls.loadRequiredInstruments(mmlManager.getMMLScore());
			dls.playNotes(noteEvent, program, MabiDLS.KEYBOARD_PLAY_CHANNEL);
		}

		repaint();
	}

	@Override
	public void offNote() {
		playNote = null;
		int program = mmlManager.getActivePartProgram();
		MabiDLS.getInstance().playNote(Integer.MIN_VALUE, program, MabiDLS.KEYBOARD_PLAY_CHANNEL, 0);

		repaint();
	}

	public void setRelativeInst(InstClass inst) {
		this.relativeInst = inst;
	}
}

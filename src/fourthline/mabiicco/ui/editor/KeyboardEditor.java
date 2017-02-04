/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;

import javax.sound.midi.Sequencer;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fourthline.mabiicco.midi.IPlayNote;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.PianoRollView;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.parser.MMLEventParser;

import static fourthline.mabiicco.AppResource.appText;


/**
 * キーボード入力による編集.
 */
public final class KeyboardEditor extends JPanel implements KeyListener {

	private static final long serialVersionUID = 5355275205517097613L;

	private final JDialog dialog;

	private final int width = 16;
	private final int height = 90;
	private final int paintOct = 3;

	private final IMMLManager mmlManager;
	private final IPlayNote player;
	private final IEditAlign editAlign;
	private final PianoRollView pianoRollView;
	private int octave = 4;
	private final int minOct = 0;
	private final int maxOct = 8;

	private MMLNoteEvent editNote = null;
	private final JPanel panel = new JPanel();

	public KeyboardEditor(Frame parentFrame, IMMLManager mmlManager, IPlayNote player, IEditAlign editAlign, PianoRollView pianoRollView) {
		this.mmlManager = mmlManager;
		this.player = player;
		this.editAlign = editAlign;
		this.pianoRollView = pianoRollView;
		dialog = new JDialog(parentFrame, appText("edit.keyboard.input"));
		setPreferredSize(new Dimension(width*paintOct*12, height));
		initializePanel();
	}

	private void initializePanel() {
		panel.setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea(appText("edit.keyboard.input.description"));
		textArea.setEditable(false);
		textArea.setFocusable(false);
		panel.add(textArea, BorderLayout.NORTH);
		panel.add(this, BorderLayout.CENTER);

		dialog.addKeyListener(this);
		dialog.getContentPane().add(panel);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);
	}

	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}


	/**
	 * 横向きのキーボードを描画する.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g.create();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, width, height);

		for (int i = 0; i <= paintOct; i++) {
			paintOctPianoLine(g2, i, (i+octave-1));
		}

		g2.dispose();
	}


	private final Color colorG  = new Color(0.3f, 0.3f, 0.3f);
	private final Color colorW1 = new Color(0.5f, 0.5f, 0.5f);
	private final Color colorW2 = new Color(0.9f, 0.9f, 0.9f);
	private final Color colorB1 = new Color(0.1f, 0.1f, 0.1f);
	private final Color colorB2 = new Color(0.2f, 0.2f, 0.2f);
	private void paintOctPianoLine(Graphics2D g, int pos, int paintOctave) {
		int octWidth = width * 12;
		int whiteH = height-20;
		int blackH = whiteH/3*2;
		// ド～シのしろ鍵盤

		int startX = octWidth * pos;
		for (int i = 0; i < 7; i++) {
			double x1 = octWidth * i / 7;
			double x2 = octWidth * (i+1) / 7;
			g.setColor(colorW1);
			g.fillRect((int)(startX+x1), 0, (int)(x2-x1), whiteH);
			g.setColor(colorG);
			g.drawRect((int)(startX+x1), 0, (int)(x2-x1), whiteH);
			g.setColor(colorW2);
			g.fillRoundRect((int)(startX+x1)+2, -10, (int)(x2-x1)-3, whiteH-5+10, 10, 10);
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
			int x = octWidth * (7-black_posIndex[i]) / 7 - width / 2-1;
			x += startX;

			g.setColor(colorB1);
			g.fillRect(x, 0, width, blackH);
			g.setColor(colorB2);
			g.fillRoundRect(x+2, -5, width-3, blackH-5+5, 5, 5);

			g.drawRect(x, 0, width, blackH);
		}

		// グリッド
		g.setColor(new Color(0.3f, 0.3f, 0.6f));
		g.drawLine(startX, blackH, startX, height);

		// オクターブ
		String octText = "o"+paintOctave;
		if ( (paintOctave < minOct) || (paintOctave > maxOct) ) {
			octText = "X";
		} else if (octave - 1 == paintOctave) {
			octText = "<";
		} else if (octave + 1 == paintOctave) {
			octText = ">";
		}
		char o_char[] = octText.toCharArray();
		int y = startX + octWidth;
		g.drawChars(o_char, 0, o_char.length, startX+width*6-2, height-6);
		g.drawLine(y, blackH, y, height);

		// Code Char
		if (paintOctave == octave) {
			char c_char[] = "CDEFGAB".toCharArray();
			for (int i = 0; i < c_char.length; i++) {
				g.drawChars(c_char, i, 1, startX+(octWidth*i/7)+(octWidth/14)-2, whiteH-8);
			}
		}
	}

	private void addNote(char code) {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		int note = MMLEventParser.firstNoteNumber("o"+octave+code);
		MMLNoteEvent prevNote = activePart.searchPrevNoteOnTickOffset(tickOffset);
		int velocity = prevNote.getVelocity();
		int nextTick = tickOffset + editAlign.getEditAlign();
		nextTick -= nextTick % editAlign.getEditAlign();
		MMLNoteEvent noteEvent = new MMLNoteEvent(note, nextTick-tickOffset, tickOffset, velocity);
		activePart.addMMLNoteEvent(noteEvent);
		player.playNote(note, velocity);
		pianoRollView.setSequenceTick(nextTick);
		mmlManager.updateActivePart(true);
		mmlManager.updatePianoRollView();
		this.editNote = noteEvent;
	}

	private void addRest() {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		MMLNoteEvent noteEvent = activePart.searchOnTickOffset(tickOffset);
		activePart.deleteMMLEvent(noteEvent);
		int nextTick = tickOffset + editAlign.getEditAlign();
		nextTick -= nextTick % editAlign.getEditAlign();
		pianoRollView.setSequenceTick(nextTick);
		mmlManager.updateActivePart(true);
		mmlManager.updatePianoRollView();
		this.editNote = null;
	}

	private void backDelete() {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		int nextTick = tickOffset - editAlign.getEditAlign();
		nextTick -= nextTick % editAlign.getEditAlign();
		MMLNoteEvent deleteEvent = activePart.searchOnTickOffset(nextTick);
		activePart.deleteMMLEvent(deleteEvent);
		pianoRollView.setSequenceTick(nextTick);
		mmlManager.updateActivePart(true);
		this.editNote = null;
	}

	private void octaveChange(char code) {
		if (code == '<') {
			if (octave > minOct) {
				octave--;
				repaint();
			}
		} else if (code == '>') {
			if (octave < maxOct) {
				octave++;
				repaint();
			}
		}
		this.editNote = null;
	}

	private void addSharpFlat(char code) {
		if ( (code == '+') || (code == '#') ) {
			if (editNote != null) {
				editNote.setNote(editNote.getNote()+1);
				player.playNote(editNote.getNote(), editNote.getVelocity());
				mmlManager.updateActivePart(true);
			}
		} else if (code == '-') {
			if (editNote != null) {
				editNote.setNote(editNote.getNote()-1);
				player.playNote(editNote.getNote(), editNote.getVelocity());
				mmlManager.updateActivePart(true);
			}
		}
		this.editNote = null;
	}

	private void addEditTick() {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		MMLNoteEvent noteEvent = activePart.searchOnTickOffset(tickOffset-1);
		if (noteEvent != null) {
			noteEvent = noteEvent.clone();
			int nextTick = tickOffset + editAlign.getEditAlign();
			nextTick -= nextTick % editAlign.getEditAlign();
			noteEvent.setTick(nextTick - noteEvent.getTickOffset());
			activePart.addMMLNoteEvent(noteEvent);
			pianoRollView.setSequenceTick(nextTick);
			mmlManager.updateActivePart(true);
			mmlManager.updatePianoRollView();
			this.editNote = noteEvent;
		}
	}

	private void pressAction(char code) {
		if ( (code >= 'a') && (code <= 'g') || (code >= 'A') && (code <= 'G') ) {
			addNote(code);
		} else if ( (code == 'r') || (code == 'R') ) {
			addRest();
		} else if (code == KeyEvent.VK_BACK_SPACE) {
			backDelete();
		} else if ( (code == '<') || (code == '>') ) {
			octaveChange(code);
		} else if ( (code == '+') || (code == '#') || (code == '-') ) {
			addSharpFlat(code);
		} else if (code == ' ') {
			addEditTick();
		}
	}

	private void releaseAction() {
		player.offNote();
	}

	private Optional<Character> inputCode = Optional.empty();
	@Override
	public void keyTyped(KeyEvent e) {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		if (sequencer.isRunning()) {
			return;
		}
		synchronized (inputCode) {
			if (!inputCode.isPresent() || (inputCode.get() != e.getKeyChar()) ) {
				char code = e.getKeyChar();
				pressAction(code);
				inputCode = Optional.of(code);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		synchronized (inputCode) {
			if (inputCode.isPresent() && (inputCode.get() == e.getKeyChar())) {
				releaseAction();
				inputCode = Optional.empty();
			}
		}
	}

}

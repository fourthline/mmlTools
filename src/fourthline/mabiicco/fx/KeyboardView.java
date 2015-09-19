/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.fx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class KeyboardView {

	private int playNote = -1;
	private final int width = 60;

	private final PianoRollView pianoRollView;

	private Canvas canvas;

	/**
	 * Create the panel.
	 */
	public KeyboardView(Canvas canvas, final PianoRollView pianoRollView) {
		this.canvas = canvas;
		this.pianoRollView = pianoRollView;
		canvas.setWidth( width );
		paint();
	}

	private void updateHeight() {
		canvas.setHeight( 12*PianoRollView.OCTNUM*pianoRollView.getNoteHeight()+1  );
	}

	public void paint() {
		updateHeight();
		GraphicsContext gc = canvas.getGraphicsContext2D();
		paintComponent(gc);
	}

	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	public void paintComponent(GraphicsContext gc) {
		int height = pianoRollView.getTotalHeight()-1;

		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, width, height);

		for (int i = 0; i < PianoRollView.OCTNUM; i++) {
			paintOctPianoLine(gc, i, (char)('0'+PianoRollView.OCTNUM-i-1));
		}

		gc.setFill(Color.BLUE);
		gc.fillRect(width-1, 0, 1, height);
		paintPlayNote(gc);
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

	private void paintPlayNote(GraphicsContext gc) {
		int yAdd[] = { -2, -2, -1, -2, 1, -3, -2, -2, -1, 0, 0, 2 }; // 補正値
		if (playNote < 0) {
			return;
		}

		int x = 15;
		if ( isWhiteKey(playNote) ) {
			x += 20;
		}
		int y = pianoRollView.getTotalHeight() - ((playNote +1) * pianoRollView.getNoteHeight()) + yAdd[playNote%12];
		gc.setFill(Color.RED);
		gc.fillOval(x, y, 4, 4);
	}

	private void drawRect(GraphicsContext gc, int x, int y, int width, int height) {
		gc.fillRect(x, y, 1, height);
		gc.fillRect(x, y, width, 1);
		gc.fillRect(x+width, y, 1, height);
		gc.fillRect(x, y+height, width, 1);
	}

	private void paintOctPianoLine(GraphicsContext gc, int pos, char posText) {
		int octHeight = pianoRollView.getNoteHeight() * 12;
		// ド～シのしろ鍵盤
		gc.setFill(Color.color(0.3f, 0.3f, 0.3f));

		int startY = octHeight * pos;
		for (int i = 0; i < 7; i++) {
			double y1 = octHeight * i / 7;
			double y2 = octHeight * (i+1) / 7;
			drawRect(gc, 0, (int)(startY+y1), 40, (int)(y2-y1));
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

			gc.setFill(Color.color(0.0f, 0.0f, 0.0f));
			gc.fillRect(0, y, 20, pianoRollView.getNoteHeight());

			gc.setFill(Color.color(0.3f, 0.3f, 0.3f));
			drawRect(gc, 0, y, 20, pianoRollView.getNoteHeight());
		}

		// グリッド
		gc.setFill(Color.color(0.3f, 0.3f, 0.6f));
		gc.fillRect(40, startY, width, 1);
		
		// オクターブ
		String o_str = "o" + posText;
		gc.setFont(Font.font(12));
		int y = startY + octHeight;
		gc.fillText(o_str, 42, y);
		gc.fillRect(40, y, width, 1);
	}
}

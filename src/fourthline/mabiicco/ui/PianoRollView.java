/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import fourthline.mmlTools.parser.MMLEvent;
import fourthline.mmlTools.parser.MMLNoteEvent;
import fourthline.mmlTools.parser.MMLTrack;


public class PianoRollView extends JPanel implements IMMLView {



	/**
	 * 
	 */
	private static final long serialVersionUID = -7229093886476553295L;


	private MMLTrack track = null;
	private int wideScale = 3;

	/**
	 * Create the panel.
	 */
	public PianoRollView() {
		setPreferredSize(new Dimension(1000, 649));
	}

	public void setWidth(int width) {
		super.setPreferredSize(new Dimension(width, 649));
	}


	public void setMMLTrack(MMLTrack track) {
		this.track = track;

		int tickLength = track.getMaxTickLength();
		setWidth(tickLength / wideScale);

		repaint();
	}


	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g.create();
		for (int i = 0; i <= IMMLView.OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+IMMLView.OCTNUM-i-1));
		}

		paintMeasure(g2);
		paintMusicScore(g2);

		g2.dispose();
	}


	private void paintOctPianoLine(Graphics2D g, int pos, char posText) {
		int startY = 12 * IMMLView.HEIGHT * pos;
		Color c1 = new Color(0.9f, 0.9f, 0.9f); // 白鍵盤用
		Color c2 = new Color(0.8f, 0.8f, 0.8f); // 黒鍵盤用
		Color c3 = new Color(0.6f, 0.6f, 0.6f); // 境界線用
		Color colors[] = new Color[] {
				c1, c2, c1, c2, c1, c2, c1, c1, c2, c1, c2, c1
		};

		// グリッド
		int y = startY;
		int width = getWidth();
		g.drawLine(0, y, width, y);
		for (int i = 0; i < 12; i++) {
			g.setColor(colors[i]);
			g.fillRect(0, i*6+y, width, IMMLView.HEIGHT);
			g.setColor(c3);
			g.drawLine(0, i*6+y, width, i*6+y);
		}
	}

	/**
	 * メジャーを表示します。
	 */
	private void paintMeasure(Graphics2D g) {
		int width = getWidth();
		int sect = 96 / wideScale;

		g.setColor(new Color(0.5f, 0.5f, 0.5f));

		for (int i = 0; i < width; i += sect) {
			int x1 = i;
			int x2 = i;
			int y1 = 0;
			int y2 = getHeight();
			g.drawLine(x1, y1, x2, y2);
		}
	}

	private void drawNote(Graphics2D g, int note, int tick, int offset) {
		int x = offset;
		int y = getHeight() - ((note +1) * IMMLView.HEIGHT);
		int width = tick;
		int height = IMMLView.HEIGHT;
		g.fillRoundRect(x, y, width, height, 3, 3);
	}

	/**
	 * MMLEventリストのロールを表示します。
	 * @param g
	 * @param mmlPart
	 * @return
	 */
	private int paintMMLPart(Graphics2D g, List<MMLEvent> mmlPart) {
		int totalTick = 0;
		for (Iterator<MMLEvent> i = mmlPart.iterator(); i.hasNext(); ) {
			MMLEvent event = i.next();

			if (event instanceof MMLNoteEvent) {
				MMLNoteEvent noteEvent = (MMLNoteEvent) event;
				int note = noteEvent.getNote();
				int tick = noteEvent.getTick() / wideScale;

				drawNote(g, note, tick, totalTick);
				totalTick += tick;
			}
		}

		return totalTick;
	}

	/**
	 * 1トラック分のロールを表示します。
	 * @param g
	 */
	private void paintMusicScore(Graphics2D g) {
		if (track == null) {
			return;
		}

		int maxLength = 0;
		Color color[] = {
				new Color(0.7f, 0.0f, 0.0f),
				new Color(0.0f, 0.7f, 0.0f),
				new Color(0.0f, 0.0f, 0.7f)
		};

		for (int i = 0; i < 3; i++) {
			List<MMLEvent> mmlPart = track.getMMLEvent(i);
			g.setColor(color[i]);
			int length = paintMMLPart(g, mmlPart);
			if (maxLength < length) {
				maxLength = length;
			}
		}
	}

}

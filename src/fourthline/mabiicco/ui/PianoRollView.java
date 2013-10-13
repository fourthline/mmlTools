/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.Sequencer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.parser.MMLEvent;
import fourthline.mmlTools.parser.MMLNoteEvent;
import fourthline.mmlTools.parser.MMLTrack;


public class PianoRollView extends JPanel implements IMMLView {



	/**
	 * 
	 */
	private static final long serialVersionUID = -7229093886476553295L;


	private MMLTrack trackArray[];
	private int wideScale = 3;
	private int width;
	
	private JViewport viewport;
	private JComponent parent;

	/**
	 * Create the panel.
	 */
	public PianoRollView() {
		setPreferredSize(new Dimension(1000, 649));

		createSequenceThread();
	}
	
	public void setViewportAndParent(JViewport viewport, JComponent parent) {
		this.viewport = viewport;
		this.parent = parent;
	}

	public void setWidth(int width) {
		this.width = width;
		super.setPreferredSize(new Dimension(width, 649));
	}

	public void setMMLTrack(MMLTrack track[]) {
		this.trackArray = track;

		int tickLength = 0;
		for (int i = 0; i < track.length; i++) {
			int length = track[i].getMaxTickLength();
			if (tickLength < length) {
				tickLength = length;
			}
		}
		setWidth(tickLength / wideScale);
	}


	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g.create();
		for (int i = 0; i <= IMMLView.OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+IMMLView.OCTNUM-i-1));
		}

		paintMeasure(g2);
		if (trackArray != null) {
			for (int i = 0; i < trackArray.length; i++) {
				paintMusicScore(g2, i);
			}
		}

		paintSequenceLine(g2);

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


	// TODO: 重いかなぁ・・・
	private void createSequenceThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Sequencer sequencer = MabiDLS.getInstance().getSequencer();
				while (true) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (sequencer.isRunning()) {
						long position = sequencer.getTickPosition() / wideScale;
						Point point = viewport.getViewPosition();
						Dimension dim = viewport.getExtentSize();
						double x1 = point.getX();
						double x2 = x1 + dim.getWidth();
						if ( (position < x1) || (position > x2) ) {
							/* ビュー外にあるので、現在のポジションにあわせる */
							point.setLocation(position, point.getY());
							viewport.setViewPosition(point);
						}
						parent.repaint();
					}
				}
			}
		});

		thread.start();
	}

	private void paintSequenceLine(Graphics2D g) {
		long position = MabiDLS.getInstance().getSequencer().getTickPosition();

		Color color = Color.RED;
		int x1 = (int)(position / wideScale);
		int x2 = (int)(position / wideScale);
		int y1 = 0;
		int y2 = getHeight();

		g.setColor(color);
		g.drawLine(x1, y1, x2, y2);
	}

	/**
	 * メジャーを表示します。
	 */
	private void paintMeasure(Graphics2D g) {
		int width = getWidth() * wideScale;
		int sect = 96;

		g.setColor(new Color(0.5f, 0.5f, 0.5f));

		for (int i = 0; i < width; i += sect) {
			int x1 = i / wideScale;
			int x2 = i / wideScale;
			int y1 = 0;
			int y2 = getHeight();
			g.drawLine(x1, y1, x2, y2);
		}
	}



	private void drawNote(Graphics2D g, int note, int tick, int offset, Color color) {
		int x = offset / wideScale;
		int y = getHeight() - ((note +1) * IMMLView.HEIGHT);
		int width = (tick / wideScale);
		int height = IMMLView.HEIGHT;
		Color fillColor = new Color(
				color.getRed(),
				color.getGreen(),
				color.getBlue(),
				120
				);
		Color rectColor = new Color(
				color.getRed(),
				color.getGreen(),
				color.getBlue(),
				200
				);

		g.setColor(fillColor);
		g.fillRect(x, y, width, height);
		g.setColor(rectColor);
		g.drawRect(x, y, width, height);
	}

	/**
	 * MMLEventリストのロールを表示します。
	 * @param g
	 * @param mmlPart
	 * @return
	 */
	private int paintMMLPart(Graphics2D g, List<MMLEvent> mmlPart, Color color) {
		int totalTick = 0;
		for (Iterator<MMLEvent> i = mmlPart.iterator(); i.hasNext(); ) {
			MMLEvent event = i.next();

			if (event instanceof MMLNoteEvent) {
				MMLNoteEvent noteEvent = (MMLNoteEvent) event;
				int note = noteEvent.getNote();
				int tick = noteEvent.getTick();

				// TODO: tie判定
				drawNote(g, note, tick, totalTick, color);
				totalTick += tick;
			}
		}

		return totalTick;
	}

	/**
	 * 1トラック分のロールを表示します。
	 * @param g
	 */
	private void paintMusicScore(Graphics2D g, int index) {
		int maxLength = 0;
		Color color[] = {
				new Color(0.7f, 0.0f, 0.0f),
				new Color(0.0f, 0.7f, 0.0f),
				new Color(0.0f, 0.0f, 0.7f),
				new Color(0.35f, 0.35f, 0.0f),
				new Color(0.35f, 0.0f, 0.35f),
				new Color(0.0f, 0.35f, 0.35f)
		};

		for (int i = 0; i < 3; i++) {
			List<MMLEvent> mmlPart = trackArray[index].getMMLEvent(i);
			int length = paintMMLPart(g, mmlPart, color[index%color.length]);
			if (maxLength < length) {
				maxLength = length;
			}
		}
	}


	public JPanel getRulerPanel() {
		return new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4391191253349838837L;

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(width, 12);
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2 = (Graphics2D)g.create();
				/**
				 * メジャーを表示します。
				 */
				paintRuler(g2);

				g2.dispose();
			}


			/**
			 * ルーラを表示します。
			 */
			private void paintRuler(Graphics2D g) {
				int width = getWidth();
				int sect = 96 * 4;

				g.setColor(new Color(0.4f, 0.4f, 0.4f));

				int count = 0;
				for (int i = 0; i < (width*wideScale); i += sect) {
					int x1 = i / wideScale;
					int x2 = i / wideScale;
					int y1 = 0;
					int y2 = getHeight();
					g.drawLine(x1, y1, x2, y2);

					String s = "" + (count++);
					g.drawChars( s.toCharArray(), 0, s.length(), x1+2, y1+10);
				}
			}

		};
	}

}

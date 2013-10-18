/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
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


	private List<MMLTrack> trackList;
	private int wideScale = 6; // ピアノロールの拡大/縮小率 (1~6)
	private int width;

	private JViewport viewport;
	private JComponent parent;

	private long sequencePosition;

	private boolean donePaint = true;


	// ピアノロール上に表示する各トラックの色リスト（枠線）
	private static final Color trackRectColor[] = {
		new Color(0.7f, 0.0f, 0.0f, 0.8f),
		new Color(0.0f, 0.7f, 0.0f, 0.8f),
		new Color(0.0f, 0.0f, 0.7f, 0.8f),
		new Color(0.35f, 0.35f, 0.0f, 0.8f),
		new Color(0.35f, 0.0f, 0.35f, 0.8f),
		new Color(0.0f, 0.35f, 0.35f, 0.8f)
	};
	// ピアノロール上に表示する各トラックの色リスト（塗りつぶし）
	private static final Color trackFillColor[] = {
		new Color(0.7f, 0.0f, 0.0f, 0.4f),
		new Color(0.0f, 0.7f, 0.0f, 0.4f),
		new Color(0.0f, 0.0f, 0.7f, 0.4f),
		new Color(0.35f, 0.35f, 0.0f, 0.4f),
		new Color(0.35f, 0.0f, 0.35f, 0.4f),
		new Color(0.0f, 0.35f, 0.35f, 0.4f)
	};

	private static final Color wKeyColor = new Color(0.9f, 0.9f, 0.9f); // 白鍵盤用
	private static final Color bKeyColor = new Color(0.8f, 0.8f, 0.8f); // 黒鍵盤用
	private static final Color borderColor = new Color(0.6f, 0.6f, 0.6f); // 境界線用
	private static final Color keyColors[] = new Color[] {
		wKeyColor, 
		bKeyColor, 
		wKeyColor, 
		bKeyColor, 
		wKeyColor, 
		bKeyColor, 
		wKeyColor, 
		wKeyColor, 
		bKeyColor, 
		wKeyColor, 
		bKeyColor, 
		wKeyColor
	};

	private static final Color barBorder = new Color(0.5f, 0.5f, 0.5f);
	private static final Color beatBorder = new Color(0.4f, 0.4f, 0.4f);



	/**
	 * Create the panel.
	 */
	public PianoRollView() {
		super();
		setPreferredSize(new Dimension(0, 649));

		setSequenceX(0);
		createSequenceThread();
	}

	public void setViewportAndParent(JViewport viewport, JComponent parent) {
		this.viewport = viewport;
		this.parent = parent;
	}

	public void setWidth(int width) {
		this.width = width;
		super.setPreferredSize(new Dimension(width, 649));
		revalidate();
	}

	public void setMMLTrack(List<MMLTrack> trackList) {
		this.trackList = trackList;

		int tickLength = 0;
		for (int i = 0; i < trackList.size(); i++) {
			int length = trackList.get(i).getMaxTickLength();
			if (tickLength < length) {
				tickLength = length;
			}
		}
		setWidth(tickLength / wideScale);
		repaint();
	}

	public long convertXtoTick(int x) {
		return (x * wideScale);
	}

	public long getSequencePossition() {
		return sequencePosition;
	}

	public int getSequenceX() {
		return (int)sequencePosition / wideScale;
	}

	public void setSequenceX(int x) {
		long tick = x * wideScale;

		if (!MabiDLS.getInstance().getSequencer().isRunning()) {
			sequencePosition = tick;

			if (parent != null) {
				parent.repaint();
			}
		}
	}


	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	@Override
	public void paint(Graphics g) {
		int oldPri = Thread.currentThread().getPriority();
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g.create();
		for (int i = 0; i <= IMMLView.OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+IMMLView.OCTNUM-i-1));
		}

		paintMeasure(g2);
		if (trackList != null) {
			for (int i = 0; i < trackList.size(); i++) {
				paintMusicScore(g2, i);
			}
		}

		paintSequenceLine(g2);

		g2.dispose();

		donePaint = true;
		Thread.currentThread().setPriority(oldPri);
	}


	private void paintOctPianoLine(Graphics2D g, int pos, char posText) {
		int startY = 12 * IMMLView.HEIGHT * pos;


		// グリッド
		int y = startY;
		int width = getWidth();
		g.drawLine(0, y, width, y);
		for (int i = 0; i < 12; i++) {
			g.setColor(keyColors[i]);
			g.fillRect(0, i*6+y, width, IMMLView.HEIGHT);
			g.setColor(borderColor);
			g.drawLine(0, i*6+y, width, i*6+y);
		}
	}


	// TODO: 重いかなぁ・・・
	private void createSequenceThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Sequencer sequencer = MabiDLS.getInstance().getSequencer();
					if (sequencer.isRunning()) {
						if (!donePaint) {
							continue;
						}
						donePaint = false;
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								Sequencer sequencer = MabiDLS.getInstance().getSequencer();
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
						});
					}
				}
			}
		});

		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	private void paintSequenceLine(Graphics2D g) {
		long position = sequencePosition;
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			position = MabiDLS.getInstance().getSequencer().getTickPosition();
		}

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

		g.setColor(barBorder);

		for (int i = 0; i < width; i += sect) {
			int x1 = i / wideScale;
			int x2 = i / wideScale;
			int y1 = 0;
			int y2 = getHeight();
			g.drawLine(x1, y1, x2, y2);
		}
	}



	private void drawNote(Graphics2D g, int note, int tick, int offset, Color rectColor, Color fillColor) {
		int x = offset / wideScale;
		int y = getHeight() - ((note +1) * IMMLView.HEIGHT);
		int width = (tick / wideScale);
		int height = IMMLView.HEIGHT;

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
	private void paintMMLPart(Graphics2D g, List<MMLEvent> mmlPart, Color rectColor, Color fillColor) {
		int totalTick = 0;

		// 現在のView範囲のみを描画する.
		Point point = viewport.getViewPosition();
		Dimension dim = viewport.getExtentSize();
		double x1 = point.getX();
		double x2 = x1 + dim.getWidth();
		x1 *= wideScale; // base tick
		x2 *= wideScale; // base tick

		for (Iterator<MMLEvent> i = mmlPart.iterator(); i.hasNext(); ) {
			MMLEvent event = i.next();

			if (event instanceof MMLNoteEvent) {
				MMLNoteEvent noteEvent = (MMLNoteEvent) event;
				int note = noteEvent.getNote();
				int tick = noteEvent.getTick();

				if ( x1 <= (totalTick+tick) ) {
					drawNote(g, note, tick, totalTick, rectColor, fillColor);
				}
				totalTick += tick;
				if (totalTick >= x2) {
					break;
				}
			}
		}
	}

	/**
	 * 1トラック分のロールを表示します。
	 * @param g
	 */
	private void paintMusicScore(Graphics2D g, int index) {
		for (int i = 0; i < 3; i++) {
			List<MMLEvent> mmlPart = trackList.get(index).getMMLEvent(i);
			paintMMLPart(g, mmlPart, 
					trackRectColor[index%trackRectColor.length], 
					trackFillColor[index%trackFillColor.length]);
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
				paintSequenceLine(g2);

				g2.dispose();
			}


			/**
			 * ルーラを表示します。
			 */
			private void paintRuler(Graphics2D g) {
				int width = getWidth();
				int sect = 96 * 4;

				g.setColor(beatBorder);

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

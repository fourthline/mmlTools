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
import java.util.List;

import javax.sound.midi.Sequencer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.MouseInputListener;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTicks;


/**
 * ピアノロール表示を行うためのビューです.
 */
public class PianoRollView extends AbstractMMLView {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7229093886476553295L;

	private double wideScale = 6; // ピアノロールの拡大/縮小率 (1~6)
	private int width;

	private JViewport viewport;
	private JComponent parentComponent;
	private IMMLManager mmlManager;

	private long sequencePosition;
	private long playPosition;

	private boolean donePaint = true;

	// 描画位置判定用 (tick base)
	private double startViewTick;
	private double endViewTick;

	// 編集中のノートイベント
	private MMLNoteEvent editNote;

	private int activeTrackIndex   = 0;


	// ピアノロール上に表示する各トラックの色リスト
	private static final Color trackBaseColor[] = {
		new Color(0.7f, 0.0f, 0.0f),
		new Color(0.0f, 0.7f, 0.0f),
		new Color(0.0f, 0.0f, 0.7f),
		new Color(0.35f, 0.35f, 0.0f),
		new Color(0.35f, 0.0f, 0.35f),
		new Color(0.0f, 0.35f, 0.35f)
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

	/**
	 * ピアノロール上で編集を行うためのマウス入力のイベントリスナーを登録します.
	 * @param listener  編集時処理を行うMMLEditor.
	 */
	public void addMouseInputListener(MouseInputListener listener) {
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
	}

	public void setViewportAndParent(JViewport viewport, JComponent parent, IMMLManager mmlManager) {
		this.viewport = viewport;
		this.parentComponent = parent;
		this.mmlManager = mmlManager;
	}

	public void setWidth(int width) {
		this.width = width;
		super.setPreferredSize(new Dimension(width, 649));
		revalidate();
	}

	public void setEditNote(MMLNoteEvent note) {
		editNote = note;
	}

	public MMLNoteEvent getEditNote() {
		return editNote;
	}

	/**
	 * 現在のトラックの内容に合わせた幅に設定します.
	 */
	private void updateViewWidthTrackLength() {
		long tickLength = 0;
		List<MMLTrack> trackList = mmlManager.getTrackList();

		for (int i = 0; i < trackList.size(); i++) {
			long length = trackList.get(i).getMaxTickLength();
			if (tickLength < length) {
				tickLength = length;
			}
		}
		try {
			// 最後に4小節分のマージンを作成します.
			int t1 = MMLTicks.getTick("1");
			tickLength += t1*4;
			tickLength -= tickLength % t1;
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
		setWidth( convertTicktoX(tickLength) );
	}

	/**
	 * ピアノロールの横方向の拡大/縮小スケールを設定します.
	 * @param scale
	 */
	public void setWideScale(double scale) {
		// 拡大/縮小したときの表示位置を調整します.
		Point p = viewport.getViewPosition();
		p.setLocation(p.getX() * (this.wideScale / scale), p.getY());

		// 拡大/縮小したときの表示幅を調整します.
		this.wideScale = scale;
		updateViewWidthTrackLength();

		viewport.setViewPosition(p);
	}

	public double getWideScale() {
		return this.wideScale;
	}

	public long convertXtoTick(int x) {
		return (long)(x * wideScale);
	}

	public int convertTicktoX(long tick) {
		return (int)(tick / wideScale);
	}

	public long getSequencePossition() {
		return sequencePosition;
	}

	public int getSequenceX() {
		return convertTicktoX( sequencePosition );
	}

	public void setSequenceX(int x) {
		long tick = convertXtoTick(x);

		if (!MabiDLS.getInstance().getSequencer().isRunning()) {
			sequencePosition = tick;

			if (mmlManager != null) {
				mmlManager.updateActivePart();
			}
		}
	}


	/**
	 * 現在の描画位置 tick値を更新します.
	 */
	private void updateViewTick() {
		double x = viewport.getViewPosition().getX();
		double width = viewport.getExtentSize().getWidth();
		startViewTick = convertXtoTick((int)x);
		endViewTick = convertXtoTick((int)(x + width));
	}

	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		updateViewTick();
		List<MMLTrack> trackList = mmlManager.getTrackList();

		// FIXME: しぼったほうがいいかも？
		updateViewWidthTrackLength();

		Graphics2D g2 = (Graphics2D)g.create();
		for (int i = 0; i <= AbstractMMLView.OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+AbstractMMLView.OCTNUM-i-1));
		}

		paintMeasure(g2);
		if (trackList != null) {
			for (int i = 0; i < trackList.size(); i++) {
				paintMusicScore(g2, i, trackList.get(i));
			}
		}

		paintActivePart(g2);
		paintEditNote(g2);

		paintSequenceLine(g2);

		g2.dispose();

		donePaint = true;
	}


	private void paintOctPianoLine(Graphics2D g, int pos, char posText) {
		int startY = 12 * AbstractMMLView.HEIGHT * pos;

		// グリッド
		int y = startY;
		int width = getWidth();
		g.drawLine(0, y, width, y);
		for (int i = 0; i < 12; i++) {
			g.setColor(keyColors[i]);
			g.fillRect(0, i*6+y, width, AbstractMMLView.HEIGHT);
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
								long position = sequencer.getTickPosition();
								playPosition = position;
								position = convertTicktoX(position);
								Point point = viewport.getViewPosition();
								Dimension dim = viewport.getExtentSize();
								double x1 = point.getX();
								double x2 = x1 + dim.getWidth();
								if ( (position < x1) || (position > x2) ) {
									/* ビュー外にあるので、現在のポジションにあわせる */
									point.setLocation(position, point.getY());
									viewport.setViewPosition(point);
								}
								parentComponent.repaint();
							}
						});
					}
				}
			}
		});

		thread.start();
	}

	private void paintSequenceLine(Graphics2D g) {
		long position = sequencePosition;
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			position = playPosition;
		}

		Color color = Color.RED;
		int x = convertTicktoX(position);
		int y1 = 0;
		int y2 = getHeight();

		g.setColor(color);
		g.drawLine(x, y1, x, y2);
	}

	/**
	 * メジャーを表示します。
	 */
	private void paintMeasure(Graphics2D g) {
		int width = (int)convertXtoTick(getWidth());
		try {
			int sect = MMLTicks.getTick("4");
			g.setColor(barBorder);

			for (int i = 0; i < width; i += sect) {
				int x = convertTicktoX(i);
				int y1 = 0;
				int y2 = getHeight();
				g.drawLine(x, y1, x, y2);
			}
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
	}



	private void drawNote(Graphics2D g, MMLNoteEvent noteEvent, Color rectColor, Color fillColor) {
		int note = noteEvent.getNote();
		int tick = noteEvent.getTick();
		int offset = noteEvent.getTickOffset();
		int x = convertTicktoX(offset);
		int y = getHeight() - ((note +1) * AbstractMMLView.HEIGHT);
		int width = convertTicktoX(tick) -1;
		int height = AbstractMMLView.HEIGHT;

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
	private void paintMMLPart(Graphics2D g, List<MMLNoteEvent> mmlPart, Color rectColor, Color fillColor) {
		// 現在のView範囲のみを描画する.
		for (int i = 0; i < mmlPart.size(); i++) {
			MMLNoteEvent noteEvent = mmlPart.get(i);

			if (noteEvent.getEndTick() < startViewTick) {
				continue;
			}
			if (noteEvent.getTickOffset() > endViewTick) {
				break;
			}

			drawNote(g, noteEvent, rectColor, fillColor);
		}
	}

	/**
	 * 1トラック分のロールを表示します。
	 * @param g
	 * @param index トラックindex
	 */
	private void paintMusicScore(Graphics2D g, int index, MMLTrack track) {
		Color baseColor = trackBaseColor[index%trackBaseColor.length];
		Color rectColor = new Color(
				baseColor.getRed(),
				baseColor.getGreen(),
				baseColor.getBlue(),
				100
				);
		Color fillColor = new Color(
				baseColor.getRed(),
				baseColor.getGreen(),
				baseColor.getBlue(),
				50
				);

		MMLEventList activePart = mmlManager.getActiveMMLPart();

		for (int i = 0; i < 3; i++) {
			MMLEventList targetPart = track.getMMLEventList(i);
			if ( targetPart != activePart ) {
				// アクティブトラック中のアクティブパートはここでは描画しない.
				paintMMLPart(g, targetPart.getMMLNoteEventList(), rectColor, fillColor);
			} else {
				activeTrackIndex = index;
			}
		}
	}

	private void paintActivePart(Graphics2D g) {
		int index = activeTrackIndex;
		Color baseColor = trackBaseColor[index%trackBaseColor.length];
		Color rectColor = new Color(
				baseColor.getRed(),
				baseColor.getGreen(),
				baseColor.getBlue(),
				255
				);
		Color fillColor = new Color(
				baseColor.getRed(),
				baseColor.getGreen(),
				baseColor.getBlue(),
				200
				);

		MMLEventList activePart = mmlManager.getActiveMMLPart();
		paintMMLPart(g, activePart.getMMLNoteEventList(), rectColor, fillColor);
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
				try {
					g.setColor(beatBorder);
					int sect = convertTicktoX( MMLTicks.getTick("1") );
					int count = 0;
					for (int i = 0; i < width; i += sect) {
						int x = i;
						int y1 = 0;
						int y2 = getHeight();
						g.drawLine(x, y1, x, y2);

						String s = "" + (count++);
						g.drawChars( s.toCharArray(), 0, s.length(), x+2, y1+10);
					}

				} catch (UndefinedTickException e) {
					e.printStackTrace();
					return;
				}
			}

		};
	}



	private void paintEditNote(Graphics2D g) {
		if (editNote != null) {
			drawNote(g, editNote, Color.YELLOW, Color.YELLOW);
		}
	}

}

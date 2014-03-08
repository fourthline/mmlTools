/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JViewport;
import javax.swing.event.MouseInputListener;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTicks;


/**
 * ピアノロール表示を行うためのビューです.
 */
public class PianoRollView extends AbstractMMLView {
	private static final long serialVersionUID = -7229093886476553295L;

	private double wideScale = 6; // ピアノロールの拡大/縮小率 (1~6)

	private JViewport viewport;
	private IMMLManager mmlManager;

	private long sequencePosition;

	// 描画位置判定用 (tick base)
	private double startViewTick;
	private double endViewTick;

	// 選択中のノートイベント
	private List<MMLNoteEvent> selectNoteList;

	// 選択用の枠
	private Rectangle selectingRect;

	private int activeTrackIndex = 0;

	// draw pitch range
	private int lowerNote = 0;
	private int upperNote = 14;

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
	private static final Color pitchRangeBorderColor = Color.RED; // pitch range
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
	private static final Color noSoundColor = new Color(0.9f, 0.8f, 0.8f);

	private static final Color barBorder = new Color(0.5f, 0.5f, 0.5f);
	private static final Color timeBarBorder = new Color(0.3f, 0.2f, 0.3f);


	/**
	 * Create the panel.
	 */
	public PianoRollView() {
		super();
		setPreferredSize(new Dimension(0, 649));

		setSequenceX(0);
	}

	/**
	 * ピアノロール上で編集を行うためのマウス入力のイベントリスナーを登録します.
	 * @param listener  編集時処理を行うMMLEditor.
	 */
	public void addMouseInputListener(MouseInputListener listener) {
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
	}

	public void setViewportAndParent(JViewport viewport, IMMLManager mmlManager) {
		this.viewport = viewport;
		this.mmlManager = mmlManager;
	}

	public void setWidth(int width) {
		super.setPreferredSize(new Dimension(width, 649));
		revalidate();
	}

	public void setSelectNote(List<MMLNoteEvent> list) {
		selectNoteList = list;
	}

	public void setSelectingArea(Rectangle rect) {
		selectingRect = rect;
	}

	/**
	 * 現在のトラックの内容に合わせた幅に設定します.
	 */
	private void updateViewWidthTrackLength() {
		long tickLength = 0;
		MMLScore mmlScore = mmlManager.getMMLScore();
		int trackCount = mmlScore.getTrackCount();

		for (int i = 0; i < trackCount; i++) {
			long length = mmlScore.getTrack(i).getMaxTickLength();
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

	public long getSequencePlayPosition() {
		long position = sequencePosition;
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			position = MabiDLS.getInstance().getSequencer().getTickPosition();
		}

		return position;
	}

	public void setSequenceX(int x) {
		long tick = convertXtoTick(x);

		if (!MabiDLS.getInstance().getSequencer().isRunning()) {
			sequencePosition = tick;
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

		MMLScore mmlScore = mmlManager.getMMLScore();

		// FIXME: しぼったほうがいいかも？
		updateViewWidthTrackLength();

		Graphics2D g2 = (Graphics2D)g.create();
		for (int i = 0; i <= AbstractMMLView.OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+AbstractMMLView.OCTNUM-i-1));
		}

		paintMeasure(g2);
		paintPitchRangeBorder(g2);

		if (mmlScore != null) {
			int trackCount = mmlScore.getTrackCount();
			for (int i = 0; i < trackCount; i++) {
				paintMusicScore(g2, i, mmlScore.getTrack(i));
			}
		}

		paintActivePart(g2);
		paintSelectedNote(g2);
		paintSelectingArea(g2);

		paintSequenceLine(g2, getHeight());

		g2.dispose();
	}


	private void paintOctPianoLine(Graphics2D g, int pos, char posText) {
		int startY = 12 * AbstractMMLView.HEIGHT * pos;
		int octave = AbstractMMLView.OCTNUM - pos - 1;

		// グリッド
		int y = startY;
		int width = getWidth();
		g.drawLine(0, y, width, y);
		for (int i = 0; i < 12; i++) {
			int line = octave*12 + (11-i);
			Color fillColor = keyColors[i];
			if ( (line < lowerNote) || (line > upperNote) ) {
				fillColor = noSoundColor;
			}
			g.setColor(fillColor);
			g.fillRect(0, i*HEIGHT+y, width, AbstractMMLView.HEIGHT);
			g.setColor(borderColor);
			g.drawLine(0, i*HEIGHT+y, width, i*HEIGHT+y);
		}
	}

	private void paintPitchRangeBorder(Graphics2D g) {
		int width = getWidth();
		int y1 = convertNote2Y(lowerNote-1);
		int y2 = convertNote2Y(upperNote);
		g.setColor(pitchRangeBorderColor);
		g.drawLine(0, y1, width, y1);
		g.drawLine(0, y2, width, y2);
	}

	public void paintSequenceLine(Graphics2D g, int height) {
		long position = getSequencePlayPosition();

		Color color = Color.RED;
		int x = convertTicktoX(position);

		g.setColor(color);
		g.drawLine(x, 0, x, height);
	}

	/**
	 * メジャーを表示します。
	 */
	private void paintMeasure(Graphics2D g) {
		int width = (int)convertXtoTick(getWidth());
		try {
			int sect = MMLTicks.getTick("4");
			for (int i = 0; i*sect < width; i++) {
				if (i%4 == 0) {
					g.setColor(timeBarBorder);
				} else {
					g.setColor(barBorder);
				}
				int x = convertTicktoX(i*sect);
				int y1 = 0;
				int y2 = getHeight();
				g.drawLine(x, y1, x, y2);
			}
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
	}

	private boolean drawOption = false;
	private void drawNote(Graphics2D g, MMLNoteEvent noteEvent, Color rectColor, Color fillColor) {
		int note = noteEvent.getNote();
		int tick = noteEvent.getTick();
		int offset = noteEvent.getTickOffset();
		int x = convertTicktoX(offset);
		int y = getHeight() - ((note +1) * AbstractMMLView.HEIGHT);
		int width = convertTicktoX(tick) -1;
		int height = AbstractMMLView.HEIGHT-2;

		g.setColor(fillColor);
		g.fillRect(x, y, width, height);
		g.setColor(rectColor);
		g.drawRect(x, y, width, height);

		if (drawOption) {
			// velocityの描画.
			int velocity = noteEvent.getVelocity();
			if (velocity > 0) {
				String s = "V" + velocity;
				g.drawString(s, x, y);
			}
		}
	}

	/**
	 * MMLEventリストのロールを表示します。
	 * @param g
	 * @param mmlPart
	 * @return
	 */
	private void paintMMLPart(Graphics2D g, List<MMLNoteEvent> mmlPart, Color rectColor, Color fillColor) {
		// 現在のView範囲のみを描画する.
		for (MMLNoteEvent noteEvent : mmlPart) {
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

		int count = track.getMMLEventListSize();
		for (int i = 0; i < count; i++) {
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
		drawOption = true;
		paintMMLPart(g, activePart.getMMLNoteEventList(), rectColor, fillColor);
		drawOption = false;
	}

	private void paintSelectedNote(Graphics2D g) {
		// 選択中ノートの表示
		if (selectNoteList != null) {
			paintMMLPart(g, selectNoteList, Color.YELLOW, Color.YELLOW);
		}
	}

	private void paintSelectingArea(Graphics2D g) {
		if (selectingRect != null) {
			g.setColor(Color.BLUE);
			g.draw(selectingRect);
		}
	}

	public void setPitchRange(InstClass inst) {
		this.lowerNote = inst.getLowerNote();
		this.upperNote = inst.getUpperNote();
	}
}

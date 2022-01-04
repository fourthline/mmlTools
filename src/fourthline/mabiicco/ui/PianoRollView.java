/*
 * Copyright (C) 2013-2021 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.MouseInputListener;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.color.ColorManager;
import fourthline.mabiicco.ui.color.ScaleColor;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.core.UndefinedTickException;


/**
 * ピアノロール表示を行うためのビューです.
 */
public final class PianoRollView extends JPanel {
	private static final long serialVersionUID = -7229093886476553295L;

	public static final int OCTNUM = 9;

	/**
	 * ノートの表示高さ
	 */
	public static int NOTE_HEIGHT_TABLE[] = { 6, 8, 10, 12, 14 };
	private int noteHeight = NOTE_HEIGHT_TABLE[3];
	public int getNoteHeight() {
		return noteHeight;
	}
	public void setNoteHeightIndex(int index) {
		if ( (index >= 0) && (index < NOTE_HEIGHT_TABLE.length) ) {
			noteHeight = NOTE_HEIGHT_TABLE[index];
		}
	}
	public int getTotalHeight() {
		return (12*OCTNUM*noteHeight)+noteHeight;
	}

	private double wideScale = 6; // ピアノロールの拡大/縮小率 (1~6)

	private JViewport viewport;
	private IMMLManager mmlManager;

	private long sequencePosition = 0;
	private long runningSequencePosition = 0;

	// 描画位置判定用 (tick base)
	private long startViewTick;
	private long endViewTick;

	// 選択中のノートイベント
	private List<MMLNoteEvent> selectNoteList;

	// 選択用の枠
	private Rectangle selectingRect;

	// draw valid range
	private InstClass relativeInst;

	// スケール表示の色
	private ScaleColor scaleColor = ScaleColor.C_MAJOR;

	public void setScaleColor(ScaleColor scaleColor) {
		this.scaleColor = scaleColor;
	}

	private static final Color noSoundColor = new Color(0.9f, 0.8f, 0.8f);

	private static final Color barBorder = new Color(0.5f, 0.5f, 0.5f);
	private static final Color darkBarBorder = new Color(0.3f, 0.2f, 0.3f);

	private static final Color shadowColor = Color.GRAY;

	private static final int DRAW_START_MARGIN = 192;

	public enum PaintMode {
		ALL_TRACK("paintMode.all_track"), 
		ACTIVE_TRACK("paintMode.active_track"),
		ACTIVE_PART("paintMode.active_part");

		final private String resourceName;
		private PaintMode(String name) {
			resourceName = name;
		}
		public String toString() {
			return AppResource.appText(resourceName);
		}
	}
	private PaintMode paintMode = PaintMode.ALL_TRACK;

	public PaintMode getPaintMode() {
		return paintMode;
	}

	public void setPaintMode(PaintMode mode) {
		paintMode = mode;
	}

	/**
	 * Create the panel.
	 */
	public PianoRollView() {
		super();
		setPreferredSize(new Dimension(0, getTotalHeight()));

		MabiIccoProperties properties = MabiIccoProperties.getInstance();
		setNoteHeightIndex( properties.getPianoRollViewHeightScaleProperty() );
		setSequenceTick(0);
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
		super.setPreferredSize(new Dimension(width, getTotalHeight()));
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
		MMLScore mmlScore = mmlManager.getMMLScore();
		long tickLength = mmlScore.getTotalTickLengthWithAll();
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
		if ( (scale > 6.0) || (scale < 0.1) ) {
			return;
		}
		// 拡大/縮小したときの表示幅を調整します.
		this.wideScale = scale;
		updateViewWidthTrackLength();
	}

	/**
	 * pointが表示領域になければ、Viewportをスクロールする.
	 * pointの位置は表示領域内に補正される.
	 * @param point
	 */
	public void onViewScrollPoint(Point point) {
		int y = point.y;
		int y1 = viewport.getViewPosition().y;
		int y2 = y1 + viewport.getHeight() - noteHeight;
		int x = viewport.getViewPosition().x;
		if (x + viewport.getWidth() < point.x) {
			x++;
		}

		if (y < y1) {
			// up-scroll
			y1 -= noteHeight;
			if (y1 < 0) {
				y1 = 0;
			}
			y = y1;
		} else if (y > y2) {
			// down-scroll
			y1 += noteHeight;
			if (y1 > getHeight() - viewport.getHeight()) {
				y1 = getHeight() - viewport.getHeight();
				y = y2;
			} else {
				y = y2 + noteHeight;
			}
		}

		viewport.setViewPosition(new Point(x, y1));
		point.y = y;
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

	/**
	 * Panel上のy座標をnote番号に変換します.
	 * @param y
	 * @return
	 */
	public final int convertY2Note(int y) {
		if (y < 0) y = 0;
		int note = (OCTNUM*12-(y/noteHeight)) -1;

		return note;
	}

	/**
	 * note番号をPanel上のy座標に変換します.
	 * @param note
	 * @return
	 */
	public final int convertNote2Y(int note) {
		int y = OCTNUM*12 - note - 1;
		y *= noteHeight;
		return y;
	}

	public long getSequencePosition() {
		return sequencePosition;
	}

	public int getSequenceX() {
		return convertTicktoX( sequencePosition );
	}

	public void updateRunningSequencePosition() {
		runningSequencePosition = MabiDLS.getInstance().getSequencer().getTickPosition();
	}

	public long getSequencePlayPosition() {
		long position = sequencePosition;
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			return runningSequencePosition;
		}

		return position;
	}

	public void setSequenceTick(long tick) {
		if (!MabiDLS.getInstance().getSequencer().isRunning()) {
			if (tick < 0) {
				tick = 0;
			}
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

	private boolean showAllVelocity = false;

	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		showAllVelocity = MabiIccoProperties.getInstance().showAllVelocity.get();
		updateViewTick();

		// FIXME: しぼったほうがいいかも？
		updateViewWidthTrackLength();

		Graphics2D g2 = (Graphics2D)g.create();
		for (int i = 0; i <= OCTNUM; i++) {
			paintOctPianoLine(g2, i, (char)('0'+OCTNUM-i-1));
		}

		paintMeasure(g2);

		paintOtherTrack(g2);
		paintActiveTrack(g2);
		paintSelectedNote(g2);
		paintSelectingArea(g2);
		paintSequenceLine(g2, getTotalHeight());

		g2.dispose();
	}

	private void paintOctPianoLine(Graphics2D g, int pos, char posText) {
		int startY = 12 * noteHeight * pos;
		int octave = OCTNUM - pos - 1;

		// グリッド
		int y = startY;
		int width = getWidth();
		for (int i = 0; i < 12; i++) {
			int line = octave*12 + (11-i);
			Color fillColor = scaleColor.getColor(i);
			if (relativeInst.isValid(line) == false) {
				if (MabiIccoProperties.getInstance().viewRange.get()) {
					fillColor = noSoundColor;
				}
			}
			g.setColor(fillColor);
			g.fillRect(0, i*noteHeight+y, width, noteHeight);
			if (i == 0) {
				g.setColor(darkBarBorder);
			} else {
				g.setColor(ScaleColor.BORDER_COLOR);
			}
			g.drawLine(0, i*noteHeight+y, width, i*noteHeight+y);
		}
		g.setColor(darkBarBorder);
		g.drawLine(0, 12*noteHeight+y, width, 12*noteHeight+y);
	}

	void paintSequenceLine(Graphics2D g, int height) {
		long position = getSequencePlayPosition();

		Color color = Color.RED;
		int x = convertTicktoX(position);

		g.setColor(color);
		g.drawLine(x, 0, x, height);
	}

	/**
	 * 現在のスコアを基準とした1小節の幅を取得する.
	 * @return
	 */
	public int getMeasureWidth() {
		try {
			int sect = MMLTicks.getTick(mmlManager.getMMLScore().getBaseOnly());
			sect = convertTicktoX(sect);
			int borderCount = mmlManager.getMMLScore().getTimeCountOnly();
			return (sect * borderCount);
		} catch (UndefinedTickException e) {
			throw new AssertionError();
		}
	}

	/**
	 * 補助線の描画.
	 */
	private static final float dash[] = { 2.0f, 4.0f };
	private static final BasicStroke dashStroke = new BasicStroke(1.0f, 
			BasicStroke.CAP_BUTT, 
			BasicStroke.JOIN_MITER, 
			10.0f, 
			dash, 
			0.0f);
	private void paintHalfMeasure(Graphics2D g, int offset, int w) {
		int y = getTotalHeight();
		Stroke oldStroke = g.getStroke();
		g.setStroke(dashStroke);
		g.setColor(barBorder);

		int step = w;
		while (step >= 32) {
			step /= 2;
		}
		for (int x = offset + step; x < (offset + w); x+=step) {
			g.drawLine(x, 0, x, y);
		}

		g.setStroke(oldStroke);
	}

	/**
	 * メジャーを表示します。
	 */
	private void paintMeasure(Graphics2D g) {
		int width = (int)convertXtoTick(getWidth());
		try {
			int sect = MMLTicks.getTick(mmlManager.getMMLScore().getBaseOnly());
			int borderCount = mmlManager.getMMLScore().getTimeCountOnly();
			int y = getTotalHeight();
			for (int i = 0; i*sect < width; i++) {
				if (i*sect < startViewTick-sect) {
					continue;
				}
				if (i*sect > endViewTick) {
					break;
				}
				if (i%borderCount == 0) {
					g.setColor(darkBarBorder);
				} else {
					g.setColor(barBorder);
				}
				int x = convertTicktoX(i*sect);
				g.drawLine(x, 0, x, y);
				paintHalfMeasure(g, x, convertTicktoX(sect));
			}
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
	}

	private void drawRect(Graphics2D g, Color rectColor, Color fillColor, int x, int y, int width, int height) {
		g.setColor(fillColor);
		if (width != 0) {
			g.fillRect(x+1, y+1, width, height-1);
		} else {
			g.drawLine(x+1, y+1, x+1, y+height-1);
		}
		g.setColor(rectColor);
		g.drawLine(x+1, y+1, x+1, y+height-1);
		g.drawLine(x+width+1, y+height-1, x+width+1, y+1);
		g.drawLine(x+2, y, x+width, y);
		g.drawLine(x+width, y+height, x+2, y+height);
	}

	private void drawNote(Graphics2D g, MMLNoteEvent noteEvent, Color rectColor, Color fillColor, boolean drawOption, MMLNoteEvent prevNote) {
		int note = noteEvent.getNote();
		int tick = noteEvent.getTick();
		int offset = noteEvent.getTickOffset();
		int x = convertTicktoX(offset);
		int y = convertNote2Y(note) +1;
		int width = convertTicktoX(tick) -1;
		int height = noteHeight-2;
		if (width > 1) width--;

		if (drawOption) {
			// shadow
			drawRect(g, shadowColor, shadowColor, x+2, y+2, width, height);
		}
		drawRect(g, rectColor, fillColor, x, y, width, height);

		if (drawOption) {
			// velocityの描画.
			int velocity = noteEvent.getVelocity();
			if ( showAllVelocity || (prevNote == null) || (prevNote.getVelocity() != velocity) ) {
				String s = "V" + velocity;
				g.setColor(Color.DARK_GRAY);
				g.drawString(s, x, y);
			}
		}
	}

	/**
	 * MMLEventリストのロールを表示します。
	 * @param g
	 * @param mmlPart
	 */
	private void paintMMLPart(Graphics2D g, List<MMLNoteEvent> mmlPart, Color rectColor, Color fillColor, boolean drawOption) {
		MMLNoteEvent prevNote = new MMLNoteEvent(0, 0, 0, MMLNoteEvent.INIT_VOL);
		// 現在のView範囲のみを描画する.
		for (MMLNoteEvent noteEvent : mmlPart) {
			if ( (noteEvent.getEndTick() < startViewTick) && (noteEvent.getTickOffset() < startViewTick - DRAW_START_MARGIN) ) {
				prevNote = noteEvent;
				continue;
			}
			if (noteEvent.getTickOffset() > endViewTick) {
				break;
			}

			drawNote(g, noteEvent, rectColor, fillColor, drawOption, prevNote);
			prevNote = noteEvent;
		}
	}

	/**
	 * 1トラック分のロールを表示します。（アクティブトラックは表示しない）
	 * @param g
	 * @param index トラックindex
	 */
	private void paintMMLTrack(Graphics2D g, int index, MMLTrack track) {
		if (!track.isVisible()) {
			return;
		}

		boolean instEnable[] = InstClass.getEnablePartByProgram(track.getProgram());
		boolean songExEnable[] = InstClass.getEnablePartByProgram(track.getSongProgram());
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		int colorIndex = 0;
		for (int i = 0; i < track.getMMLEventList().size(); i++) {
			MMLEventList targetPart = track.getMMLEventAtIndex(i);
			if (targetPart == activePart) {
				colorIndex++;
				continue;
			}
			Color rectColor = ColorManager.defaultColor().getPartRectColor(index, colorIndex);
			Color fillColor = ColorManager.defaultColor().getPartFillColor(index, colorIndex);
			if ( !instEnable[i] && !songExEnable[i] ) {
				fillColor = ColorManager.defaultColor().getUnusedFillColor();
			} else {
				colorIndex++;
			}
			paintMMLPart(g, track.getMMLEventList().get(i).getMMLNoteEventList(), rectColor, fillColor, false);
		}
	}

	private void paintActiveTrack(Graphics2D g) {
		int trackIndex = mmlManager.getActiveTrackIndex();
		if (paintMode != PaintMode.ACTIVE_PART) {
			paintMMLTrack(g, trackIndex, mmlManager.getMMLScore().getTrack(trackIndex));
		}
		MMLEventList activePart = mmlManager.getActiveMMLPart();
		if (activePart != null) {
			Color rectColor = ColorManager.defaultColor().getActiveRectColor(trackIndex);
			Color fillColor = ColorManager.defaultColor().getActiveFillColor(trackIndex);
			paintMMLPart(g, activePart.getMMLNoteEventList(), rectColor, fillColor, true);
		}
	}

	private void paintOtherTrack(Graphics2D g) {
		MMLScore mmlScore = mmlManager.getMMLScore();
		if (paintMode != PaintMode.ALL_TRACK) {
			return;
		}
		if (mmlScore != null) {
			for (int i = 0; i < mmlScore.getTrackCount(); i++) {
				MMLTrack track = mmlScore.getTrack(i);
				if (track != mmlScore.getTrack(mmlManager.getActiveTrackIndex())) {
					paintMMLTrack(g, i, track);
				}
			}
		}
	}

	private void paintSelectedNote(Graphics2D g) {
		// 選択中ノートの表示
		if (selectNoteList != null) {
			paintMMLPart(g, selectNoteList, Color.YELLOW, Color.YELLOW, false);
		}
	}

	private void paintSelectingArea(Graphics2D g) {
		if (selectingRect != null) {
			g.setColor(Color.BLUE);
			g.draw(selectingRect);
		}
	}

	public void setRelativeInst(InstClass inst) {
		this.relativeInst = inst;
		repaint();
	}
}

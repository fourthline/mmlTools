/*
 * Copyright (C) 2013-2015 たんらる
 */

package fourthline.mabiicco.fx;

import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.fx.color.ColorManager;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTicks;


/**
 * ピアノロール表示を行うためのビューです.
 */
public final class PianoRollView {

	public static final int OCTNUM = 9;

	private final Canvas canvas;
	private final IMMLManager mmlManager;

	public PianoRollView(Canvas canvas, IMMLManager mmlManager) {
		this.canvas = canvas;
		this.mmlManager = mmlManager;
		setNoteHeightIndex( MabiIccoProperties.getInstance().getPianoRollViewHeightScaleProperty() );
		GraphicsContext gc = canvas.getGraphicsContext2D();
		paintComponent(gc);
	}

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
		canvas.setHeight( getTotalHeight() );
	}
	public int getTotalHeight() {
		return (12*OCTNUM*noteHeight+1);
	}

	private double wideScale = 6; // ピアノロールの拡大/縮小率 (1~6)


	private long sequencePosition = 0;
	private long runningSequencePosition = 0;

	// 選択中のノートイベント
	private List<MMLNoteEvent> selectNoteList;

	// 選択用の枠
	private Rectangle selectingRect;

	// draw pitch range
	private int lowerNote = 0;
	private int upperNote = 14;

	private static final Color wKeyColor =  Color.color(0.9f, 0.9f, 0.9f); // 白鍵盤用
	private static final Color bKeyColor = Color.color(0.8f, 0.8f, 0.8f); // 黒鍵盤用
	private static final Color borderColor = Color.color(0.6f, 0.6f, 0.6f); // 境界線用
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
	private static final Color noSoundColor = Color.color(0.9f, 0.8f, 0.8f);

	private static final Color barBorder = Color.color(0.5f, 0.5f, 0.5f);
	private static final Color darkBarBorder = Color.color(0.3f, 0.2f, 0.3f);

	private static final Color shadowColor = Color.GRAY;

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

		for (MMLTrack track : mmlScore.getTrackList()) {
			long length = track.getMaxTickLength();
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

		canvas.setWidth( convertTicktoX(tickLength) );
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
		int note = -1;
		if (y >= 0) {
			note = (OCTNUM*12-(y/noteHeight)) -1;
		}

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
			sequencePosition = tick;
		}
	}

	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	public void paintComponent(GraphicsContext gc) {
		updateViewWidthTrackLength();

		for (int i = 0; i < OCTNUM; i++) {
			paintOctPianoLine(gc, i, (char)('0'+OCTNUM-i-1));
		}

		paintMeasure(gc);
		paintPitchRangeBorder(gc);

		paintOtherTrack(gc);
		paintActiveTrack(gc);
		paintSelectedNote(gc);
		paintSelectingArea(gc);
		paintSequenceLine(gc, convertNote2Y(-1));
	}

	private void paintOctPianoLine(GraphicsContext gc, int pos, char posText) {
		int startY = 12 * noteHeight * pos;
		int octave = OCTNUM - pos - 1;

		// グリッド
		int y = startY;
		double width = canvas.getWidth();
		for (int i = 0; i < 12; i++) {
			int line = octave*12 + (11-i);
			Color fillColor = keyColors[i];
			if ( (line < lowerNote) || (line > upperNote) ) {
				if (MabiIccoProperties.getInstance().getViewRage()) {
					fillColor = noSoundColor;
				}
			}
			gc.setFill(fillColor);
			gc.fillRect(0, i*noteHeight+y, width, noteHeight);
			if (i == 0) {
				gc.setFill(darkBarBorder);
			} else {
				gc.setFill(borderColor);
			}
			gc.fillRect(0, i*noteHeight+y, width, 1);
		}
		gc.setFill(darkBarBorder);
		gc.fillRect(0, 12*noteHeight+y, width, 1);
	}

	private void paintPitchRangeBorder(GraphicsContext gc) {
		if (MabiIccoProperties.getInstance().getViewRage()) {
			double width = canvas.getWidth();
			int y1 = convertNote2Y(lowerNote-1);
			int y2 = convertNote2Y(upperNote);
			gc.setFill(pitchRangeBorderColor);
			gc.fillRect(0, y1, width, 1);
			gc.fillRect(0, y2, width, 1);
		}
	}

	private void paintSequenceLine(GraphicsContext gc, int height) {
		long position = getSequencePlayPosition();

		Color color = Color.RED;
		int x = convertTicktoX(position);

		gc.setFill(color);
		gc.fillRect(x, 0, 1, height);
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
	private void paintHalfMeasure(GraphicsContext gc, int offset, int w) {}

	/**
	 * メジャーを表示します。
	 */
	private void paintMeasure(GraphicsContext gc) {
		int width = (int)convertXtoTick((int)canvas.getWidth());
		try {
			int sect = MMLTicks.getTick(mmlManager.getMMLScore().getBaseOnly());
			int borderCount = mmlManager.getMMLScore().getTimeCountOnly();
			for (int i = 0; i*sect < width; i++) {
				if (i%borderCount == 0) {
					gc.setFill(darkBarBorder);
				} else {
					gc.setFill(barBorder);
				}
				int x = convertTicktoX(i*sect);
				int y = convertNote2Y(-1);
				gc.fillRect(x, 0, 1, y);
				paintHalfMeasure(gc, x, convertTicktoX(sect));
			}
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
	}

	private void drawRect(GraphicsContext gc, Color rectColor, Color fillColor, int x, int y, int width, int height) {
		gc.setFill(fillColor);
		if (width != 0) {
			gc.fillRect(x+1, y+1, width, height-1);
		} else {
			gc.fillRect(x+1, y+1, 1, y+height-1);
		}
		gc.setFill(rectColor);
		gc.fillRect(x+1, y+1, 1, height-1);
		gc.fillRect(x+width+1, y+1, 1, height-1);
		gc.fillRect(x+2, y, width, 1);
		gc.fillRect(x+2, y+height, width, 1);
	}

	private void drawNote(GraphicsContext gc, MMLNoteEvent noteEvent, Color rectColor, Color fillColor, boolean drawOption, MMLNoteEvent prevNote) {
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
			drawRect(gc, shadowColor, shadowColor, x+2, y+2, width, height);
		}
		drawRect(gc, rectColor, fillColor, x, y, width, height);

		if (drawOption) {
			// velocityの描画.
			int velocity = noteEvent.getVelocity();
			if ( (prevNote == null) || (prevNote.getVelocity() != velocity) ) {
				String s = "V" + velocity;
				gc.setStroke(Color.DARKGRAY);
				gc.strokeText(s, x, y);
			}
		}
	}

	/**
	 * MMLEventリストのロールを表示します。
	 * @param g
	 * @param mmlPart
	 * @return
	 */
	private void paintMMLPart(GraphicsContext gc, List<MMLNoteEvent> mmlPart, Color rectColor, Color fillColor, boolean drawOption) {
		MMLNoteEvent prevNote = new MMLNoteEvent(0, 0, 0, MMLNoteEvent.INIT_VOL);
		// 現在のView範囲のみを描画する.
		for (MMLNoteEvent noteEvent : mmlPart) {
			drawNote(gc, noteEvent, rectColor, fillColor, drawOption, prevNote);
			prevNote = noteEvent;
		}
	}

	/**
	 * 1トラック分のロールを表示します。（アクティブトラックは表示しない）
	 * @param g
	 * @param index トラックindex
	 */
	private void paintMMLTrack(GraphicsContext gc, int index, MMLTrack track) {
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
			paintMMLPart(gc, track.getMMLEventList().get(i).getMMLNoteEventList(), rectColor, fillColor, false);
		}
	}

	private void paintActiveTrack(GraphicsContext gc) {
		int trackIndex = mmlManager.getActiveTrackIndex();
		if (paintMode != PaintMode.ACTIVE_PART) {
			paintMMLTrack(gc, trackIndex, mmlManager.getMMLScore().getTrack(trackIndex));
		}
		MMLEventList activePart = mmlManager.getActiveMMLPart();
		if (activePart != null) {
			Color rectColor = ColorManager.defaultColor().getActiveRectColor(trackIndex);
			Color fillColor = ColorManager.defaultColor().getActiveFillColor(trackIndex);
			paintMMLPart(gc, activePart.getMMLNoteEventList(), rectColor, fillColor, true);
		}
	}

	private void paintOtherTrack(GraphicsContext gc) {
		MMLScore mmlScore = mmlManager.getMMLScore();
		if (paintMode != PaintMode.ALL_TRACK) {
			return;
		}
		if (mmlScore != null) {
			for (int i = 0; i < mmlScore.getTrackCount(); i++) {
				MMLTrack track = mmlScore.getTrack(i);
				if (track != mmlScore.getTrack(mmlManager.getActiveTrackIndex())) {
					paintMMLTrack(gc, i, track);
				}
			}
		}
	}

	private void paintSelectedNote(GraphicsContext gc) {
		// 選択中ノートの表示
		if (selectNoteList != null) {
			paintMMLPart(gc, selectNoteList, Color.YELLOW, Color.YELLOW, false);
		}
	}

	private void paintSelectingArea(GraphicsContext gc) {}

	public void setPitchRange(InstClass inst) {
		if (inst == null) {
			return;
		}
		this.lowerNote = inst.getLowerNote();
		this.upperNote = inst.getUpperNote();
	}
}

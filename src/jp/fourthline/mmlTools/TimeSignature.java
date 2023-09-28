/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.ArrayList;
import java.util.Comparator;

import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLException;


public final class TimeSignature extends MMLEvent {
	private static final long serialVersionUID = 8326662121191415432L;

	/** 拍子の分子 */
	public static final String[] TIME_COUNT_LIST;

	/** 拍子の分母 */
	public static final String[] TIME_BASE_LIST = { "1", "2", "4", "8", "16", "32", "64" };

	static {
		ArrayList<String> list = new ArrayList<>();
		for (int i = 1; i <= 32; i++) {
			list.add(Integer.toString(i));
		}
		list.add(Integer.toString(64));
		TIME_COUNT_LIST = list.toArray(String[]::new);
	}

	private int numTime;
	private int baseTick;
	private int measureOffset;      // 小節で設定する

	public TimeSignature(MMLScore score, int tickOffset, int numTime, int baseTime) throws MMLException {
		super(tickOffset);
		this.numTime = numTime;
		this.baseTick = MMLTicks.getTick(Integer.toString(baseTime));
		this.measureOffset = tickToMeasure(score, tickOffset);
	}

	public TimeSignature(MMLScore score, int tickOffset, String numTime, String baseTime) throws MMLException {
		this(score, tickOffset, Integer.parseInt(numTime), Integer.parseInt(baseTime));
	}

	@Override
	public void setTickOffset(int tickOffset) {
		// 小節単位に固定するため、個別の設定は不可
	}

	/**
	 * 表示用TickOffsetの設定（内部用）
	 * @param tickOffset
	 */
	private void setViewTickOffset(int tickOffset) {
		super.setTickOffset(tickOffset);
	}

	/**
	 * TimeSignatureリストのOffset情報を再計算する
	 * @param score
	 */
	static void recalcTimeSignatureList(MMLScore score) {
		var list = score.getTimeSignatureList();
		list.sort(Comparator.comparingInt(t -> t.getMeasureOffset()));
		for (TimeSignature ts : list) {
			int tick = measureToCalcTick(score, ts.getMeasureOffset());
			ts.setViewTickOffset(tick);
		}
	}

	/**
	 * tickを小節番号へ変換する
	 * @param score
	 * @param tick
	 * @return
	 */
	public static int tickToMeasure(MMLScore score, int tick) {
		var timeSignatureIterator = score.getTimeSignatureList().iterator();
		int numTime = score.getTimeCountOnly();
		int beatTick = score.getBeatTick();
		int m = 0;

		TimeSignature timeSignature = timeSignatureIterator.hasNext() ? timeSignatureIterator.next() : null;
		while (beatTick <= tick) {
			if ( (timeSignature != null) && (m >= timeSignature.getMeasureOffset()) ) {
				numTime = timeSignature.getNumTime();
				beatTick = timeSignature.getBaseTick();
				timeSignature = timeSignatureIterator.hasNext() ? timeSignatureIterator.next() : null;
			}
			int measureTick = numTime * beatTick;
			if (measureTick <= tick) {
				m++;
				tick -= measureTick;
			} else {
				tick -= beatTick;
			}
		}
		return m;
	}

	public static int measureToTick(MMLScore score, int measure) {
		var timeSignatureList = score.getTimeSignatureList();
		int numTime = score.getTimeCountOnly();
		int beatTick = score.getBeatTick();
		int m = 0;
		int baseTick = 0;

		for (TimeSignature t : timeSignatureList) {
			if (t.getMeasureOffset() <= measure) {
				baseTick = t.getTickOffset();
				m = t.getMeasureOffset();
				beatTick = t.getBaseTick();
				numTime = t.getNumTime();
			} else {
				break;
			}
		}

		return baseTick + (measure - m) * (numTime * beatTick);
	}

	static int measureToCalcTick(MMLScore score, int measure) {
		var timeSignatureIterator = score.getTimeSignatureList().iterator();
		int measureTick = score.getTimeCountOnly() * score.getBeatTick();
		int m = 0;
		int md = 0;

		TimeSignature timeSignature = timeSignatureIterator.hasNext() ? timeSignatureIterator.next() : null;
		while (m < measure) {
			if ( (timeSignature != null) && (m >= timeSignature.getMeasureOffset()) ) {
				measureTick = timeSignature.getNumTime() * timeSignature.getBaseTick();
				timeSignature = timeSignatureIterator.hasNext() ? timeSignatureIterator.next() : null;
			}
			m++;
			md += measureTick;
		}
		return md;
	}

	/**
	 * 1小節追加における拍子記号処理
	 * @param score
	 * @param measurePosition
	 */
	public static void addMeasure(MMLScore score, int measurePosition) {
		var eventList = score.getTimeSignatureList();
		for (int i = 0; i < eventList.size(); i++) {
			var event = eventList.get(i);
			if (event.getMeasureOffset() > measurePosition) {
				event.measureOffset++;
			}
		}

		recalcTimeSignatureList(score);
	}

	/**
	 * 1小節削除における拍子記号処理
	 * @param score
	 * @param measurePosition
	 */
	public static void removeMeasure(MMLScore score, int measurePosition) {
		var eventList = score.getTimeSignatureList();
		for (int i = 0; i < eventList.size(); i++) {
			var event = eventList.get(i);
			if (event.getMeasureOffset() > measurePosition) {
				event.measureOffset--;
				if ((i > 0) && (eventList.get(i-1).getMeasureOffset() == event.measureOffset)) {
					eventList.remove(--i);
				}
			}
		}		

		recalcTimeSignatureList(score);
	}

	public int getNumTime() {
		return numTime;
	}

	public int getBaseTick() {
		return baseTick;
	}

	public int getMeasureOffset() {
		return measureOffset;
	}

	@Override
	public String toString() {
		try {
			String baseTime = new MMLTicks("", baseTick).toMMLText();
			return String.format("%d=%d/%s", getTickOffset(), numTime, baseTime);
		} catch (MMLException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String toMMLString() {
		try {
			String baseTime = new MMLTicks("", baseTick).toMMLText();
			return String.format("%d/%s", numTime, baseTime);
		} catch (MMLException e) {
			e.printStackTrace();
		}
		return "";
	}
}

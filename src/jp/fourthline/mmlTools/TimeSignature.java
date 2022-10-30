/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.Comparator;

import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.UndefinedTickException;


public final class TimeSignature extends MMLEvent {
	private static final long serialVersionUID = 8326662121191415432L;

	private int numTime;
	private int baseTick;
	private int measureOffset;      // 小節で設定する

	public TimeSignature(MMLScore score, int tickOffset, int numTime, int baseTime) throws UndefinedTickException {
		super(tickOffset);
		this.numTime = numTime;
		this.baseTick = MMLTicks.getTick(Integer.toString(baseTime));
		this.measureOffset = tickToMeasure(score, tickOffset);
	}

	public TimeSignature(MMLScore score, int tickOffset, String numTime, String baseTime) throws UndefinedTickException {
		this(score, tickOffset, Integer.parseInt(numTime), Integer.parseInt(baseTime));
	}

	static void recalcTimeSignatureList(MMLScore score) {
		var list = score.getTimeSignatureList();
		list.sort(Comparator.comparingInt(t -> t.getMeasureOffset()));
		for (TimeSignature ts : list) {
			int tick = measureToCalcTick(score, ts.getMeasureOffset());
			ts.setTickOffset(tick);
		}
	}

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
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String toMMLString() {
		try {
			String baseTime = new MMLTicks("", baseTick).toMMLText();
			return String.format("%d/%s", numTime, baseTime);
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
		return "";
	}
}
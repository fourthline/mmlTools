/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mmlTools;

import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLException;


/**
 * 小節表示計算
 */
public final class Measure {
	private final int measure;
	private final int beat;
	private final int tick;
	private final int numTime;
	private final int beatTick;
	private final int measuredTick;

	/**
	 * tickからその小節の先頭tickを算出する
	 * @param score
	 * @param tick
	 * @return
	 */
	public static int measuredTick(MMLScore score, int tick) {
		return new Measure(score, tick).measuredTick();
	}

	/**
	 * tickから小節番号を算出する
	 * @param score
	 * @param tick
	 * @return
	 */
	public static int tickToMeasure(MMLScore score, int tick) {
		return new Measure(score, tick).getMeasure();
	}

	public static int nextMeasure(MMLScore score, int tick, boolean next) {
		var measure = new Measure(score, tick);
		if (next) {
			tick += measure.getMeasureTick();
		} else {
			tick -= measure.getBeatTick();
		}
		tick = new Measure(score, tick).measuredTick();
		return tick;
	}

	public Measure(MMLScore score, int tick) {
		var timeSignatureList = score.getTimeSignatureList();
		int numTime = score.getTimeCountOnly();
		int beatTick = score.getBeatTick();
		int m = 0;
		int baseTick = 0;

		for (TimeSignature t : timeSignatureList) {
			if (t.getTickOffset() <= tick) {
				baseTick = t.getTickOffset();
				m = t.getMeasureOffset();
				beatTick = t.getBaseTick();
				numTime = t.getNumTime();
			} else {
				break;
			}
		}

		this.measure = m + ((tick - baseTick) / (beatTick * numTime));
		int barR = (tick - baseTick) % (beatTick * numTime);
		this.beat = barR / beatTick;
		this.tick = barR % beatTick;
		this.numTime = numTime;
		this.beatTick = beatTick;
		this.measuredTick = tick - (this.beat * this.beatTick) - this.tick;
	}

	public int getMeasure() {
		return measure;
	}

	public int getBeat() {
		return beat;
	}

	public int getBeatTick() {
		return beatTick;
	}

	public int getNumTime() {
		return numTime;
	}

	public int getMeasureTick() {
		return numTime * beatTick;
	}

	public int measuredTick() {
		return measuredTick;
	}

	public String timeCount() {
		return Integer.toString(numTime);
	}

	public String timeBase() throws MMLException {
		return new MMLTicks("", beatTick).toMMLText();
	}

	public String toString() {
		if (beatTick >= 100) {
			return String.format("%d:%02d:%03d", measure, beat, tick);
		}
		return String.format("%d:%02d:%02d", measure, beat, tick);
	}
}

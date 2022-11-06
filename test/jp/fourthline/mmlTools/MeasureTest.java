/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.fourthline.mmlTools.core.UndefinedTickException;

public final class MeasureTest {

	private MMLScore score = new MMLScore();

	@Test
	public void test_barText01() {
		assertEquals("0:00:00", score.getBarTextTick(0));
		assertEquals("0:00:48", score.getBarTextTick(48));
		assertEquals("0:01:00", score.getBarTextTick(96));
		assertEquals("1:00:00", score.getBarTextTick(384));

		score.setBaseTime("3/4");
		assertEquals("0:01:00", score.getBarTextTick(96));
		assertEquals("1:01:00", score.getBarTextTick(384));

		score.setBaseTime("8/8");
		assertEquals("0:01:00", score.getBarTextTick(48));
		assertEquals("0:01:47", score.getBarTextTick(95));
		assertEquals("0:02:00", score.getBarTextTick(96));
		assertEquals("1:00:00", score.getBarTextTick(384));

		score.setBaseTime("2/2");
		assertEquals("0:00:048", score.getBarTextTick(48));
		assertEquals("0:00:120", score.getBarTextTick(120));
		assertEquals("0:01:100", score.getBarTextTick(292));
	}

	@Test
	public void test_barText02() throws UndefinedTickException {
		var ts1 = new TimeSignature(score, 384, 3, 4);
		var ts2 = new TimeSignature(score, 384*3, 6, 8);

		assertEquals(1, ts1.getMeasureOffset());
		assertEquals(3, ts2.getMeasureOffset());  // TODO: tickOffsetの検査もいれておく

		score.addTimeSignature(ts1);
		score.addTimeSignature(ts2);

		assertEquals("0:00:00", score.getBarTextTick(0));
		assertEquals("0:00:48", score.getBarTextTick(48));
		assertEquals("0:01:00", score.getBarTextTick(96));
		assertEquals("1:00:00", score.getBarTextTick(384));

		assertEquals("1:00:48", score.getBarTextTick(384+48));
		assertEquals("1:01:00", score.getBarTextTick(384+96));
		assertEquals("2:01:00", score.getBarTextTick(384+384));

		assertEquals("2:01:48", score.getBarTextTick(384+384+48));
		assertEquals("2:02:00", score.getBarTextTick(384+384+96));
		assertEquals("2:02:95", score.getBarTextTick(384+384+191));
		assertEquals("3:00:00", score.getBarTextTick(384+384+192));

		assertEquals("3:04:00", score.getBarTextTick(384+384+384));
	}

	@Test
	public void test_measuredTick() throws UndefinedTickException {
		var ts1 = new TimeSignature(score, 384, 3, 4);
		var ts2 = new TimeSignature(score, 384*3, 6, 8);
		score.getTimeSignatureList().add(ts1);
		score.getTimeSignatureList().add(ts2);

		assertEquals(0, new Measure(score, 0).measuredTick());
		assertEquals(0, new Measure(score, 383).measuredTick());
		assertEquals(384, new Measure(score, 384).measuredTick());
		assertEquals(384, new Measure(score, 384+192+95).measuredTick());
		assertEquals(672, new Measure(score, 384+192+96).measuredTick());
		assertEquals(672, new Measure(score, 384+384).measuredTick());

		assertEquals("3", new Measure(score, 384+384).timeCount());
		assertEquals("4", new Measure(score, 384+384).timeBase());
		assertEquals("6", new Measure(score, 384+384+384).timeCount());
		assertEquals("8", new Measure(score, 384+384+384).timeBase());
	}
}

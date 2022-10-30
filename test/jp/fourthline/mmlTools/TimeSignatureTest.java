/*
 * Copyright (C) 2022 ‚½‚ñ‚ç‚é
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import jp.fourthline.mmlTools.core.NanoTime;
import jp.fourthline.mmlTools.core.UndefinedTickException;

public class TimeSignatureTest {
	private MMLScore score = new MMLScore();

	@Before
	public void setup() throws UndefinedTickException {
		score.addTimeSignature(new TimeSignature(score, 0, 3, 4));
		score.addTimeSignature(new TimeSignature(score, 384, 4, 4));
		score.addTimeSignature(new TimeSignature(score, 3840, 6, 8));
	}

	@Test
	public void test() {
		assertEquals(0, TimeSignature.measureToCalcTick(score, 0));
		assertEquals(288, TimeSignature.measureToCalcTick(score, 1));
		assertEquals(1440, TimeSignature.measureToCalcTick(score, 4));
		assertEquals(5472, TimeSignature.measureToCalcTick(score, 16));
		assertEquals(58464, TimeSignature.measureToCalcTick(score, 200));
	}

	@Test
	public void testCompare() {
		long totalTime1 = 0;
		long totalTime2 = 0;

		for (int i = 0; i < 10000; i++) {
			NanoTime time = NanoTime.start();
			int t1 = TimeSignature.measureToCalcTick(score, i);
			long time1 = time.us(); time = NanoTime.start();
			int t2 = TimeSignature.measureToTick(score, i);
			long time2 = time.us();
			totalTime1 += time1;
			totalTime2 += time2;
			assertEquals(t1, t2);
			System.out.println(i + ": " + time1 + ", " + time2);
		}

		System.out.println(totalTime1 + ", " + totalTime2);
	}
}

/*
 * Copyright (C) 2022-2023 たんらる
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

	@Test
	public void testAddMeasure() {
		var list = score.getTimeSignatureList();
		assertEquals(0, list.get(0).getMeasureOffset());
		assertEquals(1, list.get(1).getMeasureOffset());
		assertEquals(10, list.get(2).getMeasureOffset());
		assertEquals("[0=3/4, 288=4/4, 3744=6/8]", list.toString());

		TimeSignature.addMeasure(score, 0);
		assertEquals(0, list.get(0).getMeasureOffset());
		assertEquals(2, list.get(1).getMeasureOffset());
		assertEquals(11, list.get(2).getMeasureOffset());
		assertEquals("[0=3/4, 576=4/4, 4032=6/8]", list.toString());

		TimeSignature.addMeasure(score, 2);
		assertEquals(0, list.get(0).getMeasureOffset());
		assertEquals(2, list.get(1).getMeasureOffset());
		assertEquals(12, list.get(2).getMeasureOffset());
		assertEquals("[0=3/4, 576=4/4, 4416=6/8]", list.toString());

		TimeSignature.addMeasure(score, 12);
		assertEquals(0, list.get(0).getMeasureOffset());
		assertEquals(2, list.get(1).getMeasureOffset());
		assertEquals(12, list.get(2).getMeasureOffset());
		assertEquals("[0=3/4, 576=4/4, 4416=6/8]", list.toString());
	}

	@Test
	public void testRemoveMeasure() {
		var list = score.getTimeSignatureList();
		assertEquals(0, list.get(0).getMeasureOffset());
		assertEquals(1, list.get(1).getMeasureOffset());
		assertEquals(10, list.get(2).getMeasureOffset());
		assertEquals("[0=3/4, 288=4/4, 3744=6/8]", list.toString());

		TimeSignature.removeMeasure(score, 10);
		assertEquals(0, list.get(0).getMeasureOffset());
		assertEquals(1, list.get(1).getMeasureOffset());
		assertEquals(10, list.get(2).getMeasureOffset());
		assertEquals("[0=3/4, 288=4/4, 3744=6/8]", list.toString());

		TimeSignature.removeMeasure(score, 9);
		assertEquals(0, list.get(0).getMeasureOffset());
		assertEquals(1, list.get(1).getMeasureOffset());
		assertEquals(9, list.get(2).getMeasureOffset());
		assertEquals("[0=3/4, 288=4/4, 3360=6/8]", list.toString());

		TimeSignature.removeMeasure(score, 0);
		assertEquals(0, list.get(0).getMeasureOffset());
		assertEquals(8, list.get(1).getMeasureOffset());
		assertEquals("[0=4/4, 3072=6/8]", list.toString());

		TimeSignature.removeMeasure(score, 0);
		TimeSignature.removeMeasure(score, 0);
		TimeSignature.removeMeasure(score, 0);
		TimeSignature.removeMeasure(score, 0);
		TimeSignature.removeMeasure(score, 0);
		TimeSignature.removeMeasure(score, 0);
		TimeSignature.removeMeasure(score, 0);
		TimeSignature.removeMeasure(score, 0);
		assertEquals("[0=6/8]", list.toString());
	}
}

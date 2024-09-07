/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mmlTools.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLTempoEvent;

public class MMLRestTicksTest {

	@Test
	public final void test_0() throws MMLException {
		String expect = "";
		MMLNoteEvent prevNoteEvent = new MMLNoteEvent(0, 0, 0);
		var tick = new MMLRestTicks(0, prevNoteEvent);
		var str = tick.toMMLTextWithMotionFix(MMLTempoEvent.INITIAL_TEMPO);
		assertEquals(expect, str);
	}

	@Test
	public final void test_48() throws MMLException {
		String expect = "r8";
		MMLNoteEvent prevNoteEvent = new MMLNoteEvent(0, 0, 0);
		var tick = new MMLRestTicks(48, prevNoteEvent);
		var str = tick.toMMLTextWithMotionFix(MMLTempoEvent.INITIAL_TEMPO);
		assertEquals(expect, str);
	}

	@Test
	public final void test_96() throws MMLException {
		String expect = "r4";
		MMLNoteEvent prevNoteEvent = new MMLNoteEvent(0, 0, 0);
		var tick = new MMLRestTicks(96, prevNoteEvent);
		var str = tick.toMMLTextWithMotionFix(MMLTempoEvent.INITIAL_TEMPO);
		assertEquals(expect, str);
	}

	@Test
	public final void test_19200() throws MMLException {
		String expect = "r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.v0c1.r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.r1.c1.r1.r1.r1.r1r1";
		MMLNoteEvent prevNoteEvent = new MMLNoteEvent(0, 0, 0);
		var tick = new MMLRestTicks(19200,  prevNoteEvent);
		var str = tick.toMMLTextWithMotionFix(MMLTempoEvent.INITIAL_TEMPO);
		assertEquals(expect, str);
	}


	private static record Data(int tick, int tempo, boolean expected) {}

	@Test
	public final void test() {
		List.of(
				new Data(8589, 120, false),
				new Data(8590, 120, true),
				new Data(8661, 121, false),
				new Data(8662, 121, true),
				new Data(18253, 255, false),
				new Data(18254, 255, true),
				new Data(2290, 32, false),
				new Data(2291, 32, true))
		.forEach(t -> {	
			var r = new MMLRestTicks(t.tick);
			try {
				r.toMMLTextWithMotionFix(t.tempo);
			} catch (MMLException e) {
				e.printStackTrace();
			}
			assertEquals(t.expected, r.isReplaced());
		});
	}

	@Test
	public void test_all() throws MMLException {
		long t = (long)Integer.MAX_VALUE * 2;
		for (int i = 32; i <= 256; i++) {
			int divTick = new MMLRestTicks(0).calcDivTick(i);
			int divTick2 = divTick - 1;
			var r1 = new MMLRestTicks(divTick);
			var r2 = new MMLRestTicks(divTick2);
			r1.toMMLTextWithMotionFix(i);
			r2.toMMLTextWithMotionFix(i);
			assertEquals(true, r1.isReplaced());
			assertEquals(false, r2.isReplaced());
			long h1 = divTick;
			long h2 = divTick2;
			h1 *= 60000000L;
			h2 *= 60000000L;
			h1 /= i;
			h2 /= i;
			assertTrue(h1 > t);
			assertTrue(h2 < t);
			System.out.println(divTick2);
		}
	}
}

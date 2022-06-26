/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author fourthline
 *
 */
public class MMLTempoEventTest {

	/**
	 * Test method for {@link jp.fourthline.mmlTools.MMLTempoEvent#appendToListElement(java.util.List)}.
	 */
	@Test
	public void testAppendToListElement() {
		ArrayList<MMLTempoEvent> tempoList = new ArrayList<MMLTempoEvent>();
		ArrayList<MMLTempoEvent> expectList = new ArrayList<MMLTempoEvent>();
		MMLTempoEvent tempoEvent1 = new MMLTempoEvent(120, 10);
		MMLTempoEvent tempoEvent2 = new MMLTempoEvent(150, 10);
		MMLTempoEvent tempoEvent3 = new MMLTempoEvent(150, 20);       // 違う位置に同じテンポも追加できる
		MMLTempoEvent tempoEvent4 = new MMLTempoEvent(150, 30, true); // isFirstの場合は追加されない

		tempoEvent1.appendToListElement(tempoList);
		tempoEvent2.appendToListElement(tempoList);
		tempoEvent3.appendToListElement(tempoList);
		tempoEvent4.appendToListElement(tempoList);

		tempoEvent2.appendToListElement(expectList);
		tempoEvent3.appendToListElement(expectList);
		System.out.println(tempoList);

		assertEquals(expectList.toString(), tempoList.toString());
	}

	private void checkGetTimeOnTickTest(String mml, long expect) {
		ArrayList<MMLTempoEvent> tempoList = new ArrayList<MMLTempoEvent>();
		MMLEventList eventList = new MMLEventList(mml, tempoList);

		int tick = (int) eventList.getTickLength();
		System.out.println("tick: " + tick);
		long time = Math.round(MMLTempoEvent.getTimeOnTickOffset(tempoList, tick));
		assertEquals(expect, time);
		assertEquals(tick, MMLTempoEvent.getTickOffsetOnTime(tempoList, time));
	}

	@Test
	public void testGetTimeOnTickOffset_0() {
		String mml = "t60cccccccccct120cccccccccc";
		long expect = 15000;

		checkGetTimeOnTickTest(mml, expect);
	}

	@Test
	public void testGetTimeOnTickOffset_1() {
		String mml = "cccccccccct60cccccccccc";
		long expect = 15000;

		checkGetTimeOnTickTest(mml, expect);
	}

	@Test
	public void testGetTimeOnTickOffset_2() {
		String mml = "t32l1.c";
		long expect = 11250;

		checkGetTimeOnTickTest(mml, expect);
	}

	@Test
	public void testGetTimeOnTickOffset_3() {
		String mml = "";
		long expect = 0;

		checkGetTimeOnTickTest(mml, expect);
	}

	@Test
	public void test_staticFunc() {
		MMLTempoEvent t0 = new MMLTempoEvent(120, 0);
		ArrayList<MMLTempoEvent> tempoList = new ArrayList<>();
		tempoList.add(new MMLTempoEvent(90, 96));

		assertEquals(96, MMLTempoEvent.getTickOffsetOnTime(tempoList, 500));
		assertEquals(120, MMLTempoEvent.searchOnTick(tempoList, 0));
		assertTrue(t0.equals(MMLTempoEvent.getMaxTempoEvent(tempoList)));
	}

	@Test
	public void test_fromString() {
		MMLTempoEvent t1 = new MMLTempoEvent(140, 192);
		String str = t1.toString();
		System.out.println(str);
		MMLTempoEvent t2 = MMLTempoEvent.fromString(str);
		System.out.println(t1);
		System.out.println(t2);
		assertEquals(true, t1.equals(t2));
	}

	@Test
	public void test_searchEqualsTick() {
		ArrayList<MMLTempoEvent> tempoList = new ArrayList<>();
		tempoList.add(new MMLTempoEvent(90, 96));
		tempoList.add(new MMLTempoEvent(150, 192));

		assertFalse(MMLTempoEvent.searchEqualsTick(tempoList, 95));
		assertTrue (MMLTempoEvent.searchEqualsTick(tempoList, 96));
		assertFalse(MMLTempoEvent.searchEqualsTick(tempoList, 97));
		assertFalse(MMLTempoEvent.searchEqualsTick(tempoList, 191));
		assertTrue (MMLTempoEvent.searchEqualsTick(tempoList, 192));
		assertFalse(MMLTempoEvent.searchEqualsTick(tempoList, 193));
	}

	@Test
	public void test_equals() {
		MMLTempoEvent t1 = new MMLTempoEvent(120, 0);
		MMLTempoEvent t2 = new MMLTempoEvent(120, 0);
		MMLTempoEvent t3 = new MMLTempoEvent(121, 0);
		MMLTempoEvent t4 = new MMLTempoEvent(120, 1);

		assertTrue(t1.equals(t2));
		assertFalse(t1.equals(t3));
		assertFalse(t1.equals(t4));
		assertFalse(t1.equals(""));
	}

	@Test
	public void test_getTimeOnTickOffset() {
		List<MMLTempoEvent> empty = List.of();
		List<MMLTempoEvent> t240 = List.of(new MMLTempoEvent(240, 0));
		List<MMLTempoEvent> t60 = List.of(new MMLTempoEvent(60, 0));
		double delta = 0.1;

		assertEquals(5.2, MMLTempoEvent.getTimeOnTickOffset(empty, 1), delta);
		assertEquals(10.4, MMLTempoEvent.getTimeOnTickOffset(empty, 2), delta);
		assertEquals(15.6, MMLTempoEvent.getTimeOnTickOffset(empty, 3), delta);
		assertEquals(20.8, MMLTempoEvent.getTimeOnTickOffset(empty, 4), delta);
		assertEquals(26.0, MMLTempoEvent.getTimeOnTickOffset(empty, 5), delta);
		assertEquals(31.2, MMLTempoEvent.getTimeOnTickOffset(empty, 6), delta);
		assertEquals(36.5, MMLTempoEvent.getTimeOnTickOffset(empty, 7), delta);
		assertEquals(41.7, MMLTempoEvent.getTimeOnTickOffset(empty, 8), delta);
		assertEquals(46.8, MMLTempoEvent.getTimeOnTickOffset(empty, 9), delta);
		assertEquals(52.0, MMLTempoEvent.getTimeOnTickOffset(empty, 10), delta);

		assertEquals(2.6, MMLTempoEvent.getTimeOnTickOffset(t240, 1), delta);
		assertEquals(5.2, MMLTempoEvent.getTimeOnTickOffset(t240, 2), delta);
		assertEquals(7.8, MMLTempoEvent.getTimeOnTickOffset(t240, 3), delta);
		assertEquals(10.4, MMLTempoEvent.getTimeOnTickOffset(t240, 4), delta);
		assertEquals(13.0, MMLTempoEvent.getTimeOnTickOffset(t240, 5), delta);
		assertEquals(15.6, MMLTempoEvent.getTimeOnTickOffset(t240, 6), delta);
		assertEquals(18.2, MMLTempoEvent.getTimeOnTickOffset(t240, 7), delta);
		assertEquals(20.8, MMLTempoEvent.getTimeOnTickOffset(t240, 8), delta);
		assertEquals(23.4, MMLTempoEvent.getTimeOnTickOffset(t240, 9), delta);
		assertEquals(26.0, MMLTempoEvent.getTimeOnTickOffset(t240, 10), delta);

		assertEquals(10.4, MMLTempoEvent.getTimeOnTickOffset(t60, 1), delta);
		assertEquals(20.8, MMLTempoEvent.getTimeOnTickOffset(t60, 2), delta);
		assertEquals(31.2, MMLTempoEvent.getTimeOnTickOffset(t60, 3), delta);
		assertEquals(41.7, MMLTempoEvent.getTimeOnTickOffset(t60, 4), delta);
		assertEquals(52.0, MMLTempoEvent.getTimeOnTickOffset(t60, 5), delta);
		assertEquals(62.5, MMLTempoEvent.getTimeOnTickOffset(t60, 6), delta);
		assertEquals(73.0, MMLTempoEvent.getTimeOnTickOffset(t60, 7), delta);
		assertEquals(83.3, MMLTempoEvent.getTimeOnTickOffset(t60, 8), delta);
		assertEquals(93.8, MMLTempoEvent.getTimeOnTickOffset(t60, 9), delta);
		assertEquals(104.2, MMLTempoEvent.getTimeOnTickOffset(t60, 10), delta);
	}

	@Test
	public void test_getTickOffsetOnTime() {
		List<MMLTempoEvent> empty = List.of();
		List<MMLTempoEvent> t240 = List.of(new MMLTempoEvent(240, 0));
		List<MMLTempoEvent> t60 = List.of(new MMLTempoEvent(60, 0));

		assertEquals(0, MMLTempoEvent.getTickOffsetOnTime(empty, 1));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(empty, 2));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(empty, 3));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(empty, 4));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(empty, 5));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(empty, 6));
		assertEquals(2, MMLTempoEvent.getTickOffsetOnTime(empty, 7));
		assertEquals(2, MMLTempoEvent.getTickOffsetOnTime(empty, 8));
		assertEquals(2, MMLTempoEvent.getTickOffsetOnTime(empty, 9));
		assertEquals(2, MMLTempoEvent.getTickOffsetOnTime(empty, 10));

		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t240, 1));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t240, 2));
		assertEquals(2, MMLTempoEvent.getTickOffsetOnTime(t240, 3));
		assertEquals(2, MMLTempoEvent.getTickOffsetOnTime(t240, 4));
		assertEquals(2, MMLTempoEvent.getTickOffsetOnTime(t240, 5));
		assertEquals(3, MMLTempoEvent.getTickOffsetOnTime(t240, 6));
		assertEquals(3, MMLTempoEvent.getTickOffsetOnTime(t240, 7));
		assertEquals(3, MMLTempoEvent.getTickOffsetOnTime(t240, 8));
		assertEquals(4, MMLTempoEvent.getTickOffsetOnTime(t240, 9));
		assertEquals(4, MMLTempoEvent.getTickOffsetOnTime(t240, 10));

		assertEquals(0, MMLTempoEvent.getTickOffsetOnTime(t60, 1));
		assertEquals(0, MMLTempoEvent.getTickOffsetOnTime(t60, 2));
		assertEquals(0, MMLTempoEvent.getTickOffsetOnTime(t60, 3));
		assertEquals(0, MMLTempoEvent.getTickOffsetOnTime(t60, 4));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t60, 5));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t60, 6));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t60, 7));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t60, 8));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t60, 9));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t60, 10));
		assertEquals(1, MMLTempoEvent.getTickOffsetOnTime(t60, 11));
	}

	@Test
	public void test_getTimeOnTickOffset_getTickOffsetOnTime_01() {
		List<MMLTempoEvent> empty = List.of();
		List<MMLTempoEvent> t240 = List.of(new MMLTempoEvent(240, 0));
		List<MMLTempoEvent> t60 = List.of(new MMLTempoEvent(60, 0));

		for (int i = 0; i < MMLEvent.MAX_TICK; i++) {
			for (List<MMLTempoEvent> t : Arrays.asList(empty, t240, t60)) {
				double time = MMLTempoEvent.getTimeOnTickOffset(t, i);
				assertEquals(i, MMLTempoEvent.getTickOffsetOnTime(t, time));
			}
		}
	}

	@Test
	public void test_getTimeOnTickOffset_getTickOffsetOnTime_02() {
		var tempoList = Arrays.asList(
				new MMLTempoEvent(240, 0),
				new MMLTempoEvent(60, 2001),
				new MMLTempoEvent(77, 80007),
				new MMLTempoEvent(255, 703498));

		for (int i = 0; i < MMLEvent.MAX_TICK; i++) {
			double time = MMLTempoEvent.getTimeOnTickOffset(tempoList, i);
			assertEquals(i, MMLTempoEvent.getTickOffsetOnTime(tempoList, time));
		}
	}
}

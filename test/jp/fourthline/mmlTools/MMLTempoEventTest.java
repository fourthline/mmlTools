/*
 * Copyright (C) 2013-2017 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import java.util.ArrayList;

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

		tempoEvent1.appendToListElement(tempoList);
		tempoEvent2.appendToListElement(tempoList);

		tempoEvent2.appendToListElement(expectList);
		System.out.println(tempoList);

		assertEquals(expectList.toString(), tempoList.toString());
	}

	private void checkGetTimeOnTickTest(String mml, long expect) {
		ArrayList<MMLTempoEvent> tempoList = new ArrayList<MMLTempoEvent>();
		MMLEventList eventList = new MMLEventList(mml, tempoList);

		int tick = (int) eventList.getTickLength();
		System.out.println("tick: " + tick);
		long time = MMLTempoEvent.getTimeOnTickOffset(tempoList, tick);
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
}

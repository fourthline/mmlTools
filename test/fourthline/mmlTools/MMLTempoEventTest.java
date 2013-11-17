/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

/**
 * @author fourthline
 *
 */
public class MMLTempoEventTest {

	/**
	 * Test method for {@link fourthline.mmlTools.MMLTempoEvent#appendToListElement(java.util.List)}.
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

	private void checkGetTimeOnTickTest(String mml, double expect) {
		ArrayList<MMLTempoEvent> tempoList = new ArrayList<MMLTempoEvent>();
		MMLEventList eventList = new MMLEventList(mml, tempoList);

		int tick = (int) eventList.getTickLength();
		System.out.println("tick: " + tick);
		assertEquals(expect, MMLTempoEvent.getTimeOnTickOffset(tempoList, tick), 0.0001);
	}

	@Test
	public void testGetTimeOnTickOffset_0() {
		String mml = "t60cccccccccct120cccccccccc";
		double expect = 15.0;

		checkGetTimeOnTickTest(mml, expect);
	}

	@Test
	public void testGetTimeOnTickOffset_1() {
		String mml = "cccccccccct60cccccccccc";
		double expect = 15.0;

		checkGetTimeOnTickTest(mml, expect);
	}

	@Test
	public void testGetTimeOnTickOffset_2() {
		String mml = "t32l1.c";
		double expect = 11.25;

		checkGetTimeOnTickTest(mml, expect);
	}

	@Test
	public void testGetTimeOnTickOffset_3() {
		String mml = "";
		double expect = 0.0;

		checkGetTimeOnTickTest(mml, expect);
	}

}

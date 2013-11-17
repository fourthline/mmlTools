/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fourthline.mmlTools.core.MMLTicks;

/**
 * @author fourthline
 *
 */
public class MMLEventListTest {

	/**
	 * Test method for {@link fourthline.mmlTools.MMLEventList#getMMLNoteEventList(fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws UndefinedTickException 
	 */
	@Test
	public void testGetMMLNoteEventList() throws UndefinedTickException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("aaa");
		List<MMLNoteEvent> expectList = 
				Arrays.asList(
						new MMLNoteEvent(57, t4, 0),
						new MMLNoteEvent(57, t4, t4),
						new MMLNoteEvent(57, t4, t4*2)
						);

		List<MMLNoteEvent> noteList = eventList.getMMLNoteEventList();
		System.out.println(noteList);
		System.out.println(expectList);
		assertEquals(expectList.toString(), noteList.toString());
	}

	/**
	 * Test method for {@link fourthline.mmlTools.MMLEventList#addMMLNoteEvent(fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws UndefinedTickException 
	 */
	@Test
	public void testAddMMLNoteEvent_0() throws UndefinedTickException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("ab-a");

		// 2つ目のノートを上書きします.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t4, t4));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}

	/**
	 * Test method for {@link fourthline.mmlTools.MMLEventList#addMMLNoteEvent(fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws UndefinedTickException 
	 */
	@Test
	public void testAddMMLNoteEvent_1() throws UndefinedTickException {
		int t4 = MMLTicks.getTick("4");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("a8.b-16aa");

		// 1つ目のノートの途中位置に新しく挿入します.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t16, t4 - t16));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}


	/**
	 * Test method for {@link fourthline.mmlTools.MMLEventList#addMMLNoteEvent(fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws UndefinedTickException 
	 */
	@Test
	public void testAddMMLNoteEvent_2() throws UndefinedTickException {
		int t4 = MMLTicks.getTick("4");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("a8.b-16&b-a");

		// 1つ目のノートの途中位置に新しく挿入します. かつ、2つ目のノートを上書きします.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t16+t4, t4 - t16));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}


	/**
	 * Test method for {@link fourthline.mmlTools.MMLEventList#addMMLNoteEvent(fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws UndefinedTickException 
	 */
	@Test
	public void testAddMMLNoteEvent_3() throws UndefinedTickException {
		int t4 = MMLTicks.getTick("4");
		int t8 = MMLTicks.getTick("8");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("a8.b-16&b-8r8a");

		// 1つ目のノートの途中位置に新しく挿入します. かつ、2つ目のノートを上書きしますが、tickが足りないパターンです.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t16+t8, t4 - t16));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}

	/**
	 * Test method for {@link fourthline.mmlTools.MMLEventList#addMMLNoteEvent(fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws UndefinedTickException 
	 */
	@Test
	public void testAddMMLNoteEvent_4() throws UndefinedTickException {
		int t4 = MMLTicks.getTick("4");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("aaa");
		MMLEventList expectList = new MMLEventList("a8.b-16&b-4&b-4");

		// 1つ目のノートの途中位置に新しく挿入します. かつ、2つ目、3つ目のノートを上書きします.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t16+t4+t4, t4 - t16));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}

	/**
	 * Test method for {@link fourthline.mmlTools.MMLEventList#addMMLNoteEvent(fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws UndefinedTickException 
	 */
	@Test
	public void testAddMMLNoteEvent_5() throws UndefinedTickException {
		int t4 = MMLTicks.getTick("4");
		MMLEventList eventList = new MMLEventList("rra");
		MMLEventList expectList = new MMLEventList("rb-a");

		// 休符の位置に挿入します.
		eventList.addMMLNoteEvent(new MMLNoteEvent(58, t4, t4));

		String expected = expectList.getMMLNoteEventList().toString();
		String actual = eventList.getMMLNoteEventList().toString();
		System.out.println(expected);
		System.out.println(actual);
		assertEquals(expected, actual);
	}


	/**
	 * Test method for {@link fourthline.mmlTools.MMLEventList#searchOnTickOffset(fourthline.mmlTools.MMLNoteEvent)}.
	 * @throws UndefinedTickException 
	 */
	@Test
	public void testSearchOnTickOffset() throws UndefinedTickException {
		int t4 = MMLTicks.getTick("4");
		int t16 = MMLTicks.getTick("16");
		MMLEventList eventList = new MMLEventList("ab-a");

		// 2つ目のノートを検索で取得します.
		MMLNoteEvent noteEvent = eventList.searchOnTickOffset(t4+t16);

		assertEquals(58, noteEvent.getNote());

	}


	@Test
	public void testToMMLString_0() {
		MMLEventList eventList = new MMLEventList("ara");

		String mml = eventList.toMMLString();
		System.out.println(mml);

		assertEquals(eventList.toString(), new MMLEventList(mml).toString());
	}

	/**
	 * オクターブ変化を付けたMMLの双方向変換のテスト.
	 */
	@Test
	public void testToMMLString_1() {
		MMLEventList eventList = new MMLEventList("cdef-gab>cdef+gab+<c1");

		String mml = eventList.toMMLString();
		System.out.println(mml);

		assertEquals(eventList.toString(), new MMLEventList(mml).toString());
	}

	/**
	 * tieによる連結が必要な音価さん.
	 */
	@Test
	public void testToMMLString_2() {
		MMLEventList eventList = new MMLEventList("c4&c16g2&g8d1.&d1.&d1.&d24");

		String mml = eventList.toMMLString();
		System.out.println(mml);

		assertEquals(eventList.toString(), new MMLEventList(mml).toString());
	}

	/**
	 * tempo, velocity を含むMML.
	 */
	@Test
	public void testToMMLString_3() {
		MMLEventList eventList = new MMLEventList("T150v10c8.g16e4v8g-<at120<b-");

		String mml = eventList.toMMLString(true);
		System.out.println(mml);

		assertEquals(eventList.toString(), new MMLEventList(mml).toString());
	}

	/**
	 * tempo, velocity を含むMML. (テンポを跨ぐNoteがある)
	 */
	@Test
	public void testToMMLString_4() {
		MMLEventList eventList = new MMLEventList("c4d4");
		List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();
		String expectMML = "c8t150r8d4";
		globalTempoList.add(new MMLTempoEvent(150, 48));

		eventList.setGlobalTempoList(globalTempoList);
		String mml = eventList.toMMLString(true);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * tempo, velocity を含むMML. (テンポ指定が最終ノートより後方にある)
	 */
	@Test
	public void testToMMLString_5() {
		MMLEventList eventList = new MMLEventList("c4d4");
		String expectMML = "c4d4v0c1.c1.t150";
		List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();
		globalTempoList.add(new MMLTempoEvent(150, 96*2+96*12));

		eventList.setGlobalTempoList(globalTempoList);
		String mml = eventList.toMMLString(true);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * tempo, velocity を含むMML. (全体長分の後方R埋め)
	 */
	@Test
	public void testToMMLString_6() {
		MMLEventList eventList = new MMLEventList("c4d4");
		String expectMML = "c4d4r1r1";

		String mml = eventList.toMMLString(false, 96+96+(96*8));
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}

	/**
	 * tempo, velocity を含むMML. (rTn式)
	 */
	@Test
	public void testToMMLString_7() {
		MMLEventList eventList = new MMLEventList("c1r1c1");
		String expectMML = "c1v0c1v8t150c1";
		List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();
		globalTempoList.add(new MMLTempoEvent(150, (96*8)));

		eventList.setGlobalTempoList(globalTempoList);
		String mml = eventList.toMMLString(true);
		System.out.println(mml);

		assertEquals(expectMML, mml);
	}
}

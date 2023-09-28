/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.Test;

import jp.fourthline.FileSelect;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.TuningBase;
import jp.fourthline.mmlTools.core.MMLException;


/**
 * @author fourthline
 *
 */
public class MMLNoteEventTest extends FileSelect {

	@Test
	public void testGetOctave() {
		MMLEventList eventList = new MMLEventList("c");
		MMLNoteEvent noteEvent = eventList.getMMLNoteEventList().get(0);

		System.out.println(noteEvent.getOctave());
		assertEquals(4, noteEvent.getOctave());
	}

	@Test
	public void testIncreaseOctave() {
		MMLEventList eventList = new MMLEventList("c>>c");
		MMLNoteEvent noteEvent0 = eventList.getMMLNoteEventList().get(0);
		MMLNoteEvent noteEvent1 = eventList.getMMLNoteEventList().get(1);

		int noteOctave0 = noteEvent0.getOctave();
		assertEquals(4, noteEvent0.getOctave());

		String changedOctave = noteEvent1.changeOctaveinMMLString(noteOctave0);
		assertEquals(">>", changedOctave);
	}

	@Test
	public void testDecreaseOctave() {
		MMLEventList eventList = new MMLEventList("c<<c");
		MMLNoteEvent noteEvent0 = eventList.getMMLNoteEventList().get(0);
		MMLNoteEvent noteEvent1 = eventList.getMMLNoteEventList().get(1);

		int noteOctave0 = noteEvent0.getOctave();
		assertEquals(4, noteEvent0.getOctave());

		String changedOctave = noteEvent1.changeOctaveinMMLString(noteOctave0);
		assertEquals("<<", changedOctave);
	}

	@Test
	public void testTuningNote_64() throws MMLException {
		MMLEventList eventList = new MMLEventList("c1&c64");
		String expected = "c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64";
		MMLNoteEvent noteEvent = eventList.getMMLNoteEventList().get(0);
		noteEvent.setTuningNote(TuningBase.L64);

		System.out.println(noteEvent.toMMLString());
		assertEquals(expected, noteEvent.toMMLString());

		assertEquals(TuningBase.L64, new MMLEventList(expected).getMMLNoteEventList().get(0).getTuningBase());
	}

	@Test
	public void testTuningNote_32() throws MMLException {
		MMLEventList eventList = new MMLEventList("c1&c64");
		String expected = "c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c32&c64";
		MMLNoteEvent noteEvent = eventList.getMMLNoteEventList().get(0);
		noteEvent.setTuningNote(TuningBase.L32);

		System.out.println(noteEvent.toMMLString());
		assertEquals(expected, noteEvent.toMMLString());

		assertEquals(TuningBase.L32, new MMLEventList(expected).getMMLNoteEventList().get(0).getTuningBase());
	}

	@Test
	public void testTuningNote_16() throws MMLException {
		MMLEventList eventList = new MMLEventList("c1&c64");
		String expected = "c16&c16&c16&c16&c16&c16&c16&c16&c16&c16&c16&c16&c16&c16&c16&c16&c64";
		MMLNoteEvent noteEvent = eventList.getMMLNoteEventList().get(0);
		noteEvent.setTuningNote(TuningBase.L16);

		System.out.println(noteEvent.toMMLString());
		assertEquals(expected, noteEvent.toMMLString());

		assertEquals(TuningBase.L16, new MMLEventList(expected).getMMLNoteEventList().get(0).getTuningBase());
	}

	@Test
	public void testParse_0() {
		MMLEventList eventList1 = new MMLEventList("v14l16o5aav16cv-1c");
		MMLEventList eventList2 = new MMLEventList("V14L16O5aacc");

		assertEquals(eventList1.toString(), eventList2.toString());
	}

	private String getExpect(String filename) throws IOException {
		StringBuilder sb = new StringBuilder();
		var scanner = new Scanner(fileSelect(filename));
		scanner.forEachRemaining(s -> sb.append(s).append('\n'));
		scanner.close();
		return sb.toString();
	}

	private Map<Integer, String> parseTableList(String list) {
		Map<Integer, String> map = new HashMap<>();
		var scanner = new Scanner(list);
		scanner.forEachRemaining(s -> {
			var t = s.split("=");
			map.put(Integer.parseInt(t[0]), t[1]);
		});
		scanner.close();
		return map;
	}

	private boolean checkTable(String expect, String actual) {
		boolean ret = true;
		var m1 = parseTableList(expect);
		var m2 = parseTableList(actual);
		for (var key : m1.keySet()) {
			String s1 = m1.get(key);
			String s2 = m2.get(key);
			if (s1.length() < s2.length()) {
				System.out.println(key+":"+s2+" > "+s1);
				ret = false;
			}
		}
		return ret;
	}

	@Test
	public void testNoteTexts() throws MMLException, IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = MMLTicks.minimumTick(); i <= 768; i++) {
			MMLNoteEvent event = new MMLNoteEvent(48, i, 0);
			String s = event.toMMLString();
			sb.append(i+"=").append(s).append(':').append(s.length()).append('\n');
		}
		String expect = getExpect("noteTexts.txt");
		String actual = sb.toString();
		assertTrue(checkTable(expect, actual));
		assertEquals(expect, actual);
	}

	@Test
	public void testRestTexts() throws MMLException, IOException {
		StringBuilder sb = new StringBuilder();
		MMLNoteEvent prev = new MMLNoteEvent(48, 48, 0);
		for (int i = MMLTicks.minimumTick(); i <= 768; i++) {
			MMLNoteEvent event = new MMLNoteEvent(48, 0, 48+i);
			String s = event.toMMLString(prev);
			sb.append(i+"=").append(s).append(':').append(s.length()).append('\n');
		}
		String expect = getExpect("restTexts.txt");
		String actual = sb.toString();
		assertTrue(checkTable(expect, actual));
		assertEquals(expect, actual);
	}
}

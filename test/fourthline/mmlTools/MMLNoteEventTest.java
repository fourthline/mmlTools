/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import org.junit.Test;

import fourthline.mmlTools.core.TuningBase;


/**
 * @author fourthline
 *
 */
public class MMLNoteEventTest {

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
	public void testTuningNote() throws UndefinedTickException {
		MMLEventList eventList = new MMLEventList("c1");
		String expected = "c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64";
		MMLNoteEvent noteEvent = eventList.getMMLNoteEventList().get(0);
		noteEvent.setTuningNote(TuningBase.L64);

		System.out.println(noteEvent.toMMLString());
		assertEquals(expected, noteEvent.toMMLString());
	}

	@Test
	public void testParse_0() {
		MMLEventList eventList1 = new MMLEventList("v14l16o5aa");
		MMLEventList eventList2 = new MMLEventList("V14L16O5aa");

		assertEquals(eventList1.toString(), eventList2.toString());
	}


}

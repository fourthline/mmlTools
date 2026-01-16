/*
 * Copyright (C) 2014-2026 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLException;

public class MMLEventTest {
	/**
	 * insertTick
	 * @throws MMLExceptionList 
	 * @throws MMLException 
	 */
	@Test
	public void testInsertTick0() throws MMLExceptionList, MMLException {
		MMLEventList eventList1 = new MMLEventList("aabb");
		MMLEventList eventList2 = new MMLEventList("aar1bb");

		MMLEvent.insertTick(eventList1.getMMLNoteEventList(), MMLTicks.getTick("2"), MMLTicks.getTick("1"));

		assertEquals(MMLBuilder.create(eventList2).toMMLString(), MMLBuilder.create(eventList1).toMMLString());
	}

	/**
	 * removeTick
	 * @throws MMLExceptionList 
	 * @throws MMLException 
	 */
	@Test
	public void testRemoveTick0() throws MMLExceptionList, MMLException {
		MMLEventList eventList1 = new MMLEventList("aac1bb");
		MMLEventList eventList2 = new MMLEventList("a4a4b8&b9b4");

		MMLEvent.removeTick(eventList1.getMMLNoteEventList(), MMLTicks.getTick("2"), MMLTicks.getTick("1")+MMLTicks.getTick("64"));

		assertEquals(MMLBuilder.create(eventList2).toMMLString(), MMLBuilder.create(eventList1).toMMLString());
	}

	/**
	 * removeTick
	 * @throws MMLExceptionList 
	 * @throws MMLException 
	 */
	@Test
	public void testRemoveTick1() throws MMLExceptionList, MMLException {
		MMLEventList eventList1 = new MMLEventList("aac1bb");
		MMLEventList eventList2 = new MMLEventList("a4a8&a9c64b4b4");

		MMLEvent.removeTick(eventList1.getMMLNoteEventList(), MMLTicks.getTick("2")-MMLTicks.getTick("64"), MMLTicks.getTick("1"));

		assertEquals(MMLBuilder.create(eventList2).toMMLString(), MMLBuilder.create(eventList1).toMMLString());
	}
}

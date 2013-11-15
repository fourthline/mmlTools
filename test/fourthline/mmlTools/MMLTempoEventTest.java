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

}

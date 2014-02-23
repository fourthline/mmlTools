/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;


import static org.junit.Assert.*;

import org.junit.*;



public class MMLTimeToolsTest {

	/**
	 * test aaaa
	 * @throws Exception
	 */
	@Test
	public void testTime_0() throws Exception {
		MMLTimeTools tools = new MMLTimeTools("MML@aaaa;", false);

		assertEquals(2.0, tools.getPlayTime(), 0.00001);
		assertEquals(2.0, tools.getMabinogiTime(), 0.00001);
	}
}

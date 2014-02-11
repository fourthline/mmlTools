/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;


import static org.junit.Assert.*;

import org.junit.*;


/**
 * @author たんらる
 *
 */
public class DrumToolsTest {

	/**
	 * text format
	 */
	@Test
	public void testMakeForMabiMML_partformat() {
		DrumTools tools = new DrumTools("aaa");

		assertEquals("", tools.getMelody());
		assertEquals("", tools.getChord1());
		assertEquals("", tools.getChord2());
		assertEquals("MML@,,;", tools.getMML());
	}

	/**
	 * MML@ format
	 */
	@Test
	public void testMakeForMabiMML_MMLformat() {
		DrumTools tools = new DrumTools("MML@aaa,bbb,ccc;");

		assertEquals("aaa", tools.getMelody());
		assertEquals("bbb", tools.getChord1());
		assertEquals("ccc", tools.getChord2());
		assertEquals("MML@aaa,bbb,ccc;", tools.getMML());
	}

	/**
	 * t32 case
	 */
	@Test
	public void testMakeForMabiMML_t32_0() throws Exception {
		DrumTools tools = new DrumTools("MML@t32l1.cccc;");

		tools.makeForMabiMML(new ComposeRank(8, 2, 10));

		assertEquals("t32l1.cc", tools.getMelody());
		assertEquals("cc", tools.getChord1());
		assertEquals("l1.rrrr", tools.getChord2());
		assertEquals(tools.getOverSec(), 0.0, 0.0001);
	}

	/**
	 * t32 case + t240r64
	 */
	@Test
	public void testMakeForMabiMML_t32_1() throws Exception {
		DrumTools tools = new DrumTools("MML@t32l1.cccct240r64;");

		tools.makeForMabiMML(new ComposeRank(8, 9, 10));

		assertEquals("t32l1.cc", tools.getMelody());
		assertEquals("cct240r64", tools.getChord1());
		assertEquals("t32l1.rrrrr", tools.getChord2());
		assertEquals(true, (tools.getOverSec() >= 0.0) );
	}

	/**
	 * t64 case
	 */
	@Test
	public void testMakeForMabiMML_t64_2() throws Exception {
		DrumTools tools = new DrumTools("MML@t64l1.cccc;");

		tools.makeForMabiMML(new ComposeRank(8, 9, 10));

		assertEquals("t64l1.cc", tools.getMelody());
		assertEquals("cc", tools.getChord1());
		assertEquals("t32l1.rr", tools.getChord2());
		assertEquals(true, (tools.getOverSec() >= 0.0) );
	}

	/**
	 * t128 case
	 */
	@Test
	public void testMakeForMabiMML_t128_2() throws Exception {
		DrumTools tools = new DrumTools("MML@t128l1.cccc;");

		tools.makeForMabiMML(new ComposeRank(9, 9, 10));

		assertEquals("t128l1.cc", tools.getMelody());
		assertEquals("cc", tools.getChord1());
		assertEquals("t32l1.r", tools.getChord2());
		assertEquals(0.0, tools.getOverSec(), 0.001);
	}

	/**
	 * over time test
	 */
	@Test
	public void testOverTime_0() throws Exception {
		DrumTools tools = new DrumTools("MML@T32l1.rrrr2");

		tools.makeForMabiMML(new ComposeRank(5, 10, 10));

		assertEquals("T32", tools.getMelody());
		assertEquals("l1.rrrr2", tools.getChord1());
		assertEquals("l1.rrrr", tools.getChord2());
		assertEquals(7.5, tools.getOverSec(), 0.001);
	}

	/**
	 * ドラムの音量調節テスト
	 */
	@Test
	public void testDrumDisVol() throws Exception {
		DrumTools tools = new DrumTools("MML@v0v1v2v3v4v5v6v7v8v9v10v11v12v13v14v15");

		String result = tools.disDrumVolumn();

		assertEquals("v0v1v1v2v3v4v4v5v6v7v7v8v9v10v10v11", result);
	}

	/**
	 * maki option t1
	 */
	@Test
	public void testMakeForMabiMML4Maki_t1() throws Exception {
		DrumTools tools = new DrumTools("MML@abct120l64rrrl2.r;");


		tools.setMakiOption(true);
		tools.makeForMabiMML(new ComposeRank(10, 10, 4));

		assertEquals("abc", tools.getMelody());
		assertEquals("t120l64rrr", tools.getChord1());
		assertEquals("l2.rt32l1.r", tools.getChord2());
	}

	/**
	 * maki option t2
	 */
	@Test
	public void testMakeForMabiMML4Maki_t2() throws Exception {
		DrumTools tools = new DrumTools("MML@abct120l64rrrl2.r;");


		tools.setMakiOption(true);
		tools.makeForMabiMML(new ComposeRank(11, 10, 4));

		assertEquals("abct120l64r", tools.getMelody());
		assertEquals("l64rrl2.r", tools.getChord1());
		assertEquals("t32l1.r", tools.getChord2());
	}
}

/*
 * Copyright (C) 2013 たんらる
 */

package jp.fourthline.mmlTools.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;


public class MelodyParserTest {

	@Test
	public void testParserNull() throws Exception {
		MelodyParser parser = new MelodyParser(null);
		assertEquals(0, parser.getLength());
		assertEquals("4", parser.getMmlL());
		assertEquals(0.0, parser.getPlayLengthByTempoList(), 0.0001);
	}

	@Test
	public void testParser0() throws Exception {
		MelodyParser parser = new MelodyParser("");
		assertEquals(0, parser.getLength());
		assertEquals("4", parser.getMmlL());
		assertEquals(0.0, parser.getPlayLengthByTempoList(), 0.0001);
	}

	@Test
	public void testParser1() throws Exception {
		MelodyParser parser = new MelodyParser("t120c8c16c16l16ccl8c16c16<c&ccc>");
		assertEquals(384, parser.getLength());
		assertEquals("8", parser.getMmlL());
		assertEquals(2.0, parser.getPlayLengthByTempoList(), 0.0001);
	}

	/**
	 * play length test1
	 */
	@Test
	public void testPlayLength_1() throws Exception {
		MelodyParser parser = new MelodyParser("t60cccccccccc");

		assertEquals(10.0, parser.getPlayLengthByTempoList(), 0.0001);
	}

	/**
	 * play length test2
	 */
	@Test
	public void testPlayLength_2() throws Exception {
		MelodyParser parser = new MelodyParser("t120cccccccccc");

		assertEquals(5.0, parser.getPlayLengthByTempoList(), 0.0001);
	}

	/**
	 * play length test3
	 */
	@Test
	public void testPlayLength_3() throws Exception {
		MelodyParser parser = new MelodyParser("t60cccccccccct120cccccccccc");

		assertEquals(15.0, parser.getPlayLengthByTempoList(), 0.0001);
	}

	/**
	 * play length test4
	 */
	@Test
	public void testPlayLength_4() throws Exception {
		MelodyParser parser = new MelodyParser("cccccccccct60cccccccccc");

		assertEquals(15.0, parser.getPlayLengthByTempoList(), 0.0001);
	}

	/**
	 * play t32 l1.
	 */
	@Test
	public void testPlay96Length_0() throws Exception {
		MelodyParser parser = new MelodyParser("t32l1.c");

		assertEquals(11.25, parser.getPlayLengthByTempoList(), 0.0001);
	}

	/**
	 * play time test non opt
	 */
	@Test
	public void testSnowPlay() throws Exception {
		MelodyParser parser = new MelodyParser("t112r2o5v12r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c-4.<r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g-4.r8.g2&g4&g16&g2o5r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c4.r4.c-4.<r4.g4.r4.g4.r4.g4.r4.g4.r4.g-4.r4.g-4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.r4.g4.t108r4.g-4.r8.g2&g4&g16&g2.");

		assertEquals(60+51.1, parser.getPlayLengthByTempoList(), 0.1);
	}

	/**
	 * play time test  opt
	 */
	@Test
	public void testSnowPlay_opt() throws Exception {
		MelodyParser parser = new MelodyParser("t112r2.r8v12l4.>crcrcrcrcrcrcrcrcrcrcrcrcrcrcr<brgrgrgrgrgrgrgrgrgrgrgrgrgrgrgrf+r8.g2&g4&g16&g2r>crcrcrcrcrcrcrcrcrcrcrcrcrcrcr<brgrgrgrgrf+rf+rgrgrgrgrgrgrgrgrgt108rf+r8.g2&g4&g16&g2.");

		assertEquals(60+51.1, parser.getPlayLengthByTempoList(), 0.1);
	}

	/**
	 * tempo list 1
	 */
	@Test
	public void testTempoList_1() throws Exception {
		MelodyParser parser = new MelodyParser("t120cccct60cc");
		MelodyParser parser2 = new MelodyParser("cccccc");
		MelodyParser parser3 = new MelodyParser("l2ccc");

		assertEquals(4.0, parser.getPlayLengthByTempoList(), 0.0001);
		parser.mergeParser(parser2);
		assertEquals(4.0, parser.getPlayLengthByTempoList(), 0.0001);
		parser.mergeParser(parser3);
		assertEquals(4.0, parser.getPlayLengthByTempoList(), 0.0001);
	}	

	/**
	 * tempo list 2
	 */
	@Test
	public void testTempoList_2() throws Exception {
		MelodyParser parser = new MelodyParser("t120cccct60cc");
		MelodyParser parser2 = new MelodyParser("ccccct120c");
		MelodyParser parser3 = new MelodyParser("l2cct120c");

		assertEquals(4.0, parser.getPlayLengthByTempoList(), 0.0001);
		parser.mergeParser(parser2);
		assertEquals(3.5, parser.getPlayLengthByTempoList(), 0.0001);
		parser.mergeParser(parser3);
		assertEquals(3.0, parser.getPlayLengthByTempoList(), 0.0001);
	}

	/**
	 * tempo check1
	 */
	@Test
	public void testTempoCheck_0() throws Exception {
		MelodyParser parser = new MelodyParser("t120cccro2v2t60cc");
		parser.getLength();
		List<Integer> warnList = parser.getWarnIndex();

		assertEquals(4.0, parser.getPlayLengthByTempoList(), 0.0001);
		assertArrayEquals(new Integer[] { 12 } , warnList.toArray());
	}

	/**
	 * note min, max test
	 */
	@Test
	public void testMinMaxTest_0() throws Exception {
		MelodyParser parser = new MelodyParser("t120cccco2v2t60cc");
		parser.getLength();

		assertEquals(24, parser.getMinNote());
		assertEquals(48, parser.getMaxNote());
	}

	/**
	 * note min, max test
	 */
	@Test
	public void testMinMaxTest_1() throws Exception {
		MelodyParser parser = new MelodyParser("t120cgcco2v2t60gg");
		parser.getLength();

		assertEquals(31, parser.getMinNote());
		assertEquals(55, parser.getMaxNote());
	}


	/**
	 * note min, max test
	 */
	@Test
	public void testMinMaxTest_2() throws Exception {
		MelodyParser parser = new MelodyParser("t120cg+cco2v2t60gg-");
		parser.getLength();

		assertEquals(30, parser.getMinNote());
		assertEquals(56, parser.getMaxNote());
	}

	/**
	 * note min, max test
	 */
	@Test
	public void testMinMaxTest_3() throws Exception {
		MelodyParser parser = new MelodyParser("o3v12b+");
		parser.getLength();

		assertEquals(48, parser.getMinNote());
		assertEquals(48, parser.getMaxNote());
	}
}

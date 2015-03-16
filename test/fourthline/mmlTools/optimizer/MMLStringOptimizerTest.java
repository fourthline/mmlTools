/*
 * Copyright (C) 2013-2015 たんらる
 */

package fourthline.mmlTools.optimizer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTicks;

/**
 * MML最適化のテスト.
 */
public class MMLStringOptimizerTest {

	private static final int TIMEOUT = 100;

	@Before
	public void setup() throws UndefinedTickException {
		MMLStringOptimizer.setDebug(true);
		MMLTicks.getTick("64");
	}

	@Test(timeout=TIMEOUT)
	public void test0() {
		MMLStringOptimizer optimizer = new MMLStringOptimizer("");
		assertEquals("", optimizer.toString());
	}

	private void checkMMLStringOptimize(String input, String expect) {
		MMLStringOptimizer optimizer = new MMLStringOptimizer(input);
		String mml = optimizer.toString();

		System.out.println(input);
		System.out.println(mml);
		System.out.println(expect);
		System.out.printf("%d > %d\n", input.length(), mml.length());
		System.out.printf("expect: %d\n", expect.length());
		assertTrue(mml.length() <= expect.length());
		assertEquals(expect, mml);

		MMLEventList eventList1 = new MMLEventList(input);
		MMLEventList eventList2 = new MMLEventList(mml);
		assertEquals(eventList1.getMMLNoteEventList().toString(), eventList2.getMMLNoteEventList().toString());
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_0() {
		String input  = "c8c8c16c16c8c8c16";
		String expect = "c8c8l16ccc8c8c";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_1() {
		String input  = "c8c8c16c16c8c8";
		String expect = "l8ccc16c16cc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_2() {
		String input  = "c4c4c16c4c4c4c16c16c8c8c4c4c16c16c4c4c4";
		String expect = "ccc16cccl16ccc8c8c4c4ccl4ccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_3() {
		String input  = "c16c16c16c16c1c1c1c1c16";
		String expect = "l16ccccl1ccccc16";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_4() {
		String input  = "c16c16c16c16c1c1c1c16";
		String expect = "l16ccccc1c1c1c";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_5() {
		String input  = "c16c4c4c16c16c4c4c4";
		String expect = "c16ccc16c16ccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_6() {
		String input  = "c16c16c4c4c16c16c4c4c4";
		String expect = "l16ccc4c4ccl4ccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_7() {
		String input  = "c16ccc16c16ccc";
		String expect = "c16ccc16c16ccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_8() {
		String input  = "c16c16c16c8c8c8c16c8c16c4.c8c16c8.c16c8c16c16c16";
		String expect = "l16cccc8c8c8cc8cc4.c8cc8.cc8ccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_9_0() {
		String input  = "c2c8c2c8c16c16c8c8c8";
		String expect = "c2l8cc2cc16c16ccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_9_1() {
		String input  = "c2c8c2c8c16c16c2c2c2";
		String expect = "l2cc8cc8c16c16ccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_amp_L_0() {
		String input  = "c2&c8d8d8d8d8d8d8";
		String expect = "c2l8&cdddddd";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_x1_1() {
		String input  = "r1.r1.r1.r1.r1.r1.r1.c4a2c4a2c4a2c4a2c4a2c4a2c4a2r1.r1.r1.r1.r1.r1.r1.r1.r1.";
		String expect = "l1.rrrrrrrc4l2ac4ac4ac4ac4ac4ac4al1.rrrrrrrrr";
		checkMMLStringOptimize(input, expect);
	}

	@Test
	public void testOptimize_oct() {
		String input  = ">c<<<c32.>>>c16.<<v12<c64>>t121>co6d<<d";
		String expect = ">co2c32.o5c16.o2v12c64o5t121c>d<<d";
		checkMMLStringOptimize(input, expect);
	}

	@Test
	public void testOptimize_cb_oct() {
		String input  = "c<b>c<v10b>c<t121b>v12cd<<b>d>>c<g";
		String expect = "cc-c<v10bb+t121b>v12cd<c-d>b+g";
		checkMMLStringOptimize(input, expect);
	}

	@Test @Ignore
	public void testOptimize_n() {
		String input  = "o7co1co7c";
		String expect = "o7cn12c";
		checkMMLStringOptimize(input, expect);
	}
}

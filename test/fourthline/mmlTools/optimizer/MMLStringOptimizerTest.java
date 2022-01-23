/*
 * Copyright (C) 2013-2022 たんらる
 */

package fourthline.mmlTools.optimizer;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.core.UndefinedTickException;

/**
 * MML最適化のテスト.
 */
public class MMLStringOptimizerTest {

	private static final int TIMEOUT = 100;

	@Before
	public void setup() throws UndefinedTickException {
		checkMMLStringOptimize("c4", "c");
		MMLStringOptimizer.setDebug(true);
		MMLTicks.getTick("64");
	}

	@After
	public void cleanup() {
		MMLStringOptimizer.setDebug(false);
	}

	@Test(timeout=TIMEOUT)
	public void test0() {
		MMLStringOptimizer optimizer = new MMLStringOptimizer("");
		assertEquals("", optimizer.toString());
	}

	private void checkMMLStringOptimize(String input, String expect) {
		checkMMLStringOptimize(input, expect, (t) -> t.toString());
	}

	private void checkMMLStringOptimize(String input, String expect, Function<MMLStringOptimizer, String> f) {
		try {
			MMLStringOptimizer optimizer = new MMLStringOptimizer(new MMLEventList(input).toMMLString(true, true));
			String mml = f.apply(optimizer);

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
		} catch (UndefinedTickException e) {
			fail(e.getMessage());
		}
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
		String input  = "r1.r1.r1.r1.r1.r1.r1.c4a2c4a2c4a2c4a2c4a2c4a2c4a2r1.r1.r1.r1.r1.r1.r1.r1.r1.b4";
		String expect = "l1.rrrrrrrc4l2ac4ac4ac4ac4ac4ac4al1.rrrrrrrrrb4";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_oct() {
		String input  = ">c<<<c32.>>>c16.<<v12<c64>>t121>co6d<<d";
		String expect = ">co2c21o5c16.v12o2c64t121o5c>d<<d";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_oct2() {
		String input  = "r.>cr16<a+r.>cr16<a+r.>cr16<a+e.";
		String expect = "r.b+r16a+r.b+r16a+r.b+r16a+e.";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_oct3() {
		String input  = "r.<br16>a+r.<br16>a+r.<br16>a+g.";
		String expect = "r.c-r16a+r.c-r16a+r.c-r16a+g.";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_cb_oct() {
		String input  = "c<b>c<v10b>c<t121b>v12cd<v11<b>d>>t118c<g";
		String expect = "cc-cv10c-ct121c-v12cdv11<c-dt118>b+g";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_n() {
		String input  = "c<g+>c<g>>f<<a";
		String expect = "c<g+b+gn65a";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_n2() {
		String input  =  "o7cccc<<<<<c+>>>>cccc";
		String expect =  "o7ccccn25<cccc";
		// other:        "o7cccco2c+o6cccc"
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_n3() {
		String input  = "o7cccc<<<<<c+>>>>>cccc";
		String expect = "o7ccccn25cccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_n4() {
		String input  =  "o7cccc<<<<<c+c+>>>>>cccc";
		String expect =  "o7ccccn25n25cccc";
		// other:        "o7cccco2c+c+o7cccc"
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_n5() {
		String input  =  "o7cccc<<<<<c+>c+>>>>cccc";
		String expect =  "o7ccccn25n37cccc";
		// other:        "o7cccco2c+>c+o7cccc"
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testOptimize_n6() {
		String input  =  "n1n25n1c64n25n1n25n1n25";
		String expect =  "n1n25n1c64n25n1n25n1n25";
		checkMMLStringOptimize(input, expect);
	}

	@Ignore
	@Test(timeout=TIMEOUT)
	public void testSwap_l1() {
		String input  =  "l16aaaaaaab4&bl4ccccccccc";
		String expect =  "l16aaaaaaabl4&bccccccccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void testSwap_l2() {
		String input  =  "aaaaaaab16&bl16ccccccccc";
		String expect =  "aaaaaaabl16&bccccccccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void test_o0cm() {
		String input  =  "o4co0c-o4ccccco0c-c-c-c-cc-cc-c-cccc>ccc<c->ccc";
		String expect =  "co0c-o4ccccco0c-c-c-c-cc-cc-c-cccc>ccc<c->ccc";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void test_F1() {
		String input  =  "l32ererer8.rer8.rer8.";
		String expect =  "l32erererr8.err8.e";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void test_F2() {
		String input  =  "a32a32a32a32c8&c16.a8a8a8a8a8";
		String expect =  "l32aaaacl8&c.aaaaa";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void test_con4() {
		/* &で接続されたノート */
		String input  =  "d4d4d4d4c2.&c8";
		String expect =  "ddddc2&c.";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test(timeout=TIMEOUT)
	public void test_con8() {
		/* &で接続されたノート */
		String input  =  "d8d8d8d8c2.&c8";
		String expect =  "l8ddddc&c2.";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test(timeout=TIMEOUT)
	public void test_con16() {
		/* &で接続されたノート */
		String input  =  "d16d16d16d16c2.&c8";
		String expect =  "l16ddddc2&c4.";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test(timeout=TIMEOUT)
	public void test_con2() {
		/* &で接続されたノート */
		String input  =  "d2d2d2d2c2.&c8";
		String expect =  "l2ddddc&c4.";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test(timeout=TIMEOUT)
	public void test_con2_8() {
		/* 最適化改善パターン: &で接続されたノートで順いれかえ */
		String input  =  "d2d2d2d2c2.&c8d8d8d8d8";
		String expect =  "l2ddddc.l8&cdddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test(timeout=TIMEOUT)
	public void test_F3() {
		String input  =  "a32a32a32a32r8r16.a8a8a8a8a8";
		String expect =  "l32aaaarl8r.aaaaa";
		checkMMLStringOptimize(input, expect);
	}

	@Test(timeout=TIMEOUT)
	public void test_bn2() {
		/* 最適化改善パターン */
		String input  =  "c1<a+b+a+>c1";
		String expect =  "c1n46cn46c1";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test(timeout=TIMEOUT)
	public void test_rl32() {
		/* 最適化改善パターン: 1つの休符を分割使用する */
		String input  =  "l32crrrcrrrcrrrcrrrb";
		String expect =  "l32crrrcrrrcrrrcrrrb";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}
}

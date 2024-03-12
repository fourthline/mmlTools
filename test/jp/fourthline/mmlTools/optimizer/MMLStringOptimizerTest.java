/*
 * Copyright (C) 2013-2024 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jp.fourthline.mmlTools.MMLBuilder;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLException;

/**
 * MML最適化のテスト.
 */
public class MMLStringOptimizerTest {

	private static final int TIMEOUT = 100;

	@Before
	public void setup() throws MMLException {
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
			MMLStringOptimizer optimizer = new MMLStringOptimizer(MMLBuilder.create(new MMLEventList(input)).toMMLString(true, true));
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
		} catch (MMLExceptionList e) {
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

	@Test
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

	@Test
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

	@Test
	public void test_gen2_n23() throws MMLException {
		/* Gen2チェックパターン1 */
		String input  = "l2f+.r4f+.f+4<f1b>b<f+4>c+4f+f1f+1f+1f+.r4f+.f+4f8>f+4f+<c-bff4r4c-1c+<f+c-1n49d+c-1n49f+";
		String expect = "l2f+.r4f+.f+4<f1b>b<f+4>c+4f+f1f+1f+1f+.r4f+.f+4f8>f+4f+<c-bff4r4c-1c+<f+c-1n49d+c-1n49f+";
		String expect2= "f+2.rf+2.f+<f1b2>b2n42c+l2f+f1f+1f+1f+.r4f+.f+4f8>f+4f+<c-bff4r4<b1n49f+c-1n49d+c-1n49f+";
		checkMMLStringOptimize(input, expect);
		checkMMLStringOptimize(input, expect2, t -> t.optimizeGen2());
	}

	@Test
	public void test_gen2_n24() throws MMLException {
		/* Gen2チェックパターン2 */
		String input  = "l64ffffffr1v12l4<<a+fg";
		String expect = "l64ffffffr1v12l4<<a+fg";
		checkMMLStringOptimize(input, expect);
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_gen2_ln25() throws MMLException {
		/* Gen2チェックパターン3 */
		String input =  "l2fffl8n22f&f2.n22f1d+1f1&f2.ff";
		String expect=  "l2fffl8n22f&f2.n22f1d+1f1&f2.ff";
		checkMMLStringOptimize(input, expect);
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test(timeout=TIMEOUT)
	public void test_F4() {
		String input  =  "l24cr12cr12cr12c";
		String expect =  "l24cr12cr12cr12c";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	public void test_F4_g3() {
		String input  =  "l24cr12cr12cr12c";
		String expect =  "l24crrcrrcrrc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	public void test_F5() {
		String input  =  "l64cr32cr32cr32c";
		String expect =  "l64cr32cr32cr32c";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	public void test_F5_g3() {
		String input  =  "l64cr32cr32cr32c";
		String expect =  "l64crrcrrcrrc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_F6() {
		String input  =  "l12ccccg4&g6";
		String expect =  "l12ccccg4&g6";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_F6_g3() {
		String input  =  "l12ccccg4&g6";
		String expect =  "l12ccccg&g3";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_F7() {
		String input  =  "l24cccr8rrccc";
		String expect =  "l24cccr8r12ccc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_F7_g3() {
		String input  =  "l24cccr8rrccc";
		String expect =  "l24cccrr6ccc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_1() {
		String input  = "l8ddddc4.&c7dddd";
		String expect = "l8ddddc4.&c7dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_altPattern_1r() {
		String input  = "l8ddddr4.r7dddd";
		String expect = "l8ddddr4.r7dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_altPattern_2() {
		String input  = "l7ddddc4.&c7dddd";
		String expect = "l7ddddc4.&cdddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_altPattern_2r() {
		String input  = "l7ddddr4.r7dddd";
		String expect = "l7ddddr4.rdddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_altPattern_3() {
		String input  = "l64ddddc4.&c7dddd";
		String expect = "l64ddddc&c2dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_3r() {
		String input  = "l64ddddr4.r7dddd";
		String expect = "l64ddddrr2dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_4() {
		String input  = "l27ddddc4&c18dddd";
		String expect = "l27ddddc.&c4dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_4r() {
		String input  = "l27ddddr4r18dddd";
		String expect = "l27ddddr.r4dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_4l() {
		String input  = "l27ddddc4&c18l4dddd";
		String expect = "l27ddddc.l4&cdddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_4lr() {
		String input  = "l27ddddr4r18l4dddd";
		String expect = "l27ddddr.l4rdddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_5() {
		String input  = "l18ddddc4&c18dddd";
		String expect = "l18ddddc4&cdddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_altPattern_5r() {
		String input  = "l18ddddr4r18dddd";
		String expect = "l18ddddr4rdddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_altPattern_6() {
		String input  = "l6ddddc4&c18dddd";
		String expect = "l6ddddc.&c18dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_6r() {
		String input  = "l6ddddr4r18dddd";
		String expect = "l6ddddr.r18dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_altPattern_7() {
		String input  = "l6.ddddc4&c18dddd";
		String expect = "ddddc&c18dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_altPattern_7r() {
		String input  = "l6.ddddr4r18dddd";
		String expect = "ddddrr18dddd";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_F8() {
		String input  = "l2.rg2gg8g4f+";
		String expect = "l2.rg2gg8g4f+";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_F8g3() {
		String input  = "l2.rg2gg8g4f+";
		String expect = "l2.rg2gg8g4f+";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_44d1() {
		String input  = "l64dddc2&c8";
		String expect = "l64dddc2&c8";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_44d2() {
		String input  = "l64dddl4.dddc2&c8";
		String expect = "l64dddl4.dddc&c4";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_44d3() {
		String input  = "l64dddl4dddc2&c8";
		String expect = "l64dddl4dddc.&c";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_44d4() {
		String input  = "l64dddc2&c8l4ffff";
		String expect = "l64dddl4c.&cffff";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_44d5() {
		String input  = "l64dddc2&c8l4.ffff";
		String expect = "l64dddl4.c4&cffff";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}

	@Test
	public void test_R1() {
		String input  = "c64c64c64r1.r2c1c1c1c1";
		String expect = "l64cccl1rrcccc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_R1d1() {
		String input  = "c64c64c64r1.r2c1.c1.c1.c1.";
		String expect = "l64cccl1rrl1.cccc";    // "l64cccl1.rr2cccc"
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_R1d2() {
		String input  = "c64c64c64r1.r2c2c2c2c2";
		String expect = "l64cccl1rrl2cccc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_R2() {
		String input  = "c64c64c64d1.&d2c1c1c1c1";
		String expect = "l64cccl1d&dcccc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen2());
	}

	@Test
	public void test_R3() {
		String input  = "l24cccr8r12ccc";
		String expect = "l24cccrr6ccc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}
	@Test
	public void test_R4() {
		String input  = "r3r9r16ccc";
		String expect = "rr5r17ccc";
		checkMMLStringOptimize(input, expect, t -> t.optimizeGen3());
	}
}

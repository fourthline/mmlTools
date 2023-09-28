/*
 * Copyright (C) 2015-2020 たんらる
 */

package jp.fourthline.mmlTools.core;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * 
 */
public final class MMLTicksTest {

	@Test
	public void test_getTick4() throws MMLException {
		int tick = MMLTicks.getTick("4");
		assertEquals(96, tick);
	}

	@Test
	public void test_getTick16() throws MMLException {
		int tick = MMLTicks.getTick("16");
		assertEquals(24, tick);
	}

	@Test
	public void test_getTick16dot() throws MMLException {
		int tick1 = MMLTicks.getTick("16...");
		int tick2 = MMLTicks.getTick("16.");
		assertEquals(tick1, tick2);
	}

	/**
	 * 不要文字の無視.
	 */
	@Test
	public void test_getTick9dotAN() throws MMLException {
		int tick1 = MMLTicks.getTick("9.@@@@");
		int tick2 = MMLTicks.getTick("9.");
		assertEquals(tick1, tick2);
	}

	/**
	 * 変換できない.
	 */
	@Test(expected = MMLException.class)
	public void test_getTick65() throws MMLException {
		MMLTicks.getTick("65");
	}

	/**
	 * 調律符生成.
	 */
	@Test
	public void test_toMMLTextByBase() throws MMLException {
		MMLTicks note = new MMLTicks("c", 96+6);
		String expect64 = "c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64";
		String expect32 = "c32&c32&c32&c32&c32&c32&c32&c32&c64";
		String expect16 = "c16&c16&c16&c16&c64";
		String expect48 = "c48&c48&c48&c48&c48&c48&c48&c48&c48&c48&c48&c48&c64";
		String expect24 = "c24&c24&c24&c24&c24&c24&c64";
		String expect12 = "c12&c12&c12&c64";
		assertEquals(expect64, note.toMMLTextByBase(TuningBase.L64));
		assertEquals(expect32, note.toMMLTextByBase(TuningBase.L32));
		assertEquals(expect16, note.toMMLTextByBase(TuningBase.L16));
		assertEquals(expect48, note.toMMLTextByBase(TuningBase.L48));
		assertEquals(expect24, note.toMMLTextByBase(TuningBase.L24));
		assertEquals(expect12, note.toMMLTextByBase(TuningBase.L12));
	}

	/**
	 * &の連結なし調律.
	 */
	@Test
	public void test_toMMLTextByBaseR() throws MMLException {
		MMLTicks note = new MMLTicks("r", 96, false);
		String expect16 = "r16r16r16r16";
		assertEquals(expect16, note.toMMLTextByBase(TuningBase.L16));
	}

	/**
	 * 数Tickオーバーの調律符1
	 */
	@Test
	public void test_toMMLTextByBaseE1() throws MMLException {
		MMLTicks note = new MMLTicks("r", 96+5, false);
		String expect16 = "r16r16r16r13";
		assertEquals(expect16, note.toMMLTextByBase(TuningBase.L16));
	}

	/**
	 * 数Tickオーバーの調律符2
	 */
	@Test
	public void test_toMMLTextByBaseE2() throws MMLException {
		MMLTicks note = new MMLTicks("r", 48+1, false);
		String expect16 = "r32r32r32r29";
		assertEquals(expect16, note.toMMLTextByBase(TuningBase.L32));
	}

	@Test
	public void test_minimumTick() {
		assertEquals(6, MMLTicks.minimumTick());
	}
}

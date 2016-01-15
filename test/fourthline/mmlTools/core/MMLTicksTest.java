/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mmlTools.core;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */
public final class MMLTicksTest {

	@Test
	public void test_getTick4() throws UndefinedTickException {
		int tick = MMLTicks.getTick("4");
		assertEquals(96, tick);
	}

	@Test
	public void test_getTick16() throws UndefinedTickException {
		int tick = MMLTicks.getTick("16");
		assertEquals(24, tick);
	}

	@Test
	public void test_getTick16dot() throws UndefinedTickException {
		int tick1 = MMLTicks.getTick("16...");
		int tick2 = MMLTicks.getTick("16.");
		assertEquals(tick1, tick2);
	}

	/**
	 * 不要文字の無視.
	 */
	@Test
	public void test_getTick9dotAN() throws UndefinedTickException {
		int tick1 = MMLTicks.getTick("9.@@@@");
		int tick2 = MMLTicks.getTick("9.");
		assertEquals(tick1, tick2);
	}

	/**
	 * 変換できない.
	 */
	@Test(expected = UndefinedTickException.class)
	public void test_getTick65() throws UndefinedTickException {
		MMLTicks.getTick("65");
	}

	/**
	 * 調律符生成.
	 */
	@Test
	public void test_toMMLTextByBase() throws UndefinedTickException {
		MMLTicks note = new MMLTicks("c", 96+6);
		String expect64 = "c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64&c64";
		String expect32 = "c32&c32&c32&c32&c32&c32&c32&c32&c64";
		String expect16 = "c16&c16&c16&c16&c64";
		assertEquals(expect64, note.toMMLTextByBase(TuningBase.L64));
		assertEquals(expect32, note.toMMLTextByBase(TuningBase.L32));
		assertEquals(expect16, note.toMMLTextByBase(TuningBase.L16));
	}

	/**
	 * &の連結なし調律.
	 */
	@Test
	public void test_toMMLTextByBaseR() throws UndefinedTickException {
		MMLTicks note = new MMLTicks("r", 96, false);
		String expect16 = "r16r16r16r16";
		assertEquals(expect16, note.toMMLTextByBase(TuningBase.L16));
	}

	/**
	 * 指定調律長で分割後, 生成できなければNG.
	 */
	@Test(expected = UndefinedTickException.class)
	public void test_toMMLTextByBaseE() throws UndefinedTickException {
		MMLTicks note = new MMLTicks("r", 96+5, false);
		note.toMMLTextByBase(TuningBase.L16);
	}

	@Test
	public void test_minimumTick() {
		assertEquals(6, MMLTicks.minimumTick());
	}
}

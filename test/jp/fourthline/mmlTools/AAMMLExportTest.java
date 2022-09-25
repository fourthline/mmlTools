/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import org.junit.Test;

public class AAMMLExportTest {
	@Test
	public void test() {
		String mml[] = { "c4&c16d16e16" };
		assertEquals("V64c&c16l16de", AAMMLExport.toAAText(mml));
	}

	@Test
	public void test1() {
		String mml[] = { "c1&c4&c16c1c1c1c1" };
		assertEquals("V64l1c&c4&c16cccc", AAMMLExport.toAAText(mml));
	}

	@Test
	public void test4() {
		String mml[] = { "c1&c4&c16c4c4c4c4" };
		assertEquals("V64c1&c&c16cccc", AAMMLExport.toAAText(mml));
	}

	@Test
	public void test16() {
		String mml[] = { "c1&c4&c16c16c16c16c16" };
		assertEquals("V64c1&c&c16l16cccc", AAMMLExport.toAAText(mml));
	}

	@Test
	public void testVol1() {
		// Vの引き継ぎテスト
		String mml[] = { "v4c1", "c1" };
		assertEquals("V32l1c,V64c", AAMMLExport.toAAText(mml));
	}

	@Test
	public void testMML01() {
		String mml[] = { "o1d16d16d16c4&c16.", "c16c16" };
		assertEquals("o2V64l16dddc4&c.,V64cc", AAMMLExport.toAAText(mml));
	}

	@Test
	public void testMML02() {
		String mml[] = { "o1c4&c16.", "c16c16" };
		assertEquals("o2V64c&c16.l16,V64cc", AAMMLExport.toAAText(mml));
	}
}

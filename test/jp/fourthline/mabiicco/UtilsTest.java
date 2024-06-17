/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco;

import static org.junit.Assert.*;

import org.junit.Test;

public final class UtilsTest {
	@Test
	public final void testCompressString() {
		String str = "test\ntest";
		String compressed = Utils.compress(str);
		String act = new String(Utils.decompress(compressed));
		assertEquals("eJwrSS0u4SoBEgARrQOL", compressed);
		assertEquals(str, act);
	}

	@Test
	public final void testCompressByteArray() {
		byte[] data = { 0x00, 0x00, 0x00, 0x01, (byte)0x80, 0x00, 0x7f, 0x20 };
		String compressed = Utils.compress(data);
		byte[] act = Utils.decompress(compressed);
		assertEquals("eJxjYGBgbGCoVwAAAysBIQ==", compressed);
		assertArrayEquals(data, act);
	}
}

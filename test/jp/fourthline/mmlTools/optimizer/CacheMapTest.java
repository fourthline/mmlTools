/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import static org.junit.Assert.*;

import org.junit.Test;

public class CacheMapTest {
	@Test
	public final void test() {
		var map = new CacheMap<Integer, String>(256);
		for (int i = 0; i < 1000; i++) {
			map.put(i, "");
		}
		assertEquals(256, map.size());
	}
}

/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import static org.junit.Assert.*;

import org.junit.Test;

public final class OxLxFixedOptimizerTest {

	@Test
	public void test() {
		String mml = "c64c64c64c64c64d42";
		assertEquals("l64cccccd42", new MMLStringOptimizer(mml).optimizeGen2());
		assertEquals("l64cccccd.", new MMLStringOptimizer(mml).optimizeGen3());
	}

}

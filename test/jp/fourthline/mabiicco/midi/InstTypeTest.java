/*
 * Copyright (C) 2015 たんらる
 */

package jp.fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */
public final class InstTypeTest {

	private void checktInstType(InstType type, boolean allowTranspose, boolean[] expectPart, int[] expectVelocity) {
		assertEquals(allowTranspose, type.allowTranspose());
		assertEquals(expectPart.length, type.getEnablePart().length);
		for (int i = 0; i < expectPart.length; i++) {
			assertEquals(expectPart[i], type.getEnablePart()[i]);
		}
		for (int i = -1; i <= 16; i++) {
			assertEquals(expectVelocity[i+1], type.convertVelocityMML2Midi(i));
		}
	}

	@Test
	public void test() {
		boolean[] nonePart = new boolean[] { false, false, false, false };
		boolean[] threePart = new boolean[] { true,  true,  true,  false };
		boolean[] onePart = new boolean[] { true,  false, false, false };
		boolean[] songPart = new boolean[] { false, false, false, true };

		int[] noneVelocity = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int[] normalVelocity = new int[] { 0, 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96, 104, 112, 120, 120 };
		int[] drumVelocity = new int[] { 0, 0, 11, 22, 33, 44, 55, 66, 77, 88, 99, 110, 121, 121, 121, 121, 121, 121 };

		checktInstType(InstType.NONE,   true,  nonePart,  noneVelocity);
		checktInstType(InstType.NORMAL, true,  threePart, normalVelocity);
		checktInstType(InstType.DRUMS,  false, onePart,   drumVelocity);
		checktInstType(InstType.KPUR,   true,  onePart,   drumVelocity);
		checktInstType(InstType.VOICE,  true,  songPart,  normalVelocity);
		checktInstType(InstType.CHORUS, true,  songPart,  normalVelocity);
	}
}

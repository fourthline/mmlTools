/*
 * Copyright (C) 2015-2023 たんらる
 */

package jp.fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;


public final class InstTypeTest extends UseLoadingDLS {

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
		checktInstType(InstType.PERCUSSION,  false, onePart,   drumVelocity);
		checktInstType(InstType.KPUR,   true,  onePart,   drumVelocity);
		checktInstType(InstType.VOICE,  true,  songPart,  normalVelocity);
		checktInstType(InstType.CHORUS, true,  songPart,  normalVelocity);
		checktInstType(InstType.DRUMS, false,  threePart,  normalVelocity);
	}

	@Test
	public void testStrInst() {
		assertEquals(InstType.PERCUSSION, InstType.getInstType("P"));
		assertEquals(InstType.DRUMS, InstType.getInstType("D"));
		assertEquals(InstType.NORMAL, InstType.getInstType("N"));
		assertEquals(InstType.NONE, InstType.getInstType("0"));
		assertEquals(InstType.VOICE, InstType.getInstType("V"));
		assertEquals(InstType.CHORUS, InstType.getInstType("C"));
		assertEquals(InstType.KPUR, InstType.getInstType("K"));
	}

	@Test(expected =  AssertionError.class)
	public void testStrInstEx() {
		assertEquals(InstType.NONE, InstType.getInstType("A"));
	}

	@Test
	public void testInstType() {
		assertEquals(InstType.DRUMS, MabiDLS.getInstance().getInstByProgram(27).getType());
		assertEquals(InstType.NORMAL, MabiDLS.getInstance().getInstByProgram(2).getType());
		assertEquals(InstType.VOICE, MabiDLS.getInstance().getInstByProgram(120).getType());
		assertEquals(InstType.PERCUSSION, MabiDLS.getInstance().getInstByProgram(66).getType());
		assertEquals(InstType.CHORUS, MabiDLS.getInstance().getInstByProgram(110).getType());
		assertEquals(InstType.KPUR, MabiDLS.getInstance().getInstByProgram(77).getType());
	}
}

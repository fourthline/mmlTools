/*
 * Copyright (C) 2015-2023 たんらる
 */

package jp.fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;


public final class InstTypeTest extends UseLoadingDLS {

	private void checktInstType(InstType type, boolean allowTranspose, boolean allowTempoChord, boolean[] expectPart, int[] expectVelocity) {
		assertEquals(allowTranspose, type.allowTranspose());
		assertEquals(allowTempoChord, type.allowTempoChordPart());
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

		checktInstType(InstType.NONE,   false, false, nonePart,  noneVelocity);
		checktInstType(InstType.NORMAL, true, true, threePart, normalVelocity);
		checktInstType(InstType.PERCUSSION,  false, false, onePart,   drumVelocity);
		checktInstType(InstType.KPUR,   true, false, onePart,   drumVelocity);
		checktInstType(InstType.VOICE,  true, false, songPart,  normalVelocity);
		checktInstType(InstType.CHORUS, true, false, songPart,  normalVelocity);
		checktInstType(InstType.DRUMS, false, true, threePart,  normalVelocity);
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
		var dls = MabiDLS.getInstance();
		assertEquals(InstType.DRUMS, dls.getInstByProgram(27).getType());
		assertEquals(InstType.NORMAL, dls.getInstByProgram(2).getType());
		assertEquals(InstType.VOICE, dls.getInstByProgram(120).getType());
		assertEquals(InstType.PERCUSSION, dls.getInstByProgram(66).getType());
		assertEquals(InstType.CHORUS, dls.getInstByProgram(110).getType());
		assertEquals(InstType.KPUR, dls.getInstByProgram(77).getType());
	}

	@Test
	public void testInstAll() {
		var dls = MabiDLS.getInstance();
		var mainList = dls.getAvailableInstByInstType(InstType.MAIN_INST_LIST);
		var subList = dls.getAvailableInstByInstType(InstType.SUB_INST_LIST);
		var allList = dls.getAllInst();
		assertEquals(allList.size(), mainList.length + subList.length);
		List.of(mainList).forEach(t -> assertTrue(allList.contains(t)));
		List.of(subList).forEach(t -> assertTrue(allList.contains(t)));
	}
}

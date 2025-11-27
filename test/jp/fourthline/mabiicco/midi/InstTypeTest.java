/*
 * Copyright (C) 2015-2025 たんらる
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
		checktInstType(InstType.PERCUSSION_MOBILE, false, false, onePart, normalVelocity);
	}

	@Test
	public void testStrInst() {
		assertEquals(InstType.PERCUSSION, InstType.getInstType("P"));
		assertEquals(InstType.PERCUSSION_MOBILE, InstType.getInstType("M"));
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

	@Test
	public void testAPI1() {
		var type = InstType.NORMAL;
		var option = new InstClass.Options(new boolean[] {false, true, false, true, false});

		// ノート変換
		assertEquals(0, type.convertNoteMML2Midi(-12, option));
		assertEquals(12, type.convertNoteMML2Midi(0, option));

		// ノートインデックス
		assertEquals(0, type.convertNote2ValidIndex(-12));
		assertEquals(12, type.convertNote2ValidIndex(0));

		// ノートValid
		assertEquals(false, type.isValidNote(-12, option));
		assertEquals(true,  type.isValidNote(-11, option));
		assertEquals(false, type.isValidNote(-10, option));
		assertEquals(true,  type.isValidNote(-9, option));
		assertEquals(false, type.isValidNote(-8, option));
		assertEquals(false, type.isValidNote(-7, option));
		assertEquals(false, type.isValidNote(0, option));
	}

	@Test
	public void testAPI2() {
		var type = InstType.PERCUSSION_MOBILE;
		var option = new InstClass.Options(new boolean[] {false, true, false, true, false, true, true, true, true, true});

		// ノート変換
		var note1 = type.convertNoteMML2Midi(0, option);
		var note2 = type.convertNoteMML2Midi(0, option);
		var note3 = type.convertNoteMML2Midi(0, option);
		assertFalse( (note1 == note2) && (note2 == note3) );  // ランダム値なので全部一致はありえない

		// ノートインデックス
		assertEquals(0, type.convertNote2ValidIndex(-12));
		assertEquals(12, type.convertNote2ValidIndex(0));

		// ノートValid
		assertEquals(false, type.isValidNote(-1, option));
		assertEquals(true,  type.isValidNote(0, option));
		assertEquals(true,  type.isValidNote(1, option));
	}

}

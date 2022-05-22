/*
 * Copyright (C) 2013 たんらる
 */

package jp.fourthline.mmlTools;


import static org.junit.Assert.*;

import org.junit.*;

import jp.fourthline.mmlTools.core.MelodyParser;
import jp.fourthline.mmlTools.core.UndefinedTickException;


public class MMLOptToolsTest {
	private final String[] orig_tick = {
			"32",
			"32.",
			"16",
			"16.",
			"8",
			"8.",
			"4",
			"4.",
			"2",
			"2.",
			"1",
			"1.",

			"6",
			"12",
			"24"
	};

	public int[] mmlTimeTest(String mml1, String mml2) {
		try {
			MelodyParser parser1 = new MelodyParser(mml1);
			MelodyParser parser2 = new MelodyParser(mml2);
			int result1 = parser1.getLength();
			int result2 = parser2.getLength();
			System.out.printf("*** %d, %d\n", result1, result2);
			int[] result = { result1, result2 };
			return result;
		} catch (UndefinedTickException e) {}

		return null;
	}


	@Test
	public void basic_test01() {
		MMLOptTools tools = new MMLOptTools();

		for (int i = 0; i < orig_tick.length; i++) {
			try {
				String orig = "c";
				String s = tools.replaceTail64(orig, orig_tick[i]);

				int[] result = mmlTimeTest(orig+orig_tick[i], s);
				assertEquals(result[0], result[1]);
			} catch (UndefinedTickException e) {
				e.printStackTrace();
			}
		}
	}


	public String mml_conv(String input) {
		MMLOptTools tools = new MMLOptTools();

		try {
			String s = tools.replaceNoise(input);
			System.out.println("----"+s);
			return s;
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}

		return null;
	}


	@Test
	public void mml_test01() {
		String input  = "c4<b+4>c4c4c4d4d-dd";
		String expect = "c4<b+4>c8&c9r64c8&c9r64c4d4d-d8&d9r64d";

		String s = mml_conv(input);
		int[] result = mmlTimeTest(input, s);
		assertEquals(result[0], result[1]);
		assertEquals(expect, s);
	}

}

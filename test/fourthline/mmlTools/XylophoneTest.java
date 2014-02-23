/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fourthline.mmlTools.core.MMLTools;

public class XylophoneTest {

	@Test
	public void mml_test01() throws UndefinedTickException {
		XylophoneMML tool = new XylophoneMML();
		String input  = "o3cc+dd+eff+gg+aa+bn57";
		String expect = "o3cc+dd+eff+gg+>aa+bn69";

		String s = tool.conv(input);

		assertEquals(expect, s);
	}

	@Test
	public void mml_test_back_01() throws UndefinedTickException {
		XylophoneMML tool = new XylophoneMML();
		String input = "o3cc+dd+eff+gg+>aa+bn69";
		String expect  = "o3cc+dd+eff+gg+aa+bn57";

		String s = tool.conv(input, true);

		assertEquals(expect, s);
	}

	@Test
	public void mml_test02() throws UndefinedTickException {
		XylophoneMML tool = new XylophoneMML();
		String input  = "eg+n73b.";
		String expect = "eg+n73>b.";

		String s = tool.conv(input);

		assertEquals(expect, s);
	}

	@Test
	public void mml_test_checkpitch() throws UndefinedTickException {
		String input  = "MML@v15l8o2eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+";
		boolean expect = true;

		MMLTools tool = new MMLTools(input);

		boolean result = tool.checkPitch(40-12, 92-12);

		assertEquals(expect, result);
	}
}

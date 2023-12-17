/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.fourthline.mmlTools.core.MMLText;

public final class MMLXImportPanelTest {
	@Test
	public final void test() {
		var panel = new MMLXImportPanel(null, "track", new MMLManagerStub());
		var textList = panel.getTextList();
		textList.set(0, new MMLText().setMMLText("MML@abc,cde,,c1;"));
		textList.set(9, new MMLText().setMMLText("MML@c2c2,r1,c1,b2;"));
		var r = panel.textJoin();
		assertEquals("MML@abcc2c2,cder1,c1,c1b2;", r.getMML());
	}
}

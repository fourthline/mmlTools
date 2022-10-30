/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.UndefinedTickException;



public enum NoteAlign {
	N1("1"),
	N2("2"),
	N4("4"),
	N8("8"),
	N16("16"),
	N32("32"),
	N64("64"),
	N12("12"),
	N24("24"),
	N48("48"),
	N2D("2."),
	N4D("4."),
	N8D("8."),
	N16D("16."),
	N32D("32."),
	N64D("64.");

	public static final NoteAlign DEFAULT_ALIGN = N4;
	private final String viewText;
	private int alignTick;
	private NoteAlign(String s) {
		viewText = AppResource.appText("editor.note_" + s);
		try {
			alignTick = MMLTicks.getTick(s);
		} catch (UndefinedTickException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public String toString() {
		return viewText;
	}

	public int getAlign() {
		return alignTick;
	}
}

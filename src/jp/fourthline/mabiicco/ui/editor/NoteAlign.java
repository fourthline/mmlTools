/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.event.KeyEvent;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.UndefinedTickException;



public enum NoteAlign {
	N1("1", KeyEvent.VK_1),
	N2("2", KeyEvent.VK_2),
	N4("4", KeyEvent.VK_3),
	N8("8", KeyEvent.VK_4),
	N16("16", KeyEvent.VK_5),
	N32("32", KeyEvent.VK_6),
	N64("64", KeyEvent.VK_7),
	N12("12", KeyEvent.VK_8),
	N24("24", KeyEvent.VK_9),
	N48("48", KeyEvent.VK_0),
	N2D("2."),
	N4D("4."),
	N8D("8."),
	N16D("16."),
	N32D("32."),
	N64D("64.");

	public static final NoteAlign DEFAULT_ALIGN = N4;
	private final String viewText;
	private int alignTick;
	private int keyCode;
	private NoteAlign(String s) {
		this(s, 0);
	}
	private NoteAlign(String s, int keyCode) {
		viewText = AppResource.appText("editor.note_" + s);
		try {
			alignTick = MMLTicks.getTick(s);
		} catch (UndefinedTickException e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.keyCode = keyCode;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public String toString() {
		return viewText;
	}

	public int getAlign() {
		return alignTick;
	}
}

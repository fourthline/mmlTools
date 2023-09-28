/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.event.KeyEvent;
import java.util.List;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLException;



public enum NoteAlign {
	N1("1", List.of(KeyEvent.VK_1, KeyEvent.VK_NUMPAD1)),
	N2("2", List.of(KeyEvent.VK_2, KeyEvent.VK_NUMPAD2)),
	N4("4", List.of(KeyEvent.VK_3, KeyEvent.VK_NUMPAD3)),
	N8("8", List.of(KeyEvent.VK_4, KeyEvent.VK_NUMPAD4)),
	N16("16", List.of(KeyEvent.VK_5, KeyEvent.VK_NUMPAD5)),
	N32("32", List.of(KeyEvent.VK_6, KeyEvent.VK_NUMPAD6)),
	N64("64", List.of(KeyEvent.VK_7, KeyEvent.VK_NUMPAD7)),
	N12("12", List.of(KeyEvent.VK_8, KeyEvent.VK_NUMPAD8)),
	N24("24", List.of(KeyEvent.VK_9, KeyEvent.VK_NUMPAD9)),
	N48("48", List.of(KeyEvent.VK_0, KeyEvent.VK_NUMPAD0)),
	N2D("2."),
	N4D("4."),
	N8D("8."),
	N16D("16."),
	N32D("32."),
	N64D("64.");

	public static final NoteAlign DEFAULT_ALIGN = N4;
	private final String viewText;
	private int alignTick;
	private List<Integer> keyCodeList;
	private NoteAlign(String s) {
		this(s, null);
	}
	private NoteAlign(String s, List<Integer> keyCodeList) {
		viewText = AppResource.appText("editor.note_" + s);
		try {
			alignTick = MMLTicks.getTick(s);
		} catch (MMLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.keyCodeList = keyCodeList;
	}

	public List<Integer> getKeyCodeList() {
		return keyCodeList;
	}

	public String toString() {
		return viewText;
	}

	public int getAlign() {
		return alignTick;
	}
}

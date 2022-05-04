/*
 * Copyright (C) 2013-2014 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.util.ArrayList;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.UndefinedTickException;



public final class NoteAlign {
	public static int DEFAULT_ALIGN_INDEX = 2;
	public static NoteAlign[] createAlignList() {
		final String keyList[] = {
				"editor.note_1",
				"editor.note_2",
				"editor.note_4",
				"editor.note_8",
				"editor.note_16",
				"editor.note_32",
				"editor.note_64",
				"editor.note_6",
				"editor.note_12",
				"editor.note_24",
				"editor.note_48",
				"editor.note_2.",
				"editor.note_4.",
				"editor.note_8.",
				"editor.note_16.",
				"editor.note_32.",
				"editor.note_64."
		};
		ArrayList<NoteAlign> list = new ArrayList<>();
		for (String key : keyList) {
			try {
				String tickText = key.substring("editor.note_".length());
				NoteAlign noteAlign = new NoteAlign(AppResource.appText(key), tickText);
				list.add(noteAlign);
			} catch (UndefinedTickException e) {
				e.printStackTrace();
			}
		}

		return list.toArray(new NoteAlign[list.size()]);
	}

	private final String viewText;
	private final int alignTick;

	public NoteAlign(String viewText, String tickName) throws UndefinedTickException {
		this.viewText = viewText;
		alignTick = MMLTicks.getTick(tickName);
	}

	public String toString() {
		return viewText;
	}

	public int getAlign() {
		return alignTick;
	}
}

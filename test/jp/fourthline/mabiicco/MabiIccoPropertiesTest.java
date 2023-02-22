/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.mabiicco.ui.PianoRollView;

public class MabiIccoPropertiesTest {

	private MabiIccoProperties obj;
	private Properties properties;

	@Before
	public void setup() {
		obj = MabiIccoProperties.getInstance();
		try {
			properties = (Properties) (getField("properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void cleanup() {
		properties.clear();
	}

	private Object getField(String fieldName) throws Exception {
		Field f = MabiIccoProperties.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(obj);
	}

	@Test
	public void test() {
		String noteHeight = "view.pianoRoll.heightScale";
		String timeBox = "view.timeBox";
		String soundEnv = "function.sound_env";

		// 初期値
		assertEquals("H8", obj.pianoRollNoteHeight.get().name());
		assertEquals("MEASURE", obj.timebox.get().name());
		assertEquals("MABINOGI", obj.soundEnv.get().name());

		// Zero
		properties.put(noteHeight, "0");
		properties.put(timeBox, "0");
		properties.put(soundEnv, "0");
		assertEquals("H6", obj.pianoRollNoteHeight.get().name());
		assertEquals("MEASURE", obj.timebox.get().name());
		assertEquals("MABINOGI", obj.soundEnv.get().name());

		// 存在しない値
		properties.put(noteHeight, "H0");
		properties.put(timeBox, "T0");
		properties.put(soundEnv, "A0");
		assertEquals("H8", obj.pianoRollNoteHeight.get().name());
		assertEquals("MEASURE", obj.timebox.get().name());
		assertEquals("MABINOGI", obj.soundEnv.get().name());

		// Index値による指定
		properties.put(noteHeight, "4");
		properties.put(timeBox, "1");
		properties.put(soundEnv, "1");
		assertEquals("H14", obj.pianoRollNoteHeight.get().name());
		assertEquals("TIME", obj.timebox.get().name());
		assertEquals("ARCHEAGE", obj.soundEnv.get().name());

		// Index値による指定 (範囲外)
		properties.put(noteHeight, "5");
		properties.put(timeBox, "-1");
		properties.put(soundEnv, "10000");
		assertEquals("H8", obj.pianoRollNoteHeight.get().name());
		assertEquals("MEASURE", obj.timebox.get().name());
		assertEquals("MABINOGI", obj.soundEnv.get().name());

		// set
		obj.pianoRollNoteHeight.set(PianoRollView.NoteHeight.H14);
		assertEquals("H14", obj.pianoRollNoteHeight.get().name());

		// setIndex
		obj.pianoRollNoteHeight.setIndex(-1);
		assertEquals("H8", obj.pianoRollNoteHeight.get().name());
		obj.pianoRollNoteHeight.setIndex(0);
		assertEquals("H6", obj.pianoRollNoteHeight.get().name());
		obj.pianoRollNoteHeight.setIndex(4);
		assertEquals("H14", obj.pianoRollNoteHeight.get().name());
		obj.pianoRollNoteHeight.setIndex(5);
		assertEquals("H8", obj.pianoRollNoteHeight.get().name());
	}
}

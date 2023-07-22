/*
 * Copyright (C) 2015-2023 たんらる
 */

package jp.fourthline.mabiicco;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.midi.SoundEnv;
import jp.fourthline.mabiicco.ui.MainFrame;
import jp.fourthline.mabiicco.ui.PianoRollView;
import jp.fourthline.mabiicco.ui.color.ScaleColor;

public final class ActionDispatcherTest extends UseLoadingDLS {

	private final MabiIccoProperties properties = MabiIccoProperties.getInstance();
	private ActionDispatcher obj;
	private PianoRollView.NoteHeight noteHeight;
	private ScaleColor scaleColor;
	private SoundEnv soundEnv;
	private boolean useDefaultSound;

	@Before
	public void initializeObj() {
		obj = ActionDispatcher.getInstance();
		obj.setTestMode(true);
		noteHeight = properties.pianoRollNoteHeight.get();
		scaleColor = properties.scaleColor.get();
		soundEnv = properties.soundEnv.get();
		useDefaultSound = properties.useDefaultSoundBank.get();
		properties.laf.set(Laf.LIGHT);
	}

	@After
	public void cleanup() {
		properties.pianoRollNoteHeight.set(noteHeight);
		properties.scaleColor.set(scaleColor);
		properties.soundEnv.set(soundEnv);
		properties.useDefaultSoundBank.set(useDefaultSound);
		properties.laf.set(Laf.LIGHT);
	}

	private Object getField(String fieldName) throws Exception {
		Field f = ActionDispatcher.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(obj);
	}

	@Test
	public void test_initialize() throws Exception {
		obj.initialize();
		HashMap<?, ?> actionMap = (HashMap<?, ?>) getField("actionMap");
		Set<?> keySet = actionMap.keySet();
		System.out.println("keySet size: " + keySet.size());
		Field[] fields = ActionDispatcher.class.getDeclaredFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(ActionDispatcher.Action.class)) {
				String key = f.get(obj).toString();
				assertTrue(key, keySet.contains(key));
				System.out.println(key + " -> " + actionMap.get(key).toString());
			}
		}
	}

	@Test
	public void test_changeAction() {
		obj.setMainFrame(new MainFrame(obj, obj)).initialize();

		// ノート高さ表示変更
		assertEquals(PianoRollView.NoteHeight.H8, properties.pianoRollNoteHeight.get());
		Supplier<PianoRollView.NoteHeight> s1 = () -> PianoRollView.NoteHeight.H12;
		obj.actionPerformed(new ActionEvent(s1, ActionEvent.ACTION_PERFORMED, ActionDispatcher.CHANGE_ACTION));
		assertEquals(PianoRollView.NoteHeight.H12, properties.pianoRollNoteHeight.get());

		// 音階表示の変更
		assertEquals(ScaleColor.C_MAJOR, properties.scaleColor.get());
		Supplier<ScaleColor> s2 = () -> ScaleColor.Eb_MAJOR;
		obj.actionPerformed(new ActionEvent(s2, ActionEvent.ACTION_PERFORMED, ActionDispatcher.CHANGE_ACTION));
		assertEquals(ScaleColor.Eb_MAJOR, properties.scaleColor.get());

		// 音源環境の変更
		assertEquals(SoundEnv.MABINOGI, properties.soundEnv.get());
		Supplier<SoundEnv> s3 = () -> SoundEnv.ARCHEAGE;
		obj.actionPerformed(new ActionEvent(s3, ActionEvent.ACTION_PERFORMED, ActionDispatcher.CHANGE_ACTION));
		assertEquals(SoundEnv.ARCHEAGE, properties.soundEnv.get());
	}
}

/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComponent;

import org.junit.Test;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;

public final class MMLNotePropertyPanelTest {

	private void assertNotePropertyEnabled(MMLNotePropertyPanel panel, String name, boolean b) {
		try {
			Field field = panel.getClass().getDeclaredField(name);
			field.setAccessible(true);
			boolean actual = ((JComponent)field.get(panel)).isEnabled();
			assertEquals(b, actual);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail(e.getMessage());
		}
	}

	private void setNotePropertySelected(MMLNotePropertyPanel panel, String name, boolean b) {
		try {
			Field field = panel.getClass().getDeclaredField(name);
			field.setAccessible(true);
			((AbstractButton)field.get(panel)).setSelected(b);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException  e) {
			fail(e.getMessage());
		}
	}

	private void assertCheckBoxStatus(String mml,
			boolean velocityCheckBox,            // 音量コマンドチェックBox
			boolean velocityValueField,          // 音量入力欄
			boolean incDecrVelocityEditOption,   // 増減入力チェックBox
			boolean tuningNoteCheckBox,          // 調律チェックBox
			boolean tuningBaseList               // 調律設定
			) {
		MMLEventList eventList = new MMLEventList(mml);
		List<MMLNoteEvent> noteList = eventList.getMMLNoteEventList();
		MMLNoteEvent noteEvent[] = new MMLNoteEvent[] { noteList.get(1), noteList.get(2) };
		MMLNotePropertyPanel panel = new MMLNotePropertyPanel(noteEvent, eventList);

		assertNotePropertyEnabled(panel, "velocityCheckBox", velocityCheckBox);
		assertNotePropertyEnabled(panel, "velocityValueField", velocityValueField);
		assertNotePropertyEnabled(panel, "incDecrVelocityEditOption", incDecrVelocityEditOption);
		assertNotePropertyEnabled(panel, "tuningNoteCheckBox", tuningNoteCheckBox);
		assertNotePropertyEnabled(panel, "tuningBaseList", tuningBaseList);
	}

	@Test
	public void test() {
		assertCheckBoxStatus(
				"abc",
				true,    // 音量コマンドチェックBox
				false,   // 音量入力欄
				false,   // 増減入力チェックBox
				true,    // 調律チェックBox
				false);  // 調律設定
		assertCheckBoxStatus(
				"av5bc",
				true,    // 音量コマンドチェックBox
				true,    // 音量入力欄
				false,   // 増減入力チェックBox
				true,    // 調律チェックBox
				false);  // 調律設定
		assertCheckBoxStatus(
				"al64b&b&b&b&b&b&b&bc",
				true,    // 音量コマンドチェックBox
				false,   // 音量入力欄
				false,   // 増減入力チェックBox
				false,   // 調律チェックBox
				false);  // 調律設定
		assertCheckBoxStatus(
				"al64b&b&b&b&b&b&b&bc&c&c&c&c&c&c&c",
				true,    // 音量コマンドチェックBox
				false,   // 音量入力欄
				false,   // 増減入力チェックBox
				true,    // 調律チェックBox
				true);   // 調律設定
		assertCheckBoxStatus(
				"al64b&b&b&b&b&b&b&bl32c&c&c&c&c&c&c&c",
				true,    // 音量コマンドチェックBox
				false,   // 音量入力欄
				false,   // 増減入力チェックBox
				false,   // 調律チェックBox
				false);  // 調律設定
	}

	@Test
	public void test_editVelocityOptionView() {
		MMLEventList eventList = new MMLEventList("abc");
		List<MMLNoteEvent> noteList = eventList.getMMLNoteEventList();
		MMLNoteEvent noteEvent[] = new MMLNoteEvent[] { noteList.get(1), noteList.get(2) };
		MMLNotePropertyPanel panel = new MMLNotePropertyPanel(noteEvent, eventList);

		// 音量コマンドが選択されていない状態.
		assertNotePropertyEnabled(panel, "velocityCheckBox", true);
		assertNotePropertyEnabled(panel, "velocityValueField", false);
		assertNotePropertyEnabled(panel, "onlySelectedNoteOption", true);
		assertNotePropertyEnabled(panel, "incDecrVelocityEditOption", false);
		assertNotePropertyEnabled(panel, "velocityValueField2", false);

		// 音量コマンドを選択.
		setNotePropertySelected(panel, "velocityCheckBox", true);
		panel.actionPerformed(null);
		assertNotePropertyEnabled(panel, "velocityCheckBox", true);
		assertNotePropertyEnabled(panel, "velocityValueField", true);
		assertNotePropertyEnabled(panel, "onlySelectedNoteOption", true);
		assertNotePropertyEnabled(panel, "incDecrVelocityEditOption", false);
		assertNotePropertyEnabled(panel, "velocityValueField2", false);

		// 選択したノートだけを変更.
		setNotePropertySelected(panel, "onlySelectedNoteOption", true);
		panel.actionPerformed(null);
		assertNotePropertyEnabled(panel, "velocityCheckBox", false);
		assertNotePropertyEnabled(panel, "velocityValueField", true);
		assertNotePropertyEnabled(panel, "onlySelectedNoteOption", true);
		assertNotePropertyEnabled(panel, "incDecrVelocityEditOption", true);
		assertNotePropertyEnabled(panel, "velocityValueField2", false);

		// 増減量指定.
		setNotePropertySelected(panel, "incDecrVelocityEditOption", true);
		panel.actionPerformed(null);
		assertNotePropertyEnabled(panel, "velocityCheckBox", false);
		assertNotePropertyEnabled(panel, "velocityValueField", false);
		assertNotePropertyEnabled(panel, "onlySelectedNoteOption", true);
		assertNotePropertyEnabled(panel, "incDecrVelocityEditOption", true);
		assertNotePropertyEnabled(panel, "velocityValueField2", true);
	}
}

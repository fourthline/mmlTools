/*
 * Copyright (C) 2015-2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import javax.swing.JMenuItem;

import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.MainFrame.PlayStateComponent;


public final class MainFrameTest extends UseLoadingDLS {
	private MainFrame mainFrame;

	@Before
	public void setup() {
		mainFrame = new MainFrame(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {}
		}, null);
	}

	private Object getField(Object obj, String fieldName) throws Exception {
		Field f = MainFrame.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(obj);
	}

	private void checkNotNullField(Object obj, String fieldName) throws Exception {
		assertNotNull(getField(obj, fieldName));
	}

	@Test
	public final void test() throws Exception {
		checkNotNullField(mainFrame, "reloadMenuItem");
		checkNotNullField(mainFrame, "undoMenu");
		checkNotNullField(mainFrame, "redoMenu");
		checkNotNullField(mainFrame, "saveMenuItem");
		checkNotNullField(mainFrame, "cutMenu");
		checkNotNullField(mainFrame, "copyMenu");
		checkNotNullField(mainFrame, "pasteMenu");
		checkNotNullField(mainFrame, "deleteMenu");
	}

	@Test
	public void test_PlayStateComponent() {
		var menu = new JMenuItem("");
		var stat = new MainFrame.PlayStateComponent<>(menu);
		assertEquals(true, menu.isEnabled());

		stat.setNoplay(false);
		assertEquals(false, menu.isEnabled());
		stat.setNoplay(true);
		assertEquals(true, menu.isEnabled());

		stat.setEnabled(false);
		assertEquals(false, menu.isEnabled());
		stat.setNoplay(false);
		assertEquals(false, menu.isEnabled());
		stat.setNoplay(true);
		assertEquals(false, menu.isEnabled());
		stat.setEnabled(true);
		assertEquals(true, menu.isEnabled());

		stat.setNoplay(false);
		stat.setEnabled(true);
		assertEquals(false, menu.isEnabled());
	}

	@Test
	public void test_PlayStateMenu() throws Exception {
		@SuppressWarnings("unchecked")
		MainFrame.PlayStateComponent<JMenuItem> c = (PlayStateComponent<JMenuItem>) getField(mainFrame, "removeRestsBetweenNotesMenu");

		mainFrame.setRemoveRestsBetweenNotesEnable(true);
		assertEquals(true, c.get().isEnabled());

		mainFrame.disableNoplayItems();
		assertEquals(false, c.get().isEnabled());

		mainFrame.enableNoplayItems();
		assertEquals(true, c.get().isEnabled());

		mainFrame.disableNoplayItems();
		assertEquals(false, c.get().isEnabled());
		mainFrame.setRemoveRestsBetweenNotesEnable(false);
		mainFrame.enableNoplayItems();
		assertEquals(false, c.get().isEnabled());
		mainFrame.setRemoveRestsBetweenNotesEnable(true);
		assertEquals(true, c.get().isEnabled());
		mainFrame.setRemoveRestsBetweenNotesEnable(false);
		assertEquals(false, c.get().isEnabled());
	}
}

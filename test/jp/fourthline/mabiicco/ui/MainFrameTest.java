/*
 * Copyright (C) 2015 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;


public final class MainFrameTest extends UseLoadingDLS {

	private void checkNotNullField(Object obj, String fieldName) throws Exception {
		Field f = MainFrame.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		assertNotNull(f.get(obj));
	}

	@Test
	public final void test() throws Exception {
		MainFrame mainFrame = new MainFrame(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {}
		}, null);

		checkNotNullField(mainFrame, "reloadMenuItem");
		checkNotNullField(mainFrame, "undoMenu");
		checkNotNullField(mainFrame, "redoMenu");
		checkNotNullField(mainFrame, "saveMenuItem");
		checkNotNullField(mainFrame, "cutMenu");
		checkNotNullField(mainFrame, "copyMenu");
		checkNotNullField(mainFrame, "pasteMenu");
		checkNotNullField(mainFrame, "deleteMenu");
	}
}

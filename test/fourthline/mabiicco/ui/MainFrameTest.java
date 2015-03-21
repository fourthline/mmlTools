/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.ui;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;

import org.junit.BeforeClass;
import org.junit.Test;

import fourthline.mabiicco.midi.MabiDLS;


public final class MainFrameTest {

	@BeforeClass
	public static void initialize() throws Exception {
		MabiDLS midi = MabiDLS.getInstance();
		midi.initializeMIDI();
		midi.loadingDLSFile(new File(MabiDLS.DEFALUT_DLS_PATH));
	}

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
		});

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

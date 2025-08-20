/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.mabiicco.MabiIccoProperties;
import java.io.File;
import java.io.IOException;
import java.util.List;


public final class DLSSetupDialogTest {

	private List<File> initialDlsFiles;
	private MabiIccoProperties prop = MabiIccoProperties.getInstance();

	@Before
	public void setup() throws IOException {
		// backup properties
		initialDlsFiles = List.copyOf(prop.getDlsFile());

		new File("a.dls").createNewFile();
		new File("b.dls").createNewFile();
		new File("c.dls").createNewFile();
	}

	@After
	public void tearDown() {
		// restore properties
		prop.setDlsFile(initialDlsFiles);

		new File("a.dls").delete();
		new File("b.dls").delete();
		new File("c.dls").delete();
	}

	@Test
	public void testAdd() {
		// initial: a, b
		MabiIccoProperties.getInstance().setDlsFile(List.of(new File("a.dls"), new File("b.dls")));
		var dialog = new DLSSetupDialog(null);
		assertEquals("[a.dls, b.dls]", dialog.getFileList().toString());

		// add "c.dls"
		dialog.addDLSFile(List.of(new File("c.dls")));
		assertEquals("[a.dls, b.dls, c.dls]", dialog.getFileList().toString());
		assertEquals("[a.dls, b.dls]", prop.getDlsFile().toString());

		dialog.onOK();
		assertEquals("[a.dls, b.dls, c.dls]", prop.getDlsFile().toString());
	}

	@Test
	public void testRemove() {
		// initial: a, b, c
		MabiIccoProperties.getInstance().setDlsFile(List.of(new File("a.dls"), new File("b.dls"), new File("c.dls")));
		var dialog = new DLSSetupDialog(null);
		assertEquals("[a.dls, b.dls, c.dls]", dialog.getFileList().toString());

		// remove row(1)
		dialog.removeRow(1);
		assertEquals("[a.dls, c.dls]", dialog.getFileList().toString());
		assertEquals("[a.dls, b.dls, c.dls]", prop.getDlsFile().toString());

		dialog.onOK();
		assertEquals("[a.dls, c.dls]", prop.getDlsFile().toString());
	}

	@Test
	public void testDefault() {
		// initial: a, b, c
		MabiIccoProperties.getInstance().setDlsFile(List.of(new File("a.dls"), new File("b.dls"), new File("c.dls")));
		var dialog = new DLSSetupDialog(null);
		assertEquals("[a.dls, b.dls, c.dls]", dialog.getFileList().toString());

		// set default
		dialog.setDefault();
		assertEquals("[]", dialog.getFileList().toString());    // TODO
		assertEquals("[a.dls, b.dls, c.dls]", prop.getDlsFile().toString());

		dialog.onOK();
		assertEquals("[]", prop.getDlsFile().toString());    // TODO
	}
}

/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

public class DLSLoaderTest {

	@Test
	public void test() {
		var list = new ArrayList<File>();
		list.add(new File("2.dls"));
		list.add(new File("3.dls"));
		list.add(new File("MSXspirit03.dls"));
		list.add(new File("MSXspirit02.dls"));
		list.add(new File("MSXspirit04.dls"));
		list.add(new File("MSXspirit01.dls"));
		list.add(new File("1.dls"));
		Collections.sort(list, new DLSLoader(list));

		String str = list.toString();
		System.out.println(str);
		assertEquals("[MSXspirit01.dls, MSXspirit02.dls, MSXspirit03.dls, MSXspirit04.dls, 1.dls, 2.dls, 3.dls]", str);
	}

	@Test
	public void testDup() throws IOException {
		var f1 = File.createTempFile("test", ".dls");
		var list = new ArrayList<File>();
		var name = f1.getAbsolutePath();
		list.add(new File(name));
		list.add(new File(name));
		list.add(new File("2.dls"));
		var loader = new DLSLoader(list);

		String str = loader.getFileList().toString();
		System.out.println(str);
		assertEquals("["+name+"]", str);

		f1.deleteOnExit();
	}

}

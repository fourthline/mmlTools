/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JDialog;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;

public final class LevelMonitorTest extends UseLoadingDLS {

	@Test
	public void test() throws FileNotFoundException, IOException {
		byte[] z_data = new byte[1200];
		byte[] data = new byte[1200];
		for (int i = 0; i < data.length; i++) {
			if (i < 600) {
				data[i] = (byte) (((i&2) == 0) ? 120 : 20);
			} else {
				data[i] = 0;
			}
		}
		var obj = new LevelMonitor(false);
		JDialog dialog = new JDialog();
		dialog.add(obj);
		dialog.pack();
		dialog.setVisible(true);

		int width = dialog.getWidth();
		int height = dialog.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// データありのレベル
		obj.write(data);
		obj.write(data);
		assertEquals(30840, obj.getLeft().getMax());
		assertEquals(5140, obj.getRight().getMax());

		assertEquals(39, obj.getLeft().getValue());
		assertEquals(39, obj.getLeft().getHoltValue());
		assertEquals(31, obj.getRight().getValue());
		assertEquals(31, obj.getRight().getHoltValue());
		obj.paintComponent(image.getGraphics());
		assertImage(fileSelect("levelMonitor1.png"), image);

		// データがなくなった
		obj.write(z_data);
		obj.write(z_data);
		assertEquals(0, obj.getLeft().getMax());
		assertEquals(0, obj.getRight().getMax());

		assertEquals(0, obj.getLeft().getValue());
		assertEquals(39, obj.getLeft().getHoltValue());
		assertEquals(0, obj.getRight().getValue());
		assertEquals(31, obj.getRight().getHoltValue());
		obj.paintComponent(image.getGraphics());
		assertImage(fileSelect("levelMonitor2.png"), image);

		// データがなくなって経過
		for (int i = 0; i < 40; i++) {
			obj.write(z_data);
			obj.write(z_data);
		}

		assertEquals(0, obj.getLeft().getValue());
		assertEquals(35, obj.getLeft().getHoltValue());
		assertEquals(0, obj.getRight().getValue());
		assertEquals(27, obj.getRight().getHoltValue());
		obj.paintComponent(image.getGraphics());
		assertImage(fileSelect("levelMonitor3.png"), image);

		dialog.setVisible(false);
	}

}

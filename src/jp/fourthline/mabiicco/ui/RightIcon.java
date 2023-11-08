/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public final class RightIcon implements Icon {
	private final int size;

	public RightIcon(int size) {
		this.size = size;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(c.getForeground());
		int px[] = { x, x+size, x };
		int py[] = { y, y+size/2, y+size };
		g.fillPolygon(px, py, px.length);
	}

	@Override
	public int getIconWidth() {
		return size;
	}

	@Override
	public int getIconHeight() {
		return size;
	}
}

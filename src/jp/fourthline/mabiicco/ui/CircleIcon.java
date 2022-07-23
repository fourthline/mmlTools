/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public final class CircleIcon implements Icon {
	private final int width;
	private final Color fillColor;
	private final Color drawColor;

	public CircleIcon(int width, Color fillColor, Color drawColor) {
		this.width = width;
		this.fillColor = fillColor;
		this.drawColor = drawColor;
	}

	@Override
	public int getIconHeight() {
		return width;
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (fillColor != null) {
			g.setColor(fillColor);
			g.fillOval(x+2, y+2, width-4, width-4);
		}
		if (drawColor != null) {
			g.setColor(drawColor);
			g.drawOval(x+2, y+2, width-4, width-4);
		}
	}
}

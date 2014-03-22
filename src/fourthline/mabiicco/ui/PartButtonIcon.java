/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class PartButtonIcon implements Icon {
	protected static final int WIDTH = 12;

	private static final Icon instance;

	static {
		instance = new PartButtonIcon();
	}

	public static Icon getInstance() {
		return instance;
	}

	protected PartButtonIcon() {}

	@Override
	public int getIconHeight() {
		return WIDTH;
	}

	@Override
	public int getIconWidth() {
		return WIDTH;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		int width = WIDTH - 4;
		g.setColor(ColorPalette.UNUSED.getFillColor(0));
		g.fillOval(x+2, y+2, width, width);
	}
}

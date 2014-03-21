/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class PartButtonIcon implements Icon {
	private static final int WIDTH = 12;

	private static PartButtonIcon instance[][];

	static {
		int pattern = MMLTrackView.MMLPART_NAME.length;
		int size = ColorPalette.MELODY.size();
		instance = new PartButtonIcon[pattern][size];
		for (int i = 0; i < pattern; i++) {
			for (int j = 0; j < size; j++) {
				instance[i][j] = new PartButtonIcon(i, j);
			}
		}
	}

	public static PartButtonIcon getInstance(int part, int index) {
		return instance[part][index];
	}

	private PartButtonIcon() {}

	private int pattern;
	private int index;
	private PartButtonIcon(int pattern, int index) {
		this.pattern = pattern;
		this.index = index;
	}

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
		g.setColor(ColorPalette.getColorType(pattern).getFillColor(index));
		g.fillOval(x+2, y+2, width, width);
		g.setColor(ColorPalette.ACTIVE.getFillColor(index));
		g.drawOval(x+2, y+2, width, width);
	}
}

/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public final class PartButtonIconArray extends PartButtonIcon {

	private final static PartButtonIconArray instance[][];
	private final static int indexSize;

	static {
		int pattern = MMLTrackView.MMLPART_NAME.length;
		indexSize = ColorPalette.MELODY.size();
		instance = new PartButtonIconArray[pattern][indexSize];
		for (int i = 0; i < pattern; i++) {
			for (int j = 0; j < indexSize; j++) {
				instance[i][j] = new PartButtonIconArray(i, j);
			}
		}
	}

	public static Icon getInstance(int part, int index) {
		return instance[part][index%indexSize];
	}

	private PartButtonIconArray() {}

	private int pattern;
	private int index;
	private PartButtonIconArray(int pattern, int index) {
		this.pattern = pattern;
		this.index = index;
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

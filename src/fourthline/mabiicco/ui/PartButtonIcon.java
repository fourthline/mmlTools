/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import fourthline.mabiicco.ui.color.ColorManager;
import fourthline.mmlTools.MMLScore;

public class PartButtonIcon implements Icon {
	protected static final int WIDTH = 12;

	private final static PartButtonIcon instance[][];
	private final static int indexSize;
	private final static PartButtonIcon defaultIcon = new PartButtonIcon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int width = WIDTH - 4;
			g.setColor(ColorManager.defaultColor().getUnusedFillColor());
			g.fillOval(x+2, y+2, width, width);
		}
	};

	static {
		int pattern = MMLTrackView.MMLPART_NAME.length;
		indexSize = MMLScore.MAX_TRACK;
		instance = new PartButtonIcon[pattern][indexSize];
		for (int i = 0; i < pattern; i++) {
			for (int j = 0; j < indexSize; j++) {
				instance[i][j] = new PartButtonIcon(i, j);
			}
		}
	}

	public static Icon getInstance(int part, int index) {
		return instance[part][index%indexSize];
	}

	public static Icon getDefautIcon() {
		return defaultIcon;
	}

	private PartButtonIcon() {}

	private int part;
	private int index;
	private PartButtonIcon(int part, int index) {
		this.part = part;
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
		g.setColor(ColorManager.defaultColor().getPartFillColor(index, part));
		g.fillOval(x+2, y+2, width, width);
		g.setColor(ColorManager.defaultColor().getActiveFillColor(index));
		g.drawOval(x+2, y+2, width, width);
	}
}

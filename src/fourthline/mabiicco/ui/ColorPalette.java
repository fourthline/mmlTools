/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Color;

public enum ColorPalette {
	ACTIVE(250, 200, 0) {
		@Override
		public Color getRectColor(int index) {
			return Color.DARK_GRAY;
		}
	},
	MELODY(0, 200, 0),
	CHORD1(0, 200, 40),
	CHORD2(0, 200, 80),
	SONGEX(0, 200, 120);

	protected Color filter(Color color) {
		return new Color(
				limit(color.getRed()+beta), 
				limit(color.getGreen()+beta), 
				limit(color.getBlue()+beta), 
				color.getAlpha());
	}

	private final Color trackBaseColor[] = {
			new Color(200, 0, 0),
			new Color(0, 200, 0),
			new Color(0, 0, 200),
			Color.ORANGE.darker(),
			Color.CYAN.darker(),
			Color.MAGENTA.darker(),
			Color.YELLOW.darker(),
			Color.decode("#FF5564"),
	};

	private final Color rectColorTable[];
	private final Color fillColorTable[];
	private final int beta;

	private static int limit(int a) {
		if (a > 255) return 255;
		if (a < 0)   return 0;
		return a;
	}

	private ColorPalette(int rectAlpha, int fillAlpha, int beta) {
		this.beta = beta;
		rectColorTable = new Color[trackBaseColor.length];
		fillColorTable = new Color[trackBaseColor.length];

		for (int i = 0; i < trackBaseColor.length; i++) {
			Color baseColor = trackBaseColor[i];
			rectColorTable[i] = filter(new Color(
					baseColor.getRed(),
					baseColor.getGreen(),
					baseColor.getBlue(),
					rectAlpha));
			fillColorTable[i] = filter(new Color(
					baseColor.getRed(),
					baseColor.getGreen(),
					baseColor.getBlue(),
					fillAlpha));
		}
	}

	public int size() {
		return rectColorTable.length;
	}

	public Color getRectColor(int index) {
		return rectColorTable[index%rectColorTable.length];
	}

	public Color getFillColor(int index) {
		return fillColorTable[index%fillColorTable.length];
	}

	public static ColorPalette getColorType(int part) {
		switch (part) {
		case 0:
			return MELODY;
		case 1:
			return CHORD1;
		case 2:
			return CHORD2;
		case 3:
			return SONGEX;
		}

		return null;
	}
}

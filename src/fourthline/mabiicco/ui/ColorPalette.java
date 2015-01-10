/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ColorPalette {
	static private enum ColorPattern {
		ACTIVE(250, 200, 0),
		MELODY(0, 200, 0),
		CHORD1(0, 200, 40),
		CHORD2(0, 200, 80),
		SONGEX(0, 200, 120),
		UNUSED(0, 80, 0) {
			@Override
			public Color getFillColor(Color baseColor) {
				return Color.GRAY;
			}
		};

		protected Color filter(Color color) {
			return new Color(
					limit(color.getRed()+beta), 
					limit(color.getGreen()+beta), 
					limit(color.getBlue()+beta), 
					color.getAlpha());
		}
		private static int limit(int a) {
			if (a > 255) return 255;
			if (a < 0)   return 0;
			return a;
		}

		private final int rectAlpha;
		private final int fillAlpha;
		private final int beta;
		private ColorPattern(int rectAlpha, int fillAlpha, int beta) {
			this.rectAlpha = rectAlpha;
			this.fillAlpha = fillAlpha;
			this.beta = beta;
		}

		public Color getRectColor(Color baseColor) {
			return filter(new Color(
					baseColor.getRed(),
					baseColor.getGreen(),
					baseColor.getBlue(),
					rectAlpha));
		}

		public Color getFillColor(Color baseColor) {
			return filter(new Color(
					baseColor.getRed(),
					baseColor.getGreen(),
					baseColor.getBlue(),
					fillAlpha));
		}
	}

	private static final Color trackBaseColor[] = {
		new Color(200, 0, 0),
		new Color(0, 200, 0),
		new Color(0, 0, 200),
		Color.ORANGE.darker(),
		Color.CYAN.darker(),
		Color.MAGENTA.darker(),
		Color.YELLOW.darker(),
		Color.decode("#FF5564"),
	};

	private static ArrayList<ColorPalette> instanceList = null;
	public static void createInstance() {
		instanceList = new ArrayList<>();
		for (Color baseColor : trackBaseColor) {
			instanceList.add( new ColorPalette(baseColor) );
		}
	}

	public static int getInstanceSize() {
		return trackBaseColor.length;
	}
	public static ColorPalette getInstance(int trackIndex) {
		if (instanceList == null) {
			createInstance();
		}
		int index = trackIndex % instanceList.size();
		return instanceList.get(index);
	}

	private final ArrayList<Color> rectColorTable = new ArrayList<>();
	private final ArrayList<Color> fillColorTable = new ArrayList<>();

	private ColorPalette(Color baseColor) {
		for (ColorPattern pattern : ColorPattern.values()) {
			rectColorTable.add( pattern.getRectColor(baseColor) );
			fillColorTable.add( pattern.getFillColor(baseColor) );
		}
	}

	private Color getColor(List<Color> colorList, ColorPattern pattern) {
		int index = Arrays.binarySearch(ColorPattern.values(), pattern);
		return colorList.get(index % colorList.size());
	}

	public Color getActiveRectColor() {
		return getColor( rectColorTable, ColorPattern.ACTIVE );
	}
	public Color getActiveFillColor() {
		return getColor( fillColorTable, ColorPattern.ACTIVE );
	}

	public Color getPartRectColor(int part) {
		return getColor( rectColorTable, getColorType(part) );
	}
	public Color getPartFillColor(int part) {
		return getColor( fillColorTable, getColorType(part) );
	}
	public Color getUnusedFillColor() {
		return getColor( fillColorTable, ColorPattern.UNUSED );
	}

	public ColorPattern getColorType(int part) {
		switch (part) {
		case 0:
			return ColorPattern.MELODY;
		case 1:
			return ColorPattern.CHORD1;
		case 2:
			return ColorPattern.CHORD2;
		case 3:
			return ColorPattern.SONGEX;
		}
		return ColorPattern.UNUSED;
	}

	public static String toText(Color c) {
		return String.format("%08x", c.getRGB());
	}

	public static Color toColor(String s) {
		return new Color(Integer.parseUnsignedInt(s, 16), true);
	}

	public static void main(String args[]) {
		createInstance();
		for (ColorPalette palette : instanceList) {
			System.out.println(" -- rect color --");
			for (Color c : palette.rectColorTable) {
				System.out.println( toText(c) );
			}
			System.out.println(" -- fill color --");
			for (Color c : palette.fillColorTable) {
				System.out.println( toText(c) );
			}
		}
	}
}

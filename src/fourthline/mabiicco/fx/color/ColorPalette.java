/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mabiicco.fx.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.paint.Color;

final class ColorPalette {
	private static enum ColorPattern {
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
					color.getOpacity());
		}
		private static double limit(double a) {
			if (a > 1.0) return 1.0;
			if (a < 0)   return 0;
			return a;
		}

		private final double rectAlpha;
		private final double fillAlpha;
		private final double beta;
		private ColorPattern(int rectAlpha, int fillAlpha, int beta) {
			this.rectAlpha = rectAlpha/250.0;
			this.fillAlpha = fillAlpha/250.0;
			this.beta = beta/250.0;
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
		Color.web("#f44336"),	// Red
		Color.web("#e91e63"),	// Pink
		Color.web("#9c27b0"),	// Purple
		Color.web("#673ab7"),	// Deep Purple
		Color.web("#3f51b5"),	// Indigo
		Color.web("#2196f3"),	// Blue
		Color.web("#03a9f4"),	// Light Blue
		Color.web("#00bcd4"),	// Cyan
		Color.web("#009688"),	// Teal
		Color.web("#4caf50"),	// Green
		Color.web("#8bc34a"),	// Light Green
		Color.web("#cddc39"),	// Lime
		Color.web("#ffeb3b"),	// Yellow
		Color.web("#ffc107"),	// Amber
		Color.web("#ff9800"),	// Orange
		Color.web("#ff5722"),	// Deep Orange
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

	private Color baseColor;
	private final ArrayList<Color> rectColorTable = new ArrayList<>();
	private final ArrayList<Color> fillColorTable = new ArrayList<>();

	public ColorPalette(Color baseColor) {
		setColor(baseColor);
	}

	public void setColor(Color baseColor) {
		rectColorTable.clear();
		fillColorTable.clear();
		this.baseColor = baseColor;
		for (ColorPattern pattern : ColorPattern.values()) {
			rectColorTable.add( pattern.getRectColor(baseColor) );
			fillColorTable.add( pattern.getFillColor(baseColor) );
		}
	}

	public Color getBaseColor() {
		return this.baseColor;
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
}

/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

final class ColorPalette {
	private enum ColorPattern {
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
		ColorPattern(int rectAlpha, int fillAlpha, int beta) {
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

	private static final Color[] trackBaseColor = {
		new Color(200, 0, 0),
		new Color(0, 200, 0),
		new Color(0, 0, 200),
		Color.decode("#FF4400"),
		Color.decode("#00AAFF"),
		Color.decode("#FF00D0"),
		Color.decode("#009933"),
		Color.decode("#FF5564"),
		Color.decode("#8100FF"),
		Color.decode("#891D1D"),
		Color.decode("#A78100"),
		Color.decode("#00600B"),
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
		int index = 0;
		var list = ColorPattern.values();
		for (; index < list.length; index++) {
			if (list[index] == pattern) break;
		}
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

	public static void main(String[] args) {
		ColorManager colorManager = ColorManager.defaultColor();
		int track = 12;
		int part = 4;
		for (int i = 0; i < track; i++) {
			System.out.println( "rectA="+ColorPalette.toText( colorManager.getActiveRectColor(i) ));
			System.out.println( "fillA="+ColorPalette.toText( colorManager.getActiveFillColor(i) ));
			for (int j = 0; j < part; j++) {
				System.out.println( "rect"+j+"="+ColorPalette.toText( colorManager.getPartRectColor(i, j) ));
				System.out.println( "fill"+j+"="+ColorPalette.toText( colorManager.getPartFillColor(i, j) ));
			}
		}
		System.out.println( "unused="+ColorPalette.toText( colorManager.getUnusedFillColor() ));
	}
}

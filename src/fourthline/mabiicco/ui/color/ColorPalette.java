/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mabiicco.ui.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class ColorPalette {
	private static enum ColorPattern {
		// 選択状態
		ACTIVE(250, 200, 0),
		// メロディ
		MELODY(0, 200, 0),
		// 和音１
		CHORD1(0, 200, 40),
		// 和音２
		CHORD2(0, 200, 80),
		// コーラス
		SONGEX(0, 200, 120),
		// 未使用
		UNUSED(0, 80, 0) {
			@Override
			public Color getFillColor(Color baseColor) {
				return Color.decode("#78909c");
			}
		};

		protected Color filter(Color color) {
			return new Color(limit(color.getRed() + beta), limit(color.getGreen() + beta),
					limit(color.getBlue() + beta), color.getAlpha());
		}

		private static int limit(int a) {
			if (a > 255)
				return 255;
			if (a < 0)
				return 0;
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
			return filter(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), rectAlpha));
		}

		public Color getFillColor(Color baseColor) {
			return filter(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), fillAlpha));
		}
	}

	private static final Color trackBaseColor[] = {
			// Red
			Color.decode("#f44336"),
			// Pink
			Color.decode("#e91e63"),
			// Purple
			Color.decode("#9c27b0"),
			// Deep Purple
			Color.decode("#673ab7"),
			// Indigo
			Color.decode("#3f51b5"),
			// Blue
			Color.decode("#2196f3"),
			// Light Blue
			Color.decode("#03a9f4"),
			// Cyan
			Color.decode("#00bcd4"),
			// Teal
			Color.decode("#009688"),
			// Green
			Color.decode("#4caf50"),
			// Light Green
			Color.decode("#8bc34a"),
			// Lime
			Color.decode("#cddc39"),
			// Yellow
			Color.decode("#ffeb3b"),
			// Amber
			Color.decode("#ffc107"),
			// Orange
			Color.decode("#ff9800"),
			// Deep Orange
			Color.decode("#ff5722"), };

	private static ArrayList<ColorPalette> instanceList = null;

	public static void createInstance() {
		instanceList = new ArrayList<>();
		for (Color baseColor : trackBaseColor) {
			instanceList.add(new ColorPalette(baseColor));
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
			rectColorTable.add(pattern.getRectColor(baseColor));
			fillColorTable.add(pattern.getFillColor(baseColor));
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
		return getColor(rectColorTable, ColorPattern.ACTIVE);
	}

	public Color getActiveFillColor() {
		return getColor(fillColorTable, ColorPattern.ACTIVE);
	}

	public Color getPartRectColor(int part) {
		return getColor(rectColorTable, getColorType(part));
	}

	public Color getPartFillColor(int part) {
		return getColor(fillColorTable, getColorType(part));
	}

	public Color getUnusedFillColor() {
		return getColor(fillColorTable, ColorPattern.UNUSED);
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
			System.out.println("rectA=" + ColorPalette.toText(colorManager.getActiveRectColor(i)));
			System.out.println("fillA=" + ColorPalette.toText(colorManager.getActiveFillColor(i)));
			for (int j = 0; j < part; j++) {
				System.out.println("rect" + j + "=" + ColorPalette.toText(colorManager.getPartRectColor(i, j)));
				System.out.println("fill" + j + "=" + ColorPalette.toText(colorManager.getPartFillColor(i, j)));
			}
		}
		System.out.println("unused=" + ColorPalette.toText(colorManager.getUnusedFillColor()));
	}
}

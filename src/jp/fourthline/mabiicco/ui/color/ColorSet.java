/*
 * Copyright (C) 2023 たんらる
 */
package jp.fourthline.mabiicco.ui.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * ライトカラーとダークカラーのセット
 */
public final class ColorSet implements Supplier<Color> {

	private static final ArrayList<ColorSet> list = new ArrayList<>();

	public static ColorSet create(Color lightColor, Color darkColor) {
		var o = new ColorSet(lightColor, darkColor, ColorSet.lightMode);
		list.add(o);
		return o;
	}

	public static ColorSet create(Color color) {
		return create(color, color);
	}

	private static boolean lightMode = true;
	public static void update(boolean lightMode) {
		ColorSet.lightMode = lightMode;
		list.forEach(t -> t.changeCurrentColor(lightMode));
	}

	private void changeCurrentColor(boolean lightMode) {
		currentColor = lightMode ? lightColor : darkColor;
	}

	private final Color lightColor;
	private final Color darkColor;
	private Color currentColor;

	private ColorSet(Color lightColor, Color darkColor, boolean lightMode) {
		this.lightColor = lightColor;
		this.darkColor = darkColor;
		changeCurrentColor(lightMode);
	}

	@Override
	public Color get() {
		return currentColor;
	}
}

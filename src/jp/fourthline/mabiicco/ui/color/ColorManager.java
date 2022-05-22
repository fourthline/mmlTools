/*
 * Copyright (C) 2015 たんらる
 */

package jp.fourthline.mabiicco.ui.color;

import java.awt.Color;

public interface ColorManager {
	Color getActiveRectColor(int trackIndex);
	Color getActiveFillColor(int trackIndex);

	Color getPartRectColor(int trackIndex, int partIndex);
	Color getPartFillColor(int trackIndex, int partIndex);

	Color getUnusedFillColor();

	static ColorManager defaultColor() {
		return DefaultColor.instance;
	}
}

final class DefaultColor implements ColorManager {
	static DefaultColor instance = new DefaultColor();

	@Override
	public Color getActiveRectColor(int trackIndex) {
		return ColorPalette.getInstance(trackIndex).getActiveRectColor();
	}

	@Override
	public Color getActiveFillColor(int trackIndex) {
		return ColorPalette.getInstance(trackIndex).getActiveFillColor();
	}

	@Override
	public Color getPartRectColor(int trackIndex, int partIndex) {
		return ColorPalette.getInstance(trackIndex).getPartRectColor(partIndex);
	}

	@Override
	public Color getPartFillColor(int trackIndex, int partIndex) {
		return ColorPalette.getInstance(trackIndex).getPartFillColor(partIndex);
	}

	@Override
	public Color getUnusedFillColor() {
		return ColorPalette.getInstance(0).getUnusedFillColor();
	}
}

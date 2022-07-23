/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui;

import javax.swing.Icon;

import jp.fourthline.mabiicco.ui.color.ColorManager;
import jp.fourthline.mmlTools.MMLScore;

public final class PartButtonIcon {
	private static final int WIDTH = 12;
	private final static Icon[][] instance;
	private final static int indexSize;
	private final static Icon defaultIcon;

	static {
		int pattern = MMLTrackView.MMLPART_NAME.length;
		indexSize = MMLScore.MAX_TRACK;
		instance = new Icon[pattern][indexSize];
		var cm = ColorManager.defaultColor();
		for (int i = 0; i < pattern; i++) {
			for (int j = 0; j < indexSize; j++) {
				instance[i][j] = new CircleIcon(WIDTH, 
						cm.getPartFillColor(j, i),
						cm.getActiveFillColor(j));
			}
		}
		defaultIcon = new CircleIcon(WIDTH, cm.getUnusedFillColor(), null);
	}

	private PartButtonIcon() {}

	public static Icon getInstance(int part, int index) {
		return instance[part][index%indexSize];
	}

	public static Icon getDefautIcon() {
		return defaultIcon;
	}
}

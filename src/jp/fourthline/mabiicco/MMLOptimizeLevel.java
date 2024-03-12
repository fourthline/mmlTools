/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco;

import jp.fourthline.mabiicco.ui.SettingButtonGroupItem;
import jp.fourthline.mmlTools.optimizer.MMLStringOptimizer;

public enum MMLOptimizeLevel implements SettingButtonGroupItem {
	LV1("Lv1", MMLStringOptimizer.GEN1),
	LV2("Lv2", MMLStringOptimizer.GEN2),
	LV3("Lv3", MMLStringOptimizer.GEN3)
	;

	private final String name;
	private final int level;
	private MMLOptimizeLevel(String name, int level) {
		this.name = name;
		this.level = level;
	}

	@Override
	public String getButtonName() {
		return this.name;
	}

	public void change() {
		MMLStringOptimizer.setOptimizeLevel(level);
	}
}

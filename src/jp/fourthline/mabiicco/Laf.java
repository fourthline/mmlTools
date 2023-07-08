/*
 * Copyright (C) 2023 たんらる
 */
package jp.fourthline.mabiicco;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import jp.fourthline.mabiicco.ui.SettingButtonGroupItem;

public enum Laf implements SettingButtonGroupItem {
	SYSTEM(UIManager.getSystemLookAndFeelClassName(), true),
	LIGHT(FlatLightLaf.class.getCanonicalName(), true),
	DARK(FlatDarkLaf.class.getCanonicalName(), false);

	private final String lafName;
	private final boolean lightMode;

	private Laf(String lafName, boolean lightMode) {
		this.lafName = lafName;
		this.lightMode = lightMode;
	}

	public Laf update() {
		try {
			UIManager.setLookAndFeel( lafName );
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			return null;
		}
		return this;
	}

	@Override
	public String getButtonName() {
		return lafName;
	}

	public boolean isLight() {
		return lightMode;
	}
}

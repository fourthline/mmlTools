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
	SYSTEM(UIManager.getSystemLookAndFeelClassName(), "ui.system", true),
	LIGHT(FlatLightLaf.class.getCanonicalName(), "ui.light", true),
	DARK(FlatDarkLaf.class.getCanonicalName(), "ui.dark", false);

	private final String lafName;
	private final String text;
	private final boolean lightMode;

	private Laf(String lafName, String text, boolean lightMode) {
		this.lafName = lafName;
		this.text = AppResource.appText(text);
		this.lightMode = lightMode;
		System.out.println(lafName);
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
		return text;
	}

	public boolean isLight() {
		return lightMode;
	}
}

/*
 * Copyright (C) 2023 たんらる
 */
package jp.fourthline.mabiicco;

import java.awt.Font;
import java.util.Collections;

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
	}

	public Laf update() {
		try {
			UIManager.setLookAndFeel( lafName );
			setUIFont();
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

	/**
	 * フォント設定の行う.
	 * laf変更時は設定維持されているので更新不要.
	 */
	private static void setUIFont() {
		String fontName = AppResource.appText("ui.font");
		if (!fontName.equals("ui.font")) {
			var resource = new javax.swing.plaf.FontUIResource(fontName, Font.PLAIN, 11);
			for (Object key : Collections.list(UIManager.getDefaults().keys())) {
				Object value = UIManager.get(key);
				if (value instanceof javax.swing.plaf.FontUIResource) {
					UIManager.put(key, resource);
				}
			}
		}
	}
}

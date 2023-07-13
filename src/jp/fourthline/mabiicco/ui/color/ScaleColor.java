/*
 * Copyright (C) 2021-2023 たんらる
 */

package jp.fourthline.mabiicco.ui.color;

import java.awt.Color;

import jp.fourthline.mabiicco.ui.SettingButtonGroupItem;

public enum ScaleColor implements SettingButtonGroupItem {
	C_MAJOR( "scale_color.c_major",  0), // ハ長調, イ短調
	G_MAJOR( "scale_color.g_major",  7), // ト長調, ホ短調
	D_MAJOR( "scale_color.d_major",  2), // ニ長調, ロ短調
	A_MAJOR( "scale_color.a_major",  9), // イ長調, 嬰ヘ短調
	E_MAJOR( "scale_color.e_major",  4), // ホ長調, 嬰ハ短調
	B_MAJOR( "scale_color.b_major",  11), // ロ長調, 変ハ長調, 嬰ト短調, 変イ短調
	Gb_MAJOR("scale_color.gb_major", 6), // 嬰ヘ長調, 変ト長調, 嬰ニ短調, 変ホ長調
	Db_MAJOR("scale_color.db_major", 1), // 嬰ハ長調, 変ニ長調, 嬰イ短調, 変ロ短調
	Ab_MAJOR("scale_color.ab_major", 8), // 変イ長調, ヘ短調
	Eb_MAJOR("scale_color.eb_major", 3), // 変ホ長調, ハ短調
	Bb_MAJOR("scale_color.bb_major", 10), // 変ロ長調, ト短調
	F_MAJOR( "scale_color.f_major",  5); // ヘ長調, ニ短調

	private final String name;
	private final int offset;
	ScaleColor(String name, int offset) {
		this.name = name;
		this.offset = offset;
	}

	@Override
	public String getButtonName() {
		return this.name;
	}

	public Color getColor(int index) {
		return ColorConstants.keyMajorColors[(index+offset)%12].get();
	}

	public static final Color BORDER_COLOR = new Color(0.6f, 0.6f, 0.6f); // 境界線用

	private interface ColorConstants {
		ColorSet wKeyColor = ColorSet.create(new Color(0.9f, 0.9f, 0.9f), Color.decode("#AAAAAA")); // 白鍵盤用
		ColorSet bKeyColor = ColorSet.create(new Color(0.8f, 0.8f, 0.8f), Color.decode("#666666")); // 黒鍵盤用
		ColorSet[] keyMajorColors = new ColorSet[] {
				wKeyColor, bKeyColor,   // B, B-
				wKeyColor, bKeyColor,   // A, A-
				wKeyColor, bKeyColor,   // G, G-
				wKeyColor,              // F
				wKeyColor, bKeyColor,   // E, E-
				wKeyColor, bKeyColor,   // D, E-
				wKeyColor				// C
		};
	}
}

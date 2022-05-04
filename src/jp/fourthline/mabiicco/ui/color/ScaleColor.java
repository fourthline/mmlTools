/*
 * Copyright (C) 2021 たんらる
 */

package jp.fourthline.mabiicco.ui.color;

import java.awt.Color;

public enum ScaleColor {
	C_MAJOR( "scale_color.c_major",  0,  ColorConstants.keyMajorColors), // ハ長調, イ短調
	G_MAJOR( "scale_color.g_major",  7,  ColorConstants.keyMajorColors), // ト長調, ホ短調
	D_MAJOR( "scale_color.d_major",  2,  ColorConstants.keyMajorColors), // ニ長調, ロ短調
	A_MAJOR( "scale_color.a_major",  9,  ColorConstants.keyMajorColors), // イ長調, 嬰ヘ短調
	E_MAJOR( "scale_color.e_major",  4,  ColorConstants.keyMajorColors), // ホ長調, 嬰ハ短調
	B_MAJOR( "scale_color.b_major",  11, ColorConstants.keyMajorColors), // ロ長調, 変ハ長調, 嬰ト短調, 変イ短調
	Gb_MAJOR("scale_color.gb_major", 6,  ColorConstants.keyMajorColors), // 嬰ヘ長調, 変ト長調, 嬰ニ短調, 変ホ長調
	Db_MAJOR("scale_color.db_major", 1,  ColorConstants.keyMajorColors), // 嬰ハ長調, 変ニ長調, 嬰イ短調, 変ロ短調
	Ab_MAJOR("scale_color.ab_major", 8,  ColorConstants.keyMajorColors), // 変イ長調, ヘ短調
	Eb_MAJOR("scale_color.eb_major", 3,  ColorConstants.keyMajorColors), // 変ホ長調, ハ短調
	Bb_MAJOR("scale_color.bb_major", 10, ColorConstants.keyMajorColors), // 変ロ長調, ト短調
	F_MAJOR( "scale_color.f_major",  5,  ColorConstants.keyMajorColors); // ヘ長調, ニ短調

	private final String name;
	private final int offset;
	private final Color colorList[];
	private ScaleColor(String name, int offset, Color colorList[]) {
		this.name = name;
		this.offset = offset;
		this.colorList = colorList;
	}

	public String getName() {
		return this.name;
	}

	public Color getColor(int index) {
		return this.colorList[(index+offset)%12];
	}

	public static final Color BORDER_COLOR = new Color(0.6f, 0.6f, 0.6f); // 境界線用

	private interface ColorConstants {
		static final Color wKeyColor = new Color(0.9f, 0.9f, 0.9f); // 白鍵盤用
		static final Color bKeyColor = new Color(0.8f, 0.8f, 0.8f); // 黒鍵盤用
		static final Color keyMajorColors[] = new Color[] {
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

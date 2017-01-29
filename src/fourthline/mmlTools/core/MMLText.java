/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mmlTools.core;

import fourthline.mmlTools.ComposeRank;


/**
 * MML基本ツール.
 * {@code MML@aaa,bbb,ccc;} 形式を扱います。
 */
public final class MMLText {
	private static int DEFAULT_PART_NUM = 4;
	private String text[];

	public MMLText() {
		text = new String[ DEFAULT_PART_NUM ];
		for (int i = 0; i < text.length; i++) {
			text[i] = "";
		}
	}

	public MMLText setMMLText(String mml) {
		if (!mml.startsWith("MML@")) {
			mml = "";
			return this;
		}

		int start = 4;
		int end = mml.indexOf(";");
		if (end <= 0)
			end = mml.length();

		mml = mml.substring(start, end);

		String parts[] = mml.split(",");
		setMMLText(parts);
		return this;
	}

	public MMLText setMMLText(String parts[]) {
		for (int i = 0; (i < parts.length) && (i < this.text.length); i++) {
			this.text[i] = parts[i];
		}
		return this;
	}

	public MMLText setMMLText(String melody, String chord1, String chord2, String songEx) {
		String parts[] = { melody, chord1, chord2, songEx };
		setMMLText(parts);
		return this;
	}

	public boolean isEmpty() {
		for (String s : this.text) {
			if (s.length() > 0) {
				return false;
			}
		}
		return true;
	}

	public String getText(int index) {
		if (index < this.text.length) {
			return this.text[index];
		}
		return "";
	}

	/**
	 * {@code MML@aaa,bbb,ccc,ddd;} 形式でMMLを取得する
	 *   ddd: 歌パートがない場合は、dddは出力しない.
	 * @return　{@code MML@aaa,bbb,ccc,ddd;} 形式の文字列
	 */
	public String getMML() {
		String mml = "MML@"
				+ this.text[0] + ","
				+ this.text[1]+ ","
				+ this.text[2];
		if ( this.text[3].length() > 0 ) {
			mml += "," + this.text[3];
		}

		mml += ";";
		return mml;
	}

	public ComposeRank mmlRank() {
		return ComposeRank.mmlRank(this.text[0], this.text[1], this.text[2], this.text[3]);
	}

	/**
	 * 作曲ランクを表す文字列をフォーマットして作成します.
	 * 歌パートがある場合は、4つ分のパート表示を行います.
	 * @return フォーマット済みの文字列
	 */
	public String mmlRankFormat() {
		String str = "Rank "
				+ this.mmlRank().getRank() + " "
				+ "( " + this.text[0].length()
				+ ", " + this.text[1].length()
				+ ", " + this.text[2].length();
		if ( this.text[3].length() > 0 ) {
			str += ", " + this.text[3].length();
		}

		str += " )";
		return str;
	}
}

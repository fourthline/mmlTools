/*
 * Copyright (C) 2014-2021 たんらる
 */

package fourthline.mmlTools.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fourthline.mmlTools.ComposeRank;


/**
 * MML基本ツール.
 * {@code MML@aaa,bbb,ccc;} 形式を扱います。
 */
public final class MMLText {
	private static int DEFAULT_PART_NUM = 4;
	private String text[];

	/** 歌パートを除いて他のパートの文字数を増やすオプション (2021/11/18 MabiKR) */
	private boolean excludeSongPart = false;

	/* メロディパートが空のときに作曲スキルで怒られないようにするために入れる文字列 */
	private static String melody_empty_str = "";

	public static void setMelodyEmptyStr(String str) {
		MMLText.melody_empty_str = str;
	}

	public static String getMelodyEmptyStr() {
		return MMLText.melody_empty_str;
	}

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

	public MMLText setMMLText(String text, int partIndex) {
		if (partIndex < this.text.length) {
			this.text[partIndex] = text;
		} else {
			throw new IndexOutOfBoundsException();
		}

		return this;
	}

	public MMLText setMMLText(String melody, String chord1, String chord2, String songEx) {
		if (melody == null) {
			melody = "";
		}
		if (chord1 == null) {
			chord1 = "";
		}
		if (chord2 == null) {
			chord2 = "";
		}
		if (songEx == null) {
			songEx = "";
		}

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
		// メロディ or 歌 パートがどちらも空で楽譜の文字がある場合、メロディパートに1文字入れる.
		String melody_part = text[0];
		if (( melody_part.length() == 0) && ((this.text[1].length() != 0) || (this.text[2].length() != 0)) && !validSongPart() ) {
			melody_part = MMLText.melody_empty_str;
		}
		String mml = "MML@"
				+ melody_part + ","
				+ this.text[1]+ ","
				+ this.text[2];
		if (validSongPart()) {
			mml += "," + this.text[3];
		}

		mml += ";";
		return mml;
	}

	private boolean validSongPart() {
		return (!this.excludeSongPart) && (this.text[3].length() > 0);
	}

	public ComposeRank mmlRank() {
		if (!this.excludeSongPart) {
			return ComposeRank.mmlRank(this.text[0], this.text[1], this.text[2], this.text[3]);
		} else {
			return ComposeRank.mmlExcludeSongRank(this.text[0], this.text[1], this.text[2], "");
		}
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
		if (validSongPart()) {
			str += ", " + this.text[3].length();
		}

		str += " )";
		return str;
	}

	/**
	 * rankで分割する (楽譜集)
	 * @param rank
	 * @return
	 */
	public List<MMLText> splitMML(ComposeRank rank) {
		int cut[] = { rank.getMelody(), rank.getChord1(), rank.getChord2(), rank.getMelody() };
		StringBuffer sb[] = new StringBuffer[cut.length];
		for (int i = 0; i < sb.length; i++) {
			sb[i] = new StringBuffer(text[i]);
		}

		ArrayList<MMLText> mmlList = new ArrayList<>();
		while (Arrays.asList(sb).stream().anyMatch(s -> s.length() != 0)) {
			String parts[] = new String[sb.length];
			for (int i = 0; i < sb.length; i++) {
				int min = Math.min( sb[i].length(), cut[i] );
				parts[i] = sb[i].substring(0, min);
				sb[i].delete(0, min);
			}
			MMLText item = new MMLText().setMMLText(parts);
			item.setExcludeSongPart(this.isExcludeSongPart());
			mmlList.add( item );
		}
		return mmlList;
	}

	public boolean isExcludeSongPart() {
		return excludeSongPart;
	}

	public void setExcludeSongPart(boolean b) {
		this.excludeSongPart = b;
	}
}

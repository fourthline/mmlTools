/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * 作曲ランク.
 */
public class ComposeRank {
	final private static String RESOURCE_NAME = "rank";
	/** 作曲不可ランク */
	final private static ComposeRank RANK_0 = new ComposeRank(0, 0, 0, "-");

	final private static ArrayList<ComposeRank> rankList;
	static {
		rankList = new ArrayList<>();
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_NAME);
		for (String key : Collections.list(bundle.getKeys()) ) {
			String s[] = bundle.getString(key).split(",");
			if (s.length != 4) {
				continue;
			}
			int melody = Integer.parseInt(s[0].trim());
			int chord1 = Integer.parseInt(s[1].trim());
			int chord2 = Integer.parseInt(s[2].trim());
			rankList.add( new ComposeRank(melody, chord1, chord2, s[3].trim()) );
		}

		rankList.sort((rank1, rank2) -> {
			return (rank1.melody + rank1.chord1 + rank1.chord2)
					- (rank2.melody + rank2.chord1 + rank2.chord2);
		});
	}

	private int melody;
	private int chord1;
	private int chord2;
	private String rank = "-";

	/**
	 * MMLのランクを計算します。
	 * @param melody MMLのメロディ
	 * @param chord1 MMLの和音1
	 * @param chord2 MMLの和音2
	 * @param songEx MMLの歌
	 * @return ランクの文字
	 */
	static public String mmlRank(String melody, String chord1, String chord2, String songEx) {
		for (ComposeRank rank : rankList) {
			if (rank.compare(melody, chord1, chord2, songEx))
				return rank.getRank();
		}

		return RANK_0.getRank();
	}

	/**
	 * for Test, package private
	 * @param melody
	 * @param chord1
	 * @param chord2
	 * @return
	 */
	static ComposeRank createComposeRank(int melody, int chord1, int chord2) {
		return new ComposeRank(melody, chord1, chord2);
	}

	private ComposeRank() {}

	private ComposeRank(int melody, int chord1, int chord2) {
		this.melody = melody;
		this.chord1 = chord1;
		this.chord2 = chord2;
	}

	private ComposeRank(int melody, int chord1, int chord2, String rank) {
		this(melody, chord1, chord2);
		this.rank = rank;
	}

	public boolean compare(String melody, String chord1, String chord2, String songEx) {
		if ( (melody.length() <= this.melody) &&
				(chord1.length() <= this.chord1) &&
				(chord2.length() <= this.chord2) &&
				(songEx.length() <= this.melody))
			return true;

		return false;
	}

	public int getMelody() {
		return melody;
	}

	public int getChord1() {
		return chord1;
	}

	public int getChord2() {
		return chord2;
	}

	public String getRank() {
		return rank;
	}

	public String toString() {
		String result = "Rank " + rank + " ( " + melody + ", " + chord1 + ", " + chord2 + " )";

		return result;
	}
}

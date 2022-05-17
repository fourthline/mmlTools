/*
 * Copyright (C) 2013-2021 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import jp.fourthline.mmlTools.core.ResourceLoader;

/**
 * 作曲ランク.
 */
public final class ComposeRank {
	final private static String RESOURCE_NAME = "rank";
	/** 作曲不可ランク */
	final private static ComposeRank RANK_0 = new ComposeRank(0, 0, 0, "-");

	final private static ArrayList<ComposeRank> rankList = new ArrayList<>();
	final private static ArrayList<ComposeRank> excludeSongRankList = new ArrayList<>();
	static {
		loadRankResource(rankList, RESOURCE_NAME);
		rankList.forEach(t -> {
			int d = t.getMelody() / 3;
			ComposeRank r = new ComposeRank(t.getMelody()+d, t.getChord1()+d, t.getChord2()+d, t.getRank()+'`');
			excludeSongRankList.add(r);
		});
	}

	static private void loadRankResource(List<ComposeRank> list, String name) {
		ResourceBundle bundle = ResourceBundle.getBundle(name, new ResourceLoader());
		for (String key : Collections.list(bundle.getKeys()) ) {
			String[] s = bundle.getString(key).split(",");
			if (s.length != 4) {
				continue;
			}
			int melody = Integer.parseInt(s[0].trim());
			int chord1 = Integer.parseInt(s[1].trim());
			int chord2 = Integer.parseInt(s[2].trim());
			list.add( new ComposeRank(melody, chord1, chord2, s[3].trim()) );
		}

		list.sort((rank1, rank2) -> {
			return (rank1.melody + rank1.chord1 + rank1.chord2)
					- (rank2.melody + rank2.chord1 + rank2.chord2);
		});
	}

	public static ComposeRank getTopRank() {
		return rankList.get(rankList.size()-1);
	}

	public static ComposeRank getTopExcludeSongRank() {
		return excludeSongRankList.get(excludeSongRankList.size()-1);
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
	public static ComposeRank mmlRank(String melody, String chord1, String chord2, String songEx) {
		return mmlRank( melody.length(), chord1.length(), chord2.length(), songEx.length() );
	}

	public static ComposeRank mmlRank(int melody, int chord1, int chord2, int songEx) {
		return mmlRank(rankList, melody, chord1, chord2, songEx);
	}

	public static ComposeRank mmlExcludeSongRank(String melody, String chord1, String chord2, String songEx) {
		return mmlExcludeSongRank( melody.length(), chord1.length(), chord2.length(), songEx.length() );
	}

	public static ComposeRank mmlExcludeSongRank(int melody, int chord1, int chord2, int songEx) {
		return mmlRank(excludeSongRankList, melody, chord1, chord2, songEx);
	}

	private static ComposeRank mmlRank(List<ComposeRank> list, int melody, int chord1, int chord2, int songEx) {
		for (ComposeRank rank : list) {
			if (rank.compare(melody, chord1, chord2, songEx))
				return rank;
		}

		return RANK_0;
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

	public boolean compare(int melody, int chord1, int chord2, int songEx) {
		return (melody <= this.melody) &&
				(chord1 <= this.chord1) &&
				(chord2 <= this.chord2) &&
				(songEx <= this.melody);
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

	public boolean canCompose() {
		return (this != RANK_0);
	}

	@Override
	public String toString() {
		String result = "Rank " + rank + " ( " + melody + ", " + chord1 + ", " + chord2 + " )";
		return result;
	}
}

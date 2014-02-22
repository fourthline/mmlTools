/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools;

/**
 * 作曲ランク
 * @author たんらる
 */
public class ComposeRank {
	/** 練習ランク  */
	final public static ComposeRank RANK_t = new ComposeRank( 200, 100,   0, "練習");
	final public static ComposeRank RANK_F = new ComposeRank( 400, 200, 100, "F");
	final public static ComposeRank RANK_E = new ComposeRank( 500, 200, 100, "E");
	final public static ComposeRank RANK_D = new ComposeRank( 600, 250, 150, "D");
	final public static ComposeRank RANK_C = new ComposeRank( 650, 250, 200, "C");
	final public static ComposeRank RANK_B = new ComposeRank( 700, 300, 200, "B");
	final public static ComposeRank RANK_A = new ComposeRank( 750, 300, 200, "A");
	final public static ComposeRank RANK_9 = new ComposeRank( 800, 350, 200, "9");
	final public static ComposeRank RANK_8 = new ComposeRank( 850, 400, 200, "8");
	final public static ComposeRank RANK_7 = new ComposeRank( 900, 400, 200, "7");
	final public static ComposeRank RANK_6 = new ComposeRank( 950, 450, 200, "6");
	final public static ComposeRank RANK_5 = new ComposeRank(1000, 500, 250, "5");
	final public static ComposeRank RANK_4 = new ComposeRank(1050, 550, 300, "4");
	final public static ComposeRank RANK_3 = new ComposeRank(1100, 600, 350, "3");
	final public static ComposeRank RANK_2 = new ComposeRank(1150, 700, 400, "2");
	final public static ComposeRank RANK_1 = new ComposeRank(1200, 800, 500, "1");
	/** 作曲不可ランク */
	final public static ComposeRank RANK_0 = new ComposeRank(   0,   0,   0, "-");

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
		ComposeRank rankList[] = {
				RANK_t,
				RANK_F,
				RANK_E,
				RANK_D,
				RANK_C,
				RANK_B,
				RANK_A,
				RANK_9,
				RANK_8,
				RANK_7,
				RANK_6,
				RANK_5,
				RANK_4,
				RANK_3,
				RANK_2,
				RANK_1
		};

		for (int i = 0; i < rankList.length; i++) {
			if (rankList[i].compare(melody, chord1, chord2, songEx))
				return rankList[i].getRank();
		}

		return RANK_0.getRank();
	}


	@SuppressWarnings("unused")
	private ComposeRank() {}

	public ComposeRank(int melody, int chord1, int chord2) {
		this.melody = melody;
		this.chord1 = chord1;
		this.chord2 = chord2;
	}

	public ComposeRank(int melody, int chord1, int chord2, String rank) {
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

/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.core;

import java.util.List;

import fourthline.mmlTools.ComposeRank;
import fourthline.mmlTools.UndefinedTickException;



/**
 * MML基本ツール.
 * {@code MML@aaa,bbb,ccc;} 形式を扱います。
 * @author たんらる
 */
public class MMLTools {

	protected String mml_melody = "";
	protected String mml_chord1 = "";
	protected String mml_chord2 = "";

	protected MelodyParser playParser;
	protected MelodyParser melodyParser;
	protected MelodyParser chord1Parser;
	protected MelodyParser chord2Parser;

	private double play_length = -1.0;
	private double mabinogi_length = -1.0;

	protected void clearTimeCalc() {
		play_length = -1.0;
		mabinogi_length = -1.0;
	}

	/**　
	 * MMLの演奏時間を取得する.
	 * @return 時間（秒）
	 * @throws UndefinedTickException
	 */
	public double getPlayTime() throws UndefinedTickException  {
		if (play_length < 0.0) {
			play_length = playParser.getPlayLengthByTempoList();
		}

		return play_length;
	}

	/**
	 * マビノギでの演奏スキル時間を取得する.
	 * <p>演奏時間  － 0.6秒 ＜ スキル時間 であれば、切れずに演奏される</p>
	 * @return 時間（秒）
	 * @throws UndefinedTickException
	 */
	public double getMabinogiTime() throws UndefinedTickException {
		if (mabinogi_length < 0.0) {
			double max = 0.0;
			double timeList[] = {
					melodyParser.getPlayLengthByTempoList(),
					chord1Parser.getPlayLengthByTempoList(),
					chord2Parser.getPlayLengthByTempoList()
			};

			for (int i = 0; i < timeList.length; i++) { 
				max = Math.max(max, timeList[i]);
			}

			mabinogi_length = max;
		}

		return mabinogi_length;
	}

	/**
	 * マビノギ演奏でのMML解析
	 * @throws UndefinedTickException
	 */
	protected void parseMMLforMabinogi() throws UndefinedTickException {
		melodyParser = new MelodyParser(mml_melody);
		chord1Parser = new MelodyParser(mml_chord1, "4", melodyParser.getTempo());
		chord2Parser = new MelodyParser(mml_chord2, "4", chord1Parser.getTempo());
	}



	/**
	 * 演奏時間の解析
	 * @param drumMode
	 * @throws UndefinedTickException
	 */
	protected void parsePlayMode(boolean drumMode) throws UndefinedTickException {
		if (drumMode) {
			String s = mml_melody
					+ "T"+melodyParser.getTempo() + mml_chord1
					+ "T"+chord1Parser.getTempo() + mml_chord2;
			playParser = new MelodyParser(s);
		} else {
			playParser = new MelodyParser(mml_melody);
			int initTempo = playParser.getTempoList().get(0);
			playParser.mergeParser(new MelodyParser(mml_chord1, "4", initTempo));
			playParser.mergeParser(new MelodyParser(mml_chord2, "4", initTempo));
		}
	}


	public MMLTools(String mml) {
		if (!mml.startsWith("MML@")) {
			mml = "";
			return;
		}

		int start = 4;
		int end = mml.indexOf(";");
		if (end <= 0)
			end = mml.length();

		mml = mml.substring(start, end);

		String parts[] = mml.split(",");
		if (parts.length > 0)
			mml_melody = parts[0];
		if (parts.length > 1)
			mml_chord1 = parts[1];
		if (parts.length > 2)
			mml_chord2 = parts[2];
	}

	public MMLTools(String melody, String chord1, String chord2) {
		mml_melody = melody;
		mml_chord1 = chord1;
		mml_chord2 = chord2;
	}

	public String getMelody() {
		return mml_melody;
	}

	public String getChord1() {
		return mml_chord1;
	}

	public String getChord2() {
		return mml_chord2;
	}

	/**
	 * {@code MML@aaa,bbb,ccc;} 形式でMMLを取得する
	 * @return　{@code MML@aaa,bbb,ccc;} 形式の文字列
	 */
	public String getMML() {
		String mml = "MML@"
				+ mml_melody + ","
				+ mml_chord1 + ","
				+ mml_chord2 + ";";

		return mml;
	}

	public String mmlRank() {
		return ComposeRank.mmlRank(mml_melody, mml_chord1, mml_chord2);
	}

	public String mmlRankFormat() {
		String str = "Rank "
				+ this.mmlRank() + " "
				+ "( " + this.mml_melody.length()
				+ ", " + this.mml_chord1.length()
				+ ", " + this.mml_chord2.length()
				+ " )";

		return str;
	}



	private String toStringFormatWarnList(List<Integer> list) {
		String s = "";
		for (int i = 0; i < list.size(); i++) {
			s += list.get(i)+", ";
		}

		return s;
	}

	public String getMMLWarning() {
		String s = toStringFormatWarnList(melodyParser.getWarnIndex());
		s += toStringFormatWarnList(chord1Parser.getWarnIndex());
		s += toStringFormatWarnList(chord2Parser.getWarnIndex());

		return s;
	}

	public boolean checkPitch(int min, int max) throws UndefinedTickException {
		parseMMLforMabinogi();
		parsePlayMode(false);

		if (
				melodyParser.checkPitch(min, max) &&
				chord1Parser.checkPitch(min, max) && 
				chord1Parser.checkPitch(min, max) ) {
			return true;
		} else {
			return false;
		}
	}

}

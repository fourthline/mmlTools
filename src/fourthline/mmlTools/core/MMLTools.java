/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools.core;

import java.util.List;



/**
 * MMLツール（旧式）
 */
public class MMLTools {

	protected String mml_melody = "";
	protected String mml_chord1 = "";
	protected String mml_chord2 = "";
	protected String mml_songEx = "";

	protected MelodyParser playParser;
	protected MelodyParser melodyParser;
	protected MelodyParser chord1Parser;
	protected MelodyParser chord2Parser;
	protected MelodyParser songExParser;

	private double play_length = -1.0;
	private double mabinogi_length = -1.0;

	protected void clearTimeCalc() {
		play_length = -1.0;
		mabinogi_length = -1.0;
	}

	/**　
	 * MMLの演奏時間を取得する.
	 * @return 時間（秒）
	 * @throws UndefinedTickException 変換に失敗した
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
	 * @throws UndefinedTickException 変換に失敗した
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
	 * @throws UndefinedTickException 変換に失敗した
	 */
	public void parseMMLforMabinogi() throws UndefinedTickException {
		melodyParser = new MelodyParser(mml_melody);
		chord1Parser = new MelodyParser(mml_chord1, "4", melodyParser.getTempo());
		chord2Parser = new MelodyParser(mml_chord2, "4", chord1Parser.getTempo());
		// TODO: 歌パートの扱いってどうなってるんだろ？
	}

	/**
	 * 演奏時間の解析
	 * @param drumMode 打楽器モードの場合 trueを指定する.
	 * @throws UndefinedTickException 変換に失敗した
	 */
	public void parsePlayMode(boolean drumMode) throws UndefinedTickException {
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

	public MMLTools() {}

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
		if (parts.length > 3)
			mml_songEx = parts[3];
	}

	public MMLTools(String melody, String chord1, String chord2) {
		this(melody, chord1, chord2, "");
	}

	public MMLTools(String melody, String chord1, String chord2, String songEx) {
		mml_melody = melody;
		mml_chord1 = chord1;
		mml_chord2 = chord2;
		mml_songEx = songEx;
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

	public String getSongEx() {
		return mml_songEx;
	}

	protected boolean hasSongPart() {
		if ( (mml_songEx != null) && (mml_songEx.length() > 0) ) {
			return true;
		}

		return false;
	}

	/**
	 * {@code MML@aaa,bbb,ccc,ddd;} 形式でMMLを取得する
	 *   ddd: 歌パートがない場合は、dddは出力しない.
	 * @return　{@code MML@aaa,bbb,ccc,ddd;} 形式の文字列
	 */
	public String getMML() {
		String mml = "MML@"
				+ mml_melody + ","
				+ mml_chord1 + ","
				+ mml_chord2;
		if ( hasSongPart() ) {
			mml += "," + mml_songEx;
		}

		mml += ";";
		return mml;
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
				chord2Parser.checkPitch(min, max) ) {
			return true;
		} else {
			return false;
		}
	}
}

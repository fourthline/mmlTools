/*
 * Copyright (C) 2013 たんらる
 */

package jp.fourthline.mmlTools;

import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLTokenizer;
import jp.fourthline.mmlTools.core.MMLTools;
import jp.fourthline.mmlTools.core.MelodyParser;
import jp.fourthline.mmlTools.core.UndefinedTickException;

/**
 * ドラムの和音分割
 * 2011/12/15からマビさんの仕様が変わりました. (和音1、和音2が使えません)
 * @author たんらる
 */
public class DrumTools extends MMLTools {	

	private boolean makiOption = false;

	public DrumTools(String mml) {
		super(mml);
	}

	public double getOverSec() throws UndefinedTickException {
		return (getMabinogiTime() - getPlayTime());
	}

	public void setMakiOption(boolean value) {
		makiOption = value;
	}


	/**
	 * メロディーパートを指定のランクの文字数でカットする（打楽器分割用）
	 * @param cutRank
	 */
	private void cutMMLDrum(ComposeRank cutRank) {
		MMLCutter cutter = new MMLCutter(mml_melody);
		mml_melody = cutter.cut(cutRank.getMelody(), makiOption);
		mml_chord1 = cutter.cut(cutRank.getChord1(), makiOption);	
		mml_chord2 = cutter.cut(cutRank.getChord2()+1000, false);
	}


	/**
	 * ドラムの和音分割で、和音２を曲の長さ分以上になるように休符を挿入する（打楽器分割用）
	 * ほしいもの：　編集後のChord2とOversec
	 * @throws UndefinedTickException
	 */
	private void drumChord2InsertPlaylength(boolean minText) throws UndefinedTickException {
		// total length - chord2 timing value
		double total_length = getPlayTime(); // 分割前の演奏時間を計算しておく
		double pre_chord2_length = chord2Parser.getPlayLengthByTempoList();
		double sub_length = total_length - pre_chord2_length;

		// add R to chord2
		double beats = (sub_length*96.0) / MMLTicks.getTick("1.");

		beats *= (32.0/60.0); // tempo / 60
		StringBuffer sb = new StringBuffer(mml_chord2);
		if (chord2Parser.getTempo() != 32) {
			sb.append("t32");
		}
		if (!chord2Parser.getMmlL().equals("1.")) {
			sb.append("l1.");
		}

		for (int i = 1; i < beats; i++) {
			sb.append('r');
		}

		if (!minText) { // 終了調整モード
			sb.append(tailLength(beats));
		} else {    // 文字数最小モード
			sb.append('r');
		}

		System.out.println("beats: "+beats);

		mml_chord2 = sb.toString();
		chord2Parser = new MelodyParser(mml_chord2, "4", chord1Parser.getTempo());

	}

	/**
	 * OverTime分を少なくするコード
	 * @param time
	 * @return
	 */
	private String tailLength(double time) {
		String tickCandidate[] = {
				//				"1", "2", "3", "4", "5", "6", "7", "8", "12", "16", "32"
				"1", "2", "4", "8", "12", "16", "32"
		};
		double overTime = time - Math.floor(time);
		String result = "";

		try {
			for (int i = 0; i < tickCandidate.length; i++) {
				double overFollow = MMLTicks.getTick(tickCandidate[i])/51.20/11.25;
				if (overFollow < overTime) {
					overTime -= overFollow;
					result += "r" + tickCandidate[i];
				}
			} // for
		} catch (UndefinedTickException e) {
			return "<Internal_ERROR>";
		}

		return result;
	}


	public String makeForMabiMML(ComposeRank cutRank) throws UndefinedTickException {
		return makeForMabiMML(cutRank, true);
	}


	/**
	 * 指定ランクで、ドラムのメロディパートを和音に分割する
	 * @param cutRank 分割するランク
	 * @param minText 終端モード　falseの場合、時間最適。trueの場合、文字数最小。
	 * @return 分割後のMML
	 * @throws UndefinedTickException
	 */
	public String makeForMabiMML(ComposeRank cutRank, boolean minText) throws UndefinedTickException {
		cutMMLDrum(cutRank);

		parseMMLforMabinogi();
		parsePlayMode(true);

		if (mml_chord1 != "") {
			// 分割した場合、MMLを編集するため、再計算されるようにする
			clearTimeCalc();
			drumChord2InsertPlaylength(minText);
		}

		return getMML();
	}

	/**
	 * 打楽器音量調節テーブル
	 * @param vol
	 * @return
	 */
	int convDrumVol(int vol) {
		int table[] = {
				0, 1, 1, 2, 3, 4, 4, 5, 6, 7, 7, 8, 9, 10, 10, 11 
		};

		if ( (vol < 0) || (vol >= table.length) ) {
			return 0;
		}

		return table[vol];
	}

	/**
	 * ボリュームを　2/3　にする。
	 * 高速に処理するため、独自実装。
	 * 分割前に実行すること。
	 * @throws UndefinedTickException
	 */
	public String disDrumVolumn() throws UndefinedTickException {
		MMLTokenizer mt = new MMLTokenizer(mml_melody);
		StringBuffer sb = new StringBuffer(mml_melody.length());

		while (mt.hasNext()) {
			String item = mt.next();
			if (item.startsWith("v") || item.startsWith("V")) {
				String vol = item.substring(1);
				int volInt = Integer.parseInt(vol);
				volInt = convDrumVol(volInt);

				sb.append(item.charAt(0));
				sb.append(volInt);
			} else {
				sb.append(item);
			}
		}

		String result = sb.toString();
		mml_melody = result;
		return result;
	}

}


/**
 * MMLを分割する（打楽器分割用）
 *
 */
class MMLCutter {
	private String mml_src;

	public MMLCutter(String mml) {
		mml_src = mml;
	}

	private int alignMaki(int cut) {
		MMLTokenizer tokenizer = new MMLTokenizer(mml_src);

		while (cut >= 0) {
			int backIndex = tokenizer.backSearchToken(cut);
			if (backIndex < 0) {
				return -1;
			} else if ( MMLTokenizer.isNote( mml_src.charAt(backIndex)) ) {
				break;
			} else {
				cut = backIndex;
			}
		}

		return cut;
	}

	public String cut(int cut, boolean maki) {
		if (mml_src.length() <= cut) {
			String result = mml_src;
			mml_src = "";

			return result;
		}

		while (!MMLTokenizer.isToken(mml_src.charAt(cut))) {
			cut--;
		}

		if (maki) {
			cut = alignMaki(cut);
		}

		String result = mml_src.substring(0, cut);
		String strL = "";

		if (maki) {
			try {
				MelodyParser parser = new MelodyParser(result);
				String lastL = parser.getMmlL();
				if ( !lastL.equals("4") ) {  // not if 4 then, next part start L token
					strL = "l" + lastL;
				}
			} catch (UndefinedTickException e) {}
		}

		mml_src = mml_src.substring(cut);
		if ( (mml_src.charAt(0) != 'l') && (mml_src.charAt(0) != 'L') ) {
			mml_src = strL + mml_src;
		}

		return result;
	}
}

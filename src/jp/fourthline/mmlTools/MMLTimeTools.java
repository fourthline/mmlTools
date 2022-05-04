/*
 * Copyright (C) 2013 たんらる
 */

package jp.fourthline.mmlTools;


import java.util.Map;

import jp.fourthline.mmlTools.core.MMLTools;
import jp.fourthline.mmlTools.core.UndefinedTickException;


/**
 * マビノギでの演奏時間
 * @author たんらる
 *
 */
public class MMLTimeTools extends MMLTools {
	public final static int CHECK_RANGE_N = 5;

	/**
	 * 音域チェックテーブル
	 * リュート
	 * O1e～O7e  　(16 @ 88)
	 * フルート
	 * O4c～O6b  (48 @ 83)
	 * ホイッスル
	 * O5c～O7e  (60 @ 88)
	 * シャリュモー
	 * O2c～O4b  (24 @ 59)
	 * チューバ
	 * O1c～O4b  (12 @ 59)
	 */
	final private int minTable[] = {
			16, 48, 60, 24, 12
	};
	final private int maxTable[] = {
			88, 83, 88, 59, 59
	};

	public MMLTimeTools(String mml, boolean drumMode) throws UndefinedTickException {
		super(mml);

		parseMMLforMabinogi();
		parsePlayMode(drumMode);
	}


	public boolean[] checkRange() {
		jp.fourthline.mmlTools.core.MelodyParser parser[] = {
				melodyParser, chord1Parser, chord2Parser
		};

		boolean result[] = new boolean[minTable.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = true;
		}

		// 楽器ごとの音域チェック
		for (int tableIndex = 0; tableIndex < minTable.length; tableIndex++) {
			// パートごとの音域チェック
			for (int i = 0; i < parser.length; i++) {
				int min = parser[i].getMinNote();
				int max = parser[i].getMaxNote();
				if ( (min < 0) || (max < 0) ) {
					continue;
				}

				if ( (minTable[tableIndex] > min) ||
						(maxTable[tableIndex] < max) ) {
					result[tableIndex] = false;
					break;
				}
			}
		}

		return result;
	}


	public Map<Integer, Integer> getPlayList() throws UndefinedTickException {
		return playParser.getTempoList();
	}

	public Map<Integer, Integer> getMelodyList() throws UndefinedTickException {
		return melodyParser.getTempoList();
	}

	public Map<Integer, Integer> getChord1List() throws UndefinedTickException {
		return chord1Parser.getTempoList();
	}

	public Map<Integer, Integer> getChord2List() throws UndefinedTickException {
		return chord2Parser.getTempoList();
	}
}

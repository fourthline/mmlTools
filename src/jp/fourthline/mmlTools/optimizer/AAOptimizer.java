/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.core.MMLTokenizer;


/**
 * Archeage向けMMLへの変換
 */
public final class AAOptimizer implements MMLStringOptimizer.Optimizer {
	private final int INIT_AA_VOL = 100;
	private StringBuilder sb = new StringBuilder();
	private int vol = INIT_AA_VOL;

	private int convertVol(int v) {
		int r = v * 8;
		if (r > 127)
			r = 127;
		if (r < 0)
			r = 0;
		return r;
	}

	private boolean volOctPattern(String token) {
		boolean ret = false;
		char firstC = Character.toLowerCase( token.charAt(0) );
		String[] s = MMLTokenizer.noteNames(token);

		if (MMLTokenizer.isNote(firstC) && (vol == INIT_AA_VOL)) {
			vol = convertVol(MMLNoteEvent.INIT_VOL);
			sb.append("V" + vol);
		} else if (s[0].equals("v")) {
			vol = convertVol(Integer.parseInt(s[1]));
			sb.append("V" + vol);
			ret = true;
		} else if (s[0].equals("o")) {
			int oct = Integer.parseInt(s[1]) + 1;
			sb.append("o"+oct);
			ret = true;
		}
		if (token.equals(",")) {
			vol = INIT_AA_VOL;
		}

		return ret;
	}

	private String lStr = null;
	private String prevToken = "";
	private boolean tieMode = false;
	private boolean lStrPattern(String token) {
		boolean ret = false;
		char firstC = Character.toLowerCase( token.charAt(0) );
		String[] s = MMLTokenizer.noteNames(token);
		if (token.startsWith("l")) {
			lStr = token.substring(1);
			ret = true;
		} else if (token.equals("&")) {
			tieMode = true;
		} else if (MMLTokenizer.isNote(firstC)) {
			if (tieMode && (lStr != null)) {
				if (!prevToken.equals("&")) {
					tieMode = false;
					if (lStr != null) {
						sb.append("l"+lStr);
						sb.append(token);
						lStr = null;
						ret = true;
					}
				} else if (s[1].equals("") || s[1].equals(".")) {
					sb.append(s[0]).append(lStr).append(s[1]);
					ret = true;
				}
			} else if (!tieMode && (lStr != null)) {
				sb.append("l"+lStr);
				lStr = null;
			}
		}

		if (token.equals(",") && (lStr != null)) {
			tieMode = false;
			sb.append("l"+lStr);
			lStr = null;
		}

		prevToken = token;
		return ret;
	}

	@Override
	public void nextToken(String token) {
		if (volOctPattern(token)) {
		} else if (lStrPattern(token)) {
		} else {
			sb.append(token);
		}
	}

	@Override
	public String getMinString() {
		return sb.toString();
	}
}

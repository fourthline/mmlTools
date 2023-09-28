/*
 * Copyright (C) 2013 たんらる
 */

package jp.fourthline.mmlTools;

import jp.fourthline.mmlTools.core.MMLTokenizer;
import jp.fourthline.mmlTools.core.MMLException;


/**
 * 通常のMMLからシロフォン用のMMLに変換する
 * @author たんらる
 *
 */
public class XylophoneMML {
	/**
	 * falseのとき、通常→シロフォン変換
	 * trueのとき、シロフォン→通常変換
	 */
	private boolean backMode = false;


	/**
	 * @return 変換後のMML
	 */
	public String conv(String mml) throws MMLException {
		return this.conv(mml, false);
	}

	/**
	 * @return 変換後のMML
	 */
	public String conv(String mml, boolean backMode) throws MMLException {
		MMLTokenizer mt = new MMLTokenizer(mml);
		this.backMode = backMode;

		StringBuilder sb = new StringBuilder();

		boolean toHighOct = false; // a, a+, b-, b, c- の領域時にtrue

		while (mt.hasNext()) {
			String token = mt.next();

			System.out.print(" ["+token+"] ");

			boolean nextTo = false;
			if ( MMLTokenizer.isNote(token.charAt(0)) ) {
				System.out.print("*");
				if (token.charAt(0) == 'n' || token.charAt(0) == 'N') {
					token = absConv(token);
				} else {
					nextTo = isHighPos(token);

					if (!backMode) {
						if ( (toHighOct) && (!nextTo) ) {
							sb.append("<");
						}
						if ( (!toHighOct) && (nextTo) ) {
							sb.append(">");
						}
					} else {
						if ( (toHighOct) && (!nextTo) ) {
							sb.append(">");
						}
						if ( (!toHighOct) && (nextTo) ) {
							sb.append("<");
						}
					}

					toHighOct = nextTo;
				}
			}

			sb.append(token);
		}

		String result = sb.toString();
		result = result.replaceAll("><", "");
		result = result.replaceAll("<>", "");

		return result;
	}



	/**
	 * n, N 絶対値の変換
	 * @param token
	 * @return
	 */
	private String absConv(String token) {
		int note = Integer.parseInt( token.substring(1) );
		int tNote = note%12;

		switch (tNote) {
		case 9: // A
		case 10: // A#
		case 11: // B
			if (!backMode) {
				note += 12;
			} else {
				note -= 12;
			}
		}

		return ""+token.charAt(0)+note;
	}

	private boolean isHighPos(String token) {
		boolean result = false;
		token = MMLTokenizer.noteName(token);
		token = token.toLowerCase();

		if ( (token.equals("a") ) ||
				(token.equals("a+") ) ||
				(token.equals("a#") ) ||
				(token.equals("b-") ) ||
				(token.equals("b") ) ||
				(token.equals("c-") ) ) {
			System.out.print("!");
			result = true;
		}

		return result;
	}
}

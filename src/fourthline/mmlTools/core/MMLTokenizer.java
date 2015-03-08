/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools.core;

import java.util.Iterator;


/**
 * MML命令の分割
 * @author たんらる
 */
public final class MMLTokenizer implements Iterator<String> {
	private static final String noteString = "abcdefgABCDEFGnNrR";
	private static final String tokenString = noteString + "tToOlLvV<>&";
	private final String mml_src;
	int startIndex = 0;
	int endIndex = 0;

	public MMLTokenizer(String src) {
		mml_src = src;
	}

	@Override
	public boolean hasNext() {
		if (endIndex < mml_src.length())
			return true;

		return false;
	}

	@Override
	public String next() {
		startIndex = endIndex;
		endIndex = searchToken(endIndex+1);

		return mml_src.substring(startIndex, endIndex);
	}

	@Override
	public void remove() {
		startIndex = 0;
		endIndex = 0;
	}

	/**
	 * 解析位置の取得
	 * @return 解析位置
	 */
	public int[] getIndex() {
		return new int[] { startIndex, endIndex };
	}

	public static boolean isToken(char ch) {
		if (tokenString.indexOf(ch) < 0)
			return false;

		return true;
	}

	public static boolean isNote(char ch) {
		if (noteString.indexOf(ch) < 0)
			return false;

		return true;
	}

	public static String noteName(String token) {
		String noteName = ""+token.charAt(0);

		if (token.length() > 1) {
			char note2 = token.charAt(1);

			if ( (note2 == '+') || (note2 == '-') || (note2 == '#') )
				noteName += note2;
		}

		return noteName;
	}

	public static String[] noteNames(String token) {
		char firstC = token.charAt(0);
		String noteName = "" + firstC;
		String noteLength = "";
		if (token.length() > 1) {
			char secondC = token.charAt(1);
			if (!Character.isDigit(secondC)) {
				noteName += secondC;
				noteLength = token.substring(2);
			} else {
				noteLength = token.substring(1);
			}
		}

		return new String[] { noteName, noteLength };
	}

	public int searchToken(int startIndex) {
		int length = mml_src.length();

		int index;
		for (index = startIndex; index < length; index++) {
			char ch = mml_src.charAt(index);
			if (isToken(ch))
				break;
		}

		return index;
	}

	public int backSearchToken(int startIndex) {
		int index;
		for (index = startIndex-1; index >=0 ; index--) {
			char ch = mml_src.charAt(index);
			if (isToken(ch))
				break;
		}

		return index;
	}
}

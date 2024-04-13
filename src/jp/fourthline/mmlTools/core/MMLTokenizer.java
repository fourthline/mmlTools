/*
 * Copyright (C) 2013-2024 たんらる
 */

package jp.fourthline.mmlTools.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * MML命令の分割
 * @author たんらる
 */
public final class MMLTokenizer implements Iterator<String> {
	private static final String noteString = "abcdefgABCDEFGnNrR";
	private static final String tokenString = noteString + "tToOlLvV<>&,";
	private final String mml_src;
	private final int mml_length;
	private final char[] mml_charArray;
	private int startIndex = 0;
	private int endIndex = 0;

	public MMLTokenizer(String src) {
		mml_src = src;
		mml_length = src.length();
		mml_charArray = src.toCharArray();
	}

	@Override
	public boolean hasNext() {
		return endIndex < mml_length;
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

	public int getStart() {
		return startIndex;
	}

	public int getEnd() {
		return endIndex;
	}

	/**
	 * 解析位置の取得
	 * @return 解析位置
	 */
	public int[] getIndex() {
		return new int[] { startIndex, endIndex };
	}

	public static boolean isToken(char ch) {
		return tokenString.indexOf(ch) >= 0;
	}

	public static boolean isNote(char ch) {
		return noteString.indexOf(ch) >= 0;
	}

	public static String noteName(String token) {
		String noteName = token.substring(0, 1);

		if (token.length() > 1) {
			char note2 = token.charAt(1);
			if ( (note2 == '+') || (note2 == '-') || (note2 == '#') )
				noteName += note2;
		}

		return noteName;
	}

	private static final Map<String, String[]> tokenCache = Collections.synchronizedMap(new HashMap<>());
	public static String[] noteNames(String token) {
		var t = tokenCache.get(token);
		if (t != null) {
			return t;
		}

		String noteName = noteName(token);
		String noteLength = token.substring(noteName.length());

		var newV = new String[] { noteName, noteLength };
		tokenCache.put(token, newV);
		return newV;
	}

	public static boolean isLenOnly(String str) {
		if (str.endsWith(".")) {
			str = str.substring(0, str.length() - 1);
		}
		return str.matches("[0-9]+");
	}

	public int searchToken(int startIndex) {
		int index;
		for (index = startIndex; index < mml_length; index++) {
			char ch = mml_charArray[index];
			if (isToken(ch))
				break;
		}

		return index;
	}

	public int backSearchToken(int startIndex) {
		int index;
		for (index = startIndex-1; index >=0 ; index--) {
			char ch = mml_charArray[index];
			if (isToken(ch))
				break;
		}

		return index;
	}
}

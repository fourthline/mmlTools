/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.core;

import java.util.Iterator;


/**
 * MML命令の分割
 * @author たんらる
 */
public class MMLTokenizer implements Iterator<String> {
	static private String noteString = "abcdefgABCDEFGnNrR";
	static private String tokenString = noteString + "tToOlLvV<>&";
	private String mml_src;
	private int index = 0;

	public MMLTokenizer(String src) {
		mml_src = src;
	}

	@Override
	public boolean hasNext() {
		if (index < mml_src.length())
			return true;

		return false;
	}

	@Override
	public String next() {
		int startIndex = index;
		int endIndex = searchToken(index+1);
		index = endIndex;

		return mml_src.substring(startIndex, endIndex);
	}

	@Override
	public void remove() {
		index = 0;
	}

	/**
	 * 解析位置の取得
	 * @return 解析位置
	 */
	public int getIndex() {
		return index;
	}

	static public boolean isToken(char ch) {
		if (tokenString.indexOf(ch) < 0)
			return false;

		return true;
	}

	static public boolean isNote(char ch) {
		if (noteString.indexOf(ch) < 0)
			return false;

		return true;
	}

	static public String noteName(String token) {
		String noteName = ""+token.charAt(0);
		char note2 = ' ';

		if (token.length() > 1) {
			note2 = token.charAt(1);

			if ( (note2 == '+') || (note2 == '-') || (note2 == '#') )
				noteName += note2;
		}

		return noteName;
	}

	static public String[] noteNames(String token) {
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

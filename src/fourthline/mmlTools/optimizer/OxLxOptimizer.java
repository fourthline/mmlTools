/*
 * Copyright (C) 2015-2017 たんらる
 */

package fourthline.mmlTools.optimizer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fourthline.mmlTools.core.MMLTokenizer;


/**
 * Ox, Lxを使用した最適化.
 */
public final class OxLxOptimizer implements MMLStringOptimizer.Optimizer {

	/**
	 * Lの文字列と、生成中文字列のBuilder.
	 */
	private final Map<String, StringBuilder> map = new HashMap<>();

	public OxLxOptimizer() {
		map.clear();
		map.put("4", new StringBuilder());
	}

	@Override
	public String getMinString() {
		StringBuilder min = null;
		int minLength = Integer.MAX_VALUE;
		for (StringBuilder sb : map.values()) {
			int len = sb.length();
			if (len < minLength) {
				min = sb;
				minLength = len;
			}
		}
		return (min == null) ? "" : min.toString();
	}

	private void printMap() {
		if (MMLStringOptimizer.getDebug()) {
			System.out.println(" --- ");
			map.forEach((key, builder) -> System.out.println(key + ": " + builder.toString()));
		}
	}

	/**
	 * すべてに文字列を無条件追加
	 */
	private void addString(String s) {
		map.values().forEach(t -> t.append(s));
	}

	private void addString(String s, int insertBack) {
		map.values().forEach(t -> {
			int len = t.length();
			t.insert(len-insertBack, s);
		});
	}

	private StringBuilder newBuilder(String minString, String lenString, String s, int insertBack) {
		StringBuilder changeBuilder = new StringBuilder( minString );
		int len = changeBuilder.length();
		// &や他の指示よりも前に配置する.
		changeBuilder.insert(len-insertBack, "l"+lenString);
		changeBuilder.append(s);
		return changeBuilder;
	}

	private final Map<String, StringBuilder> newBuilderMap = new HashMap<>();
	private void addNoteText(String noteName, String lenString, int insertBack) {
		newBuilderMap.clear();
		String minString = getMinString();

		// 保有するbuilderを更新.
		map.forEach((key, builder) -> {
			builder.append(noteName);
			if (!key.equals(lenString)) {
				if (lenString.equals(key+".")) {
					builder.append(".");
				} else {
					builder.append(lenString);
				}
				newBuilderMap.put(lenString, newBuilder(minString, lenString, noteName, insertBack));
				if (lenString.endsWith(".")) {
					String lenString2 = lenString.substring(0, lenString.length()-1);
					newBuilderMap.put(lenString2, newBuilder(minString, lenString2, noteName+".", insertBack));
				}
			}
		});

		// 新規のbuilderで保有mapを更新.
		newBuilderMap.forEach((key, builder) -> {
			if (map.containsKey(key)) {
				if ( builder.length() < map.get(key).length() ) {
					map.put(key, builder);
				}
			} else {
				map.put(key, builder);
			}
		});

		FlexDotPattern.updateFlexDot(map, noteName, lenString);
	}

	private static final class FlexDotPattern {
		private static final FlexDotPattern flexList[] = {
				new FlexDotPattern(64),
				new FlexDotPattern(32),
				new FlexDotPattern(16),
				new FlexDotPattern(8),
				new FlexDotPattern(4)
		};

		private final String lCur;
		private final String lNext;
		private final String lPrev;
		private FlexDotPattern(int l) {
			this.lCur = (l/2) + ".";
			this.lNext = Integer.toString(l);
			this.lPrev = Integer.toString(l/4);
		}

		private void updatePattern(Map<String, StringBuilder> map, String noteName, String lenString) {
			String cName = noteName;
			if (!noteName.toLowerCase().equals("r")) {
				cName = "&" + noteName;
			}

			Map<String, StringBuilder> updateMap = new HashMap<>();
			String eStr = noteName + lPrev + cName + lCur;
			for (String key : map.keySet()) {
				if (key.equals(lNext)) {
					String text = map.get(key).toString();
					if (text.endsWith(eStr)) {
						String prevText = text.substring(0, text.length() - eStr.length());
						String nextText1 = prevText + noteName + cName + lPrev+".";
						String nextText2 = prevText + noteName + "l" + lPrev+"." + cName;
						String nextText3 = prevText + noteName + "l" + lPrev + cName +".";
						updateMap.put(key, new StringBuilder(nextText1));
						updateMap.put(lPrev+".", new StringBuilder(nextText2));
						updateMap.put(lPrev, new StringBuilder(nextText3));
					}
				}
			}

			updateMap.forEach((key, builder) -> {
				StringBuilder now = map.get(key);
				if ( (now == null) || (builder.length() < now.length()) ) {
					map.put(key, builder);
				}
			});
		}

		private static void updateFlexDot(Map<String, StringBuilder> map, String noteName, String lenString) {
			for (FlexDotPattern t : flexList) {
				if (lenString.equals(t.lCur)) {
					t.updatePattern(map, noteName, lenString);
					break;
				}
			}
		}
	}

	private final ArrayList<String> deleteKey = new ArrayList<>();
	private void cleanMap() {
		int minLength = getMinString().length();
		deleteKey.clear();
		map.forEach((key, builder) -> {
			if (builder.length() > minLength+key.length()+1) {
				deleteKey.add(key);
			}
		});
		deleteKey.forEach(t -> map.remove(t));
	}

	private String section = "4";
	private int octave = 4;
	private int octD = 0;

	private int tokenStack = 0;

	private void doPattern(String noteName, String lenString, int insertBack) {
		insertOxPattern(insertBack);
		insertLxPattern(noteName, lenString, insertBack);
	}

	private void insertLxPattern(String noteName, String lenString, int insertBack) {
		if (lenString.equals("")) {
			lenString = this.section;
		} else if (lenString.equals(".")) {
			lenString = this.section + ".";
		}
		addNoteText(noteName, lenString, insertBack);
	}

	public static String getOctaveString(int prevOct, int nextOct) {
		int delta = prevOct - nextOct;
		if (Math.abs( delta ) > 2) {
			return ("o"+nextOct);
		} else {
			String s = "<<";
			if (delta < 0) {
				s = ">>";
			}
			return (s.substring(0, Math.abs( delta )));
		}
	}

	private void insertOxPattern(int insertBack) {
		int nextOct = this.octave + this.octD;
		addString(getOctaveString(this.octave, nextOct), insertBack);

		this.octave = nextOct;
		this.octD = 0;
	}

	private boolean doToken(char firstC, String lenString) {
		if (firstC == 'o') {
			octD = Integer.parseInt(lenString) - octave;
		} else if (firstC == '>') {
			octD++;
		} else if (firstC == '<') {
			octD--;
		} else if (firstC == 'l') {
			this.section = lenString;
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void nextToken(String token) {
		char firstC = Character.toLowerCase( token.charAt(0) );
		String s[] = MMLTokenizer.noteNames(token);

		if (MMLTokenizer.isNote(firstC)) {
			doPattern(s[0], s[1], tokenStack);
			tokenStack = 0;
			cleanMap();
		} else {
			boolean patternDone = doToken(firstC, s[1]);
			if (!patternDone) {
				addString(token);
			}
			if (firstC == '&') {
				// '&' 以外は順番どおり.
				tokenStack += token.length();
			}
		}

		printMap();
	}
}

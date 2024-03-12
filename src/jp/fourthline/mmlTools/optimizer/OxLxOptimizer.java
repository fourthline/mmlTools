/*
 * Copyright (C) 2015-2022 たんらる
 */

package jp.fourthline.mmlTools.optimizer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.fourthline.mmlTools.MMLBuilder;
import jp.fourthline.mmlTools.core.MMLTokenizer;


/**
 * Ox, Lxを使用した最適化.
 */
public class OxLxOptimizer implements MMLStringOptimizer.Optimizer {

	public static class OptimizerMap extends HashMap<String, StringBuilder> {
		private static final long serialVersionUID = -7335134548044714344L;

		protected void updateMapMinLength(String key, StringBuilder builder) {
			StringBuilder now = this.get(key);
			if ( (now == null) || (builder.length() < now.length()) ) {
				this.put(key, builder);
			}
		}
	}

	protected OptimizerMap createOptimizerMap() {
		return new OptimizerMap();
	}

	/**
	 * Lの文字列と、生成中文字列のBuilder.
	 */
	private final OptimizerMap map = createOptimizerMap();

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

	protected static StringBuilder newStringBuilder(Map<String, StringBuilder> map, String key, String init) {
		StringBuilder sb = map.get(key);
		if (sb != null) {
			sb.setLength(0);
			sb.append(init);
		} else {
			sb = new StringBuilder(init);
		}
		return sb;
	}

	protected StringBuilder newBuilder(StringBuilder sb, String lenString, String s, int insertBack) {
		int len = sb.length();
		// &や他の指示よりも前に配置する.
		sb.insert(len-insertBack, "l"+lenString);
		sb.append(s);
		return sb;
	}

	private final Map<String, StringBuilder> newBuilderMap = new HashMap<>();
	private void updateBuilder(String key, StringBuilder builder, String minString, String noteName, String lenString, int insertBack) {
		builder.append(noteName);
		if (!key.equals(lenString)) {
			if (lenString.equals(key+".")) {
				builder.append(".");
			} else {
				builder.append(lenString);
			}
			newBuilderMap.put(lenString, newBuilder(newStringBuilder(newBuilderMap, lenString, minString), lenString, noteName, insertBack));
			if (lenString.endsWith(".")) {
				String lenString2 = lenString.substring(0, lenString.length()-1);
				newBuilderMap.put(lenString2, newBuilder(newStringBuilder(newBuilderMap, lenString2, minString), lenString2, noteName+".", insertBack));
			}
			extendPatternBuilder(newBuilderMap, minString, noteName, lenString, insertBack);
		}
	}

	protected void extendPatternBuilder(Map<String, StringBuilder> newBuilderMap, String minString, String noteName, String lenString, int insertBack) {}

	private void addNoteText(String noteName, String lenString, int insertBack) {
		newBuilderMap.clear();
		String minString = getMinString();

		// 保有するbuilderを更新.
		map.forEach((key, builder) -> updateBuilder(key, builder, minString, noteName, lenString, insertBack));

		// 新規のbuilderで保有mapを更新.
		newBuilderMap.forEach(map::updateMapMinLength);

		FlexDotPattern.updateFlexDot(map, noteName, lenString);
	}

	private static final class FlexDotPattern {
		private static final FlexDotPattern[] flexList = {
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

		private void updatePattern(OptimizerMap map, String noteName, String lenString) {
			String cName = noteName;
			if (!noteName.equalsIgnoreCase("r")) {
				cName = "&" + noteName;
			}

			Map<String, StringBuilder> updateMap = new HashMap<>();
			String eStr = noteName + lPrev + cName + lCur;
			for (String key : map.keySet()) {
				if (key.equals(lNext)) {
					String text = map.get(key).toString();
					if (text.endsWith(eStr)) {
						String prevText = text.substring(0, text.length() - eStr.length());
						String lPrevDot = lPrev+".";
						String nextText1 = prevText + noteName + cName + lPrevDot;
						String nextText2 = prevText + noteName + "l" + lPrevDot + cName;
						String nextText3 = prevText + noteName + "l" + lPrev + cName +".";
						updateMap.put(key, newStringBuilder(updateMap, key, nextText1));
						updateMap.put(lPrevDot, newStringBuilder(updateMap, lPrevDot, nextText2));
						updateMap.put(lPrev, newStringBuilder(updateMap, lPrev, nextText3));
					}
				}
			}

			updateMap.forEach((key, builder) -> map.updateMapMinLength(key, builder));
		}

		private static void updateFlexDot(OptimizerMap map, String noteName, String lenString) {
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
	private int octave = MMLBuilder.INIT_OCT;
	private int octD = 0;

	private int tokenStack = 0;

	public void resetOctave() {
		this.octave = MMLBuilder.INIT_OCT;
		this.octD = 0;
	}

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
		fixPattern(map);
	}

	protected void fixPattern(OptimizerMap map) {}

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
		if (this.octD != 0) {
			int nextOct = this.octave + this.octD;
			addString(getOctaveString(this.octave, nextOct), insertBack);

			this.octave = nextOct;
			this.octD = 0;
		}
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
		String[] s = MMLTokenizer.noteNames(token);

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

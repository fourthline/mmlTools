/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.ArrayList;
import java.util.HashMap;

import fourthline.mmlTools.core.MMLTokenizer;

/**
 * Lx-builder, Ox
 */
public final class MMLStringOptimizer {

	private static boolean debug = false;
	public static void setDebug(boolean b) {
		debug = b;
	}

	private String originalMML;

	/**
	 * Lの文字列と、生成中文字列のBuilder.
	 */
	private HashMap<String, StringBuilder> map = new HashMap<>();

	/**
	 * @param mml   MMLEventListで出力したMML文字列.
	 */
	public MMLStringOptimizer(String mml) {
		originalMML = mml;
	}

	public String toString() {
		return getOptimizedString();
	}

	private String getOptimizedString() {
		map.clear();
		map.put("4", new StringBuilder());
		optimize();
		return getMinString();
	}

	private String getMinString() {
		return map.values()
				.stream()
				.min((t1, t2) -> (t1.length() - t2.length()))
				.get()
				.toString();
	}

	private void printMap() {
		if (debug) {
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

	private void addNoteText(String noteName, String lenString, int insertBack) {
		HashMap<String, StringBuilder> newBuilderMap = new HashMap<>();
		String minString = getMinString();

		// 保有するbuilderを更新.
		map.forEach((key, builder) -> {
			if (key.equals(lenString)) {
				builder.append(noteName);
			} else {
				builder.append(noteName);
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
	}

	private void cleanMap() {
		int minLength = getMinString().length();
		ArrayList<String> deleteKey = new ArrayList<>();
		map.forEach((key, builder) -> {
			if (builder.length() > minLength+key.length()+1) {
				deleteKey.add(key);
			}
		});
		deleteKey.forEach(t -> map.remove(t));
	}

	private class OptimizeBuilder {
		private String section = "4";
		private int octave = 4;
		private int octD = 0;

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

		private void insertOxPattern(int insertBack) {
			int nextOct = this.octave + this.octD;
			int octRo = this.octD;
			if (octRo < 0) {
				octRo = -octRo;
			}
			if (octRo > 2) {
				addString("o"+nextOct, insertBack);
			} else if (octRo > 0) {
				String s = ">>";
				if (this.octave > nextOct) {
					s = "<<";
				}
				addString(s.substring(0, octRo), insertBack);
			}

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
	}

	private void optimize() {
		MMLTokenizer tokenizer = new MMLTokenizer(originalMML);
		OptimizeBuilder optimizer = new OptimizeBuilder();
		int tokenStack = 0;

		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			char firstC = Character.toLowerCase( token.charAt(0) );
			String s[] = MMLTokenizer.noteNames(token);

			if (MMLTokenizer.isNote(firstC)) {
				optimizer.doPattern(s[0], s[1], tokenStack);
				tokenStack = 0;
				cleanMap();
			} else {
				boolean patternDone = optimizer.doToken(firstC, s[1]);
				if (!patternDone) {
					tokenStack += token.length();
					addString(token);
				}
			}

			printMap();
		}
	}

	public static void main(String args[]) {
		MMLStringOptimizer.setDebug(true);
		String mml = "c8c2c1c8c2c1c8c2c1c8c2c1";
		System.out.println( new MMLStringOptimizer(mml).toString() );
	}
}

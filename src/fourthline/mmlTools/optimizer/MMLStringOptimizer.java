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
		parse();
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

	private void insertLxNote(String noteName, String noteLength, String section, int tokenStack) {
		if (noteLength.equals("")) {
			noteLength = section;
		} else if (noteLength.equals(".")) {
			noteLength = section + ".";
		}
		addNoteText(noteName, noteLength, tokenStack);
	}

	private void addNoteText(String s, String lenString, int insertBack) {
		HashMap<String, StringBuilder> newBuilderMap = new HashMap<>();
		String minString = getMinString();

		// 保有するbuilderを更新.
		map.forEach((key, builder) -> {
			if (key.equals(lenString)) {
				builder.append(s);
			} else {
				builder.append(s);
				if (lenString.equals(key+".")) {
					builder.append(".");
				} else {
					builder.append(lenString);
				}
				newBuilderMap.put(lenString, newBuilder(minString, lenString, s, insertBack));
				if (lenString.endsWith(".")) {
					String lenString2 = lenString.substring(0, lenString.length()-1);
					newBuilderMap.put(lenString2, newBuilder(minString, lenString2, s+".", insertBack));
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

	private int insertOctaveChange(int prevOct, int octD, int insertBack) {
		int nextOct = prevOct + octD;
		if (octD < 0) {
			octD = -octD;
		}
		if (octD > 2) {
			addString("o"+nextOct, insertBack);
		} else if (octD > 0) {
			String s = ">>";
			if (prevOct > nextOct) {
				s = "<<";
			}
			addString(s.substring(s.length()-octD), insertBack);
		}
		return nextOct;
	}

	private void parse() {
		MMLTokenizer tokenizer = new MMLTokenizer(originalMML);
		String section = "4";
		int octave = 4;
		int octD = 0;
		int tokenStack = 0;

		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			char firstC = Character.toLowerCase( token.charAt(0) );
			String s[] = MMLTokenizer.noteNames(token);
			if (MMLTokenizer.isNote(token.charAt(0))) {
				octave = insertOctaveChange(octave, octD, tokenStack);
				insertLxNote(s[0], s[1], section, tokenStack);
				cleanMap();
				tokenStack = 0;
				octD = 0;
			} else if (firstC == 'l') {
				section = s[1];
			} else if (firstC == 'o') {
				octD = Integer.parseInt(s[1]) - octave;
			} else if (firstC == '>') {
				octD++;
			} else if (firstC == '<') {
				octD--;
			} else {
				tokenStack += token.length();
				addString(token);
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

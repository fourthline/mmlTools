/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.HashMap;

import fourthline.mmlTools.core.MMLTokenizer;

/**
 * Ln-builder
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

	private StringBuilder newBuilder(String minString, String lenString, String s) {
		StringBuilder changeBuilder = new StringBuilder( minString );
		// Lの直前に '&' があると、効かなくなるため.
		int len = changeBuilder.length();
		if ( (len > 0) && (changeBuilder.charAt(len-1) == '&') ) {
			changeBuilder.insert(len-1, "l"+lenString);
		} else {
			changeBuilder.append("l"+lenString);
		}
		changeBuilder.append(s);
		return changeBuilder;
	}

	private void addString(String s, String lenString) {
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
				newBuilderMap.put(lenString, newBuilder(minString, lenString, s));
				if (lenString.endsWith(".")) {
					String lenString2 = lenString.substring(0, lenString.length()-1);
					newBuilderMap.put(lenString2, newBuilder(minString, lenString2, s+"."));
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

	private void parse() {
		MMLTokenizer tokenizer = new MMLTokenizer(originalMML);
		String section = "4";

		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			char firstC = token.charAt(0);
			if (MMLTokenizer.isNote(token.charAt(0))) {
				String s[] = MMLTokenizer.noteNames(token);
				String noteLength = s[1];
				if (noteLength.equals("")) {
					noteLength = section;
				} else if (noteLength.equals(".")) {
					noteLength = section + ".";
				}
				addString(s[0], noteLength);
			} else if ( (firstC == 'l') || (firstC == 'L') ) {
				section = MMLTokenizer.noteNames(token)[1];
			} else {
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

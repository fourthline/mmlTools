/*
 * Copyright (C) 2014-2015 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class TextParser {
	private final Pattern lineSplit = Pattern.compile("\n");
	private final HashMap<String, Consumer<String>> map = new HashMap<>();

	public TextParser() {}
	public TextParser pattern(String s, Consumer<String> func) {
		map.put(s, func);
		return this;
	}

	public void parse(String text) {
		lineSplit.splitAsStream(text).forEachOrdered((lineText) -> {
			map.keySet().forEach(key -> {
				if (lineText.startsWith(key)) {
					map.get(key).accept( lineText.substring(key.length()) );
					return;
				}
			});
		});
	}
}

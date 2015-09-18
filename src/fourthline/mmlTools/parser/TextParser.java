/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mmlTools.parser;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class TextParser {
	private final String text;

	private TextParser(String text) {
		this.text = text;
	}

	public static TextParser text(String test) {
		return new TextParser(test);
	}

	private HashMap<String, Consumer<String>> map = new HashMap<>();
	public TextParser pattern(String s, Consumer<String> func) {
		map.put(s, func);
		return this;
	}

	public void parse() {
		Pattern.compile("\n").splitAsStream(this.text).forEachOrdered((lineText) -> {
			map.keySet().forEach(key -> {
				if (lineText.startsWith(key)) {
					map.get(key).accept( lineText.substring(key.length()) );
					return;
				}
			});
		});
	}
}

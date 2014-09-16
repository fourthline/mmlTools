/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.util.function.Consumer;

public final class TextParser {

	private String text;
	private TextParser() {}

	private TextParser(String text) {
		this.text = text;
	}

	public static TextParser text(String test) {
		return new TextParser(test);
	}

	public boolean startsWith(String s, Consumer<String> func) {
		if (text.startsWith(s)) {
			func.accept( text.substring(s.length()) );
			return true;
		}
		return false;
	}
}

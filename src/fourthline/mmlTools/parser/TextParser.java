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

	/**
	 * 指定された文字列で開始するテキストであれば、funcに開始文字列以外の部分を引数として渡し実行します.
	 * @param s
	 * @param func
	 * @return 指定されたテキストで、funcを実行した場合には trueを返します.
	 */
	public boolean startsWith(String s, Consumer<String> func) {
		if (text.startsWith(s)) {
			func.accept( text.substring(s.length()) );
			return true;
		}
		return false;
	}
}

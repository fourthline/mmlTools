/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.File;
import java.io.InputStream;

import fourthline.mmlTools.MMLScore;

/**
 * MMLファイルのparser
 */
public interface IMMLFileParser {
	public MMLScore parse(InputStream istream) throws MMLParseException;

	public static IMMLFileParser getParser(File file) {
		IMMLFileParser fileParser;
		if (file.toString().endsWith(".mms")) {
			fileParser = new MMSFile();
		} else if (file.toString().endsWith(".mml")) {
			fileParser = new MMLFile();
		} else {
			fileParser = new MMLScore();
		}
		return fileParser;
	}
}

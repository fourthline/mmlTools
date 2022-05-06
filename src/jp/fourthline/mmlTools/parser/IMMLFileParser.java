/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLScoreSerializer;

/**
 * MMLファイルのparser
 */
public interface IMMLFileParser {
	public MMLScore parse(InputStream istream) throws MMLParseException;
	public Map<String, Boolean> getParseProperties();
	public String getName();

	public static IMMLFileParser getParser(File file) {
		IMMLFileParser fileParser;
		String suffix = file.toString().toLowerCase();
		if (suffix.endsWith(".mms")) {
			fileParser = new MMSFile();
		} else if (suffix.endsWith(".mml")) {
			fileParser = new MMLFile();
		} else if (suffix.endsWith(".mid")) {
			fileParser = new MidiFile();
		} else {
			fileParser = new MMLScoreSerializer(new MMLScore());
		}
		return fileParser;
	}
}

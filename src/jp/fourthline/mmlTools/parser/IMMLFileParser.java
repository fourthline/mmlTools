/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLScoreSerializer;

/**
 * MMLファイルのparser
 */
public interface IMMLFileParser {
	MMLScore parse(InputStream istream) throws MMLParseException;
	Map<String, Boolean> getParseProperties();
	Map<String, Collection<String>> getParseAttributes();
	void setParseAttribute(String key, String value);
	String getName();

	static IMMLFileParser getParser(File file) {
		IMMLFileParser fileParser;
		String suffix = file.toString().toLowerCase();
		if (suffix.endsWith(".mms")) {
			fileParser = new MMSFile();
		} else if (suffix.endsWith(".mml")) {
			fileParser = new MMLFile();
		} else if (suffix.endsWith(".mid")) {
			fileParser = new MidiFile();
		} else if (suffix.endsWith(".txt")) {
			fileParser = new TxtFile();
		} else {
			fileParser = new MMLScoreSerializer(new MMLScore());
		}
		return fileParser;
	}
}

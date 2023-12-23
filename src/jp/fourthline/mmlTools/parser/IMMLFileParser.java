/*
 * Copyright (C) 2013-2023 たんらる
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
	Map<Integer, TrackSelect> getTrackSelectMap();

	static IMMLFileParser getParser(File file) {
		IMMLFileParser fileParser;
		String suffix = file.toString().toLowerCase();
		if (suffix.endsWith(".mms")) {
			fileParser = new MMSFile();
		} else if (suffix.endsWith(".mml")) {
			fileParser = new MMLFile();
		} else if (suffix.endsWith(".mid")) {
			fileParser = new MidiFile().preparse(file);
		} else if (suffix.endsWith(".txt")) {
			fileParser = new TxtFile();
		} else {
			fileParser = new MMLScoreSerializer(new MMLScore());
		}
		return fileParser;
	}

	public static final class TrackSelect {
		private boolean enable;
		private final String name;
		public TrackSelect(String name) {
			enable = true;
			this.name = name.isBlank() ? "-" : "<"+name+">";
		}

		public void setEnable(boolean b) {
			enable = b;
		}

		public boolean isEnabled() {
			return enable;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return name;
		}
	}
}

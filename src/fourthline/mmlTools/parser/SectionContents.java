/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * テキストをセクションに区切って扱います.
 * [section-name]
 * text...
 */
public final class SectionContents {
	private final String name;
	private final StringBuilder buffer = new StringBuilder();

	private SectionContents(String name) {
		this.name = name;
	}

	/**
	 * セクション名を取得します.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * セクションコンテンツ（text...部分）を取得します.
	 * @return
	 */
	public String getContents() {
		return buffer.toString();
	}

	/**
	 * InputStreamからセクションのリストを作成します.
	 * @param istream
	 * @return
	 */
	public static List<SectionContents> makeSectionContentsByInputStream(InputStream istream) {
		return makeSectionContentsByInputStream(istream, "UTF-8");
	}

	/**
	 * InputStreamからセクションのリストを作成します.
	 * @param istream
	 * @param charsetName
	 * @return
	 */
	public static List<SectionContents> makeSectionContentsByInputStream(InputStream istream, String charsetName) {
		LinkedList<SectionContents> contentsList = new LinkedList<>();
		try {
			InputStreamReader reader = new InputStreamReader(istream, charsetName);
			new BufferedReader(reader).lines().forEach(s -> {
				if (s.startsWith("[")) {
					contentsList.add( new SectionContents(s) );
				} else if (contentsList.size() > 0) {
					SectionContents section = contentsList.getLast();
					section.buffer.append(s).append('\n');
				}
			});
		} catch (UnsupportedEncodingException e) {}

		return contentsList;
	}
}

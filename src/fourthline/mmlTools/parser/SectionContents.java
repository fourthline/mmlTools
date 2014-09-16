/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public final class SectionContents {
	private final String name;
	private final StringBuilder buffer = new StringBuilder();

	private SectionContents(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getContents() {
		return buffer.toString();
	}

	public static List<SectionContents> makeSectionContentsByInputStream(InputStream istream) {
		return makeSectionContentsByInputStream(istream, "UTF-8");
	}

	public static List<SectionContents> makeSectionContentsByInputStream(InputStream istream, String charsetName) {
		ArrayList<SectionContents> contentsList = new ArrayList<>();
		try {
			InputStreamReader reader = new InputStreamReader(istream, charsetName);
			new BufferedReader(reader).lines().forEach(s -> {
				if (s.startsWith("[")) {
					contentsList.add( new SectionContents(s) );
				} else if (contentsList.size() > 0) {
					SectionContents section = contentsList.get( contentsList.size() - 1 );
					section.buffer.append(s).append('\n');
				}
			});
		} catch (UnsupportedEncodingException e) {}

		return contentsList;
	}
}
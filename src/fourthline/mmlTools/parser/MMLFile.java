/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

public final class MMLFile implements IMMLFileParser {

	private static final class SectionContents {
		private String name;
		private StringBuilder buffer = new StringBuilder();
		private SectionContents(String name) {
			this.name = name;
		}
	}

	private SectionContents section = null;
	private ArrayList<SectionContents> contentsList = new ArrayList<>();

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		MMLScore score = new MMLScore();
		InputStreamReader reader = new InputStreamReader(istream);
		new BufferedReader(reader).lines().forEach(s -> {
			if (s.startsWith("[")) {
				section = new SectionContents(s);
				contentsList.add(section);
			} else if (section != null) {
				section.buffer.append(s).append('\n');
			}
		});

		// channel sections
		LinkedList<String> mmlParts = new LinkedList<>();
		contentsList.stream().filter(s -> s.name.matches("\\[Channel[0-9]*\\]")).forEach(s -> {
			String text = s.buffer.toString();
			text = text.replaceAll("//.*\n", "\n").replaceAll("/\\*.*\\*/", "").replaceAll("[ \t\n]", "");
			mmlParts.add(text);
			System.out.println(s.name);
			System.out.println(text);
		});

		while (!mmlParts.isEmpty()) {
			String text[] = new String[] { "", "", "" };
			for (int i = 0; i < text.length; i++) {
				if (!mmlParts.isEmpty()) {
					text[i] = mmlParts.pop();
				}
			}
			MMLTrack track = new MMLTrack(text[0], text[1], text[2], "");
			score.addTrack(track);
			track.setTrackName("Track"+score.getTrackCount());
		}

		return score;
	}
}

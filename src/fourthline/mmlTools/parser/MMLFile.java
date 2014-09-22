/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

public final class MMLFile implements IMMLFileParser {

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		MMLScore score = new MMLScore();
		List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream);
		if (contentsList.isEmpty()) {
			throw(new MMLParseException());
		}

		// channel sections
		LinkedList<String> mmlParts = new LinkedList<>();
		contentsList.stream()
		.filter(s -> s.getName().matches("\\[Channel[0-9]*\\]"))
		.map(s -> s.getContents())
		.map(s -> s.replaceAll("//.*\n", "\n").replaceAll("/\\*.*\\*/", "").replaceAll("[ \t\n]", ""))
		.forEach(s -> mmlParts.add(s));

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

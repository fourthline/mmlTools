/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

public final class MMLFile implements IMMLFileParser {
	private final MMLScore score = new MMLScore();

	// channel sections
	private LinkedList<String> mmlParts = new LinkedList<>();
	private List<Extension3mleTrack> trackList = null;

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream, "Shift_JIS");
		if (contentsList.isEmpty()) {
			throw(new MMLParseException());
		}
		parseSection(contentsList);
		if ( (trackList == null) || (trackList.size() == 0) ) {
			throw new MMLParseException();
		}
		createTrack();
		return score;
	}

	private void parseSection(List<SectionContents> contentsList) {
		for (SectionContents contents : contentsList) {
			if (contents.getName().equals("[3MLE EXTENSION]")) {
				trackList = Extension3mleTrack.parse3mleExtension(contents.getContents(), score.getMarkerList());
			} else if (contents.getName().matches("\\[Channel[0-9]*\\]")) {
				mmlParts.add( contents.getContents()
						.replaceAll("//.*\n", "\n")
						.replaceAll("/\\*/?([^/]|[^*]/)*\\*/", "")
						.replaceAll("[ \t\n]", "") );
			} else if (contents.getName().equals("[Settings]")) {
				parseSettings(contents.getContents());
			}
		}

	}

	private void createTrack() {
		for (Extension3mleTrack track : trackList) {
			int program = track.getInstrument() - 1; // 3MLEのInstruments番号は1がスタート.
			String text[] = new String[] { "", "", "" };
			for (int i = 0; i < track.getTrackCount(); i++) {
				text[i] = mmlParts.pop();
			}
			InstClass instClass = MabiDLS.getInstance().getInstByProgram(program);
			MMLTrack mmlTrack;
			if ( (instClass.getType() == InstType.VOICE) || (instClass.getType() == InstType.CHORUS) ) {
				// 歌パート
				mmlTrack = new MMLTrack("", "", "", text[0]);
			} else {
				mmlTrack = new MMLTrack(text[0], text[1], text[2], "");
			}
			score.addTrack(mmlTrack);
			mmlTrack.setProgram(program);
			mmlTrack.setPanpot(track.getPanpot());
			mmlTrack.setTrackName(track.getTrackName());
		}
	}

	/**
	 * parse [Settings] contents
	 * @param contents
	 */
	private void parseSettings(String contents) {
		Pattern.compile("\n").splitAsStream(contents).forEachOrdered((s) -> {
			TextParser textParser = TextParser.text(s);
			if ( textParser.startsWith("Title=", score::setTitle) ) {
			} else if ( textParser.startsWith("Source=", score::setAuthor) ) {
			}
		});
	}
}

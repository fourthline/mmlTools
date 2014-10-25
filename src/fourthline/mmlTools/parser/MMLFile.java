/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.Marker;

public final class MMLFile implements IMMLFileParser {
	private final MMLScore score = new MMLScore();

	// channel sections
	private LinkedList<String> mmlParts = new LinkedList<>();
	private List<Extension3mleTrack> trackList = null;

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream, "Shift_JIS");
		if (contentsList.isEmpty()) {
			throw(new MMLParseException("no contents"));
		}
		parseSection(contentsList);
		if ( (trackList == null) || (trackList.size() == 0) ) {
			throw new MMLParseException("no track");
		}
		createTrack();
		setStartPosition();
		return score;
	}

	private void parseSection(List<SectionContents> contentsList) throws MMLParseException {
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
			InstType instType = MabiDLS.getInstance().getInstByProgram(program).getType();
			MMLTrack mmlTrack;
			if ( (instType == InstType.VOICE) || (instType == InstType.CHORUS) ) {
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
	 * 再生開始位置を設定します.
	 */
	private void setStartPosition() {
		if (score.getMarkerList().size() == 0) {
			return;
		}
		for (int i = 0; i < trackList.size(); i++) {
			Extension3mleTrack track = trackList.get(i);
			MMLTrack mmlTrack = score.getTrack(i);
			int markerId = track.getStartMarker();
			if (markerId > 0) {
				Marker marker = score.getMarkerList().get(markerId-1);
				int tickOffset = marker.getTickOffset();
				for (MMLEventList eventList : mmlTrack.getMMLEventList()) {
					eventList.insertTick(0, tickOffset);
				}
			}
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
			} else if ( textParser.startsWith("Encoding=", (t) -> Extension3mleTrack.setEncoding(t)) ) {
			}
		});
	}
}

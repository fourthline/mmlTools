/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.Marker;

/**
 * まきまびしーくさんのファイルフォーマットを扱います.
 */
public final class MMSFile implements IMMLFileParser {
	private final MMLScore score = new MMLScore();

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream, "Shift_JIS");
		if (contentsList.isEmpty()) {
			throw(new MMLParseException());
		}

		for (SectionContents section : contentsList) {
			String sectionName = section.getName();

			if ( sectionName.matches("\\[part[0-9]+\\]") ) {
				/* MMLパート */
				System.out.println("part");
				MMLTrack track = parseMMSPart(section.getContents());
				System.out.println(track.getMML());
				System.out.println(track.getProgram());
				score.addTrack(track);
			} else if (sectionName.equals("[infomation]")) {
				parseInfomation(section.getContents());
			} else if (sectionName.equals("[marker]")) {
				parseMarker(section.getContents());
			}
		}

		return score;
	}

	/* MMS->programへの変換テーブル */
	static final int mmsInstTable[] = {
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
		10, 11, 12, 13, 14, 15, 16, 17, 18, 66, 
		67, 68,	69, 70, 71, 72, 73, 74, 75, 76, 
		18
	};

	/**
	 * mmsファイルのinstrument値は、DLSのものではないので変換を行います.
	 * @param mmsInst
	 * @return DLSのprogram値
	 */
	private int convertInstProgram(int mmsInst) {
		return mmsInstTable[mmsInst];
	}

	/**
	 * parse [information] contents
	 * @param contents
	 */
	private void parseInfomation(String contents) {
		Pattern.compile("\n").splitAsStream(contents).forEachOrdered((s) -> {
			TextParser textParser = TextParser.text(s);
			if ( textParser.startsWith("title=", score::setTitle) ) {
			} else if ( textParser.startsWith("auther=", score::setAuthor) ) {
			} else if ( textParser.startsWith("rythmNum=",  t -> score.setTimeCountOnly(Integer.valueOf(t)) )) {
			} else if ( textParser.startsWith("rythmBase=", t -> score.setBaseOnly(Integer.valueOf(t)) )) {
			}
		});
	}

	/**
	 * parse [marker] contents
	 * @param contens
	 */
	private void parseMarker(String contents) {
		LinkedList<Marker> markerList = new LinkedList<>();
		Pattern.compile("\n").splitAsStream(contents).forEachOrdered((s) -> {
			TextParser textParser = TextParser.text(s);
			if ( textParser.startsWith("label", t -> markerList.add(new Marker(t.substring(5), 0))) ) {
			} else if ( textParser.startsWith("position", t -> {
				int tickOffset = Integer.parseInt(t.substring(5));
				markerList.getLast().setTickOffset( tickOffset );
			})) {
			}
		});
		score.getMarkerList().addAll(markerList);
	}

	/**
	 * parse [part*] contents
	 * @param contents
	 * @return
	 */
	private MMLTrack parseMMSPart(String contents) {
		final int intValue[] = { 0, 0 };
		final String stringValue[] = { "", "", "", "" };

		Pattern.compile("\n").splitAsStream(contents).forEachOrdered((s) -> {
			TextParser textParser = TextParser.text(s);
			if ( textParser.startsWith("instrument=",
					t -> intValue[0] = convertInstProgram(Integer.parseInt(t)) )) {
			} else if ( textParser.startsWith("panpot=",
					t -> intValue[1] = Integer.parseInt(t) + 64 )) {
			} else if ( textParser.startsWith("name=",    t -> stringValue[0] = t) ) {
			} else if ( textParser.startsWith("ch0_mml=", t -> stringValue[1] = t) ) {
			} else if ( textParser.startsWith("ch1_mml=", t -> stringValue[2] = t) ) {
			} else if ( textParser.startsWith("ch2_mml=", t -> stringValue[3] = t) ) {
				return;
			}
		});

		MMLTrack track = new MMLTrack(stringValue[1], stringValue[2], stringValue[3], "");
		track.setTrackName(stringValue[0]);
		track.setProgram(intValue[0]);
		track.setPanpot(intValue[1]);
		return track;
	}
}

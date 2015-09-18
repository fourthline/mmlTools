/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.Marker;
import fourthline.mmlTools.core.ResourceLoader;

/**
 * まきまびしーくさんのファイルフォーマットを扱います.
 */
public final class MMSFile implements IMMLFileParser {
	private static final String PATCH_NAME = "mms_instPatch";

	/* MMS->programへの変換テーブル */
	private static int mmsInstTable[] = {
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
		10, 11, 12, 13, 14, 15, 16, 17, 65, 66, 
		67, 68,	69, 70, 71, 72, 73, 74, 75, 76, 
		18
	};

	static {
		try {
			ResourceBundle instPatch = ResourceBundle.getBundle(PATCH_NAME, new ResourceLoader());

			// パッチファイルがある場合は、変換テーブルを更新します.
			for (String key : instPatch.keySet()) {
				String newInst = instPatch.getString(key).replaceAll("#.*", "");
				int keyInt = Integer.parseInt(key.trim());
				int newInstInt = Integer.parseInt(newInst.trim());
				System.out.println("[MMS-PATCH] " + keyInt + " -> " + newInstInt);
				for (int i = 0; i < mmsInstTable.length; i++) {
					if (mmsInstTable[i] == keyInt) {
						mmsInstTable[i] = newInstInt;
					}
				}
			}
		} catch (MissingResourceException e) {}
	}

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
				System.out.println(track.getOriginalMML());
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
		TextParser.text(contents)
		.pattern("title=", score::setTitle)
		.pattern("auther=", score::setAuthor)
		.pattern("rythmNum=",  t -> score.setTimeCountOnly(Integer.valueOf(t)) )
		.pattern("rythmBase=", t -> score.setBaseOnly(Integer.valueOf(t)) )
		.parse();
	}

	/**
	 * parse [marker] contents
	 * @param contens
	 */
	private void parseMarker(String contents) {
		LinkedList<Marker> markerList = new LinkedList<>();
		TextParser.text(contents)
		.pattern("label", t -> markerList.add(new Marker(t.substring(5), 0)))
		.pattern("position", t -> {
			int tickOffset = Integer.parseInt(t.substring(5));
			markerList.getLast().setTickOffset( tickOffset );
		})
		.parse();
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

		TextParser.text(contents)
		.pattern("instrument=", t -> intValue[0] = convertInstProgram(Integer.parseInt(t)) )
		.pattern("panpot=",     t -> intValue[1] = Integer.parseInt(t) + 64 )
		.pattern("name=",       t -> stringValue[0] = t)
		.pattern("ch0_mml=",    t -> stringValue[1] = t)
		.pattern("ch1_mml=",    t -> stringValue[2] = t)
		.pattern("ch2_mml=",    t -> stringValue[3] = t)
		.parse();

		MMLTrack track = new MMLTrack().setMML(stringValue[1], stringValue[2], stringValue[3], "");
		track.setTrackName(stringValue[0]);
		track.setProgram(intValue[0]);
		track.setPanpot(intValue[1]);
		return track;
	}
}

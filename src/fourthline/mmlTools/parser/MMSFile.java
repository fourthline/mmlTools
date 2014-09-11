/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

/**
 * まきまびしーくさんのファイルフォーマットを扱います.
 * @author fourthline
 *
 */
public final class MMSFile implements IMMLFileParser {

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		MMLScore score = new MMLScore();
		BufferedReader reader = null;
		try {
			InputStreamReader isReader = new InputStreamReader(istream, "Shift_JIS");
			reader = new BufferedReader(isReader);

			String s;
			while (true) {
				s = reader.readLine();
				if (s == null) {
					throw(new MMLParseException());
				}

				/* ヘッダチェック */
				if (s.equals("[mms-file]")) {
					break;
				}
			}

			/* バージョン */
			s = reader.readLine();
			if ( s == null ) {
				throw(new MMLParseException());
			}

			final String rythm[] = { "4", "4" };
			while ( (s = reader.readLine()) != null ) {
				TextParser textParser = TextParser.text(s);
				if ( s.matches("\\[part[0-9]+\\]") ) {
					/* MMLパート */
					System.out.println("part");
					MMLTrack track = parseMMSPart(reader);
					System.out.println(track.getMML());
					System.out.println(track.getProgram());
					score.addTrack(track);
				} else if ( textParser.startsWith("title=",     t -> score.setTitle(t)) ) {
				} else if ( textParser.startsWith("auther=",    t -> score.setAuthor(t)) ) {
				} else if ( textParser.startsWith("rythmNum=",  t-> rythm[0] = t) ) {
				} else if ( textParser.startsWith("rythmBase=", t -> rythm[1] = t) ) {
				}
			}
			score.setBaseTime(rythm[0]+"/"+rythm[1]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
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


	private MMLTrack parseMMSPart(BufferedReader reader) throws IOException {
		final int intValue[] = { 0, 0 };
		final String stringValue[] = { "", "", "", "" };

		String s;
		while ( (s = reader.readLine()) != null ) {
			TextParser textParser = TextParser.text(s);
			if ( textParser.startsWith("instrument=",
					t -> intValue[0] = convertInstProgram(Integer.parseInt(t)) )) {
			} else if ( textParser.startsWith("panpot=",
					t -> intValue[1] = Integer.parseInt(t) + 64 )) {
			} else if ( textParser.startsWith("name=",    t -> stringValue[0] = t) ) {
			} else if ( textParser.startsWith("ch0_mml=", t -> stringValue[1] = t) ) {
			} else if ( textParser.startsWith("ch1_mml=", t -> stringValue[2] = t) ) {
			} else if ( textParser.startsWith("ch2_mml=", t -> stringValue[3] = t) ) {
				break;
			}
		}

		MMLTrack track = new MMLTrack(stringValue[1], stringValue[2], stringValue[3], "");
		track.setTrackName(stringValue[0]);
		track.setProgram(intValue[0]);
		track.setPanpot(intValue[1]);
		return track;
	}
}

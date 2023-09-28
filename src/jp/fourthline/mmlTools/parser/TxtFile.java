/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.MMLVerifyException;
import jp.fourthline.mmlTools.core.MMLTokenizer;


public final class TxtFile extends AbstractMMLParser {
	private static final int MAX_SIZE = 1024*1024;  // 1MB


	// Parse Option
	public static final String PARSE_CONVERT_VOL = "parse.txt.convertVol";
	public static final String PARSE_CONVERT_OCT = "parse.txt.convertOct";
	public static final String PARSE_L_CONTINUE = "parse.txt.Lcontinue";

	public TxtFile() {
		// parse properties
		parseProperties = new LinkedHashMap<>();
		parseProperties.put(PARSE_CONVERT_VOL, true);
		parseProperties.put(PARSE_CONVERT_OCT, true);
		parseProperties.put(PARSE_L_CONTINUE, true);
	}

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		try {
			int size = istream.available();
			if (size > MAX_SIZE) {
				throw new MMLParseException("too large file:" + size);
			}
			var data = MMLFile.toMMLText(new String(istream.readAllBytes()));
			StringBuilder sb = new StringBuilder();
			data.lines().forEach(t -> sb.append(MMLFile.toMMLText(t)));
			AAConverter aaConverter = new AAConverter(
					parseProperties.getOrDefault(PARSE_CONVERT_VOL, true),
					parseProperties.getOrDefault(PARSE_L_CONTINUE, true));
			new MMLTokenizer(sb.toString()).forEachRemaining(aaConverter::nextToken);
			String mml[] = aaConverter.getText().split(",");
			MMLScore score = new MMLScore();
			MMLTrack track = null;
			int trackIndex = 1;
			for (int i = 0; i < mml.length; i+=3) {
				track = new MMLTrack();
				track.setTrackName("Track"+(trackIndex++));
				String melody = mml[i];
				String chord1 = i+1 < mml.length ? mml[i+1] : "";
				String chord2 = i+2 < mml.length ? mml[i+2] : "";
				track.setMML(melody, chord1, chord2, "");
				score.addTrack(track);
			}

			return convertOctave(score);
		} catch (IOException | MMLExceptionList | MMLVerifyException e) {
			e.printStackTrace();
		}
		return null;
	}

	private MMLScore convertOctave(MMLScore score) throws MMLExceptionList, MMLVerifyException {
		if (!parseProperties.getOrDefault(PARSE_CONVERT_OCT, true)) {
			System.out.println("skip convert oct");
			return score;
		}
		System.out.println("exec convert oct");
		for (MMLTrack track : score.getTrackList()) {
			for (MMLEventList eventList : track.getMMLEventList()) {
				for (MMLNoteEvent noteEvent : eventList.getMMLNoteEventList()) {
					noteEvent.setNote(noteEvent.getNote() - 12);
				}
			}
		}
		score.generateAll();
		return score;
	}

	private static class AAConverter {
		private final boolean convertVol;
		private final boolean lContinue;
		private final StringBuilder sb = new StringBuilder();
		private String lStr = "4";
		private int octave = 5;
		private boolean start = false;
		private AAConverter(boolean convertVol, boolean lContinue) {
			this.convertVol = convertVol;
			this.lContinue = lContinue;
		}
		public void nextToken(String token) {
			if (!start) {
				sb.append("o"+octave);
				start = true;
			}
			String[] s = MMLTokenizer.noteNames(token);
			if (s[0].equalsIgnoreCase("v")) {
				int vol = Integer.parseInt(s[1]);
				if (convertVol) {
					vol /= 8;
				}
				sb.append("v" +vol);
			} else if (s[0].equalsIgnoreCase("l")) {
				lStr = s[1];
				sb.append(token);
			} else if (s[0].equalsIgnoreCase(",")) {
				sb.append(token);
				if (lContinue) {
					sb.append("l"+lStr);
				}
				sb.append("o"+octave);
			} else {
				sb.append(token);
			}
		}

		public String getText() {
			return sb.toString();
		}
	}
}

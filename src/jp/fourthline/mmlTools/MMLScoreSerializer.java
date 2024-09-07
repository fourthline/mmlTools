/*
 * Copyright (C) 2022-2024 たんらる
 */

package jp.fourthline.mmlTools;

import jp.fourthline.mabiicco.Utils;
import jp.fourthline.mmlTools.core.MMLException;
import jp.fourthline.mmlTools.optimizer.CacheMap;
import jp.fourthline.mmlTools.parser.AbstractMMLParser;
import jp.fourthline.mmlTools.parser.MMLParseException;
import jp.fourthline.mmlTools.parser.SectionContents;
import jp.fourthline.mmlTools.parser.TextParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MMLScoreSerializer extends AbstractMMLParser {

	// score section
	private static final String SCORE_SECTION = "[mml-score]";
	// marker section
	private static final String MARKER_SECTION = "[marker]";
	// time signature section
	private static final String TIME_SIGNATURE_SECTION = "[time-signature]";

	// score element
	private static final String SCORE_VERSION = "version=1"; 
	private static final String TITLE = "title=";
	private static final String AUTHOR = "author=";
	private static final String TIME = "time=";
	private static final String TEMPO = "tempo=";

	// track element
	private static final String MML_TRACK = "mml-track=";
	private static final String TRACK_NAME = "name=";
	private static final String PROGRAM = "program=";
	private static final String SONG_PROGRAM = "songProgram=";
	private static final String PANPOT = "panpot=";
	private static final String VOLUME = "volume=";
	private static final String VOLUME_COMPAT = "volumn=";
	private static final String VISIBLE = "visible=";
	private static final String START_OFFSET = "startOffset=";
	private static final String START_DELTA = "startDelta=";
	private static final String START_SONG_DELTA = "startSongDelta=";
	private static final String DELAY = "attackDelayCorrect=";
	private static final String SONG_DELAY = "attackSongDelayCorrect=";
	private static final String DISABLE_NOPT = "disableNopt=";
	private static final String IMPORTED_DATA = "IDATA=";
	private static final String TEMPO_ONLY_MELODY = "tempoOnlyMelody";

	private final MMLScore score;
	private MMLTrack lastTrack = null;
	private final TextParser parser;
	private final ParseCache cache = new ParseCache();

	public MMLScoreSerializer(MMLScore score) {
		this.score = score;

		// スタートOffsetはMMLTrack生成時に指定するため事前にみる
		parser = new TextParser()
				.pattern(START_OFFSET,    t -> cache.startOffset = Integer.parseInt(t))
				.pattern(START_DELTA,     t -> cache.startDelta = Integer.parseInt(t))
				.pattern(START_SONG_DELTA, t -> cache.startSongDelta = Integer.parseInt(t))
				// for MMLTrack
				.pattern(MML_TRACK,   t -> { 
					lastTrack = new MMLTrack(cache.startOffset, cache.startDelta, cache.startSongDelta).setMML(t);
					score.addTrack(lastTrack);
					cache.clear();
				})
				.pattern(TRACK_NAME,    t -> getLastTrack().setTrackName(t) )
				.pattern(PROGRAM,       t -> getLastTrack().setProgram(Integer.parseInt(t)) )
				.pattern(SONG_PROGRAM,  t -> getLastTrack().setSongProgram(Integer.parseInt(t)) )
				.pattern(PANPOT,        t -> getLastTrack().setPanpot(Integer.parseInt(t)) )
				.pattern(VOLUME,        t -> getLastTrack().setVolume(Integer.parseInt(t)) )
				.pattern(VOLUME_COMPAT, t -> getLastTrack().setVolume(Integer.parseInt(t)) )
				.pattern(VISIBLE,       t -> getLastTrack().setVisible(Boolean.parseBoolean(t)) )
				.pattern(DELAY,         t -> getLastTrack().setAttackDelayCorrect(Integer.parseInt(t)))
				.pattern(SONG_DELAY,    t -> getLastTrack().setAttackSongDelayCorrect(Integer.parseInt(t)))
				.pattern(DISABLE_NOPT,  t -> getLastTrack().setDisableNopt(Boolean.parseBoolean(t)))
				.pattern(TITLE,         t -> score.setTitle(t) )
				.pattern(AUTHOR,        t -> score.setAuthor(t) )
				.pattern(TIME,          t -> score.setBaseTime(t) )
				.pattern(TEMPO,         t -> putTempoObj(t) )
				.pattern(IMPORTED_DATA, t -> getLastTrack().setImportedData(t) )
				.pattern(TEMPO_ONLY_MELODY, t -> getLastTrack().setOptTempoOnlyMelody(true) );
	}

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		score.getTempoEventList().clear();
		score.getTrackList().clear();
		score.getMarkerList().clear();
		score.getTimeSignatureList().clear();

		List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream, "UTF-8");
		if (contentsList.isEmpty()) {
			throw(new MMLParseException("empty"));
		}
		for (SectionContents section : contentsList) {
			if (section.getName().equals(SCORE_SECTION)) {
				parseMMLScore(section.getContents());
			} else if (section.getName().equals(MARKER_SECTION)) {
				parseMarker(section.getContents());
			} else if (section.getName().equals(TIME_SIGNATURE_SECTION)) {
				parseTimeSignature(section.getContents());
			}
		}
		return score;
	}

	private void putTempoObj(String s) {
		if (s.length() > 0) {
			String[] l = s.split(",");
			score.getTempoEventList().clear();
			for (String str : l) {
				MMLTempoEvent e = MMLTempoEvent.fromString(str);
				if (e != null) {
					e.appendToListElement(score.getTempoEventList());
				}
			}
		}
	}

	/**
	 * MML@ - ; 内の空白文字を削除する.
	 * @param text
	 * @return
	 */
	private String fixMMLspace(String text) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		int start = 0;

		while ( (start = text.indexOf("MML@", index)) >= 0) {
			int end = text.indexOf(';', start);
			sb.append(text, index, start);
			sb.append( text.substring(start, end+1).replaceAll("[ \t\f\r\n]", "") );
			index = end + 1;
		}

		sb.append(text.substring(index));
		return sb.toString();
	}

	private final static class ParseCache {
		private int startOffset = 0;
		private int startDelta = 0;
		private int startSongDelta = 0;
		private void clear() {
			startDelta = 0;
			startSongDelta = 0;
		}
	}

	private MMLTrack getLastTrack() {
		if (lastTrack != null) {
			return lastTrack;
		} else {
			throw new IllegalStateException();
		}
	}

	/**
	 * parse [mml-score] contents
	 * @param contents
	 */
	private void parseMMLScore(String contents) {
		parser.parse(fixMMLspace(contents));
	}

	/**
	 * parse [marker] contents
	 * @param contents
	 */
	private void parseMarker(String contents) {
		score.getMarkerList().clear();
		for (String s : contents.split("\n")) {
			// <tickOffset>=<name>
			int index = s.indexOf('=');
			if (index > 0) {
				String tickString = s.substring(0, index);
				String name = s.substring(index+1);
				score.getMarkerList().add( new Marker(name, Integer.parseInt(tickString)) );
			}
		}
	}

	/**
	 * parse [time-signature] contents
	 * @param contents
	 */
	private void parseTimeSignature(String contents) {
		score.getTimeSignatureList().clear();
		for (String s : contents.split("\n")) {
			// <tickOffset>=<num>/<base>
			int index = s.indexOf('=');
			if (index > 0) {
				String tickString = s.substring(0, index);
				String sig[] = s.substring(index+1).split("/");
				int numTime = Integer.parseInt(sig[0]);
				int baseTime = Integer.parseInt(sig[1]);
				try {
					score.addTimeSignature(new TimeSignature(score, Integer.parseInt(tickString), numTime, baseTime));
				} catch (NumberFormatException | MMLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String getTempoObj() {
		var globalTempoList = score.getTempoEventList();
		if (globalTempoList.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (MMLTempoEvent t : globalTempoList) {
			sb.append(',');
			sb.append(t.toString());
		}
		return sb.substring(1);
	}

	private int getStartOffsetAll() {
		var trackList = score.getTrackList();
		if (trackList.size() > 0) {
			return trackList.get(0).getCommonStartOffset();
		}
		return 0;
	}

	private void writeMMLEventList(PrintStream stream, String name, List <? extends MMLEvent> list) {
		if (!list.isEmpty()) {
			stream.println(name);
			for (MMLEvent event : list) {
				stream.println(event.toString());
			}
		}
	}

	private static final String IMPORTED_DATA_MAGIC = "L#Vs";
	public static String toStringImportedData(List<MMLEventList> list) {
		var b = new ByteArrayOutputStream();
		DataOutputStream bo = new DataOutputStream(b);
		try {
			bo.writeBytes(IMPORTED_DATA_MAGIC);
			bo.writeByte(list.size());
			for (var item : list) {
				var noteEventList = item.getMMLNoteEventList();
				bo.writeInt(noteEventList.size());
				for (var note : noteEventList) {
					bo.writeShort(note.getNote());
					bo.writeInt(note.getTickOffset());
					bo.writeInt(note.getTick());
					bo.writeShort(note.getVelocity());
				}
			}

			// check sum
			int sum = 0;
			for (byte d : b.toByteArray()) {
				sum += d;
			}
			bo.writeByte(sum);
			String str = Utils.compress(b.toByteArray());
			if (!importedDataCache.containsKey(str)) {
				importedDataCache.put(str, list);
			}
			return str;
		} catch (IOException e) {}
		return "";
	}

	private static Map<String, List<MMLEventList>> importedDataCache = Collections.synchronizedMap(new CacheMap<>(8));
	public static List<MMLEventList> parseImportedData(String s) {
		if (s == null) {
			return null;
		}
		if (importedDataCache.containsKey(s)) {
			return importedDataCache.get(s);
		}
		var data = Utils.decompress(s);
		if (data == null) {
			importedDataCache.put(s, null);
			return null;
		}
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

			if (!new String(in.readNBytes(IMPORTED_DATA_MAGIC.length())).equals(IMPORTED_DATA_MAGIC)) {
				System.err.println("imported data: invalid magic");
				importedDataCache.put(s, null);
				return null;
			}
			int count = in.readByte();
			List<MMLEventList> list = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				var eventList = new MMLEventList("");
				int noteCount = in.readInt();
				for (int j = 0; j < noteCount; j++) {
					int note = in.readShort();
					int tickOffset = in.readInt();
					int tick = in.readInt();
					int velocity = in.readShort();
					var event = new MMLNoteEvent(note, tick, tickOffset, velocity);
					eventList.addMMLNoteEvent(event);
				}
				list.add(eventList);
			}

			// check sum
			int sum = 0;
			for (int i = 0; i < data.length - 1; i++) {
				sum += data[i];
			}
			sum &= 0xff;
			int a_sum = in.readByte();
			a_sum &= 0xff;
			if (sum != a_sum) {
				System.err.println("imported data: sum error");
				importedDataCache.put(s, null);
				return null;
			}

			importedDataCache.put(s, list);
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		importedDataCache.put(s, null);
		return null;
	}

	public void writeToOutputStream(OutputStream outputStream) {
		PrintStream stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8);

		stream.println(SCORE_SECTION);
		stream.println(SCORE_VERSION);
		stream.println(TITLE + score.getTitle());
		stream.println(AUTHOR + score.getAuthor());
		stream.println(TIME + score.getBaseTime());
		stream.println(TEMPO + getTempoObj());
		if (getStartOffsetAll() > 0) {
			stream.println(START_OFFSET + getStartOffsetAll());
		}

		for (MMLTrack track : score.getTrackList()) {
			// インスタンス時に先に指定したいので、オフセットたちは先に出力する
			if (track.getStartDelta() != 0) {
				stream.println(START_DELTA + track.getStartDelta());
			}
			if (track.getStartSongDelta() != 0) {
				stream.println(START_SONG_DELTA + track.getStartSongDelta());
			}
			//　本体
			stream.println(MML_TRACK + track.getOriginalMML());
			var importedData = track.getImportedData();
			if (importedData != null) {
				stream.println(IMPORTED_DATA + importedData);
			}
			stream.println(TRACK_NAME + track.getTrackName());
			stream.println(PROGRAM + track.getProgram());
			stream.println(SONG_PROGRAM + track.getSongProgram());
			stream.println(PANPOT + track.getPanpot());
			if (track.getVolume() != MMLTrack.INITIAL_VOLUME) {
				stream.println(VOLUME + track.getVolume());
			}
			stream.println(VISIBLE+track.isVisible());
			if (track.getAttackDelayCorrect() != 0) {
				stream.println(DELAY + track.getAttackDelayCorrect());
			}
			if (track.getAttackSongDelayCorrect() != 0) {
				stream.println(SONG_DELAY + track.getAttackSongDelayCorrect());
			}
			if (track.getDisableNopt()) {
				stream.println(DISABLE_NOPT + track.getDisableNopt());
			}
			if (track.getOptTempoMelodyOnly()) {
				stream.println(TEMPO_ONLY_MELODY);
			}
		}

		writeMMLEventList(stream, MARKER_SECTION, score.getMarkerList());
		writeMMLEventList(stream, TIME_SIGNATURE_SECTION, score.getTimeSignatureList());

		stream.close();
	}
}

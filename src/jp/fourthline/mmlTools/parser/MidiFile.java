/*
 * Copyright (C) 2017-2022 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sound.midi.*;

import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTempoEvent;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.Marker;
import jp.fourthline.mmlTools.core.MMLTickTable;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.ResourceLoader;
import jp.fourthline.mmlTools.core.UndefinedTickException;
import jp.fourthline.mmlTools.optimizer.MMLStringOptimizer;


/**
 * "*.mid" MIDIファイルの読み込み.
 */
public final class MidiFile extends AbstractMMLParser {
	private final MMLScore score = new MMLScore();
	private int resolution;

	private static final String PATCH_NAME = "mid_instPatch";

	// Parse Option
	private static final String PARSE_TRACK_NAME = "parse.midi.trackName";
	private static final String PARSE_BEAT = "parse.midi.beat";
	private static final String PARSE_TEMPO = "parse.midi.tempo";
	private static final String PARSE_MARKER = "parse.midi.marker";
	private static final String PARSE_CONVERT_OCTAVE = "parse.midi.convertOctave";
	private static final String PARSE_CONVERT_INST = "parse.midi.convertInst";
	private static final String PARSE_MULTI_TRACK = "parse.midi.multiTrack";

	// option value
	private boolean parseTrackName;
	private boolean parseBeat;
	private boolean parseTempo;
	private boolean parseMarker;
	private boolean parseConvertOctave;
	private boolean parseConvertInst;
	private boolean parseMultiTrack;

	private void updateOptions() {
		parseTrackName = parseProperties.getOrDefault(PARSE_TRACK_NAME, false);
		parseBeat = parseProperties.getOrDefault(PARSE_BEAT, false);
		parseTempo = parseProperties.getOrDefault(PARSE_TEMPO, false);
		parseMarker = parseProperties.getOrDefault(PARSE_MARKER, false);
		parseConvertOctave = parseProperties.getOrDefault(PARSE_CONVERT_OCTAVE, false);
		parseConvertInst = parseProperties.getOrDefault(PARSE_CONVERT_INST, false);
		parseMultiTrack = parseProperties.getOrDefault(PARSE_MULTI_TRACK, false);
	}

	/* MID->programへの変換 */
	private static boolean canConvertInst = false;
	private final Map<Integer, Integer> midInstTable = new HashMap<>();

	public static void enableInstPatch () {
		canConvertInst = true;
	}

	public MidiFile() {
		parseProperties = new LinkedHashMap<>();
		parseProperties.put(PARSE_TRACK_NAME, true);
		parseProperties.put(PARSE_BEAT, true);
		parseProperties.put(PARSE_TEMPO, true);
		parseProperties.put(PARSE_MULTI_TRACK, true);
		parseProperties.put(PARSE_MARKER, false);
		if (canConvertInst) {
			parseProperties.put(PARSE_CONVERT_OCTAVE, true);
			parseProperties.put(PARSE_CONVERT_INST, false);
		}

		try {
			ResourceBundle instPatch = ResourceBundle.getBundle(PATCH_NAME, new ResourceLoader());
			for (String key : instPatch.keySet()) {
				String newInst = instPatch.getString(key).replaceAll("#.*", "");
				int keyInt = Integer.parseInt(key.trim());
				int newInstInt = Integer.parseInt(newInst.trim());
				System.out.println("[MID-PATCH] " + keyInt + " -> " + newInstInt);
				midInstTable.put(keyInt, newInstInt);
			}
		} catch (MissingResourceException e) {}
	}

	@Override
	public String getName() {
		return "MIDI";
	}

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		updateOptions();
		try {
			Sequence seq = MidiSystem.getSequence(istream);
			resolution = seq.getResolution();
			System.out.println(seq.getTracks().length);
			System.out.println(resolution);
			System.out.println(seq.getDivisionType());
			System.out.println(seq.getMicrosecondLength());
			System.out.println(seq.getTickLength());

			int count = 1;
			for (Track track : seq.getTracks()) {
				parseTrack(track, count++);
			}
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}

		score.getTempoEventList().addAll(tempoList);
		try {
			return score.generateAll();
		} catch (UndefinedTickException e) {
			return score;
		}
	}

	private final HashMap<Integer, MMLNoteEvent> activeNoteMap = new HashMap<>();
	private final ArrayList<MMLNoteEvent> curNoteList = new ArrayList<>();
	private final ArrayList<MMLTempoEvent> tempoList = new ArrayList<>();

	private static final class TrackInfo {
		private String name;
		private int panpot = 64;
		private int program = 0;
		private TrackInfo(int count) {
			name = "Track"+count;
		}
		private MMLTrack createMMLTrack() {
			MMLTrack track = new MMLTrack();
			track.setTrackName(name);
			track.setPanpot(panpot);
			track.setProgram(program);
			return track;
		}
		private void setName(String name) {
			if ( (name != null) && (name.length() > 0) ) {
				this.name = name;
			}
		}
		private void setProgram(int data) {
			this.program = data;
		}
	}

	/**
	 * トラックデータパース
	 * @param track
	 * @param count
	 * @throws MMLParseException
	 */
	private void parseTrack(Track track, int count) throws MMLParseException {
		TrackInfo trackInfo = new TrackInfo(count);
		activeNoteMap.clear();
		curNoteList.clear();
		System.out.println(" - track -");
		System.out.println(track.size());
		ArrayList<MidiEvent> midiEventList = new ArrayList<>(track.size());
		for (int i = 0; i < track.size(); i++) {
			midiEventList.add(track.get(i));
		}

		for (MidiEvent event : midiEventList) {
			MidiMessage msg = event.getMessage();
			long tick = convTick( event.getTick() );
			if (msg instanceof MetaMessage) {
				parseMetaMessage((MetaMessage)msg, tick, trackInfo);
			} else if (msg instanceof ShortMessage) {
				parseShortMessage((ShortMessage)msg, tick, trackInfo);
			} else if (msg instanceof SysexMessage) {
				System.out.println("Sysex");
			} else {
				throw new MMLParseException("Unknown MIDI message.");
			}
		}

		// ノートイベントから重複しないMMLEventListへ.
		ArrayList<MMLEventList> mmlEventList = createMMLEventList();

		// MMLEventListのリストを使ってトラックを生成.
		System.out.printf(" ###### track tick: %d %d => %d\n",
				activeNoteMap.size(),
				curNoteList.size(),
				mmlEventList.size());
		createMMLTrack(mmlEventList, trackInfo);
	}

	/**
	 * 整列済みノートイベントからMMLTrackをつくる
	 * @param eventList
	 * @param trackInfo
	 */
	private void createMMLTrack(ArrayList<MMLEventList> eventList, TrackInfo trackInfo) {
		try {
			while (eventList.size() > 0) {
				String mml[] = new String[3];
				for (int i = 0; i < mml.length; i++) {
					if (!eventList.isEmpty()) {
						mml[i] = new MMLStringOptimizer(eventList.get(0).getInternalMMLString()).toString();
						eventList.remove(0);
					} else {
						mml[i] = "";
					}
				}
				MMLTrack track = trackInfo.createMMLTrack();
				track.setMML(mml[0], mml[1], mml[2], "");
				score.addTrack(track);
				if (!parseMultiTrack) {
					break;
				}
			}
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 取り込んだノートイベントから重複しないMMLEventListを生成する.
	 * @return
	 */
	private ArrayList<MMLEventList> createMMLEventList() {
		ArrayList<MMLEventList> eventList = new ArrayList<>();

		for (MMLNoteEvent noteEvent : curNoteList) {
			for (MMLEventList e : eventList) {
				if (!e.isOverlapNote(noteEvent)) {
					e.addMMLNoteEvent(noteEvent);
					noteEvent = null;
					break;
				}
			}
			if (noteEvent != null) {
				MMLEventList e = new MMLEventList("");
				e.addMMLNoteEvent(noteEvent);
				eventList.add(e);
			}
		}

		return eventList;
	}

	/**
	 * メタメッセージ
	 * @param msg
	 * @param tick
	 * @param trackInfo
	 */
	private void parseMetaMessage(MetaMessage msg, long tick, TrackInfo trackInfo) {
		System.out.print(tick+" > ");
		int type = msg.getType();
		byte[] data = msg.getData();
		switch (type) {
		case MMLTempoEvent.META: // テンポ
			ByteBuffer buf = ByteBuffer.allocate(4);
			buf.put((byte)0);
			buf.put(data);
			int tempo = 60000000/buf.getInt(0);
			System.out.println("Tempo: "+tempo);
			if (parseTempo) {
				new MMLTempoEvent(tempo, (int)tick).appendToListElement(tempoList);
			}
			break;
		case 3: // シーケンス名/トラック名
			String name = new String(data);
			System.out.println("Name: "+name);
			if (parseTrackName) {
				trackInfo.setName(name);
			}
			break;
		case 1: // テキストイベント
			System.out.println("Text: "+new String(data));
			break;
		case 2: // 著作権表示
			System.out.println("(C): "+new String(data));
			break;
		case 6: // マーカー
			String s = new String(data);
			System.out.println("Marker: "+s);
			if (parseMarker) {
				score.getMarkerList().add(new Marker(s, (int) tick));
			}
			break;
		case 4: // 楽器名
		case 5: // 歌詞
		case 7: // キューポイント
			System.out.println("Text(" + type + "): "+new String(data));
			break;
		case 0x58: // 拍子/メトロノーム設定
			System.out.printf("met: %d %d %d %d\n", data[0], 1<<data[1], data[2], data[3]);
			if (parseBeat) {
				score.setBaseOnly(1<<data[1]);
				score.setTimeCountOnly(data[0]);
			}
			break;
		case 0x59: // 調号
			System.out.printf("sig: %d %d\n", data[0], data[1]);
			break;
		default:
			System.out.printf("Meta: [%x] [%d]\n", type, data.length);
			break;
		}
	}

	/**
	 * ショートメッセージ
	 * @param msg
	 * @param tick
	 * @param trackInfo
	 * @throws MMLParseException
	 */
	private void parseShortMessage(ShortMessage msg, long tick, TrackInfo trackInfo) throws MMLParseException {
		int command = msg.getCommand();
		int channel = msg.getChannel();
		int data1 = msg.getData1();
		int data2 = msg.getData2();
		switch (command) {
		case ShortMessage.CONTROL_CHANGE:
			if (data1 == 10) { // panpot
				trackInfo.panpot = data2;
			}
			System.out.printf("control change: [%d] [%d]\n", data1, data2);
			break;
		case ShortMessage.NOTE_ON:
			if (data2 > 0) {
				int note = data1 - (parseConvertOctave ? 12 : 0);
				int velocity = data2 / 8;
				if (!activeNoteMap.containsKey(note)) {
					MMLNoteEvent noteEvent = new MMLNoteEvent(note, 0, (int)tick, velocity);
					activeNoteMap.put(note, noteEvent);
					curNoteList.add(noteEvent);
				}
				break;
			}
			// data2 == 0 は Note Off.
		case ShortMessage.NOTE_OFF:
			int note = data1 - (parseConvertOctave ? 12 : 0);
			MMLNoteEvent noteEvent = activeNoteMap.get(note);
			if (noteEvent != null) {
				tick -= noteEvent.getTickOffset();
				if (tick < MMLTicks.minimumTick()) {
					tick = MMLTicks.minimumTick();
				}
				noteEvent.setTick( (int)tick );
				activeNoteMap.remove(note);
			}
			break;
		case ShortMessage.PROGRAM_CHANGE:
			System.out.printf("program change: [%d] [%d]\n", data1, data2);
			if (!canConvertInst) {
				trackInfo.setProgram(data1);
			} else if (parseConvertInst && midInstTable.containsKey(data1)) {
				data1 = midInstTable.get(data1);
				trackInfo.setProgram(data1);
				System.out.println("   -> " + data1);
			}
			break;
		default:
			System.out.printf("short: [%x] [%d] [%d] [%d]\n", command, channel, data1, data2);
		}
	}

	/**
	 * Tick変換
	 * @param tick
	 * @return
	 */
	private long convTick(long tick) {
		int min = MMLTicks.minimumTick();
		long value = (tick * MMLTickTable.TPQN / resolution) + (min/2);
		value -= value % min;
		return value;
	}

	public static void main(String[] args) {
		try {
			MMLScore score = new MidiFile().parse(new FileInputStream("sample2.mid"));
			score.generateAll();
		} catch (FileNotFoundException | MMLParseException | UndefinedTickException e) {
			e.printStackTrace();
		}
	}
}

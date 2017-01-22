/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.*;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTickTable;
import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.core.UndefinedTickException;
import fourthline.mmlTools.optimizer.MMLStringOptimizer;


/**
 * "*.mid" MIDIファイルの読み込み.
 */
public final class MidiFile implements IMMLFileParser {
	private final MMLScore score = new MMLScore();

	private int resolution;

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
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

	private HashMap<Integer, MMLNoteEvent> activeNoteMap = new HashMap<>();
	private ArrayList<MMLNoteEvent> curNoteList = new ArrayList<>();
	private ArrayList<MMLTempoEvent> tempoList = new ArrayList<>();

	class TrackInfo {
		String name;
		int panpot = 64;
		TrackInfo(int count) {
			name = "Track"+count;
		}
		MMLTrack createMMLTrack() {
			MMLTrack track = new MMLTrack();
			track.setTrackName(name);
			track.setPanpot(panpot);
			return track;
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
						mml[i] = new MMLStringOptimizer(eventList.get(0).toMMLString(false, false)).toString();
						eventList.remove(0);
					} else {
						mml[i] = "";
					}
				}
				MMLTrack track = trackInfo.createMMLTrack();
				track.setMML(mml[0], mml[1], mml[2], "");
				score.addTrack(track);
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
		int type = ((MetaMessage) msg).getType();
		byte[] data = ((MetaMessage) msg).getData();
		switch (type) {
		case MMLTempoEvent.META: // テンポ
			ByteBuffer buf = ByteBuffer.allocate(4);
			buf.put((byte)0);
			buf.put(data);
			int tempo = 60000000/buf.getInt(0);
			System.out.println("Tempo: "+tempo);
			new MMLTempoEvent(tempo, (int)tick).appendToListElement(tempoList);
			break;
		case 3: // シーケンス名/トラック名
			String name = new String(data);
			System.out.println("Name: "+name);
			break;
		case 1: // テキストイベント
		case 2: // 著作権表示
		case 4: // 楽器名
		case 5: // 歌詞
		case 6: // マーカー
		case 7: // キューポイント
			System.out.println("Text: "+new String(data));
			break;
		case 0x58: // 拍子/メトロノーム設定
			System.out.printf("met: %d %d %d %d\n", data[0], 1<<data[1], data[2], data[3]);
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
				int note = data1 - 12;
				int velocity = data2 / 8;
				if (!activeNoteMap.containsKey(note)) {
					MMLNoteEvent noteEvent = new MMLNoteEvent(note, 0, (int)tick, velocity);
					activeNoteMap.put(note, noteEvent);
					curNoteList.add(noteEvent);
					break;
				}
			}
		case ShortMessage.NOTE_OFF:
			int note = data1 - 12;
			MMLNoteEvent noteEvent = activeNoteMap.get(note);
			if (noteEvent != null) {
				noteEvent.setTick( (int) (tick - noteEvent.getTickOffset()) );
				activeNoteMap.remove(note);
			}
			break;
		case ShortMessage.PROGRAM_CHANGE:
			System.out.printf("program change: [%d] [%d]\n", data1, data2);
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
		long value = (tick * MMLTickTable.TPQN / resolution) + min - 1 ;
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

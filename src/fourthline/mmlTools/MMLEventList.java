/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import fourthline.mmlTools.parser.MMLEventParser;


/**
 * 1行のMMLデータを扱います.
 */
public class MMLEventList {

	private List<MMLNoteEvent>   noteList   = new ArrayList<MMLNoteEvent>();
	private List<MMLTempoEvent>  tempoList  = new ArrayList<MMLTempoEvent>();
	private List<MMLVelocityEvent> velocityList = new ArrayList<MMLVelocityEvent>();


	/**
	 * 
	 * @param mml
	 */
	public MMLEventList(String mml) {
		parseMML(mml);
	}

	private void parseMML(String mml) {
		MMLEventParser parser = new MMLEventParser(mml);

		while (parser.hasNext()) {
			MMLEvent event = parser.next();

			if (event instanceof MMLTempoEvent) {
				tempoList.add((MMLTempoEvent) event);
			} else if (event instanceof MMLVelocityEvent) {
				velocityList.add((MMLVelocityEvent) event);
			} else if (event instanceof MMLNoteEvent) {
				if (((MMLNoteEvent) event).getNote() >= 0) {
					noteList.add((MMLNoteEvent) event);
				}
			}
		}
	}

	public long getTickLength() {
		if (noteList.size() > 0) {
			int lastIndex = noteList.size() - 1;
			MMLNoteEvent lastNote = noteList.get( lastIndex );

			return lastNote.getEndTick();
		} else {
			return 0;
		}
	}

	public List<MMLNoteEvent> getMMLNoteEventList() {
		return noteList;
	}

	private static final int INITIAL_TEMPO = 120;
	public MMLTempoEvent getTempoOnTick(long tick) {
		for (int i = 0; i < tempoList.size(); i++) {
			MMLTempoEvent tempoEvent = tempoList.get(i);
			if (tempoEvent.getTickOffset() <= tick) {
				return tempoEvent;
			}
		}

		return new MMLTempoEvent(INITIAL_TEMPO, 0);
	}


	private int convertVelocityMML2Midi(int mml_velocity) {
		return (mml_velocity * 8);
	}
	private int convertNoteMML2Midi(int mml_note) {
		return (mml_note + 12);
	}

	private static final int INITIAL_VOLEUMN = 8;
	public void convertMidiTrack(Track track, int channel) throws InvalidMidiDataException {
		int volumn = INITIAL_VOLEUMN;

		// テンポ
		for ( Iterator<MMLTempoEvent> i = tempoList.iterator(); i.hasNext(); ) {
			MMLTempoEvent tempoEvent = i.next();
			byte tempo[] = tempoEvent.getMetaData();
			int tickOffset = tempoEvent.getTickOffset();

			MidiMessage message = new MetaMessage(MMLTempoEvent.META, 
					tempo, tempo.length);
			track.add(new MidiEvent(message, tickOffset));
		}

		//　ボリューム
		Iterator<MMLVelocityEvent> volumnIterator = velocityList.iterator();
		MMLVelocityEvent volumnEvent = null;
		if (volumnIterator.hasNext()) {
			volumnEvent = volumnIterator.next();
		} else {
			volumnEvent = null;
		}
		// Noteイベントの変換
		for ( Iterator<MMLNoteEvent> i = noteList.iterator(); i.hasNext(); ) {
			MMLNoteEvent noteEvent = i.next();
			System.out.println(" [to midi] " + noteEvent.toString());
			int note = noteEvent.getNote();
			int tick = noteEvent.getTick();
			int tickOffset = noteEvent.getTickOffset();
			int endTickOffset = tickOffset + tick - 1;

			// ボリュームの変更
			if ( (volumnEvent != null) && (volumnEvent.getTickOffset() <= tickOffset) ) {
				volumn = volumnEvent.getVelocity();
				System.out.println(" [to midi] " + volumnEvent.toString());
				if (volumnIterator.hasNext()) {
					volumnEvent = volumnIterator.next();
				} else {
					volumnEvent = null;
				}
			}

			// ON イベント作成
			MidiMessage message1 = new ShortMessage(ShortMessage.NOTE_ON, 
					channel,
					convertNoteMML2Midi(note), 
					convertVelocityMML2Midi(volumn));
			track.add(new MidiEvent(message1, tickOffset));

			// Off イベント作成
			MidiMessage message2 = new ShortMessage(ShortMessage.NOTE_OFF,
					channel, 
					convertNoteMML2Midi(note),
					0);
			track.add(new MidiEvent(message2, endTickOffset));
		}
	}


	/**
	 * 指定したtickOffset位置にあるNoteEventを検索します.
	 * @param tickOffset
	 * @return 見つからなかった場合は、nullを返します.
	 */
	public MMLNoteEvent searchOnTickOffset(long tickOffset) {
		for (int i = 0; i < noteList.size(); i++) {
			MMLNoteEvent noteEvent = noteList.get(i);
			if (noteEvent.getTickOffset() <= tickOffset) {
				if (tickOffset <= noteEvent.getEndTick()) {
					return noteEvent;
				}
			} else {
				break;
			}
		}

		return null;
	}

	/**
	 * ノートイベントを追加します.
	 * @param addItem
	 * @param tickOffset
	 * @param editTick
	 * @param editIndex
	 */
	public void addMMLNoteEvent(MMLNoteEvent addNoteEvent) {
		int i;
		if ((addNoteEvent.getNote() <= 0) || (addNoteEvent.getTick() <= 0)) {
			return;
		}

		// 追加したノートイベントに重なる前のノートを調節します.
		for (i = 0; i < noteList.size(); i++) {
			MMLNoteEvent noteEvent = noteList.get(i);
			int tickOverlap = noteEvent.getEndTick() - addNoteEvent.getTickOffset();
			if (addNoteEvent.getTickOffset() < noteEvent.getTickOffset()) {
				break;
			}
			if (tickOverlap >= 0) {
				// 追加するノートに音が重なっている.
				int tick = noteEvent.getTick() - tickOverlap;
				if (tick == 0) {
					noteList.remove(i);
					break;
				} else {
					noteEvent.setTick(tick);
					i++;
					break;
				}
			}
		}

		// ノートイベントを追加します.
		noteList.add(i++, addNoteEvent);

		// 追加したノートイベントに重なっている後続のノートを削除します.
		for ( ; i < noteList.size(); ) {
			MMLNoteEvent noteEvent = noteList.get(i);
			int tickOverlap = addNoteEvent.getEndTick() - noteEvent.getTickOffset();

			if (tickOverlap > 0) {
				noteList.remove(i);
			} else {
				break;
			}
		}
	}

	/**
	 * テンポイベントを追加します.
	 * @param addItem
	 * @param tickOffset
	 * @param editTick
	 * @param editIndex
	 */
	public void addMMLTempoEvent(MMLTempoEvent addTempoEvent) {
		// TODO: イベントが重複しないよう注意すること!!!
	}

	/**
	 * 音量イベントを追加します.
	 * @param addItem
	 * @param tickOffset
	 * @param editTick
	 * @param editIndex
	 */
	public void addMMLVelocityEvent(MMLVelocityEvent addVolumnEvent) {
		// TODO: イベントが重複しないよう注意すること!!!
	}

	/**
	 * 指定のMMLeventを削除する.
	 * 最後尾はtrim.
	 * @param deleteItem
	 */
	public void deleteMMLEvent(MMLEvent deleteItem) {
		noteList.remove(deleteItem);
	}
	
	public String toMMLString() {
//		MMLTempoEvent tempoEvent = tempoList.get(0);
//		MMLVelocityEvent velocityEvent = velocityList.get(0);
		
		StringBuilder sb = new StringBuilder();
		int noteCount = noteList.size();
		
		// initial note: octave 4, tick 0, offset 0
		MMLNoteEvent prevNoteEvent = new MMLNoteEvent(12*4, 0, 0);
		
		for (int i = 0; i < noteCount; i++) {
			MMLNoteEvent noteEvent = noteList.get(i);
			sb.append( noteEvent.toMMLString(prevNoteEvent) );
			prevNoteEvent = noteEvent;
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return tempoList.toString() + velocityList.toString() + noteList.toString();
	}
}

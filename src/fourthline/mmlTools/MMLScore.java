/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import fourthline.mabiicco.midi.MabiDLS;

/**
 *
 */
public class MMLScore {
	private ArrayList<MMLTrack> trackList = new ArrayList<MMLTrack>();
	private List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();

	private static final int MAX_TRACK = 8;

	/**
	 * 新たにトラックを追加します.
	 * @param track
	 * @return トラック数の上限を超えていて、追加できないときは -1. 追加できた場合は、追加したindex値を返します(0以上).
	 */
	public int addTrack(MMLTrack track) {
		if (trackList.size() >= MAX_TRACK) {
			return -1;
		}

		// トラックリストの末尾に追加
		trackList.add(track);
		int trackIndex = trackList.size() - 1;

		// グローバルテンポリストの統合.
		MMLTempoEvent.mergeTempoList(track.getGlobalTempoList(), globalTempoList);
		track.setGlobalTempoList(globalTempoList);

		return trackIndex;
	}

	/**
	 * 指定したindexのトラックを削除します.
	 * @param index
	 */
	public void removeTrack(int index) {
		trackList.remove(index);
	}

	/**
	 * 保持しているトラックの数を返します.
	 * @return
	 */
	public int getTrackCount() {
		return trackList.size();
	}

	/**
	 * 指定したindexのトラックを返します.
	 * @param index
	 * @return
	 */
	public MMLTrack getTrack(int index) {
		return trackList.get(index);
	}

	/**
	 * 指定されたindexにトラックをセットします.
	 * @param index
	 * @param track
	 */
	public void setTrack(int index, MMLTrack track) {
		trackList.set(index, track);
	}

	public int getTempoOnTick(long tickOffset) {
		return MMLTempoEvent.searchOnTick(globalTempoList, tickOffset);
	}

	public List<MMLTempoEvent> getTempoEventList() {
		return globalTempoList;
	}

	/**
	 * MIDIシーケンスを作成します。
	 * @throws InvalidMidiDataException 
	 */
	public Sequence createSequence() throws InvalidMidiDataException {
		Sequence sequence = new Sequence(Sequence.PPQ, 96);

		int trackCount = getTrackCount();
		for (int i = 0; i < trackCount; i++) {
			MMLTrack mmlTrack = getTrack(i);
			mmlTrack.convertMidiTrack(sequence.createTrack(), i);
			// FIXME: パンポットの設定はここじゃない気がする～。
			int panpot = mmlTrack.getPanpot();
			MabiDLS.getInstance().setChannelPanpot(i, panpot);
		}

		// グローバルテンポ
		Track track = sequence.getTracks()[0];
		for (MMLTempoEvent tempoEvent : globalTempoList) {
			byte tempo[] = tempoEvent.getMetaData();
			int tickOffset = tempoEvent.getTickOffset();

			MidiMessage message = new MetaMessage(MMLTempoEvent.META, 
					tempo, tempo.length);
			track.add(new MidiEvent(message, tickOffset));
		}

		return sequence;
	}
}

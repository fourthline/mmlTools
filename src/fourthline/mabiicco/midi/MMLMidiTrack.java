/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mabiicco.midi;

import java.util.ArrayList;
import java.util.List;

import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.core.MMLTicks;

/**
 * 複数のMMLNoteEventリストから, MIDIトラック用リストに変換する.
 *   マビノギ内の演奏とは若干ちがうけど！（気にしない
 *   TODO: 再生方式の完全な変更が必要.
 */
public class MMLMidiTrack {
	private List<MMLTempoEvent> tempoList;
	private ArrayList<MMLNoteEvent> noteEventList;

	public MMLMidiTrack(List<MMLTempoEvent> tempoList) {
		if (tempoList != null) {
			this.tempoList = tempoList;
		} else {
			this.tempoList = new ArrayList<>();
		}
		noteEventList = new ArrayList<>();
	}

	public List<MMLNoteEvent> getNoteEventList() {
		return noteEventList;
	}

	public void add(List<MMLNoteEvent> list) {
		for (MMLNoteEvent noteEvent : list) {
			addItem(noteEvent.clone());
		}
	}

	private void addItem(MMLNoteEvent addEvent) {
		int targetTick = addEvent.getTickOffset();
		int targetIndex = 0;
		for (MMLNoteEvent noteEvent : noteEventList) {
			if (noteEvent.getTickOffset() > targetTick) {
				break;
			}
			targetIndex++;
			if ( (noteEvent.getTickOffset() == targetTick) && (noteEvent.getNote() == addEvent.getNote())) {
				break;
			}
		}

		// 前の音との重複修正
		if ( targetIndex > 0 ) {
			MMLNoteEvent prevEvent = noteEventList.get( targetIndex - 1 );
			if (addEvent.getNote() == prevEvent.getNote()) {
				if ( prevEvent.getTickOffset() == targetTick ) {
					// 開始位置が同じときには, 後発音で更新する.
					// 前の音とテンポ指定がある場合は元あったノートのまま.
					// 後発音が V0 の場合は l64音に更新する.
					if (!MMLTempoEvent.searchEqualsTick(tempoList, targetTick)) {
						if (addEvent.getVelocity() == 0) {
							prevEvent.setTick(MMLTicks.minimumTick());
						}
						return;
					} else {
						if (prevEvent.getVelocity() == 0) {
							addEvent.setTick(MMLTicks.minimumTick());
						}
						targetIndex--;
						noteEventList.remove(targetIndex);
					}
				} else {
					trimOverlapNote(prevEvent, addEvent);
				}
			}
		}

		// 後ろの音との重複修正
		if ( targetIndex < noteEventList.size() ) {
			MMLNoteEvent nextEvent = noteEventList.get( targetIndex );
			trimOverlapNote(addEvent, nextEvent);
		}

		noteEventList.add(targetIndex, addEvent);
	}

	private void trimOverlapNote(MMLNoteEvent note1, MMLNoteEvent note2) {
		if (note1.getTickOffset() >= note2.getTickOffset()) {
			new AssertionError();
		}
		if (note1.getNote() == note2.getNote()) {
			int tickOverlap = note1.getEndTick() - note2.getTickOffset();
			if (tickOverlap > 0) {
				note1.setTick( note1.getTick() - tickOverlap );
			}
		}
	}
}

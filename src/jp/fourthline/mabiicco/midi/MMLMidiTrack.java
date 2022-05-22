/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.util.ArrayList;
import java.util.List;

import jp.fourthline.mmlTools.MMLNoteEvent;

/**
 * 複数のMMLNoteEventリストから, MIDIトラック用リストに変換する.
 *   マビノギ内の演奏とは若干ちがうけど！（気にしない
 *   TODO: 再生方式の完全な変更が必要.
 */
public final class MMLMidiTrack {
	private final InstClass inst;
	private final ArrayList<MMLNoteEvent> noteEventList;
	private int attackDelayCorrect = 0;

	public MMLMidiTrack(InstClass inst) {
		this.inst = inst;
		noteEventList = new ArrayList<>();
	}

	public List<MMLNoteEvent> getNoteEventList() {
		return noteEventList;
	}

	public void setAttackDelayCorrect(int attackDelayCorrect) {
		this.attackDelayCorrect = attackDelayCorrect;
	}

	public void add(List<MMLNoteEvent> list) {
		for (MMLNoteEvent noteEvent : list) {
			addItem(noteEvent.clone());
		}
	}

	private void addItem(MMLNoteEvent addEvent) {
		int targetTick = addEvent.getTickOffset();
		int targetIndex = 0;

		// アタック遅延補正分
		if (attackDelayCorrect != 0) {
			targetTick += attackDelayCorrect;
			if (targetTick < 0) {
				int tick = addEvent.getTick() + targetTick;
				if (tick <= 0) return;
				targetTick = 0;
				addEvent.setTick(tick);
			}
			addEvent.setTickOffset(targetTick);
		}

		for (MMLNoteEvent noteEvent : noteEventList) {
			if (noteEvent.getTickOffset() > targetTick) {
				break;
			}
			targetIndex++;
			if ( (noteEvent.getTickOffset() == targetTick) && (noteEvent.getNote() == addEvent.getNote())) {
				break;
			}
		}

		addEvent = overlapNote(targetIndex, addEvent);
		if (addEvent != null) {
			noteEventList.add(targetIndex, addEvent);
		}
	}

	private MMLNoteEvent overlapNote(int targetIndex, MMLNoteEvent addEvent) {
		if (inst.isOverlap(addEvent.getNote())) {
			return addEvent;
		}

		int targetTick = addEvent.getTickOffset();

		// 前の音との重複修正
		if ( targetIndex > 0 ) {
			MMLNoteEvent prevEvent = noteEventList.get( targetIndex - 1 );
			if (addEvent.getNote() == prevEvent.getNote()) {
				if ( prevEvent.getTickOffset() == targetTick ) {
					/* 音量はテンポの有無でどちらかのノートの設定になるが対応しない */
					if (prevEvent.getTick() >= addEvent.getTick()) {
						noteEventList.get(targetIndex-1).setTick(addEvent.getTick());
					}
					return null;

					// 2021/09/18 テンポを和音出力し、ゲーム内の鳴り方もかわったようなので以下コードは使用しない
					// 開始位置が同じときには, 後発音で更新する.
					// 前の音とテンポ指定がある場合は元あったノートのまま.
					// 後発音が V0 の場合は l64音に更新する.
//					if (!MMLTempoEvent.searchEqualsTick(tempoList, targetTick)) {
//						if (addEvent.getVelocity() == 0) {
//							prevEvent.setTick(MMLTicks.minimumTick());
//						}
//						return null;
//					} else {
//						if (prevEvent.getVelocity() == 0) {
//							addEvent.setTick(MMLTicks.minimumTick());
//						}
//						targetIndex--;
//						noteEventList.remove(targetIndex);
//					}
//				} else {
//					trimOverlapNote(prevEvent, addEvent);
				}
			}
		}

		// 後ろの音との重複修正
		if ( targetIndex < noteEventList.size() ) {
			MMLNoteEvent nextEvent = noteEventList.get( targetIndex );
			trimOverlapNote(addEvent, nextEvent);
		}

		return addEvent;
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

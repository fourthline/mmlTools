/*
 * Copyright (C) 2013-2023 たんらる
 */

package jp.fourthline.mmlTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.parser.MMLEventParser;


/**
 * 1行のMMLデータを扱います.
 */
public final class MMLEventList implements Serializable, Cloneable {
	private static final long serialVersionUID = -1430758411579285535L;

	private List<MMLNoteEvent>   noteList   = new ArrayList<>();
	private List<MMLTempoEvent>  tempoList;

	/**
	 * 
	 * @param mml
	 */
	public MMLEventList(String mml) {
		this(mml, null);
	}

	public MMLEventList(String mml, List<MMLTempoEvent> globalTempoList) {
		if (globalTempoList != null) {
			tempoList = globalTempoList;
		} else {
			tempoList = new ArrayList<>();
		}

		parseMML(mml, 0);
	}

	public MMLEventList(String mml, List<MMLTempoEvent> globalTempoList, int startOffset) {
		if (globalTempoList != null) {
			tempoList = globalTempoList;
		} else {
			tempoList = new ArrayList<>();
		}

		parseMML(mml, startOffset);
	}

	private void parseMML(String mml, int startOffset) {
		MMLEventParser parser = new MMLEventParser(mml, startOffset);

		while (parser.hasNext()) {
			MMLEvent event = parser.next();

			if (event instanceof MMLTempoEvent tempoEvent) {
				tempoEvent.appendToListElement(tempoList);
			} else if (event instanceof MMLNoteEvent noteEvent) {
				noteList.add(noteEvent);
			}
		}
	}

	public void setGlobalTempoList(List<MMLTempoEvent> globalTempoList) {
		tempoList = globalTempoList;
	}

	public List<MMLTempoEvent> getGlobalTempoList() {
		return tempoList;
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

	public boolean isEmpty() {
		return noteList.isEmpty();
	}

	public List<MMLNoteEvent> getMMLNoteEventList() {
		return noteList;
	}

	/**
	 * 指定したtickOffset位置にあるNoteEventを検索します.
	 * @param tickOffset
	 * @return 見つからなかった場合は、nullを返します.
	 */
	public MMLNoteEvent searchOnTickOffset(long tickOffset) {
		for (MMLNoteEvent noteEvent : noteList) {
			if (noteEvent.getTickOffset() <= tickOffset) {
				if (tickOffset < noteEvent.getEndTick()) {
					return noteEvent;
				}
			} else {
				break;
			}
		}

		return null;
	}

	/**
	 * 指定したtickOffset位置の手前のNoteを検索します.
	 * @param tickOffset
	 * @return
	 */
	public MMLNoteEvent searchPrevNoteOnTickOffset(long tickOffset) {
		MMLNoteEvent prevNote = null;
		for (MMLNoteEvent noteEvent : noteList) {
			if (noteEvent.getTickOffset() >= tickOffset) {
				break;
			}
			prevNote = noteEvent;
		}
		return prevNote;
	}

	/**
	 * 指定したtickOffset位置のparsed-MML文字列に対するIndexを取得します.
	 * @param tickOffset
	 * @return
	 */
	public int[] indexOfMMLString(long tickOffset) {
		int start = 0;
		for (MMLNoteEvent noteEvent : noteList) {
			int[] index = noteEvent.getIndexOfMMLString();
			if (noteEvent.getTickOffset() <= tickOffset) {
				if (tickOffset < noteEvent.getEndTick()) {
					return index;
				}
			} else {
				return new int[] { start, index[0] };
			}
			start = index[1];
		}
		return new int[] { start, start };
	}

	/**
	 * ノートイベントを追加します.
	 * TODO: MMLNoteEvent のメソッドのほうがいいかな？Listを引数として渡す.
	 * @param addNoteEvent
	 */
	public void addMMLNoteEvent(MMLNoteEvent addNoteEvent) {
		int i;
		if ((addNoteEvent.getNote() < -1) || (addNoteEvent.getTick() <= 0) || (addNoteEvent.getEndTick() <= 0)) {
			return;
		}
		int offset = addNoteEvent.getTickOffset();
		if (offset < 0) {
			addNoteEvent.setTick( (addNoteEvent.getTick() + offset) );
			addNoteEvent.setTickOffset(0);
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
	 * リスト中のノートイベントに重複しているかを判定します.
	 * @param noteEvent  判定するノートイベント.
	 * @return 重複している場合は trueを返します.
	 */
	public boolean isOverlapNote(MMLNoteEvent noteEvent) {
		int i;
		int size = noteList.size();
		for (i = 0; i < size; i++) {
			MMLNoteEvent e = noteList.get(i);
			if (noteEvent.getTickOffset() < e.getEndTick()) {
				if (noteEvent.getTickOffset() >= e.getTickOffset()) {
					return true;
				}
				break;
			}
		}
		for (; i < size; i++) {
			MMLNoteEvent e = noteList.get(i);
			if (noteEvent.getEndTick() <= e.getEndTick()) {
				if (noteEvent.getEndTick()-1 >= e.getTickOffset()) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	/**
	 * 指定のMMLeventを削除する.
	 * 最後尾はtrim.
	 * @param deleteItem
	 */
	public void deleteMMLEvent(MMLEvent deleteItem) {
		noteList.remove(deleteItem);
	}

	/**
	 * 指定されたノートに音量コマンドを設定する.
	 * 後続の同音量のノートも更新する.
	 * @param targetNote
	 * @param velocity
	 */
	public void setVelocityCommand(MMLNoteEvent targetNote, int velocity) {
		setUnsetVelocityCommand(targetNote, velocity, true);
	}

	/**
	 * 指定されたノートに音量コマンドを解除する.
	 * 後続の同音量のノートも更新する.
	 * @param targetNote
	 */
	public void unsetVelocityCommand(MMLNoteEvent targetNote) {
		setUnsetVelocityCommand(targetNote, 0, false);
	}

	private void setUnsetVelocityCommand(MMLNoteEvent targetNote, int velocity, boolean isON) {
		int beforeVelocity = targetNote.getVelocity();
		int prevVelocity = MMLNoteEvent.INIT_VOL;
		for (MMLNoteEvent note : noteList) {
			if (note.getTickOffset() >= targetNote.getTickOffset()) {
				if (beforeVelocity == note.getVelocity()) {
					note.setVelocity(isON ? velocity : prevVelocity);
				} else {
					break;
				}
			} else {
				prevVelocity = note.getVelocity();
			}
		}
	}

	@Override
	public String toString() {
		return tempoList.toString() + noteList.toString();
	}

	@Override
	public MMLEventList clone() {
		try {
			MMLEventList obj = (MMLEventList) super.clone();
			obj.noteList = new ArrayList<>();
			for (MMLNoteEvent note : noteList) {
				obj.noteList.add(note.clone());
			}
			obj.tempoList = new ArrayList<>();
			for (MMLTempoEvent tempo : tempoList) {
				obj.tempoList.add(tempo.clone());
			}
			return obj;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e.getMessage());
		}
	}

	public int getAlignmentStartTick(MMLEventList list2, int tickOffset) {
		MMLNoteEvent target = list2.searchOnTickOffset(tickOffset);
		if ( (target == null) || (target.getTickOffset() == tickOffset) ) {
			return tickOffset;
		} else {
			return list2.getAlignmentStartTick(this, target.getTickOffset());
		}
	}

	public int getAlignmentEndTick(MMLEventList list2, int endTick) {
		MMLNoteEvent target = list2.searchOnTickOffset(endTick-1);
		if ( (target == null) || (target.getEndTick() == endTick) ) {
			return endTick;
		} else {
			return list2.getAlignmentEndTick(this, target.getEndTick());
		}
	}

	public void swap(MMLEventList list2, int startTick, int endTick) {
		List<MMLNoteEvent> tmp1 = new ArrayList<>();
		List<MMLNoteEvent> tmp2 = new ArrayList<>();
		for (MMLNoteEvent noteEvent : noteList) {
			if ( (noteEvent.getTickOffset() >= startTick) && (noteEvent.getEndTick() <= endTick) ) {
				tmp1.add(noteEvent);
			}
		}
		for (MMLNoteEvent noteEvent : tmp1) {
			noteList.remove(noteEvent);
		}

		for (MMLNoteEvent noteEvent : list2.getMMLNoteEventList()) {
			if ( (noteEvent.getTickOffset() >= startTick) && (noteEvent.getEndTick() <= endTick) ) {
				tmp2.add(noteEvent);
			}
		}
		for (MMLNoteEvent noteEvent : tmp2) {
			list2.getMMLNoteEventList().remove(noteEvent);
			addMMLNoteEvent(noteEvent);
		}
		for (MMLNoteEvent noteEvent : tmp1) {
			list2.addMMLNoteEvent(noteEvent);
		}
	}

	public void move(MMLEventList list2, int startTick, int endTick) {
		List<MMLNoteEvent> tmp1 = new ArrayList<>();
		for (MMLNoteEvent noteEvent : noteList) {
			if ( (noteEvent.getTickOffset() >= startTick) && (noteEvent.getEndTick() <= endTick) ) {
				tmp1.add(noteEvent);
			}
		}
		for (MMLNoteEvent noteEvent : tmp1) {
			deleteMMLEvent(noteEvent);
			list2.addMMLNoteEvent(noteEvent);
		}
	}

	public void copy(MMLEventList list2, int startTick, int endTick) {
		for (MMLNoteEvent noteEvent : noteList) {
			if ( (noteEvent.getTickOffset() >= startTick) && (noteEvent.getEndTick() <= endTick) ) {
				list2.addMMLNoteEvent( noteEvent.clone() );
			}
		}
	}

	/**
	 * ノート間の最小Tick未満の休符を詰めて消す.
	 * @param eventList
	 */
	public void deleteMinRest() {
		int min = MMLTicks.minimumTick();
		int size = noteList.size();
		for (int i = 1; i < size; i++) {
			var note1 = noteList.get(i-1);
			var note2 = noteList.get(i);
			int delta = note2.getTickOffset() - note1.getEndTick();
			if ((delta > 0) && (delta < min)) {
				int t = note1.getTick() -  min + delta;
				if (t >= min) {
					note1.setTick(t);
				} else {
					note1.setTick(note1.getTick() + delta);
				}
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MMLEventList)) {
			return false;
		}

		MMLEventList eventList = (MMLEventList) obj;
		return Arrays.equals(this.noteList.toArray(), eventList.noteList.toArray()) &&
				Arrays.equals(this.tempoList.toArray(), eventList.tempoList.toArray());
	}

	public MMLNoteEvent getLastNote() {
		int index = noteList.size() - 1;
		if (index >= 0) {
			return noteList.get(index);
		}
		return null;
	}

	public String getInternalMMLString() throws MMLExceptionList {
		return MMLBuilder.create(this).toMMLString(false, false);
	}

	/**
	 * イベントリストのリストの中で最後のTickを得る.
	 * @param eventList
	 * @return
	 */
	public static int maxEndTick(List<MMLEventList> eventList) {
		int endTick = 0;
		for (var e : eventList) {
			var lastNote = e.getLastNote();
			if ((lastNote != null) && (endTick < lastNote.getEndTick())) {
				endTick = lastNote.getEndTick();
			}
		}
		return endTick;
	}
}

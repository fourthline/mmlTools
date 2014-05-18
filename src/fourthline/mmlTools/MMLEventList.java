/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.parser.MMLEventParser;


/**
 * 1行のMMLデータを扱います.
 */
public class MMLEventList implements Serializable, Cloneable {
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
			tempoList = new ArrayList<MMLTempoEvent>();
		}

		parseMML(mml);
	}

	private void parseMML(String mml) {
		MMLEventParser parser = new MMLEventParser(mml);

		while (parser.hasNext()) {
			MMLEvent event = parser.next();

			if (event instanceof MMLTempoEvent) {
				((MMLTempoEvent) event).appendToListElement(tempoList);
			} else if (event instanceof MMLNoteEvent) {
				if (((MMLNoteEvent) event).getNote() >= 0) {
					noteList.add((MMLNoteEvent) event);
				}
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

	public List<MMLNoteEvent> getMMLNoteEventList() {
		return noteList;
	}

	private Object nextEvent(Iterator<? extends MMLEvent> iterator) {
		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			return null;
		}
	}

	/**
	 * 指定したtickOffset位置にあるNoteEventを検索します.
	 * @param tickOffset
	 * @return 見つからなかった場合は、nullを返します.
	 */
	public MMLNoteEvent searchOnTickOffset(long tickOffset) {
		for (MMLNoteEvent noteEvent : noteList) {
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
	 * TODO: MMLNoteEvent のメソッドのほうがいいかな？Listを引数として渡す.
	 * @param addNoteEvent
	 */
	public void addMMLNoteEvent(MMLNoteEvent addNoteEvent) {
		int i;
		if ((addNoteEvent.getNote() < 0) || (addNoteEvent.getTick() <= 0) || (addNoteEvent.getEndTick() <= 0)) {
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
	 * 指定のMMLeventを削除する.
	 * 最後尾はtrim.
	 * @param deleteItem
	 */
	public void deleteMMLEvent(MMLEvent deleteItem) {
		noteList.remove(deleteItem);
	}

	public String toMMLString() {
		return toMMLString(false, 0, true);
	}

	public String toMMLString(boolean withTempo, boolean mabiTempo) {
		return toMMLString(withTempo, 0, mabiTempo);
	}

	private MMLNoteEvent insertTempoMML(StringBuilder sb, MMLNoteEvent prevNoteEvent, MMLTempoEvent tempoEvent, boolean mabiTempo) {
		if (prevNoteEvent.getEndTick() != tempoEvent.getTickOffset()) {
			int tickLength = tempoEvent.getTickOffset() - prevNoteEvent.getEndTick();
			int tickOffset = prevNoteEvent.getEndTick();
			int note = prevNoteEvent.getNote();
			// FIXME: 最後の1つのrだけに細工すればよいね.
			if (mabiTempo) {
				// マビノギ補正（rrrT***N の処理
				MMLTicks ticks = new MMLTicks("c", tickLength, false);
				if (prevNoteEvent.getVelocity() != 0) {
					sb.append("v0");
				}
				sb.append(ticks.toString());
				prevNoteEvent = new MMLNoteEvent(note, tickLength, tickOffset);
				prevNoteEvent.setVelocity(0);
			} else {
				MMLTicks ticks = new MMLTicks("r", tickLength, false);
				prevNoteEvent = new MMLNoteEvent(prevNoteEvent.getNote(), tickLength, tickOffset);
				sb.append(ticks.toString());
			}
		}
		sb.append(tempoEvent.toMMLString());

		return prevNoteEvent;
	}

	/**
	 * テンポ出力を行うかどうかを指定してMML文字列を作成する.
	 * TODO: 長いなぁ。
	 * @param withTempo trueを指定すると、tempo指定を含むMMLを返します.
	 * @param totalTick 最大tick長. これに満たない場合は、末尾を休符分で埋めます.
	 * @return
	 */
	public String toMMLString(boolean withTempo, int totalTick, boolean mabiTempo) {
		//　テンポ
		Iterator<MMLTempoEvent> tempoIterator = null;
		MMLTempoEvent tempoEvent = null;
		tempoIterator = tempoList.iterator();
		tempoEvent = (MMLTempoEvent) nextEvent(tempoIterator);

		//　ボリューム
		int volumn = MMLNoteEvent.INITIAL_VOLUMN;

		StringBuilder sb = new StringBuilder();
		int noteCount = noteList.size();

		// initial note: octave 4, tick 0, offset 0
		MMLNoteEvent noteEvent = new MMLNoteEvent(12*4, 0, 0);
		MMLNoteEvent prevNoteEvent = noteEvent;
		for (int i = 0; i < noteCount; i++) {
			noteEvent = noteList.get(i);

			// テンポのMML挿入判定
			while ( (tempoEvent != null) && (tempoEvent.getTickOffset() <= noteEvent.getTickOffset()) ) {
				if (withTempo) {
					// tempo挿入 (rrrT***N の処理)
					prevNoteEvent = insertTempoMML(sb, prevNoteEvent, tempoEvent, mabiTempo);
				}
				tempoEvent = (MMLTempoEvent) nextEvent(tempoIterator);
			}

			// 音量のMML挿入判定
			int noteVelocity = noteEvent.getVelocity();
			if ( (noteVelocity >= 0) && (noteVelocity != volumn) ) {
				volumn = noteVelocity;
				sb.append(noteEvent.getVelocityString());
			} else if (prevNoteEvent.getVelocity() == 0) {
				// テンポ処理で音量がゼロになっている場合は元に戻す.
				sb.append("v"+volumn);
			}

			// endTickOffsetがTempoを跨いでいたら、'&'でつなげる.
			if ( (tempoEvent != null) && (noteEvent.getTickOffset() < tempoEvent.getTickOffset()) && (tempoEvent.getTickOffset() < noteEvent.getEndTick()) ) {
				int tick = tempoEvent.getTickOffset() - noteEvent.getTickOffset();
				int tick2 = noteEvent.getTick() - tick;

				MMLNoteEvent divNoteEvent = new MMLNoteEvent(noteEvent.getNote(), tick, noteEvent.getTickOffset());
				sb.append( divNoteEvent.toMMLString(prevNoteEvent) );

				if (withTempo) {
					sb.append( tempoEvent.toMMLString() );
				}
				tempoEvent = (MMLTempoEvent) nextEvent(tempoIterator);

				divNoteEvent.setTick(tick2);
				sb.append('&').append( divNoteEvent.toMMLString() );
			} else {
				sb.append( noteEvent.toMMLString(prevNoteEvent) );
			}
			prevNoteEvent = noteEvent;
		}

		// テンポがまだ残っていれば、その分をつなげる.
		if ( (withTempo) && (tempoEvent != null) && (noteEvent != null) && (mabiTempo) ) {
			int endTick = noteEvent.getEndTick();
			int tickOffset = tempoEvent.getTickOffset();
			int tick = tickOffset - endTick;
			if (tick > 0) {
				sb.append("v0");
				sb.append( new MMLTicks("c", tick, false).toString() );
			}
			sb.append(tempoEvent.toMMLString());
		}

		return sb.toString();
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
			obj.tempoList = new ArrayList<>(tempoList);
			for (MMLNoteEvent note : noteList) {
				obj.noteList.add(note.clone());
			}
			return obj;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
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
				list2.addMMLNoteEvent(noteEvent);
			}
		}
	}

	/**
	 * tick長の空白を挿入します.
	 * @param startTick
	 * @param tick
	 */
	public void insertTick(int startTick, int tick) {
		for (MMLNoteEvent noteEvent : noteList) {
			int noteTick = noteEvent.getTickOffset();
			if (noteTick >= startTick) {
				noteEvent.setTickOffset(noteTick + tick);
			}
		}
	}

	/**
	 * tick長の部分を削除して詰めます.
	 * @param startTick
	 * @param tick
	 */
	public void removeTick(int startTick, int tick) {
		ArrayList<MMLNoteEvent> deleteNote = new ArrayList<>();
		for (MMLNoteEvent noteEvent : noteList) {
			int noteTick = noteEvent.getTickOffset();
			if (noteTick >= startTick) {
				if (noteTick < startTick+tick) {
					// 削除リストに加えておく.
					deleteNote.add(noteEvent);
				} else {
					noteEvent.setTickOffset(noteTick - tick);
				}
			}
		}

		for (MMLNoteEvent noteEvent : deleteNote) {
			deleteMMLEvent(noteEvent);
		}
	}

	/**
	 * 重複音（開始）の判定を行います.
	 * @param note
	 * @param tickOffset
	 * @return
	 */
	private boolean searchEqualsTickOffsetNote(int note, int tickOffset) {
		for (MMLNoteEvent noteEvent : noteList) {
			if (noteEvent.getTickOffset() > tickOffset) {
				return false;
			} else if ( (noteEvent.getNote() == note) && 
					(noteEvent.getTickOffset() == tickOffset)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Mabinogi演奏に近くなるように修正を行います.
	 * 重複音（開始）、テンポ重複.
	 * @param registedPart
	 * @param globalTempoList
	 * @return
	 */
	public MMLEventList emulateMabiPlay(List<MMLEventList> registedPart, List<MMLTempoEvent> globalTempoList) {
		for (MMLNoteEvent noteEvent : noteList) {
			int tickOffset = noteEvent.getTickOffset();
			boolean onTempo = false;
			boolean shift = false;
			// テンポと重複していない && 前パートと同一開始位置: tickOffsetを1つ増やして tickを1つ減らす.
			for (MMLTempoEvent tempo : globalTempoList) {
				if (tempo.getTickOffset() == tickOffset) {
					onTempo = true;
					break;
				}
			}
			if (!onTempo) {
				for (MMLEventList prePart : registedPart) {
					if (prePart.searchEqualsTickOffsetNote(noteEvent.getNote(), tickOffset)) {
						shift = true;
						break;
					}
				}
			} else if (registedPart.isEmpty()) {
				// テンポと重複している && テンポパートであれば,
				shift = true;
			}
			if (shift) {
				int tick = noteEvent.getTick();
				noteEvent.setTickOffset(tickOffset+1);
				noteEvent.setTick(tick - 1);
			}
		}
		return this;
	}
}

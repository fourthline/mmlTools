/*
 * Copyright (C) 2013-2022 たんらる
 */

package fourthline.mmlTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.core.UndefinedTickException;
import fourthline.mmlTools.parser.MMLEventParser;


/**
 * 1行のMMLデータを扱います.
 */
public final class MMLEventList implements Serializable, Cloneable {
	private static final long serialVersionUID = -1430758411579285535L;
	private static final int STRING_BUILDER_SIZE = 2048;

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

		parseMML(mml, 0);
	}

	public MMLEventList(String mml, List<MMLTempoEvent> globalTempoList, int startOffset) {
		if (globalTempoList != null) {
			tempoList = globalTempoList;
		} else {
			tempoList = new ArrayList<MMLTempoEvent>();
		}

		parseMML(mml, startOffset);
	}

	private void parseMML(String mml, int startOffset) {
		MMLEventParser parser = new MMLEventParser(mml, startOffset);

		while (parser.hasNext()) {
			MMLEvent event = parser.next();

			if (event instanceof MMLTempoEvent) {
				((MMLTempoEvent) event).appendToListElement(tempoList);
			} else if (event instanceof MMLNoteEvent) {
				noteList.add((MMLNoteEvent) event);
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

	private MMLNoteEvent getLastNote() {
		int index = noteList.size() - 1;
		if (index >= 0) {
			return noteList.get(index);
		}
		return null;
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
			int index[] = noteEvent.getIndexOfMMLString();
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
		for (i = 0; i < noteList.size(); i++) {
			MMLNoteEvent e = noteList.get(i);
			if (noteEvent.getTickOffset() < e.getEndTick()) {
				if (noteEvent.getTickOffset() >= e.getTickOffset()) {
					return true;
				}
				break;
			}
		}
		for (i = 0; i < noteList.size(); i++) {
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

	public String toMMLString() throws UndefinedTickException {
		return toMMLString(false, true);
	}

	public String toMMLString(int startOffset) throws UndefinedTickException {
		return toMMLString(startOffset, false, true, null);
	}

	public String toMMLString(boolean withTempo, boolean mabiTempo) throws UndefinedTickException {
		return toMMLString(0, withTempo, mabiTempo, null);
	}

	/**
	 * テンポ補正に使う文字を決定する.
	 * @param relationPart     関連するパートの情報
	 * @param offset           判定するtickのオフセット
	 * @param currentOctave    現在のオクターブ
	 * @return                 テンポ補正に使う文字
	 * @throws UndefinedTickException
	 */
	private char makeTempoChar(List<MMLEventList> relationPart, long offset, int currentOctave) throws UndefinedTickException {
		boolean f[] = { true, true, true, true, true, true, true };
		// relationのパートのオフセット位置の情報をつかって、使用するabcdefg のどれを使うかを決める。
		if (relationPart != null) {
			for (MMLEventList t : relationPart) {
				MMLNoteEvent e = t.searchOnTickOffset(offset);
				if (e != null) {
					if (e.getOctave() == currentOctave) {
						char c = e.toMMLString().toLowerCase().charAt(0);
						int index = c - 'a';
						f[index] = false;
					}
				}
			}
		}

		for (int i = 0; i < f.length; i++) {
			int index = (i + 2) % f.length;
			if (f[index]) {
				return (char)('a' + index);
			}
		}

		return 'c';
	}

	private MMLNoteEvent insertTempoMML(StringBuilder sb, MMLNoteEvent prevNoteEvent, MMLTempoEvent tempoEvent, boolean mabiTempo, List<MMLEventList> relationPart)
			throws UndefinedTickException {
		if (prevNoteEvent.getEndTick() < tempoEvent.getTickOffset()) {
			int tickLength = tempoEvent.getTickOffset() - prevNoteEvent.getEndTick();
			int tickOffset = prevNoteEvent.getEndTick();
			int note = prevNoteEvent.getNote();
			int currentOctave = prevNoteEvent.getOctave();
			MMLTicks ticks = new MMLTicks("r", tickLength, false);
			prevNoteEvent = new MMLNoteEvent(prevNoteEvent.getNote(), tickLength, tickOffset, prevNoteEvent.getVelocity());
			sb.append(ticks.toMMLText());
			if (mabiTempo) {
				// 最後の1つのrだけを補正文字に置換する.
				int lastIndex = sb.lastIndexOf("r");
				sb.replace(lastIndex, lastIndex+1, "c");
				long offset = new MMLEventList(sb.toString()).getLastNote().getTickOffset();
				char inChar = makeTempoChar(relationPart, offset, currentOctave);
				sb.replace(lastIndex, lastIndex+1, (prevNoteEvent.getVelocity() != 0) ? "v0"+inChar : ""+inChar);
				prevNoteEvent = new MMLNoteEvent(note, tickLength, tickOffset, 0);
			}
		}
		sb.append(tempoEvent.toMMLString());

		return prevNoteEvent;
	}

	private void insertNoteWithTempo(StringBuilder sb, LinkedList<MMLTempoEvent> localTempoList,
			MMLNoteEvent prevNoteEvent, MMLNoteEvent noteEvent,
			boolean withTempo, boolean mabiTempo) throws UndefinedTickException {
		MMLNoteEvent divNoteEvent = noteEvent.clone();

		// endTickOffsetがTempoを跨いでいたら、'&'でつなげる. (withTempoのみ)
		while ( withTempo && (!localTempoList.isEmpty()) &&
				(divNoteEvent.getTickOffset() < localTempoList.getFirst().getTickOffset()) &&
				(localTempoList.getFirst().getTickOffset() < divNoteEvent.getEndTick()) ) {
			int tick = localTempoList.getFirst().getTickOffset() - divNoteEvent.getTickOffset();

			MMLNoteEvent partNoteEvent = new MMLNoteEvent(divNoteEvent.getNote(), tick, divNoteEvent.getTickOffset(), divNoteEvent.getVelocity());
			sb.append( partNoteEvent.toMMLString(prevNoteEvent) );

			if (withTempo) {
				sb.append( localTempoList.getFirst().toMMLString() );
			}
			localTempoList.removeFirst();

			divNoteEvent.setTick(divNoteEvent.getTick() - tick);
			divNoteEvent.setTickOffset(divNoteEvent.getTickOffset() + tick);
			prevNoteEvent = partNoteEvent;
			if (withTempo && mabiTempo) {
				divNoteEvent.setVelocity(0);
			} else if (divNoteEvent.getTick() > 0) {
				sb.append('&');
			}
		}

		if (divNoteEvent.getTick() > 0) {
			sb.append( divNoteEvent.toMMLString(prevNoteEvent) );
		}
		if (noteEvent.getVelocity() != divNoteEvent.getVelocity()) {
			sb.append("v"+noteEvent.getVelocity());
		}
	}

	/**
	 * テンポ出力を行うかどうかを指定してMML文字列を作成する.
	 * TODO: 長いなぁ。
	 * @param startOffset
	 * @param withTempo    trueを指定すると、tempo指定を含むMMLを返します.
	 * @param totalTick    最大tick長. これに満たない場合は、末尾を休符分で埋めます.
	 * @param mabiTempo    MML for mabi
	 * @param relationPart テンポ補正時に参照する関連するパートの情報
	 * @return
	 * @throws UndefinedTickException
	 */
	public String toMMLString(int startOffset, boolean withTempo, boolean mabiTempo, List<MMLEventList> relationPart)
			throws UndefinedTickException {
		long totalTick = totalTickRelationPart(relationPart);
		//　テンポ, startOffset に伴って 使う先頭のあたまがかわる
		LinkedList<MMLTempoEvent> localTempoList = new LinkedList<>(tempoList);
		while (localTempoList.size() > 1) {
			if (localTempoList.get(1).getTickOffset() <= startOffset) {
				localTempoList.removeFirst();
			} else {
				break;
			}
		}
		StringBuilder sb = new StringBuilder(STRING_BUILDER_SIZE);

		// initial note: octave 4, tick 0, offset 0, velocity 8
		MMLNoteEvent prevNoteEvent = new MMLNoteEvent(12*4, 0, startOffset, MMLNoteEvent.INIT_VOL);
		for (MMLNoteEvent noteEvent : noteList) {
			// テンポのMML挿入判定
			while ( (!localTempoList.isEmpty()) && (localTempoList.getFirst().getTickOffset() <= noteEvent.getTickOffset()) ) {
				if (withTempo) {
					// tempo挿入 (rrrT***N の処理)
					prevNoteEvent = insertTempoMML(sb, prevNoteEvent, localTempoList.getFirst(), mabiTempo, relationPart);
				}
				localTempoList.removeFirst();
			}

			insertNoteWithTempo(sb, localTempoList, prevNoteEvent, noteEvent, withTempo, mabiTempo);
			prevNoteEvent = noteEvent;
		}

		// テンポがまだ残っていれば、その分をつなげる.
		while (!localTempoList.isEmpty()) {
			MMLTempoEvent tempo = localTempoList.getFirst();
			if (mabiTempo && (tempo.getTickOffset() >= totalTick)) {
				// mabi-MMLであれば, 不要な終端テンポは付けない.
				break;
			}
			if (withTempo) {
				// tempo挿入 (rrrT***N の処理)
				prevNoteEvent = insertTempoMML(sb, prevNoteEvent, tempo, mabiTempo, relationPart);
			}
			localTempoList.removeFirst();
		}

		return sb.toString();
	}

	private int insertNoteWithTempoMusicQ(StringBuilder sb, List<MMLTempoEvent> localTempoList, int tempoIndex,
			MMLNoteEvent prevNoteEvent, MMLNoteEvent noteEvent, List<MMLEventList> relationPart) throws UndefinedTickException {
		MMLNoteEvent divNoteEvent = noteEvent.clone();
		int index = tempoIndex;

		// endTickOffsetがTempoを跨いでいたら、他のパートで挿入できるか判定する
		while ( (localTempoList.size() > index) ) {
			MMLTempoEvent tempoEvent = localTempoList.get(index);
			long tickOffset = tempoEvent.getTickOffset();
			if ( (divNoteEvent.getTickOffset() >= tickOffset) || 
					(tickOffset >= divNoteEvent.getEndTick()) ) {
				// テンポを跨がないので分割しない
				break;
			}
			// 他の関連パート中に適切な挿入位置があるかどうかを探す.
			if (searchRelationPartCanInsertTempo(relationPart, tickOffset)) {
				index++;
				continue;
			}

			int tick = localTempoList.get(index).getTickOffset() - divNoteEvent.getTickOffset();
			MMLNoteEvent partNoteEvent = new MMLNoteEvent(divNoteEvent.getNote(), tick, divNoteEvent.getTickOffset(), divNoteEvent.getVelocity());
			sb.append( partNoteEvent.toMMLString(prevNoteEvent) );
			sb.append( localTempoList.get(index).toMMLString() );
			localTempoList.remove(index);

			divNoteEvent.setTick(divNoteEvent.getTick() - tick);
			divNoteEvent.setTickOffset(divNoteEvent.getTickOffset() + tick);
			prevNoteEvent = partNoteEvent;
			divNoteEvent.setVelocity(0);
		}

		if (divNoteEvent.getTick() > 0) {
			sb.append( divNoteEvent.toMMLString(prevNoteEvent) );
		}
		if (noteEvent.getVelocity() != divNoteEvent.getVelocity()) {
			sb.append("v"+noteEvent.getVelocity());
		}

		return index;
	}

	/**
	 * 関連パートにテンポ挿入できる箇所があるかどうかを判定する
	 * @param relationPart
	 * @param tickOffset
	 * @return
	 */
	private static boolean searchRelationPartCanInsertTempo(List<MMLEventList> relationPart, long tickOffset) {
		if (relationPart != null) {
			for (MMLEventList t : relationPart) {
				MMLNoteEvent e1 = t.searchOnTickOffset(tickOffset);
				if ( (e1 == null) || (e1.getTickOffset() == tickOffset) ) {
					return true;
				}
			}
		}

		return false;
	}
	/**
	 * 関連パートに接触ノートがあるかどうかを判定する
	 * @param relationPart
	 * @param tickOffset
	 * @return
	 */
	private static boolean searchRelationPartOnTick(List<MMLEventList> relationPart, long tickOffset) {
		if (relationPart != null) {
			for (MMLEventList t : relationPart) {
				MMLNoteEvent e1 = t.searchOnTickOffset(tickOffset);
				MMLNoteEvent e2 = t.searchPrevNoteOnTickOffset(tickOffset);
				if ( ((e1 != null) && (e1.getTickOffset() == tickOffset)) ||
						((e2 != null) && (e2.getEndTick() == tickOffset)) ) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 関連するパートと合わせたTick長を算出する
	 * @param relationPart
	 * @return
	 */
	private long totalTickRelationPart(List<MMLEventList> relationPart) {
		long totalTick = getTickLength();
		if (relationPart != null) {
			for (var t : relationPart) {
				if (totalTick < t.getTickLength()) {
					totalTick = t.getTickLength();
				}
			}
		}
		return totalTick;
	}

	/**
	 * テンポ出力を行うかどうかを指定してMML文字列を作成する. MusicQ以降用. 関連パートにテンポを入れられる場合はいれない.
	 * @param localTempoList テンポリスト
	 * @param relationPart   テンポ補正時に参照する関連するパートの情報
	 * @return
	 * @throws UndefinedTickException
	 */
	public String toMMLStringMusicQ(int startOffset, List<MMLTempoEvent> localTempoList, List<MMLEventList> relationPart)
			throws UndefinedTickException {
		long totalTick = totalTickRelationPart(relationPart);
		StringBuilder sb = new StringBuilder(STRING_BUILDER_SIZE);
		int tempoIndex = 0;
		while (localTempoList.size() > tempoIndex + 1) {
			if (localTempoList.get(tempoIndex+1).getTickOffset() <= startOffset) {
				localTempoList.remove(tempoIndex);
			} else {
				break;
			}
		}

		// initial note: octave 4, tick 0, offset 0, velocity 8
		MMLNoteEvent prevNoteEvent = new MMLNoteEvent(12*4, 0, startOffset, MMLNoteEvent.INIT_VOL);
		for (MMLNoteEvent noteEvent : noteList) {
			// テンポのMML挿入判定
			while ( (localTempoList.size() > tempoIndex) && (localTempoList.get(tempoIndex).getTickOffset() <= noteEvent.getTickOffset()) ) {
				prevNoteEvent = insertTempoMML(sb, prevNoteEvent, localTempoList.get(tempoIndex), true, relationPart);
				localTempoList.remove(tempoIndex);
			}

			tempoIndex = insertNoteWithTempoMusicQ(sb, localTempoList, tempoIndex, prevNoteEvent, noteEvent, relationPart);
			prevNoteEvent = noteEvent;
		}

		// テンポがまだ残っていれば、その分をつなげる.
		while (localTempoList.size() > tempoIndex) {
			long tempoTick = localTempoList.get(tempoIndex).getTickOffset();
			if (tempoTick >= totalTick) {
				// 不要な終端テンポは付けない.
				break;
			}
			// 関連パートに接触ノートがある場合は自パートにテンポ挿入しない
			if (!searchRelationPartOnTick(relationPart, tempoTick)) {
				insertTempoMML(sb, prevNoteEvent, localTempoList.get(tempoIndex), true, relationPart);
				localTempoList.remove(tempoIndex);
			} else {
				tempoIndex++;
			}
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

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MMLEventList)) {
			return false;
		}

		MMLEventList eventList = (MMLEventList) obj;
		if ( Arrays.equals(this.noteList.toArray(), eventList.noteList.toArray()) &&
				Arrays.equals(this.tempoList.toArray(), eventList.tempoList.toArray()) ) {
			return true;
		}
		return false;
	}
}

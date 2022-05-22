/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.LinkedList;
import java.util.List;

import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.UndefinedTickException;

public final class MMLBuilder {
	private static final int STRING_BUILDER_SIZE = 2048;

	private final MMLEventList eventList;
	private final int startOffset;

	public static MMLBuilder create(MMLEventList eventList) {
		return new MMLBuilder(eventList, 0);
	}

	public static MMLBuilder create(MMLEventList eventList, int startOffset) {
		return new MMLBuilder(eventList, startOffset);
	}

	private MMLBuilder(MMLEventList eventList, int startOffset) {
		this.eventList = eventList;
		this.startOffset = startOffset;
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
		boolean[] f = { true, true, true, true, true, true, true };
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
			sb.append("v").append(noteEvent.getVelocity());
		}
	}

	public String toMMLString() throws UndefinedTickException {
		return toMMLString(false, true);
	}

	public String toMMLString(boolean withTempo, boolean mabiTempo) throws UndefinedTickException {
		return toMMLString(withTempo, mabiTempo, null);
	}

	/**
	 * テンポ出力を行うかどうかを指定してMML文字列を作成する.
	 * TODO: 長いなぁ。
	 * @param withTempo    trueを指定すると、tempo指定を含むMMLを返します.
	 * @param mabiTempo    MML for mabi
	 * @param relationPart テンポ補正時に参照する関連するパートの情報
	 * @return
	 * @throws UndefinedTickException
	 */
	public String toMMLString(boolean withTempo, boolean mabiTempo, List<MMLEventList> relationPart)
			throws UndefinedTickException {
		long totalTick = totalTickRelationPart(relationPart);
		//　テンポ, startOffset に伴って 使う先頭のあたまがかわる
		LinkedList<MMLTempoEvent> localTempoList = new LinkedList<>(eventList.getGlobalTempoList());
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
		for (MMLNoteEvent noteEvent : eventList.getMMLNoteEventList()) {
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
			sb.append("v").append(noteEvent.getVelocity());
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
		long totalTick = eventList.getTickLength();
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
	public String toMMLStringMusicQ(List<MMLTempoEvent> localTempoList, List<MMLEventList> relationPart)
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
		for (MMLNoteEvent noteEvent : eventList.getMMLNoteEventList()) {
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
}

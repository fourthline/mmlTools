/*
 * Copyright (C) 2013 たんらる
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
public class MMLEventList implements Serializable {
	private static final long serialVersionUID = -1430758411579285535L;

	private List<MMLNoteEvent>   noteList   = new ArrayList<MMLNoteEvent>();
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
		if ( (withTempo) && (tempoEvent != null) && (noteEvent != null) ) {
			int endTick = noteEvent.getEndTick();
			int tickOffset = tempoEvent.getTickOffset();
			int tick = tickOffset - endTick;
			if (tick > 0) {
				sb.append("v0");
				sb.append( new MMLTicks("c", tick, false).toString() );
			}
			sb.append(tempoEvent.toMMLString());
			noteEvent = new MMLNoteEvent(noteEvent.getNote(), noteEvent.getTick() + tick, noteEvent.getTickOffset());
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return tempoList.toString() + noteList.toString();
	}
}

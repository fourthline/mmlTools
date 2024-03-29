/*
 * Copyright (C) 2013-2023 たんらる
 */

package jp.fourthline.mmlTools;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;


public final class MMLTempoEvent extends MMLEvent implements Cloneable {
	private static final long serialVersionUID = 8014294359518840951L;

	private int tempo;
	private final boolean isFirst;
	public static final int META = 0x51;  /* MIDI meta: tempo */
	public static final int INITIAL_TEMPO = 120;

	public MMLTempoEvent(int tempo, int tickOffset) throws IllegalArgumentException {
		this(tempo, tickOffset, false);
	}

	public MMLTempoEvent(int tempo, int tickOffset, boolean isFirst) throws IllegalArgumentException {
		super(tickOffset);
		if ((tempo <= 0) || (tempo > 1000)) {
			throw new IllegalArgumentException("tempo "+tempo);
		}
		this.tempo = tempo;
		this.isFirst = isFirst;
	}

	public int getTempo() {
		return this.tempo;
	}

	public void setTempo(int tempo) {
		this.tempo = tempo;
	}

	public byte[] getMetaData() {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(60000000/tempo);
		byte[] array = buf.array();
		return Arrays.copyOfRange(array, 1, array.length);
	}

	@Override
	public String toString() {
		return getTickOffset()+"T" + tempo;
	}

	/**
	 * toString() でつくった文字列からオブジェクトを生成する.
	 * @param str
	 */
	public static MMLTempoEvent fromString(String str) {
		if ((str == null) || (str.length() == 0)) {
			return null;
		}
		String[] s = str.split("T");
		if (s.length != 2) {
			return null;
		}
		return new MMLTempoEvent(Integer.parseInt(s[1]), Integer.parseInt(s[0]));
	}

	@Override
	public String toMMLString() {
		return "t" + tempo;
	}

	/**
	 * 指定されたListにオブジェクトを追加します.
	 * @param list
	 */
	public void appendToListElement(List<MMLTempoEvent> list) {
		int index = 0;
		int targetOffset = getTickOffset();

		for ( ; index < list.size(); index++) {
			MMLTempoEvent tempoEvent = list.get(index);
			if (tempoEvent.getTickOffset() == targetOffset) {
				list.remove(index);
				break;
			} else if (tempoEvent.getTickOffset() > targetOffset) {
				break;
			}
		}

		// 連続で同じテンポであれば追加しない
		if ( (index == 0) || (list.get(index-1).getTempo() != this.tempo) || !isFirst ) {
			list.add(index, this);
		}
	}

	public static List<MMLTempoEvent> mergeTempoList(List<MMLTempoEvent> list1, List<MMLTempoEvent> list2) {
		if (list1 != list2) {
			for (MMLTempoEvent tempoEvent : list1) {
				tempoEvent.appendToListElement(list2);
			}
		}

		return list2;
	}

	public static int searchOnTick(List<MMLTempoEvent> tempoList, long tickOffset) {
		int tempo = INITIAL_TEMPO;
		for (MMLTempoEvent tempoEvent : tempoList) {
			if (tickOffset < tempoEvent.getTickOffset()) {
				break;
			}

			tempo = tempoEvent.getTempo();
		}

		return tempo;
	}

	public static boolean searchEqualsTick(List<MMLTempoEvent> tempoList, long tickOffset) {
		for (MMLTempoEvent tempo : tempoList) {
			if (tempo.getTickOffset() == tickOffset) {
				return true;
			} else if (tempo.getTickOffset() > tickOffset) {
				break;
			}
		}
		return false;
	}

	/**
	 * テンポリスト中の最大テンポ値を取得します.
	 * @param tempoList
	 * @return
	 */
	public static MMLTempoEvent getMaxTempoEvent(List<MMLTempoEvent> tempoList) {
		MMLTempoEvent maxEvent = new MMLTempoEvent(INITIAL_TEMPO, 0);
		for (MMLTempoEvent tempoEvent : tempoList) {
			int currentTempo = tempoEvent.getTempo();
			if (maxEvent.getTempo() < currentTempo) {
				maxEvent.setTempo(currentTempo);
			}
		}

		return maxEvent;
	}

	@Override
	public MMLTempoEvent clone() {
		try {
			return (MMLTempoEvent) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MMLTempoEvent)) {
			return false;
		}

		MMLTempoEvent tempoEvent = (MMLTempoEvent) obj;
		return (this.tempo == tempoEvent.tempo) &&
				(super.equals(tempoEvent));
	}
}

/*
 * Copyright (C) 2013-2017 たんらる
 */

package jp.fourthline.mmlTools;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import jp.fourthline.mmlTools.core.MMLTickTable;


public final class MMLTempoEvent extends MMLEvent implements Cloneable {
	private static final long serialVersionUID = 8014294359518840951L;

	private int tempo;
	public static final int META = 0x51;  /* MIDI meta: tempo */
	public static final int INITIAL_TEMPO = 120;

	public MMLTempoEvent(int tempo, int tickOffset) throws IllegalArgumentException {
		super(tickOffset);
		if (tempo <= 0) {
			throw new IllegalArgumentException("tempo "+tempo);
		}
		this.tempo = tempo;
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
		if ( (index == 0) || (list.get(index-1).getTempo() != this.tempo) ) {
			list.add(index, this);
		}
	}

	public static List<MMLTempoEvent> mergeTempoList(List<MMLTempoEvent> list1, List<MMLTempoEvent> list2) {
		for (MMLTempoEvent tempoEvent : list1) {
			tempoEvent.appendToListElement(list2);
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
	 * 指定したtickオフセット位置の先頭からの時間を返します.
	 * @param tempoList
	 * @param tickOffset
	 * @return 先頭からの時間（ms）
	 */
	public static long getTimeOnTickOffset(List<MMLTempoEvent> tempoList, int tickOffset) {
		long totalTime = 0L;

		int tempo = INITIAL_TEMPO;
		int currentTick = 0;
		for (MMLTempoEvent tempoEvent : tempoList) {
			int currentTempoTick = tempoEvent.getTickOffset();
			if (tickOffset < currentTempoTick) {
				break;
			}

			int currentTempo = tempoEvent.getTempo();
			if (tempo != currentTempo) {
				totalTime += (currentTempoTick - currentTick) * 60 / tempo * 1000;
				currentTick = currentTempoTick;
			}

			tempo = currentTempo;
		}

		totalTime += (tickOffset - currentTick) * 60 / tempo * 1000;
		totalTime /= (double) MMLTickTable.TPQN;
		return totalTime;
	}

	/**
	 * 指定した時間からtickオフセットを返します.
	 * @param tempoList
	 * @param time
	 * @return 先頭からの時間（ms）
	 */
	public static long getTickOffsetOnTime(List<MMLTempoEvent> tempoList, long time) {
		int tempo = INITIAL_TEMPO;
		long pointTime = 0;
		long tick = 0;
		for (MMLTempoEvent tempoEvent : tempoList) {
			long tempoTime = getTimeOnTickOffset(tempoList, tempoEvent.getTickOffset());
			if (time <= tempoTime) {
				break;
			}
			pointTime = tempoTime;
			tempo = tempoEvent.getTempo();
			tick = tempoEvent.getTickOffset();
		}

		tick += (time - pointTime) * MMLTickTable.TPQN * tempo / 60 / 1000;
		return tick;
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
	public MMLTempoEvent clone() throws CloneNotSupportedException {
		return (MMLTempoEvent) super.clone();
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

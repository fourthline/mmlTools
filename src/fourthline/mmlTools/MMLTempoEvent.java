/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import java.util.List;


public class MMLTempoEvent extends MMLEvent {

	private int tempo;
	public static final int META = 0x51;  /* MIDI meta: tempo */
	public static final int INITIAL_TEMPO = 120;

	public MMLTempoEvent(int tempo, int tickOffset) {
		super(tickOffset);

		this.tempo = tempo;
	}

	public int getTempo() {
		return this.tempo;
	}

	public void setTempo(int tempo) {
		this.tempo = tempo;
	}

	public byte[] getMetaData() {
		byte[] retVal = { (byte)tempo };

		return retVal;
	}

	@Override
	public String toString() {
		return "[Tempo] " + tempo;
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
		System.out.println("[T] "+targetOffset);

		for ( ; index < list.size(); index++) {
			MMLTempoEvent tempoEvent = list.get(index);
			if (tempoEvent.getTickOffset() == targetOffset) {
				list.remove(index);
				break;
			} else if (tempoEvent.getTickOffset() > targetOffset) {
				break;
			}
		}

		list.add(this);
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

	/**
	 * 指定したtickオフセット位置の先頭からの時間を返します.
	 * @param tempoList
	 * @param tickOffset
	 * @return 先頭からの時間（秒）
	 */
	public static double getTimeOnTickOffset(List<MMLTempoEvent> tempoList, int tickOffset) {
		double totalTime = 0.0;

		int tempo = INITIAL_TEMPO;
		int currentTick = 0;
		for (MMLTempoEvent tempoEvent : tempoList) {
			int currentTempoTick = tempoEvent.getTickOffset();
			if (tickOffset < currentTempoTick) {
				break;
			}

			int currentTempo = tempoEvent.getTempo();
			if (tempo != currentTempo) {
				totalTime += (currentTempoTick - currentTick) * 60 / tempo;
				currentTick = currentTempoTick;
			}

			tempo = currentTempo;
		}

		totalTime += (tickOffset - currentTick) * 60 / tempo;
		totalTime /= 96.0;
		return totalTime;
	}
}

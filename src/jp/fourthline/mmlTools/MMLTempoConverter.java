/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.List;

public final class MMLTempoConverter {

	private final List<MMLTempoEvent> newTempoList;
	private long conversionDiff = 0;

	public MMLTempoConverter(List<MMLTempoEvent> newTempoList) {
		this.newTempoList = newTempoList;
	}

	private long convertEvent(List<MMLTempoEvent> t1, List<MMLTempoEvent> t2, int value) {
		return MMLTempoEvent.getTickOffsetOnTime(t2,
				MMLTempoEvent.getTimeOnTickOffset(t1, value));
	}

	public void convert(MMLScore score) {
		List<MMLTempoEvent> tempoList = score.getTempoEventList();

		// 変換する
		for (var track : score.getTrackList()) {
			for (var eventList : track.getMMLEventList()) {
				for (var noteEvent : eventList.getMMLNoteEventList()) {
					long endTick = convertEvent(tempoList, newTempoList, noteEvent.getEndTick());
					long tickOffset = convertEvent(tempoList, newTempoList, noteEvent.getTickOffset());

					// 逆変換
					long reTick = convertEvent(newTempoList, tempoList, (int) endTick);
					long reTickOffset = convertEvent(newTempoList, tempoList, (int) tickOffset);

					conversionDiff += Math.abs(reTick - noteEvent.getEndTick());
					conversionDiff += Math.abs(reTickOffset - noteEvent.getTickOffset());

					noteEvent.setTickOffset((int)tickOffset);
					noteEvent.setTick((int)(endTick - tickOffset));
				}
			}
		}

		tempoList.clear();
		tempoList.addAll(newTempoList);
	}

	public long getConversionDiff() {
		return conversionDiff;
	}
}

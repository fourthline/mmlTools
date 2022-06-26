/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.List;

public final class MMLTempoConverter {

	private final List<MMLTempoEvent> newTempoList;
	private long conversionDiff = 0;
	private int conversionDiffCount = 0;

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
		score.getTrackList().parallelStream().forEach(track -> {
			for (var eventList : track.getMMLEventList()) {
				for (var noteEvent : eventList.getMMLNoteEventList()) {
					long endTick = convertEvent(tempoList, newTempoList, noteEvent.getEndTick());
					long tickOffset = convertEvent(tempoList, newTempoList, noteEvent.getTickOffset());

					// 逆変換
					long reTick = convertEvent(newTempoList, tempoList, (int) endTick);
					long reTickOffset = convertEvent(newTempoList, tempoList, (int) tickOffset);

					// 誤差算出
					long diff =  Math.abs(reTick - noteEvent.getEndTick())
							+ Math.abs(reTickOffset - noteEvent.getTickOffset());
					if (diff != 0) {
						conversionDiff += diff;
						conversionDiffCount++;
					}

					noteEvent.setTickOffset((int)tickOffset);
					noteEvent.setTick((int)(endTick - tickOffset));
				}
			}
		});

		// マーカーの変換
		score.getMarkerList().forEach(t -> t.setTickOffset((int)convertEvent(tempoList, newTempoList, t.getTickOffset())));

		// テンポリスト更新
		tempoList.clear();
		tempoList.addAll(newTempoList);
	}

	public String getConversionDiff() {
		return conversionDiff + "/" + conversionDiffCount;
	}
}

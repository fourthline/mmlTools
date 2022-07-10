/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.ArrayList;
import java.util.List;

import jp.fourthline.mmlTools.core.IllegalTickOffsetException;
import jp.fourthline.mmlTools.core.MMLTickTable;

public final class MMLTempoConverter {

	private final List<MMLTempoEvent> oldTempoList;
	private final List<MMLTempoEvent> newTempoList;
	private double convertD = 0.0;
	private int convertDCount = 0;

	MMLTempoConverter(List<MMLTempoEvent> oldTempoList, List<MMLTempoEvent> newTempoList) {
		this.oldTempoList = oldTempoList;
		this.newTempoList =  new ArrayList<>();
		newTempoList.forEach(t -> this.newTempoList.add(new MMLTempoEvent(t.getTempo(), convertEvent(t.getTickOffset(), false))));
	}

	int convertEvent(int value, boolean diff) {
		double newTick = getTickOffsetOnTime(newTempoList,
				getTimeOnTickOffset(oldTempoList, value));
		long r = (long) Math.round(newTick);
		if (Math.abs(r) > MMLEvent.MAX_TICK) {
			throw new IllegalTickOffsetException((int)r);
		}
		int tick = (int)r;
		if (diff) {
			convertD += Math.abs(newTick - tick);
			convertDCount++;
		}
		return tick;
	}

	public static MMLTempoConverter convert(MMLScore score, List<MMLTempoEvent> newTempoList) {
		List<MMLTempoEvent> tempoList = score.getTempoEventList();
		var converter = new MMLTempoConverter(tempoList, newTempoList);

		// 変換する
		score.getTrackList().parallelStream().forEach(track -> {
			for (var eventList : track.getMMLEventList()) {
				for (var noteEvent : eventList.getMMLNoteEventList()) {
					int endTick = converter.convertEvent(noteEvent.getEndTick(), true);
					int tickOffset = converter.convertEvent(noteEvent.getTickOffset(), true);
					noteEvent.setTickOffset(tickOffset);
					noteEvent.setTick(endTick - tickOffset);
				}
			}
		});

		// マーカーの変換
		score.getMarkerList().forEach(t -> t.setTickOffset(converter.convertEvent(t.getTickOffset(), true)));

		// テンポリスト更新
		tempoList.clear();
		tempoList.addAll(converter.newTempoList);

		return converter;
	}

	public String getConversionDiff() {
		return String.format("%.3f/%d", convertD, convertDCount);
	}

	/**
	 * 指定したtickオフセット位置の先頭からの時間を返します.
	 * @param tempoList
	 * @param tickOffset
	 * @return 先頭からの時間（ms）
	 */
	public static double getTimeOnTickOffset(List<MMLTempoEvent> tempoList, int tickOffset) {
		double totalTime = 0L;

		int tempo = MMLTempoEvent.INITIAL_TEMPO;
		int currentTick = 0;
		for (MMLTempoEvent tempoEvent : tempoList) {
			int currentTempoTick = tempoEvent.getTickOffset();
			if (tickOffset < currentTempoTick) {
				break;
			}

			int currentTempo = tempoEvent.getTempo();
			if (tempo != currentTempo) {
				totalTime += (currentTempoTick - currentTick) * 60000.0 / tempo;
				currentTick = currentTempoTick;
			}

			tempo = currentTempo;
		}

		totalTime += (tickOffset - currentTick) * 60000.0 / tempo;
		return totalTime / MMLTickTable.TPQN;
	}

	/**
	 * 指定した時間からtickオフセットを返します.
	 * @param tempoList
	 * @param time
	 * @return tickオフセット
	 */
	public static double getTickOffsetOnTime(List<MMLTempoEvent> tempoList, double time) {
		int tempo = MMLTempoEvent.INITIAL_TEMPO;
		double pointTime = 0;
		double tick = 0;
		for (MMLTempoEvent tempoEvent : tempoList) {
			double tempoTime = getTimeOnTickOffset(tempoList, tempoEvent.getTickOffset());
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
}

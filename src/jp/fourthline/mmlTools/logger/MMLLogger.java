/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mmlTools.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLTempoEvent;
import jp.fourthline.mmlTools.MMLTrack;

public final class MMLLogger {
	private static final Map<MMLTrack, MMLLogger> instances = new HashMap<>();

	public static synchronized void prepare(List<MMLTrack> trackList) {
		// trackListにないロガーを削除
		instances.entrySet().removeIf(entry -> !trackList.contains(entry.getKey()));

		// instancesにないロガーを生成
		trackList.stream().filter(t -> !instances.containsKey(t)).forEach(t -> instances.put(t, new MMLLogger()));
	}

	public static synchronized MMLLogger logger(MMLTrack track) {
		var o = instances.get(track);
		if (o == null) {
			o = new MMLLogger();
			instances.put(track, o);
		}
		return o;
	}

	private MMLLogger() {}

	private boolean enable = false;
	private final List<MMLLogEvent> entry = new ArrayList<>();

	public void setEnable(boolean enable) {
		this.enable = enable;
		entry.clear();
	}

	public void noteOverTempoBoundary(MMLEventList relationPart, MMLTempoEvent tempoEvent) {
		if (enable) {
			var event = new MMLLogEvent(relationPart, tempoEvent.getTickOffset(), "Validate Note Over Tempo Boundary", "validate_note_over_tempo_boundary", tempoEvent.toMMLString());
			entry.add(event);
			System.out.println("MMLLogger: " + event.getMessage());
		}
	}

	public List<MMLLogEvent> getEntryList() {
		return entry;
	}
}

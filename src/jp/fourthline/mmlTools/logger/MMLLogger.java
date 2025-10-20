/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mmlTools.logger;

import java.util.ArrayList;
import java.util.List;

import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLTempoEvent;

public final class MMLLogger {
	public MMLLogger() {}

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

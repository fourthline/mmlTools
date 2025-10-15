/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mmlTools.logger;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.logger.LogMessage.PartMessage;

public final class MMLLogEvent implements PartMessage {
	private final int tickOffset;
	private final MMLEventList relationPart;
	private final String message;
	private final String localizedMessage;

	public MMLLogEvent(MMLEventList relationPart, int tickOffset, String msg, String msg_code, String prefix) {
		this.relationPart = relationPart;
		this.tickOffset = tickOffset;
		message = prefix + " " + msg;
		localizedMessage = prefix + " " + AppResource.appText("mml_logger." + msg_code);
	}

	public int getTickOffset() {
		return tickOffset;
	}

	public MMLEventList getRelationPart() {
		return relationPart;
	}

	public String getLocalizedMessage() {
		return localizedMessage;
	}

	public String getMessage() {
		return message;
	}
}

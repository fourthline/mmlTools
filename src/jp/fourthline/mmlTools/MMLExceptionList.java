/*
 * Copyright (C) 2023-2025 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.List;

import jp.fourthline.mmlTools.core.MMLException;
import jp.fourthline.mmlTools.logger.LogMessage.PartMessage;

public final class MMLExceptionList extends Exception {
	private static final long serialVersionUID = 4585574875842403536L;

	private final List<Entry> errList;

	public static class Entry implements PartMessage {
		private final MMLEventList relationPart;
		private final MMLNoteEvent event;
		private final MMLException exception;
		public Entry(MMLEventList relationPart, MMLNoteEvent event, MMLException exception) {
			this.relationPart = relationPart;
			this.event = event;            // テンポによって分割される場合もあるので, relationPartに含まれていないものも許容.
			this.exception = exception;
		}
		public MMLEventList getRelationPart() {
			return relationPart;
		}
		public MMLNoteEvent getNote() {
			return event;
		}
		public MMLException getException() {
			return exception;
		}
		@Override
		public int getTickOffset() {
			return event.getTickOffset();
		}
		@Override
		public String getLocalizedMessage() {
			return exception.getLocalizedMessage();
		}
	}

	public MMLExceptionList(List<Entry> list) {
		super(list.get(0).exception.getMessage());
		errList = list;
	}

	public List<Entry> getErr() {
		return errList;
	}

	@Override
	public String getMessage() {
		return errList.get(0).exception.getMessage();
	}

	@Override
	public String getLocalizedMessage() {
		return errList.get(0).exception.getLocalizedMessage();
	}
}

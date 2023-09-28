/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.List;

import jp.fourthline.mmlTools.core.MMLException;

public final class MMLExceptionList extends Exception {
	private static final long serialVersionUID = 4585574875842403536L;

	private final List<Entry> errList;

	public static class Entry {
		private final MMLNoteEvent event;
		private final MMLException exception;
		public Entry(MMLNoteEvent event, MMLException exception) {
			this.event = event;
			this.exception = exception;
		}
		public MMLNoteEvent getNote() {
			return event;
		}
		public MMLException getException() {
			return exception;
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

/*
 * Copyright (C) 2013-2023 たんらる
 */

package jp.fourthline.mmlTools.core;

import java.util.function.Function;

public final class MMLException extends Exception {
	private static final long serialVersionUID = 300035736039298711L;
	private static Function<String, String> localizeFunc = null;

	public static void setLocalizeFunc(Function<String, String> func) {
		localizeFunc = func;
	}

	private final String localizedMessage;

	private MMLException(String err, String msg, String lmessage) {
		super(err+": "+msg);
		if (localizeFunc != null) {
			localizedMessage = localizeFunc.apply("mml_exception." + lmessage) + ": " + msg;
		} else {
			localizedMessage = null;
		}
	}

	@Override
	public String getLocalizedMessage() {
		return (localizedMessage != null) ? localizedMessage : getMessage();
	}

	public static MMLException createUndefinedTickException(String msg) {
		return new MMLException("Undefined tick table", msg, "undefined_tick");
	}

	public static MMLException createUndefinedTickException(int tick) {
		return createUndefinedTickException(Integer.toString(tick));
	}

	public static MMLException createUndefinedTickException(int tick, int totalTick) {
		if (tick == totalTick) {
			return createUndefinedTickException(tick);
		} else {
			return createUndefinedTickException(tick + "/" + totalTick);
		}
	}

	public static MMLException createIllegalNote(String note) {
		return new MMLException("Illegal note", note, "illegal_note");
	}

	public static MMLException createIllegalNote(int note) {
		return createIllegalNote(Integer.toString(note));
	}
}

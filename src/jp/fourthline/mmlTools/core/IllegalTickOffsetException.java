/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools.core;

public final class IllegalTickOffsetException extends IllegalArgumentException {
	private static final long serialVersionUID = -8567210205866414781L;

	public IllegalTickOffsetException(int tickOffset) {
		super("illegal tickOffset: " + tickOffset);
	}
}

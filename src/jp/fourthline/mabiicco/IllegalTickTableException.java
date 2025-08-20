/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco;

public class IllegalTickTableException extends Exception {
	private static final long serialVersionUID = -8612192197517876560L;
	public IllegalTickTableException() {
		super(AppResource.appText("error.illegal_tick_table"));
	}
}

/*
 * Copyright (C) 2013-2014 たんらる
 */

package jp.fourthline.mmlTools.core;


/**
 * UndefinedTickException
 * @author たんらる
 */
public final class UndefinedTickException extends Exception {
	private static final long serialVersionUID = 300035736039298711L;

	public UndefinedTickException(String msg) {
		super("Undefined tick table: "+msg);
	}
}

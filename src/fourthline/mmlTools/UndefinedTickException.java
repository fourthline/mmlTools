/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;


/**
 * UndefinedTickException
 * @author たんらる
 */
@SuppressWarnings("serial")
public class UndefinedTickException extends Exception {
	public UndefinedTickException(String msg) {
		super("Undefined tick table: "+msg);
	}
}

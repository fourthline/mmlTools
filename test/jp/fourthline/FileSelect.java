/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline;

import java.io.IOException;
import java.io.InputStream;

abstract public class FileSelect {
	protected final InputStream fileSelect(String name) throws IOException {
		return getClass().getResourceAsStream(name);
	}
}

/*
 * Copyright (C) 2014-2023 たんらる
 */

package jp.fourthline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public abstract class FileSelect {
	protected final InputStream fileSelect(String name) throws IOException {
		return getClass().getResourceAsStream(name);
	}

	protected final File fileSelectF(String name) throws URISyntaxException {
		return new File(getClass().getResource(name).toURI());
	}
}

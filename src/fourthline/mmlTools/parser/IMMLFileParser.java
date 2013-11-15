/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.File;

import fourthline.mmlTools.MMLScore;

/**
 * MMLファイルのparser
 * @author fourthline
 *
 */
public interface IMMLFileParser {
	public MMLScore parse(File file) throws MMLParseException;
}

/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.File;

/**
 * MMLファイルのparser
 * @author fourthline
 *
 */
public interface IMMLFileParser {
	public MMLTrack[] parse(File file) throws MMLParseException;
}

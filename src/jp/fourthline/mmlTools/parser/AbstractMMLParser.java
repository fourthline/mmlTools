/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.util.Map;


public abstract class AbstractMMLParser implements IMMLFileParser {
	protected Map<String, Boolean> parseProperties = null;

	@Override
	public Map<String, Boolean> getParseProperties() {
		return parseProperties;
	}

	@Override
	public String getName() {
		return "";
	}
}

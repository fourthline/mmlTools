/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.util.Collection;
import java.util.Map;


public abstract class AbstractMMLParser implements IMMLFileParser {
	protected Map<String, Boolean> parseProperties = null;
	protected Map<String, Collection<String>> parseAttributes = null;
	protected Map<Integer, TrackSelect> trackSelectMap = null;

	@Override
	public Map<String, Boolean> getParseProperties() {
		return parseProperties;
	}

	@Override
	public Map<String, Collection<String>> getParseAttributes() {
		return parseAttributes;
	}

	@Override
	public void setParseAttribute(String key, String value) {}

	@Override
	public String getName() {
		return "";
	}

	public Map<Integer, TrackSelect> getTrackSelectMap() {
		return trackSelectMap;
	}
}

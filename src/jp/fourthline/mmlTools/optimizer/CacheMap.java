/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 8400828433060223318L;

	private final int max;

	public CacheMap(int max) {
		super(16, 0.75f, true);
		this.max = max;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > max;
	}
}

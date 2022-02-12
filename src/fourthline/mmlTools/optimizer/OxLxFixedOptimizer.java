/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fourthline.mmlTools.core.MMLTokenizer;

/**
 * OxLx機能拡張
 */
public final class OxLxFixedOptimizer extends OxLxOptimizer {
	/**
	 * 置換パターン用レコード
	 */
	private static final record FixPattern(String lStr, String match, String replace) {
		public void patternApply(String key, StringBuilder sb) {
			if (key.equals(lStr)) {
				int len = sb.length();
				int index = sb.length()-match.length();
				if (sb.substring(sb.length()-match.length()).equals(match)) {
					sb.replace(index, len, replace);
				}
			}
		}
	};

	/**
	 * 置換パターン
	 */
	private static final List<FixPattern> pattern = List.of(
			new FixPattern("32", "r16.", "rrr"),
			new FixPattern("64", "r32.", "rrr"));

	@Override
	protected void fixPattern(Map<String, StringBuilder> map) {
		map.forEach((key, builder) -> {
			pattern.forEach(t -> t.patternApply(key, builder));
		});
	}

	/**
	 * l2c&c4. -> l2c.l8c のパターンをつくる
	 */
	@Override
	protected void extendPatternBuilder(String key, Map<String, StringBuilder> newBuilderMap, String minString, String noteName, String lenString, int insertBack) {
		if ( (insertBack > 0) && (!key.endsWith(".")) && (minString.endsWith(noteName+"&")) && (lenString.equals( Integer.parseInt(key)*2 + ".") )) {
			String newKey = ""+Integer.parseInt(key)*4;
			StringBuilder ssb = new StringBuilder(minString);
			ssb.insert(minString.length()-1, '.');
			newBuilderMap.put(newKey, newBuilder(newStringBuilder(newBuilderMap, newKey, ssb.toString()), newKey, noteName, insertBack));
		}
	}

	public static class OptimizerMap2 extends OptimizerMap {
		private static final long serialVersionUID = -1916149376927832458L;
		public static int count = 0;

		record OptimizerCache(String mml, NxBpCmOptimizer optimizer) {}

		// key, cache data
		private final HashMap<String, OptimizerCache> cacheMap = new HashMap<>();

		private NxBpCmOptimizer opti(String mml) {
			count++;
			NxBpCmOptimizer optimizer = new NxBpCmOptimizer();
			new MMLTokenizer(mml).forEachRemaining(t -> optimizer.nextToken(t));
			return optimizer;
		}

		private NxBpCmOptimizer getOptimizer(String key, String mml) {
			if (cacheMap.containsKey(key)) {
				OptimizerCache cache = cacheMap.get(key);
				int index = cache.mml.length();
				if ( (mml.length() > index) && mml.substring(0, index).equals(cache.mml)) {
					String s = mml.substring(index);
					new MMLTokenizer(s).forEachRemaining(t -> cache.optimizer.nextToken(t));
					cacheMap.put(key, new OptimizerCache(mml, cache.optimizer));
					return cache.optimizer;
				}
			}
			NxBpCmOptimizer opt = opti(mml);
			cacheMap.put(key, new OptimizerCache(mml, opt));
			return opt;
		}

		@Override
		protected void updateMapMinLength(String key, StringBuilder builder) {
			StringBuilder now = this.get(key);
			if ( (now == null) ) {
				this.put(key, builder);
			} else {
				int i1 = new MMLStringOptimizer(builder.toString()).optimizeOct().length(); // TODO: ここが重い.
				int i2 =  getOptimizer(key, now.toString()).getMinString().length();
				if (i1 < i2) {
					this.put(key, builder);
				}
			}
		}
	}

	@Override
	protected OptimizerMap createOptimizerMap() {
		return new OptimizerMap2();
	}
}

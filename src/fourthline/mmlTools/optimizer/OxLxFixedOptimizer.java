/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.List;
import java.util.Map;

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

	private static class OptimizerMap2 extends OptimizerMap {
		private static final long serialVersionUID = -1916149376927832458L;

		@Override
		protected void updateMapMinLength(String key, StringBuilder builder) {
			String s1 = new MMLStringOptimizer(builder.toString()).optimizeOct();
			StringBuilder now = this.get(key);
			if ( (now == null) || (s1.length() < new MMLStringOptimizer(now.toString()).optimizeOct().length()) ) {
				this.put(key, builder);
			}
		}
	}

	@Override
	protected OptimizerMap createOptimizerMap() {
		return new OptimizerMap2();
	}
}

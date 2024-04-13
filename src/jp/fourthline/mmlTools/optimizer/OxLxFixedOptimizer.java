/*
 * Copyright (C) 2022-2024 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jp.fourthline.mmlTools.core.MMLTokenizer;

/**
 * OxLx機能拡張
 */
public class OxLxFixedOptimizer extends OxLxOptimizer {
	/**
	 * 置換パターン用レコード
	 */
	protected static final class FixPattern {
		private final String lStr;
		private final String match;
		private final int matchLen;
		private final String replace;

		protected FixPattern(String lStr, String match, String replace) {
			this.lStr = lStr;
			this.match = match;
			this.matchLen = match.length();
			this.replace = replace;
		}

		public void patternApply(String key, StringBuilder sb) {
			if (key.equals(lStr) || lStr.equals("*")) {
				if (sb.toString().endsWith(match)) {
					int len = sb.length();
					int index = len - matchLen;
					sb.replace(index, len, replace);
				}
			}
		}
	}

	private final boolean disableNopt;
	public OxLxFixedOptimizer(boolean disableNopt) {
		this.disableNopt = disableNopt;
	}

	/**
	 * 置換パターン
	 */
	private static final List<FixPattern> pattern = List.of(
			new FixPattern("32", "r16.", "rrr"),
			new FixPattern("64", "r32.", "rrr"));

	@Override
	protected void fixPattern(OptimizerMap map) {
		map.forEach((key, builder) -> pattern.stream().forEach(t -> t.patternApply(key, builder)));
	}

	/**
	 * l2c&c4. -> l2c.l8c のパターンをつくる
	 */
	@Override
	protected void extendPatternBuilder(Map<String, StringBuilder> newBuilderMap, String minString, String noteName, String lenString, int insertBack) {
		int keyIndex = minString.lastIndexOf("l");
		String key = "4";
		if (keyIndex >= 0) {
			key = new MMLTokenizer(minString.substring(keyIndex)).next().substring(1); 
		}
		if ( (insertBack > 0) && (!key.endsWith(".")) && (minString.endsWith(noteName+"&")) && (lenString.equals( (Integer.parseInt(key) << 1) + ".") )) {
			String newKey = ""+ (Integer.parseInt(key) << 2);
			StringBuilder ssb = new StringBuilder(minString);
			ssb.insert(minString.length()-1, '.');
			newBuilderMap.put(newKey, newBuilder(newStringBuilder(newBuilderMap, newKey, ssb.toString()), newKey, noteName, insertBack));
		}
	}

	public final class OptimizerMap2 extends OptimizerMap {
		private static final long serialVersionUID = -1916149376927832458L;

		private static int compString(StringBuilder s1, StringBuilder s2) {
			char[] ss1 = s1.toString().toCharArray();
			char[] ss2 = s2.toString().toCharArray();
			int i = Arrays.mismatch(ss1, ss2) - 1;
			if (i < 0) i = 0;
			while (i > 0) {
				i--;
				char c = ss1[i];
				if (MMLTokenizer.isToken(c) || MMLTokenizer.isNote(c))
					break;
			}
			return i;
		}

		private static int calcOctave(String mml) {
			char[] a = mml.toCharArray();
			int octave = 4;
			for (int i = 0; i < a.length; i++) {
				switch (a[i]) {
				case '<':
					octave--;
					break;
				case '>':
					octave++;
					break;
				case 'o':
				case 'O':
					i++;
					octave = a[i] - '0';
					break;
				}
			}
			return octave;
		}


		private static final Map<String, Integer> cache = Collections.synchronizedMap(new CacheMap<>(1024 << 3));
		static {
			MMLStringOptimizer.addCacheList(cache);
		}

		private static int calcSubNxBpCmOptLength(String commonStr, String mml, int octave, boolean disableNopt) {
			int commonLen = commonStr.length();

			String key = commonLen + ":" + mml + ":" + + octave + ":" + (disableNopt ? "1" : "0");
			var s = cache.get(key);
			if (s != null) {
				return s;
			}

			NxBpCmOptimizer optimizer = new NxBpCmOptimizer(octave, commonStr, disableNopt);
			new MMLTokenizer(mml).forEachRemaining(optimizer::nextToken);

			var r = optimizer.getMinString().length();
			cache.put(key, r);
			return r;
		}

		@Override
		protected void updateMapMinLength(String key, StringBuilder builder) {
			StringBuilder now = this.get(key);
			if ( (now == null) ) {
				this.put(key, builder);
			} else {
				int commonLen = compString(builder, now);
				int octave = calcOctave(builder.substring(0, commonLen));
				String commonStr = builder.substring(0, commonLen);
				int i1 = calcSubNxBpCmOptLength(commonStr, builder.substring(commonLen), octave, disableNopt);
				int i2 = calcSubNxBpCmOptLength(commonStr, now.substring(commonLen), octave, disableNopt);
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

/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mmlTools.optimizer;

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
	protected void extendPatternBuilder(Map<String, StringBuilder> newBuilderMap, String minString, String noteName, String lenString, int insertBack) {
		int keyIndex = minString.lastIndexOf("l");
		String key = "4";
		if (keyIndex >= 0) {
			key = new MMLTokenizer(minString.substring(keyIndex)).next().substring(1); 
		}
		if ( (insertBack > 0) && (!key.endsWith(".")) && (minString.endsWith(noteName+"&")) && (lenString.equals( Integer.parseInt(key)*2 + ".") )) {
			String newKey = ""+Integer.parseInt(key)*4;
			StringBuilder ssb = new StringBuilder(minString);
			ssb.insert(minString.length()-1, '.');
			newBuilderMap.put(newKey, newBuilder(newStringBuilder(newBuilderMap, newKey, ssb.toString()), newKey, noteName, insertBack));
		}
	}

	public static final class OptimizerMap2 extends OptimizerMap {
		private static final long serialVersionUID = -1916149376927832458L;

		private static int compString(StringBuilder s1, StringBuilder s2) {
			char[] ss1 = s1.toString().toCharArray();
			char[] ss2 = s2.toString().toCharArray();
			int len = Math.min(ss1.length, ss2.length);
			int i;
			for (i = 0; i < len; i++) {
				if (ss1[i] != ss2[i]) {
					break;
				}
			}

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

		public static int count = 0;
		private static int calcSubNxBpCmOptLength(String mml, int commonLen, int octave) {
			count++;
			String initStr = mml.substring(0, commonLen);
			NxBpCmOptimizer optimizer = new NxBpCmOptimizer(octave, initStr);
			new MMLTokenizer(mml.substring(commonLen)).forEachRemaining(t -> optimizer.nextToken(t));
			return optimizer.getMinString().length();
		}

		@Override
		protected void updateMapMinLength(String key, StringBuilder builder) {
			StringBuilder now = this.get(key);
			if ( (now == null) ) {
				this.put(key, builder);
			} else {
				int commonLen = compString(builder, now);
				int octave = calcOctave(builder.substring(0, commonLen));
				int i1 = calcSubNxBpCmOptLength(builder.toString(), commonLen, octave);
				int i2 = calcSubNxBpCmOptLength(now.toString(), commonLen, octave);
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

/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jp.fourthline.mmlTools.core.MMLException;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLTokenizer;
import jp.fourthline.mmlTools.optimizer.MMLStringOptimizer.Optimizer;

/**
 * 代替パターンも使用する最適化.
 */
public final class OxLxFixedAltOptimizer extends OxLxFixedOptimizer {

	public static class PatternOptimizer implements Optimizer {
		private static final List<FixPattern> pattern = List.of(
				new FixPattern("*", "r1.r2", "r1r1"));

		private final StringBuilder sb = new StringBuilder();

		@Override
		public void nextToken(String token) {
			sb.append(token);

			pattern.stream().forEach(t -> t.patternApply("", sb));
		}

		@Override
		public String getMinString() {
			return sb.toString();
		}
	}

	private final OptimizerMap altPatternMap = new OptimizerMap();

	/**
	 * 置換パターン
	 */
	private static final List<FixPattern> pattern = List.of(
			new FixPattern("32", "21", "."),
			new FixPattern("24", "r12", "rr"),
			new FixPattern("32", "r16", "rr"),
			new FixPattern("48", "r24", "rr"),
			new FixPattern("64", "r32", "rr"),
			new FixPattern("24", "r12.", "rrr"),
			new FixPattern("32", "r16.", "rrr"),
			new FixPattern("48", "r24.", "rrr"),
			new FixPattern("64", "r32.", "rrr"),
			new FixPattern("4", "r3r9r16", "rr5r17"));

	public OxLxFixedAltOptimizer(boolean disableNopt) {
		super(disableNopt);
	}

	@Override
	protected void fixPattern(OptimizerMap map) {
		altPatternMap.clear();
		map.forEach((key, builder) -> tickAltPattern(altPatternMap, key, builder));
		altPatternMap.forEach(map::updateMapMinLength);

		map.forEach((key, builder) -> pattern.stream().forEach(t -> t.patternApply(key, builder)));
		super.fixPattern(map);
	}

	/**
	 * L12 g4&g6 -> L12 g&g3
	 * @param key
	 * @param builder
	 */
	private void tickAltPattern(OptimizerMap newBuilderMap, String key, StringBuilder builder) {
		int lastIndex2 = builder.length();
		int startIndex2 = lastIndex2;
		while (!MMLTokenizer.isNote(builder.charAt(--startIndex2)));
		if (startIndex2 == 0)
			return;

		var note2 = MMLTokenizer.noteNames(builder.substring(startIndex2, lastIndex2));
		int lastIndex = startIndex2 - 1;
		if (note2[0].equals("r")) {
			lastIndex++;
		} else if (builder.charAt(lastIndex) != '&') {
			return;
		}

		int startIndex = lastIndex;
		while ((startIndex > 0) && !MMLTokenizer.isNote(builder.charAt(--startIndex)));
		var note1 = MMLTokenizer.noteNames(builder.substring(startIndex, lastIndex));

		// &連結で同じはずだが, 違った場合は中断する.
		if (!note1[0].equals(note2[0]))
			return;

		// n記号は処理しない.
		if (note1[0].equals("n") || note2[0].equals("n")) 
			return;

		// 休符の場合は &連結不要.
		final String amp = !note1[0].equals("r") ? "&" : "";

		try {
			var s1 = AltPattern.getAltList(note1[1], note2[1], key);
			if (s1 != null) {
				var str = builder.substring(0, startIndex);
				var m = s1.stream().filter(t -> t.getLStr().isEmpty()).map(t -> note1[0] + (t.getNeedDot() ? "."+amp : amp ) + note2[0] + t.getAltPattern()).min(Comparator.naturalOrder());

				// 代替Lを使って更新.
				if (m.isPresent()) {
					// L16 .... a.&a8
					builder.delete(startIndex, lastIndex2);
					builder.append(m.get());
				}

				// 代替LでLパターンを新規につくる.
				for (var item : s1) {
					var pt = item.getAltPattern();
					var altStr = item.getLStr();
					if (altStr.isPresent()) {
						// a4.&a4 -> l4a.a の新規
						newBuilderMap.updateMapMinLength(altStr.get(), new StringBuilder(str + "l" + altStr.get() + note1[0] + pt + amp + note2[0]));
					} else if (!item.getNeedDot()) {
						if (pt.endsWith(".")) {
							// L8 .... al16&a.
							String pt2 = pt.substring(0, pt.length() - 1);
							newBuilderMap.updateMapMinLength(pt2, new StringBuilder(str + note1[0] + ((!key.equals(pt2)) ? "l" + pt2 : "") + amp + note2[0] + "."));
						}
						// L8 .... al16.&a
						newBuilderMap.updateMapMinLength(pt, new StringBuilder(str + note1[0] + "l" + pt + amp + note2[0]));
					} else {
						// L16 .... a.l8&a
						newBuilderMap.updateMapMinLength(pt, new StringBuilder(str + note1[0] + "." + ((!key.equals(pt)) ? "l" + pt : "") + amp + note2[0]));
					}
				}
			}
		} catch (MMLException e) {
			e.printStackTrace();
		}
	}

	private static class AltPattern {
		private static final Map<String, List<AltPattern>> altCache = Collections.synchronizedMap(new CacheMap<>(128));

		static {
			MMLStringOptimizer.addCacheList(altCache);
		}

		private final String altPattern;
		private final boolean needDot;
		private final Optional<String> lStr;
		private AltPattern(String altPattern, boolean needDot) {
			this(altPattern, needDot, Optional.empty());
		}
		private AltPattern(String altPattern, boolean needDot, Optional<String> lStr) {
			this.altPattern = altPattern;
			this.needDot = needDot;
			this.lStr = lStr;
		}
		public String getAltPattern() {
			return altPattern;
		}
		public boolean getNeedDot() {
			return needDot;
		}
		public Optional<String> getLStr() {
			return lStr;
		}

		public static List<AltPattern> getAltList(String note1, String note2, String lStr) throws MMLException {
			if (!MMLTokenizer.isLenOnly(note1) || !MMLTokenizer.isLenOnly(note2)) {
				return null;
			}

			String key = note1 + "&" + note2 + ":" + lStr;
			var v = altCache.get(key);
			if (v != null) {
				return v;
			}

			int tick = MMLTicks.getTick(note1) + MMLTicks.getTick(note2);
			var t = MMLTicks.getAlt(tick);
			var list = new ArrayList<AltPattern>();
			if (t.isPresent()) {
				var alt = t.get();
				int i = 0, j = 0;
				for ( ; i < alt.size(); i++) {
					boolean find = false;
					for (j = 0; j < alt.get(i).size(); j++) {
						var pattern = alt.get(i).get((j == 0) ? 1 : 0);
						if (lStr.equals(alt.get(i).get(j))) {
							list.add(new AltPattern(pattern, false));
							find = true;
						} else if ((lStr+".").equals(alt.get(i).get(j))) {
							if (!pattern.endsWith(".")) {
								list.add(new AltPattern(pattern, true));
								find = true;
							}
						}
					}
					if (!find) {
						String s0 = alt.get(i).get(0);
						String s1 = alt.get(i).get(1);
						if ((s1+".").equals(s0)) {
							list.add(new AltPattern(".", false, Optional.of(s1)));
							list.add(new AltPattern(s1, false, Optional.of(s0)));
						}
					}
				}
			}
			var r = (list.size() > 0) ? list : null;
			altCache.put(key, r);
			return r;
		}
	}
}

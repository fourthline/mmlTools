/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.HashMap;

/**
 * Nx + BpCm統合版
 */
public final class NxBpCmOptimizer extends NxOptimizer {
	private final class NxBuilderPattern extends HashMap<Integer, String> {
		private static final long serialVersionUID = 2475114248706563390L;
		private void addToken(NxBuilder builder, int nextOctave, String token) {
			StringBuilder sb = new StringBuilder(builder.builder.toString());
		
			sb.append( OxLxOptimizer.getOctaveString(builder.prevOct, nextOctave) );
			sb.append(token);

			String t = get(nextOctave);
			if ((t == null) || (t.length() > sb.length())) {
				put(nextOctave, sb.toString());
			}
		}
	}

	private final NxBuilderPattern map = new NxBuilderPattern();

	@Override
	protected void notePattern(String token, String noteName, String noteLength) {
		map.clear();

		for (NxBuilder t : builderList) {
			// 通常パターン
			map.addToken(t, octave, token);
			if (noteName.equals("b")) {
				// b -> <c- パターン
				map.addToken(t, octave+1, "c-" + noteLength);
			} else if (noteName.equals("c")) {
				// c -> >b+ パターン
				map.addToken(t, octave-1, "b+" + noteLength);
			}
			if (t.prevOct != octave) {
				// nパターン
				if (noteLength.length() == 0) {
					int noteNumber = getCurrentNoteNumber();
					if ( (noteNumber >= 0) && (noteNumber <= 96) ) {
						map.addToken(t, t.prevOct, "n" + noteNumber);
					}
				}
			}
		}

		builderList.clear();
		map.forEach((key, str) -> {
			builderList.add(new NxBuilder(key, str));
		});
	}
}

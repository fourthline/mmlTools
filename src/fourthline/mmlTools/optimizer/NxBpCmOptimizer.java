/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Nx + BpCm統合版
 */
public final class NxBpCmOptimizer extends NxOptimizer {
	private final Map<Integer, String> map = new HashMap<>();

	public NxBpCmOptimizer() {
		super();
	}

	private void addToken(NxBuilder builder, int nextOctave, String token) {
		StringBuilder sb = new StringBuilder(builder.builder);
	
		sb.append( OxLxOptimizer.getOctaveString(builder.prevOct, nextOctave) + token );

		String t = map.get(nextOctave);
		if ((t == null) || (t.length() > sb.length())) {
			map.put(nextOctave, sb.toString());
		}
	}

	public NxBpCmOptimizer(int octave, String initStr) {
		super();
		this.octave = octave;
		builderList.clear();
		builderList.add(new NxBuilder(octave, initStr));
		parser.setOctave(octave);
	}

	@Override
	protected void notePattern(String token, String noteName, String noteLength) {
		map.clear();

		builderList.forEach(t -> {
			// 通常パターン
			addToken(t, octave, token);
			if (noteName.equals("b")) {
				// b -> <c- パターン
				addToken(t, octave+1, "c-" + noteLength);
			} else if (noteName.equals("c")) {
				// c -> >b+ パターン
				addToken(t, octave-1, "b+" + noteLength);
			}
			if (t.prevOct != octave) {
				// nパターン
				if (noteLength.length() == 0) {
					int noteNumber = getCurrentNoteNumber();
					if ( (noteNumber >= 0) && (noteNumber <= 96) ) {
						addToken(t, t.prevOct, "n" + noteNumber);
					}
				}
			}
		});

		builderList.clear();
		map.forEach((key, str) -> {
			builderList.add(new NxBuilder(key, str));
		});
	}
}

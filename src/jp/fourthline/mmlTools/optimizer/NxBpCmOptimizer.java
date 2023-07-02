/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Nx + BpCm統合版
 */
public final class NxBpCmOptimizer extends NxOptimizer {
	/**
	 * パターン構築用のMap<Octave, MML>
	 */
	private final Map<Integer, String> map = new HashMap<>();

	private final boolean disableNopt;

	public NxBpCmOptimizer(boolean disableNopt) {
		super();
		this.disableNopt = disableNopt;
	}

	/**
	 * パターン追加
	 * @param builder
	 * @param nextOctave
	 * @param token
	 */
	private void addToken(NxBuilder builder, int nextOctave, String token) {
		StringBuilder sb = new StringBuilder(builder.builder);

		if (builder.prevOct != nextOctave) {
			sb.append( OxLxOptimizer.getOctaveString(builder.prevOct, nextOctave) );
		}
		sb.append(token);

		String t = map.get(nextOctave);
		if ((t == null) || (t.length() > sb.length())) {
			map.put(nextOctave, sb.toString());
		}
	}

	public NxBpCmOptimizer(int octave, String initStr, boolean disableNopt) {
		super();
		this.octave = octave;
		this.disableNopt = disableNopt;
		builderList.clear();
		builderList.add(new NxBuilder(octave, initStr));
		parser.setOctave(octave);
	}

	@Override
	protected void notePattern(String token, String noteName, String noteLength) {
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
			if ((!disableNopt) && (t.prevOct != octave)) {
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
		map.forEach((key, str) -> builderList.add(new NxBuilder(key, str)));
		map.clear();
	}
}

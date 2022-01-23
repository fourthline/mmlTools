/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.List;

/**
 * Nx + BpCm統合版
 */
public class NxBpCmOptimizer extends NxOptimizer {
	private void addBpCmPattern(List<NxBuilder> prevMap, String noteName, String noteLength) {
		prevMap.forEach(t -> {
			if ( (t.prevOct > octave) && (noteName.equals("b")) ) {
				NxBuilder o = t.clone();
				o.builder.append( OxLxOptimizer.getOctaveString(t.prevOct, octave+1) );
				o.builder.append("c-"+noteLength);
				o.prevOct = octave+1;
				builderList.add(o);
			} else if ( (t.prevOct < octave) && (noteName.equals("c")) ) {
				NxBuilder o = t.clone();
				o.builder.append( OxLxOptimizer.getOctaveString(t.prevOct, octave-1) );
				o.builder.append("b+"+noteLength);
				o.prevOct = octave-1;
				builderList.add(o);
			}
		});
	}

	protected void notePattern(String token, String noteName, String noteLength) {
		List<NxBuilder> prevList = listClone();
		List<NxBuilder> prevList2 = listClone();
		addNoteToken(token);
		cleanList();
		if (noteLength.length() == 0) {
			addPattern(prevList);
		}
		addBpCmPattern(prevList2, noteName, noteLength);
	}
}

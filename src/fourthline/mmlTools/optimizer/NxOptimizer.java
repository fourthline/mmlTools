/*
 * Copyright (C) 2015-2017 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import fourthline.mmlTools.core.MMLTokenizer;
import fourthline.mmlTools.core.MelodyParser;
import fourthline.mmlTools.core.ParserWarn3ML;
import fourthline.mmlTools.core.UndefinedTickException;


/**
 * Nxを使用した最適化.
 */
public final class NxOptimizer implements MMLStringOptimizer.Optimizer {

	private static final class NxBuilder implements Cloneable {
		private StringBuilder builder = new StringBuilder();
		private int nCount = 0;
		private int prevOct;
		private OptionalInt offset = OptionalInt.empty();

		private void addOctToken(int offset, String token) {
			if (!this.offset.isPresent()) {
				this.offset = OptionalInt.of( offset );
			}
		}

		private NxBuilder(int initOct) {
			prevOct = initOct;
		}

		@Override
		public String toString() {
			return builder.toString()+" [o"+prevOct+"] ("+offset+") ";
		}

		@Override
		public NxBuilder clone() {
			NxBuilder obj = new NxBuilder(prevOct);
			obj.builder = new StringBuilder(builder.toString());
			if (offset.isPresent()) {
				obj.offset = OptionalInt.of( offset.getAsInt() );
			}
			return obj;
		}
	}

	private int octave = 4;
	private final MelodyParser parser = new MelodyParser("");

	private final List<NxBuilder> builderList = new ArrayList<>();

	public NxOptimizer() {
		builderList.add(new NxBuilder(octave));
	}

	private NxBuilder minStack(List<NxBuilder> stack) {
		return stack.stream().min((t1, t2) -> {
			int ret = t1.builder.length() - t2.builder.length();
			if (ret == 0) {
				ret = t1.nCount - t2.nCount;
			}
			return ret;
		}).get();
	}

	private void addPattern(List<NxBuilder> prevMap) {
		int noteNumber = parser.getNoteNumber();
		if ( (noteNumber < 0) || (noteNumber > 96) ) {
			return;
		}
		prevMap.forEach(t -> {
			t.builder.append("n"+noteNumber);
			t.nCount++;
		});
		builderList.add( minStack(prevMap) );
		clearOctToken();
	}

	private void addOctToken(String token) {
		builderList.forEach(t -> {
			t.addOctToken(t.builder.length(), token);
		});
	}

	private void clearOctToken() {
		builderList.forEach(t -> {
			t.offset = OptionalInt.empty();
		});
	}

	private List<NxBuilder> listClone() {
		List<NxBuilder> cloneList = new ArrayList<>();
		builderList.forEach(t -> {
			cloneList.add(t.clone());
		});
		return cloneList;
	}

	private void cleanList() {
		NxBuilder min = minStack(builderList);
		builderList.clear();
		builderList.add(min);
	}

	private void notePattern(String token, String noteLength) {
		List<NxBuilder> prevList = listClone();
		addNoteToken(token);
		cleanList();
		if (noteLength.length() == 0) {
			addPattern(prevList);
		}
	}

	private void doToken(String token) {
		String s[] = MMLTokenizer.noteNames(token);
		char firstC = Character.toLowerCase(s[0].charAt(0));
		if ( (firstC >= 'a') && (firstC <= 'g') ) {
			notePattern(token, s[1]);
		} else if (firstC == '>') {
			octave++;
			addOctToken(token);
		} else if (firstC == '<') {
			octave--;
			addOctToken(token);
		} else if (firstC == 'o') {
			octave = Integer.parseInt(s[1]);
			addOctToken(token);
		} else {
			addToken(token);
		}
	}

	private void addNoteToken(String token) {
		builderList.forEach(t -> {
			t.builder.append( OxLxOptimizer.getOctaveString(t.prevOct, octave) );
			t.builder.append(token);
			t.prevOct = octave;
		});
		clearOctToken();
	}

	private void addToken(String token) {
		builderList.forEach(t -> {
			t.builder.append(token);
		});
	}

	private void printMap() {
		if (MMLStringOptimizer.getDebug()) {
			builderList.forEach(t -> {
				System.out.println(t.toString());
			});
			System.out.println(" -- ");
		}
	}

	@Override
	public void nextToken(String token) {
		try {
			parser.noteGT(token);
		} catch (UndefinedTickException | ParserWarn3ML e) {}

		doToken(token);
		printMap();
	}

	@Override
	public String getMinString() {
		return minStack(builderList).builder.toString();
	}
}

/*
 * Copyright (C) 2015-2024 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

import jp.fourthline.mmlTools.core.MMLTokenizer;
import jp.fourthline.mmlTools.core.MelodyParser;
import jp.fourthline.mmlTools.core.ParserWarn3ML;
import jp.fourthline.mmlTools.core.MMLException;


/**
 * Nxを使用した最適化.
 */
public class NxOptimizer implements MMLStringOptimizer.Optimizer {

	protected static final class NxBuilder implements Cloneable {
		private static final Comparator<NxBuilder> comparator = Comparator.comparingInt((NxBuilder t) -> t.builder.length()).thenComparingInt(t -> t.nCount);

		protected StringBuilder builder = new StringBuilder();
		private int nCount = 0;
		protected int prevOct;
		private OptionalInt offset = OptionalInt.empty();

		private void addOctToken(int offset, String token) {
			if (this.offset.isEmpty()) {
				this.offset = OptionalInt.of( offset );
			}
		}

		private NxBuilder(int initOct) {
			prevOct = initOct;
		}

		protected NxBuilder(int initOct, String initStr) {
			prevOct = initOct;
			builder = new StringBuilder(initStr);
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

	protected int octave = 4;
	protected final MelodyParser parser = new MelodyParser("");

	protected final List<NxBuilder> builderList = new ArrayList<>();

	public NxOptimizer() {
		builderList.add(new NxBuilder(octave));
	}

	private NxBuilder minStack(List<NxBuilder> stack) {
		return stack.stream().min(NxBuilder.comparator).get();
	}

	protected int getCurrentNoteNumber() {
		return parser.getNoteNumber();
	}

	/**
	 * nのパターンを追加する
	 * @param prevMap
	 */
	private void addPattern(List<NxBuilder> prevMap) {
		int noteNumber = getCurrentNoteNumber();
		if ( (noteNumber < 0) || (noteNumber > 96) ) {
			return;
		}
		prevMap.stream().forEach(t -> {
			t.builder.append("n").append(noteNumber);
			t.nCount++;
		});
		builderList.add( minStack(prevMap) );
		clearOctToken();
	}

	private void addOctToken(String token) {
		builderList.stream().forEach(t -> t.addOctToken(t.builder.length(), token));
	}

	private void clearOctToken() {
		builderList.stream().forEach(t -> t.offset = OptionalInt.empty());
	}

	private List<NxBuilder> listClone() {
		List<NxBuilder> cloneList = new ArrayList<>();
		builderList.stream().forEach(t -> cloneList.add(t.clone()));
		return cloneList;
	}

	private void cleanList() {
		NxBuilder min = minStack(builderList);
		builderList.clear();
		builderList.add(min);
	}

	protected void notePattern(String token, String noteName, String noteLength) {
		List<NxBuilder> prevList = listClone();
		addNoteToken(token);
		cleanList();
		if (noteLength.length() == 0) {
			addPattern(prevList);
		}
	}

	private void doToken(String token) {
		String[] s = MMLTokenizer.noteNames(token);
		char firstC = Character.toLowerCase(s[0].charAt(0));
		if ( (firstC >= 'a') && (firstC <= 'g') ) {
			notePattern(token, s[0], s[1]);
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
		builderList.stream().forEach(t -> {
			t.builder.append( OxLxOptimizer.getOctaveString(t.prevOct, octave) );
			t.builder.append(token);
			t.prevOct = octave;
		});
		clearOctToken();
	}

	private void addToken(String token) {
		builderList.stream().forEach(t -> t.builder.append(token));
	}

	private void printMap() {
		if (MMLStringOptimizer.getDebug()) {
			builderList.stream().forEach(t -> System.out.println(t.toString()));
			System.out.println(" -- ");
		}
	}

	@Override
	public void nextToken(String token) {
		try {
			parser.noteGT(token);
		} catch (MMLException | ParserWarn3ML e) {}

		doToken(token);
		printMap();
	}

	@Override
	public String getMinString() {
		return minStack(builderList).builder.toString();
	}
}

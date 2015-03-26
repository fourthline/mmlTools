/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mmlTools.optimizer;

import fourthline.mmlTools.core.MMLTokenizer;

/**
 * Lx-builder, Ox
 */
public final class MMLStringOptimizer {

	private static boolean debug = false;

	public static void setDebug(boolean b) {
		debug = b;
	}

	public static boolean getDebug() {
		return debug;
	}

	private String originalMML;

	/**
	 * @param mml   MMLEventListで出力したMML文字列.
	 */
	public MMLStringOptimizer(String mml) {
		originalMML = mml;
	}

	@Override
	public String toString() {
		return getOptimizedString();
	}

	private String getOptimizedString() {
		return optimize();
	}

	private String optimize() {
		String mml = originalMML;
		Optimizer optimizerList[] = {
				new OxLxOptimizer(),
				new BpCmOptimizer(),
				new NxOptimizer()
		};

		for (Optimizer optimizer : optimizerList) {
			MMLTokenizer tokenizer = new MMLTokenizer(mml);
			while (tokenizer.hasNext()) {
				String token = tokenizer.next();
				optimizer.nextToken(token);
			}
			mml = optimizer.getMinString();
		}

		return mml;
	}

	public interface Optimizer {
		public void nextToken(String token);
		public String getMinString();
	}

	public static void main(String args[]) {
		MMLStringOptimizer.setDebug(true);
		String mml = "c8c2c1c8c2c1c8c2c1c8c2c1";
		System.out.println( new MMLStringOptimizer(mml).toString() );
	}
}

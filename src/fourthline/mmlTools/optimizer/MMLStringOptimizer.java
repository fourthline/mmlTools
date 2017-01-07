/*
 * Copyright (C) 2015-2017 たんらる
 */

package fourthline.mmlTools.optimizer;

import fourthline.mmlTools.core.MMLTokenizer;

/**
 * MMLEventListで出力したMMLに対して最適化を行う.
 */
public final class MMLStringOptimizer {

	private static boolean debug = false;

	/** 最適化処理をスキップするオプション */
	private static boolean optSkip = false;
	public static void setOptSkip(boolean optSkip) {
		MMLStringOptimizer.optSkip = optSkip;
	}

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
		if (MMLStringOptimizer.optSkip) {
			return mml;
		}
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

	interface Optimizer {
		public void nextToken(String token);
		public String getMinString();
	}

	public static void main(String args[]) {
		MMLStringOptimizer.setDebug(true);
		String mml = "c8c2c1c8c2c1c8c2c1c8c2c1";
		System.out.println( new MMLStringOptimizer(mml).toString() );
	}
}

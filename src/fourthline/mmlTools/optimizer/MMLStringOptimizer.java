/*
 * Copyright (C) 2015-2022 たんらる
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
		return optimize();
	}

	public String optimizeGen2() {
		Optimizer optimizerList[] = {
				new OxLxFixedOptimizer(),
				new NxBpCmOptimizer()
		};
		return optimize(optimizerList);
	}

	private String optimize() {
		Optimizer optimizerList[] = {
				new OxLxOptimizer(),
				new BpCmOptimizer(),
				new NxOptimizer()
		};
		return optimize(optimizerList);
	}

	private String optimize(Optimizer optimizerList[]) {
		String mml = originalMML;
		if (MMLStringOptimizer.optSkip) {
			return mml;
		}

		for (Optimizer optimizer : optimizerList) {
			new MMLTokenizer(mml).forEachRemaining(t -> optimizer.nextToken(t));
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
//		String mml = "c8c2c1c8c2c1c8c2c1c8c2c1";
		String mml = "c1<a+>rc<a+>c1";
//		System.out.println( new MMLStringOptimizer(mml).toString() );
		System.out.println(new MMLStringOptimizer(mml).optimizeGen2());
	}
}

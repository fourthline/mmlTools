/*
 * Copyright (C) 2015-2022 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.core.MMLTokenizer;

/**
 * MMLEventListで出力したMMLに対して最適化を行う.
 */
public final class MMLStringOptimizer {

	/**
	 * デバッグオプション
	 */
	private static boolean debug = false;

	/**
	 * Gen2最適化を使うオプション
	 */
	private static boolean enablePreciseOptimize = true;

	public static void setDebug(boolean b) {
		debug = b;
	}

	public static boolean getDebug() {
		return debug;
	}

	public static void setEnablePreciseOptimize(boolean enable) {
		enablePreciseOptimize = enable;
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

	/**
	 * 精密なMML最適化を行う
	 * 設定によってGen2/Normalを切り替える, Gen2の場合は出力結果を再Parseして検査する.
	 */
	public String preciseOptimize() {
		if (enablePreciseOptimize) {
			String mml1 = optimizeGen2();
			return (new MMLEventList(mml1).equals(new MMLEventList(originalMML))) ? mml1 : optimize();
		} else {
			return optimize();
		}
	}

	/**
	 * MML最適化 Gen2
	 */
	public String optimizeGen2() {
		Optimizer optimizerList[] = {
				new OxLxFixedOptimizer(),
				new NxBpCmOptimizer()
		};
		return optimize(optimizerList);
	}

	/**
	 * MML最適化 Normal
	 */
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
		// String mml = "c8c2c1c8c2c1c8c2c1c8c2c1";
		String mml = "c1<a+>rc<a+>c1";
		// System.out.println( new MMLStringOptimizer(mml).toString() );
		System.out.println(new MMLStringOptimizer(mml).optimizeGen2());
	}
}

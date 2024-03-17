/*
 * Copyright (C) 2015-2024 たんらる
 */

package jp.fourthline.mmlTools.optimizer;

import java.util.Map;

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

	public static final int GEN1 = 1;
	public static final int GEN2 = 2;
	public static final int GEN3 = 3;
	/**
	 * Gen2最適化を使うオプション
	 */
	private static int optLevel = 1;

	/**
	 * キャッシュ
	 */
	private final Map<String, String> mmlCache = new CacheMap<>(4);

	public static void setDebug(boolean b) {
		debug = b;
	}

	public static boolean getDebug() {
		return debug;
	}

	public static void setOptimizeLevel(int level) {
		optLevel = level;
	}

	private String originalMML;

	private boolean disableNopt = false;

	/**
	 * @param mml   MMLEventListで出力したMML文字列.
	 */
	public MMLStringOptimizer() {}

	public MMLStringOptimizer set(String mml) {
		this.originalMML = mml;
		return this;
	}

	@Override
	public String toString() {
		return optimize(false);
	}

	/**
	 * 精密なMML最適化を行う
	 * 設定によってGen2/Normalを切り替える, Gen2の場合は出力結果を再Parseして検査する.
	 */
	public String preciseOptimize() {
		String key = optLevel + ":" + disableNopt + ":" + originalMML;
		String str = mmlCache.get(key);
		if (str != null) {
			return str;
		}

		if (optLevel == GEN2) {
			String mml1 = optimizeGen2();
			str = (new MMLEventList(mml1).equals(new MMLEventList(originalMML))) ? mml1 : optimize(disableNopt);
		} else if (optLevel == GEN3) {
			String mml1 = optimizeGen3();
			str = (new MMLEventList(mml1).equals(new MMLEventList(originalMML))) ? mml1 : optimize(disableNopt);
		} else {
			str = optimize(disableNopt);
		}

		mmlCache.put(key, str);
		return str;
	}

	/**
	 * MML最適化 Gen2
	 */
	public String optimizeGen2() {
		Optimizer[] optimizerList = {
				new OxLxFixedOptimizer(disableNopt),
				new NxBpCmOptimizer(disableNopt)
		};
		return optimize(optimizerList);
	}

	/**
	 * MML最適化 Gen3
	 */
	public String optimizeGen3() {
		Optimizer[] optimizerList = {
				new OxLxFixedAltOptimizer(disableNopt),
				new NxBpCmOptimizer(disableNopt),
				new OxLxFixedAltOptimizer.PatternOptimizer()
		};
		return optimize(optimizerList);
	}

	/**
	 * テキストエディタ用
	 */
	public String optimizeForTextEditor() {
		Optimizer[] optimizerList = {
				new OxLxFixedOptimizer(true)
		};
		return optimize(optimizerList);
	}

	/**
	 * MML最適化 Normal
	 */
	private String optimize(boolean opt) {
		return optimize(!opt ? new Optimizer[] {
				new OxLxOptimizer(),
				new BpCmOptimizer(),
				new NxOptimizer()
		} : new Optimizer[] {
				new OxLxOptimizer(),
				new BpCmOptimizer()
		});
	}

	private String optimize(Optimizer[] optimizerList) {
		String mml = originalMML;
		for (Optimizer optimizer : optimizerList) {
			new MMLTokenizer(mml).forEachRemaining(optimizer::nextToken);
			mml = optimizer.getMinString();
		}

		return mml;
	}

	public MMLStringOptimizer setDisableNopt(boolean disableNopt) {
		this.disableNopt = disableNopt;
		return this;
	}

	public interface Optimizer {
		void nextToken(String token);
		String getMinString();
	}

	Map<String, String> getCache() {
		return mmlCache;
	}

	public void setCache(MMLStringOptimizer o) {
		mmlCache.putAll(o.mmlCache);
	}

	public static void main(String[] args) {
		MMLStringOptimizer.setDebug(true);
		// String mml = "c8c2c1c8c2c1c8c2c1c8c2c1";
		String mml = "c1<a+>rc<a+>c1";
		// System.out.println( new MMLStringOptimizer(mml).toString() );
		System.out.println(new MMLStringOptimizer().set(mml).optimizeGen2());
	}
}

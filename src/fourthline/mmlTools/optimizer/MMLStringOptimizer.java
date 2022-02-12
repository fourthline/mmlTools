/*
 * Copyright (C) 2015-2022 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.List;

import fourthline.mmlTools.core.MMLTokenizer;
import fourthline.mmlTools.core.NanoTime;

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
		return optimize(optimizerList, gen2Counter);
	}

	public String optimizeOct() {
		Optimizer optimizerList[] = {
				new NxBpCmOptimizer()
		};
		return optimize(optimizerList, octCounter);
	}

	private String optimize() {
		Optimizer optimizerList[] = {
				new OxLxOptimizer(),
				new BpCmOptimizer(),
				new NxOptimizer()
		};
		return optimize(optimizerList, normalCounter);
	}

	private String optimize(Optimizer optimizerList[], PerCounter counter) {
		String mml = originalMML;
		if (MMLStringOptimizer.optSkip) {
			return mml;
		}

		counter.start();
		for (Optimizer optimizer : optimizerList) {
			new MMLTokenizer(mml).forEachRemaining(t -> optimizer.nextToken(t));
			mml = optimizer.getMinString();
		}
		counter.end(mml.length());

		return mml;
	}

	public interface Optimizer {
		public void nextToken(String token);
		public String getMinString();
	}

	private static class PerCounter {
		final String name;
		long count;
		long totalTime;
		long totalLength;
		NanoTime time;
		private PerCounter(String name) {
			this.name = name;
		}
		private void reset() {
			count = 0;
			totalTime = 0;
			totalLength = 0;
		}
		private void printCounter() {
			if (count > 0) {
				System.out.println(name + ": count = " + count + ",  totalTime = "+totalTime+",  rate = " + totalTime/count +"[us]" + "  speed = " + totalLength/(totalTime/1000) + "[/ms]");
			}
		}
		private void start() {
			time = NanoTime.start();
		}
		private void end(int length) {
			count++;
			totalTime += time.us();
			totalLength += length;
		}
	}
	private static PerCounter normalCounter = new PerCounter("Normal");
	private static PerCounter gen2Counter = new PerCounter("Gen2");
	private static PerCounter octCounter = new PerCounter("Oct");
	public static void counterReset() {
		List.of(normalCounter, gen2Counter, octCounter).forEach(t -> t.reset());
	}
	public static void printCounter() {
		List.of(normalCounter, gen2Counter, octCounter).forEach(t -> t.printCounter());
		System.out.println("count : "+OxLxFixedOptimizer.OptimizerMap2.count);
	}

	public static void main(String args[]) {
		MMLStringOptimizer.setDebug(true);
//		String mml = "c8c2c1c8c2c1c8c2c1c8c2c1";
		String mml = "c1<a+>rc<a+>c1";
//		System.out.println( new MMLStringOptimizer(mml).toString() );
		System.out.println(new MMLStringOptimizer(mml).optimizeGen2());
	}
}

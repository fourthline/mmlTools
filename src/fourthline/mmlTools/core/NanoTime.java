/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mmlTools.core;

public class NanoTime {

	private final long startTime;

	public static NanoTime start() {
		return new NanoTime();
	}

	private NanoTime() {
		startTime = System.nanoTime();
	}

	public long ms() {
		return ns()/1000000;
	}

	public long us() {
		return ns()/1000;
	}

	public long ns() {
		long now = System.nanoTime();
		return (now-startTime);
	}
}

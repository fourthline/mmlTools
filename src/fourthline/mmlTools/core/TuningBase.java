/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.core;

import fourthline.mmlTools.UndefinedTickException;

public enum TuningBase {
	L64("64"),
	L32("32"),
	L16("16");

	private final String base;
	private int tick;
	private TuningBase(String base) {
		this.base = base;
		try {
			this.tick = MMLTicks.getTick(base);
		} catch (UndefinedTickException e) {
			this.tick = 0;
		}
	}

	public String getBase() {
		return base;
	}

	public int getTick() {
		return tick;
	}

	public static TuningBase getInstance(int tick) {
		for (TuningBase obj : TuningBase.values()) {
			if (obj.tick == tick) {
				return obj;
			}
		}
		return null;
	}

	public static TuningBase getInstance(String base) {
		for (TuningBase obj : TuningBase.values()) {
			if (obj.base == base) {
				return obj;
			}
		}
		return null;
	}
}

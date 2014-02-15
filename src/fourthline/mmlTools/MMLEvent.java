/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import java.io.Serializable;


/**
 * MMLEvent
 * @author fourthline
 *
 */
public abstract class MMLEvent implements Serializable {
	private static final long serialVersionUID = -6142467143073639266L;

	// イベントの開始オフセット
	private int tickOffset;

	protected MMLEvent(int tickOffset) {
		this.tickOffset = tickOffset;
	}

	public void setTickOffset(int tickOffset) {
		this.tickOffset = tickOffset;
	}

	public int getTickOffset() {
		return this.tickOffset;
	}

	public abstract String toString();

	public abstract String toMMLString();
}

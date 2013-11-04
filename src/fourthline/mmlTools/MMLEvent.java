/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;


/**
 * MMLEvent
 * @author fourthline
 *
 */
public abstract class MMLEvent {
	
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

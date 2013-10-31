/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

public class MMLVelocityEvent extends MMLEvent {
	

	private int velocity;
	
	public MMLVelocityEvent(int velocity, int tickOffset) {
		super(tickOffset);
		
		this.velocity = velocity;
	}
	
	public int getVelocity() {
		return this.velocity;
	}

	@Override
	public String toString() {
		return "[Velocity] " + this.velocity;
	}

	@Override
	public String toMMLString() {
		return "v" + velocity;
	}

}

/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

public class MMLVelocityEvent extends MMLEvent {

	private int velocity;
	
	public MMLVelocityEvent(int velocity) {
		super(MMLEvent.VELOCITY);
		
		this.velocity = velocity;
	}

	public int getVelocity() {
		return this.velocity;
	}

	@Override
	public String toString() {
		return "[Velocity] " + velocity;
	}
}

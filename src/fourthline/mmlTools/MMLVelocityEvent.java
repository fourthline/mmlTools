/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

public class MMLVelocityEvent extends MMLEvent {
	

	private int velocity;
	
	public MMLVelocityEvent(int volumn, int tickOffset) {
		super(tickOffset);
		
		this.velocity = volumn;
	}
	
	public int getVelocity() {
		return this.velocity;
	}

	@Override
	public String toString() {
		return "[Velocity] " + this.velocity;
	}

}

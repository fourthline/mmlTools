/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

public class MMLNoteEvent extends MMLEvent {

	private int note;
	private int tick;

	public MMLNoteEvent(int note, int tickLength, int tickOffset) {
		super(tickOffset);

		this.note = note;
		this.tick = tickLength;
	}

	public int getNote() {
		return this.note;
	}
	
	public void setNote(int note) {
		this.note = note;
	}

	public int getTick() {
		return this.tick;
	}
	
	public int getEndTick() {
		return getTickOffset() + getTick();
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	@Override
	public String toString() {
		return "[Note] note: " + note + ", tick: " + tick + ", offset: " + getTickOffset();
	}
}

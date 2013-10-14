/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

public class MMLNoteEvent extends MMLEvent {

	private int note;
	private int tick;

	public MMLNoteEvent(int note, int tick) {
		super(MMLEvent.NOTE);

		this.note = note;
		this.tick = tick;
	}

	public int getNote() {
		return this.note;
	}

	public int getTick() {
		return this.tick;
	}

	public void addTick(int tick) {
		this.tick += tick;
	}

	@Override
	public String toString() {
		return "[Note] note: " + note + ", tick: " + tick;
	}
}

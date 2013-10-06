/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

public class MMLNoteEvent extends MMLEvent {
	
	private int note;
	private int tick;
	private boolean tie; /* 次の音とつなげる. シーケンスを作成するときに、Note off しない */
	
	public MMLNoteEvent(int note, int tick) {
		super(MMLEvent.NOTE);
		
		this.note = note;
		this.tick = tick;
		this.tie = false;
	}

	public int getNote() {
		return this.note;
	}
	
	public int getTick() {
		return this.tick;
	}
	
	public void setTie(boolean tie) {
		this.tie = tie;
	}
	
	public boolean getTie() {
		return this.tie;
	}

	@Override
	public String toString() {
		return "[Note] note: " + note + ", tick: " + tick + " tie: " + tie;
	}
}

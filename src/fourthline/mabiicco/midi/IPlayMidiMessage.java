/*
 * Copyright (C) 2016 たんらる
 */

package fourthline.mabiicco.midi;

public interface IPlayMidiMessage {
	public void playNote(int note, int velocity);
	public void offNote(int note);
	public void sustain(boolean isOn);
}

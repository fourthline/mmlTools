/*
 * Copyright (C) 2021 たんらる
 */

package fourthline.mabiicco.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class ExtendMessage extends ShortMessage {
	private int channel;

	public ExtendMessage(int command, int channel, int data1, int data2) throws InvalidMidiDataException {
		super(command, channel & 0xf, data1, data2);
		this.channel = channel;
	}

	@Override
	public int getChannel() {
		return this.channel;
	}
}

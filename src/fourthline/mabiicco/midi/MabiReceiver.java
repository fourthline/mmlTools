/*
 * Copyright (C) 2016 たんらる
 */

package fourthline.mabiicco.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public class MabiReceiver implements MidiDeviceReceiver {

	private static final int NOTE_OFFSET = 12;

	private IPlayMidiMessage player;

	public MabiReceiver(IPlayMidiMessage player) {
		this.player = player;
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {

//		printMessage(message);

		int noteNumber = 0;
		switch (message.getStatus() & 0xF0) {
		case ShortMessage.NOTE_ON:
			noteNumber = getNoteNumber(message);
			player.playNote(noteNumber, 15);
//			System.out.println("NOTE_ON " + noteNumber);
			break;
		case ShortMessage.NOTE_OFF:
			noteNumber = getNoteNumber(message);
			// MidiTest.offNote(noteNumber);
			player.offNote(noteNumber);
//			System.out.println("NOTE_OFF " + noteNumber);
			break;
		case ShortMessage.CONTROL_CHANGE:
			switch (getControl(message)) {
			case 0x40:
				if (0x40 <= getVerosity(message)) {
					// サステイン ON
					player.sustain(true);
//					System.out.println("CC SASTAIN ON");
				} else {
					// サステイン OFF
					player.sustain(false);
//					System.out.println("CC SASTAIN OFF");
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	private int getNoteNumber(MidiMessage message) {
		int noteNumber = 0;
		byte datas[] = message.getMessage();
		if (datas.length < 1) {
			// message length 異常
			return 0;
		}
		if (NOTE_OFFSET < datas[1]) {
			noteNumber = (datas[1] - NOTE_OFFSET);
		}
		return noteNumber;
	}

	private int getControl(MidiMessage message) {
		int control = 0;
		byte datas[] = message.getMessage();
		if (datas.length < 1) {
			// message length 異常
			return 0;
		}
		control = (datas[1]);
		return control;
	}

	private int getVerosity(MidiMessage message) {
		int v = 0;
		byte datas[] = message.getMessage();
		if (datas.length < 2) {
			// message length 異常
			return 0;
		}
		v = (datas[2]);
		return v;
	}

	private void printMessage(MidiMessage message) {
		for (byte data : message.getMessage()) {
			System.out.print(Integer.toHexString(data & 0xFF) + " ");
		}
	System.out.println("");
	}

	@Override
	public void close() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public MidiDevice getMidiDevice() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}

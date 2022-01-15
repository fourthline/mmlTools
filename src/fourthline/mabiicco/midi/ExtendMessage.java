/*
 * Copyright (C) 2021 たんらる
 */

package fourthline.mabiicco.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Receiver;


/**
 * MIDIチャネルを拡張したメッセージ.
 * っていうか, SoftShortMessage っていうのがあるのだが。
 */
public final class ExtendMessage extends ShortMessage {
	static public boolean debug = false;

	private final int channel;

	public ExtendMessage(int command, int channel, int data1, int data2) throws InvalidMidiDataException {
		super(command, channel & 0xf, data1, data2);
		this.channel = channel;
	}

	@Override
	public int getChannel() {
		return this.channel;
	}

	/**
	 * ExtendMessage以外を使わないようにするレシーバのラッパ.
	 */
	public static final class ExtendReceiver implements Receiver {
		private final Receiver target;

		public ExtendReceiver(Receiver target) {
			this.target = target;
		}

		private String messageString(MidiMessage message, long timeStamp) {
			StringBuilder sb = new StringBuilder();
			int cmd = message.getStatus() & 0xf0;
			int ch = message.getStatus() & 0x0f;
			sb.append(message.getClass().getName()).append(' ');
			switch (cmd) {
			case ShortMessage.NOTE_OFF: sb.append("NOTE_OFF"); break;
			case ShortMessage.NOTE_ON:  sb.append("NOTE_ON"); break;
			case ShortMessage.PROGRAM_CHANGE: sb.append("PROGRAM_CHANGE"); break;
			default: sb.append("unkown");
			}
			sb.append(' ');
			sb.append(ch).append(' ');
			if (message instanceof ExtendMessage e) {
				sb.append("ch=").append(e.getChannel()).append(' ');
			}
			for (var b : message.getMessage()) {
				sb.append(b).append(' ');
			}
			return sb.toString();
		}

		@Override
		public void send(MidiMessage message, long timeStamp) {
			if ((message instanceof ExtendMessage e)) {
				int cmd = e.getCommand();
				if ( (cmd == ShortMessage.NOTE_ON) || (cmd == ShortMessage.NOTE_OFF) ) {
					target.send(message, timeStamp);
					if (debug) System.out.println(messageString(message, timeStamp));
				}
			} else if (message instanceof ShortMessage e) {
				int cmd = e.getCommand();
				int ch = e.getChannel();
				int d = e.getData1();
				if ( (cmd == ShortMessage.CONTROL_CHANGE) && (ch == 0) && (d == 123) ) { // ch=0, all notes off
					// シーケンサーの停止が遅い場合があるため、ch0へのallnoteOffで全chノートOFFする.
					MabiDLS.getInstance().allNoteOff();
				}
			}
		}

		@Override
		public void close() {
			target.close();
		}
	}
}

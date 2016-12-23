/*
 * Copyright (C) 2015-2016 たんらる
 */

package fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import org.junit.Test;

import fourthline.UseLoadingDLS;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.core.UndefinedTickException;

/**
 * 
 */
public class MabiDLSTest extends UseLoadingDLS {

	private MabiDLS dls = MabiDLS.getInstance();

	private void checkMute(int trackIndex, int midiChannel) {
		System.out.println("checkMute "+trackIndex+" @ "+midiChannel);
		assertEquals(false, dls.getSynthesizer().getChannels()[midiChannel].getMute());
		dls.toggleMute(trackIndex);
		assertEquals(true, dls.getSynthesizer().getChannels()[midiChannel].getMute());
		dls.toggleMute(trackIndex);
		assertEquals(false, dls.getSynthesizer().getChannels()[midiChannel].getMute());
	}

	private void checkPanpot(int trackIndex, int midiChannel) {
		System.out.println("checkPanpot "+trackIndex+" @ "+midiChannel);
		assertEquals(64, dls.getSynthesizer().getChannels()[midiChannel].getController(10));
		dls.setChannelPanpot(trackIndex, 0);
		assertEquals(0, dls.getSynthesizer().getChannels()[midiChannel].getController(10));
		dls.setChannelPanpot(trackIndex, 64);
		assertEquals(64, dls.getSynthesizer().getChannels()[midiChannel].getController(10));
	}

	@Test
	public final void test() {
		for (int i = 0; i < 16; i++) {
			checkMute(i, i);
			checkPanpot(i, i);
		}
	}

	@Test
	public void test_loop() {
		assertFalse( dls.isLoop() );
		dls.setLoop( true );
		assertTrue( dls.isLoop() );
	}

	@Test
	public void test_createSequence() throws InvalidMidiDataException, UndefinedTickException {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@aat180aa,brb,crc,drd;").setProgram(5));
		Sequence seq = dls.createSequence(score);
		assertEquals(3, seq.getTracks().length);
		assertEquals(MMLTicks.getTick("1"), seq.getTickLength());

		byte a[][] = new byte[][] { 
				new byte[]{(byte)ShortMessage.PROGRAM_CHANGE, 0x5},
				new byte[]{(byte)ShortMessage.NOTE_ON, 69, 64}, // a
				new byte[]{(byte)ShortMessage.NOTE_ON, 71, 64}, // b
				new byte[]{(byte)ShortMessage.NOTE_ON, 60, 64}, // c
				new byte[]{(byte)ShortMessage.NOTE_OFF, 69, 0}, // a
				new byte[]{(byte)ShortMessage.NOTE_OFF, 71, 0}, // b
				new byte[]{(byte)ShortMessage.NOTE_OFF, 60, 0}, // c

				new byte[]{(byte)ShortMessage.NOTE_ON, 69, 64}, // a
				new byte[]{(byte)ShortMessage.NOTE_OFF, 69, 0}, // a

				new byte[]{(byte)MetaMessage.META, 81, 3, 5, 22, 21}, // t180
				new byte[]{(byte)ShortMessage.NOTE_ON, 69, 64}, // a
				new byte[]{(byte)ShortMessage.NOTE_ON, 71, 64}, // b
				new byte[]{(byte)ShortMessage.NOTE_ON, 60, 64}, // c
				new byte[]{(byte)ShortMessage.NOTE_OFF, 69, 0}, // a
				new byte[]{(byte)ShortMessage.NOTE_OFF, 71, 0}, // b
				new byte[]{(byte)ShortMessage.NOTE_OFF, 60, 0}, // c

				new byte[]{(byte)ShortMessage.NOTE_ON, 69, 64}, // a
				new byte[]{(byte)ShortMessage.NOTE_OFF, 69, 0}, // a
		};
		for (int i = 0; i < a.length; i++) {
			assertArrayEquals(""+i, a[i], seq.getTracks()[0].get(i).getMessage().getMessage());
		}
	}
}

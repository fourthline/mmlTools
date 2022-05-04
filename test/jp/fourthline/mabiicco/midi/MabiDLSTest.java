/*
 * Copyright (C) 2015-2022 たんらる
 */

package jp.fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.UndefinedTickException;

/**
 * 
 */
public class MabiDLSTest extends UseLoadingDLS {

	private MabiDLS dls = MabiDLS.getInstance();

	private void checkMute(int trackIndex, int midiChannel) {
		System.out.println("checkMute "+trackIndex+" @ "+midiChannel);
		assertEquals(false, dls.getMute(midiChannel));
		dls.toggleMute(trackIndex);
		assertEquals(true, dls.getMute(midiChannel));
		dls.toggleMute(trackIndex);
		assertEquals(false, dls.getMute(midiChannel));
	}

	private void checkPanpot(int trackIndex, int midiChannel) {
		System.out.println("checkPanpot "+trackIndex+" @ "+midiChannel);
		assertEquals(64, dls.getSynthesizer().getChannels()[midiChannel].getController(10));
		assertEquals(64, dls.getSynthesizer().getChannels()[midiChannel+24].getController(10));
		dls.setChannelPanpot(trackIndex, 0);
		assertEquals(0, dls.getSynthesizer().getChannels()[midiChannel].getController(10));
		assertEquals(0, dls.getSynthesizer().getChannels()[midiChannel+24].getController(10));
		dls.setChannelPanpot(trackIndex, 64);
		assertEquals(64, dls.getSynthesizer().getChannels()[midiChannel].getController(10));
		assertEquals(64, dls.getSynthesizer().getChannels()[midiChannel+24].getController(10));
	}

	@Test
	public final void test() {
		for (int i = 0; i < 24; i++) {
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
		score.getTrack(0).setSongProgram(100);
		Sequence seq = dls.createSequence(score, 1, true);
		assertEquals(3, seq.getTracks().length);
		assertEquals(MMLTicks.getTick("1"), seq.getTickLength());

		byte t0[][] = new byte[][] {
			new byte[]{(byte)MetaMessage.META, 81, 3, 5, 22, 21}, // t180
		};
		byte t1[][] = new byte[][] {
			new byte[]{(byte)ShortMessage.PROGRAM_CHANGE, 0x5},
			new byte[]{(byte)ShortMessage.NOTE_ON, 69, 39}, // a
			new byte[]{(byte)ShortMessage.NOTE_ON, 71, 39}, // b
			new byte[]{(byte)ShortMessage.NOTE_ON, 60, 39}, // c
			new byte[]{(byte)ShortMessage.NOTE_OFF, 69, 0}, // a
			new byte[]{(byte)ShortMessage.NOTE_OFF, 71, 0}, // b
			new byte[]{(byte)ShortMessage.NOTE_OFF, 60, 0}, // c

			new byte[]{(byte)ShortMessage.NOTE_ON, 69, 39}, // a
			new byte[]{(byte)ShortMessage.NOTE_OFF, 69, 0}, // a

			new byte[]{(byte)ShortMessage.NOTE_ON, 69, 39}, // a
			new byte[]{(byte)ShortMessage.NOTE_ON, 71, 39}, // b
			new byte[]{(byte)ShortMessage.NOTE_ON, 60, 39}, // c
			new byte[]{(byte)ShortMessage.NOTE_OFF, 69, 0}, // a
			new byte[]{(byte)ShortMessage.NOTE_OFF, 71, 0}, // b
			new byte[]{(byte)ShortMessage.NOTE_OFF, 60, 0}, // c

			new byte[]{(byte)ShortMessage.NOTE_ON, 69, 39}, // a
			new byte[]{(byte)ShortMessage.NOTE_OFF, 69, 0}, // a
		};
		for (int i = 0; i < t0.length; i++) {
			assertArrayEquals(""+i, t0[i], seq.getTracks()[0].get(i).getMessage().getMessage());
		}
		for (int i = 0; i < t1.length; i++) {
			assertArrayEquals(""+i, t1[i], seq.getTracks()[1].get(i).getMessage().getMessage());
		}
		assertEquals(192, seq.getTracks()[0].get(0).getTick());
		assertEquals(0, seq.getTracks()[1].get(0).getTick());
		assertEquals(1, seq.getTracks()[1].get(1).getTick());
	}

	@Test
	public void test_createSequence2() throws InvalidMidiDataException, UndefinedTickException {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@aart180a;"));
		Sequence seq = dls.createSequence(score, 1, true);
		assertEquals(2, seq.getTracks().length);
		assertEquals(MMLTicks.getTick("1"), seq.getTickLength());

		score.getTrack(0).getMMLEventAtIndex(0).getMMLNoteEventList().remove(2);
		seq = dls.createSequence(score, 1);
		assertEquals(2, seq.getTracks().length);
		assertEquals(MMLTicks.getTick("2"), seq.getTickLength());
	}

	@Test
	public void test_createSequence_attackDelayCorrect() throws InvalidMidiDataException, UndefinedTickException {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@aa,,,dd;"));
		score.getTrack(0).setSongProgram(121);
		Sequence seq = dls.createSequence(score, 1, true);

		// 遅延補正なし
		assertEquals(3, seq.getTracks().length);
		assertEquals(192, seq.getTracks()[1].ticks());
		assertEquals(1, seq.getTracks()[1].get(1).getTick());
		assertEquals(96, seq.getTracks()[1].get(2).getTick());
		assertEquals(97, seq.getTracks()[1].get(3).getTick());
		assertEquals(192, seq.getTracks()[1].get(4).getTick());
		assertEquals(192, seq.getTracks()[2].ticks());
		assertEquals(1, seq.getTracks()[2].get(1).getTick());
		assertEquals(96, seq.getTracks()[2].get(2).getTick());
		assertEquals(97, seq.getTracks()[2].get(3).getTick());
		assertEquals(192, seq.getTracks()[2].get(4).getTick());

		// 遅延補正あり
		score.getTrack(0).setAttackDelayCorrect(-6);
		score.getTrack(0).setAttackSongDelayCorrect(-12);
		seq = dls.createSequence(score, 1, true);

		assertEquals(3, seq.getTracks().length);
		assertEquals(192-6, seq.getTracks()[1].ticks());
		assertEquals(1, seq.getTracks()[1].get(1).getTick());
		assertEquals(96-6, seq.getTracks()[1].get(2).getTick());
		assertEquals(97-6, seq.getTracks()[1].get(3).getTick());
		assertEquals(192-6, seq.getTracks()[1].get(4).getTick());
		assertEquals(192-12, seq.getTracks()[2].ticks());
		assertEquals(1, seq.getTracks()[2].get(1).getTick());
		assertEquals(96-12, seq.getTracks()[2].get(2).getTick());
		assertEquals(97-12, seq.getTracks()[2].get(3).getTick());
		assertEquals(192-12, seq.getTracks()[2].get(4).getTick());
	}

	@Test
	public void testInstOptions() {
		InstClass inst = dls.getInstByProgram(0);
		for (int i = -12; i < 16; i++) {
			System.out.println(i + ": " + inst.getAttention(i) + " " + inst.isOverlap(i));
			assertEquals(false, inst.isOverlap(i));
		}
		for (int i = 16; i < 48; i++) {
			System.out.println(i + ": " + inst.getAttention(i) + " " + inst.isOverlap(i));
			assertEquals(true, inst.isOverlap(i));
		}
		for (int i = 48; i < 256-12; i++) {
			System.out.println(i + ": " + inst.getAttention(i) + " " + inst.isOverlap(i));
			assertEquals(false, inst.isOverlap(i));
		}
	}

	@Test
	public void testInstOptionsAll() {
		try {
			StringBuilder sb = new StringBuilder();
			for (InstClass inst : dls.getAllInst()) {
				sb.append(inst.toStringOptionsInfo()).append('\n');
			}

			InputStream inputStream = fileSelect("instOptions.txt");
			int size = inputStream.available();
			byte expectBuf[] = new byte[size];
			inputStream.read(expectBuf);
			inputStream.close();

			assertEquals(new String(expectBuf).replaceAll("\r", ""), sb.toString());
		} catch (IOException e) {}
	}

	@Test
	public void testInstInfos() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (InstClass inst : dls.getAllInst()) {
				inst.dlsInfoWriteToOutputStream(out);
			}

			InputStream inputStream = fileSelect("instInfos.txt");
			int size = inputStream.available();
			byte expectBuf[] = new byte[size];
			inputStream.read(expectBuf);
			inputStream.close();

			assertEquals(new String(expectBuf).replaceAll("\r", ""), out.toString().replaceAll("\r", ""));
		} catch (IOException e) {}
	}
}

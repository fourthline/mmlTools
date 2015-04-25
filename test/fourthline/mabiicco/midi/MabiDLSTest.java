/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import org.junit.Test;

import fourthline.UseLoadingDLS;

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
		int track[]   = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
		int channel[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12 };
		for (int i = 0; i < track.length; i++) {
			checkMute(track[i], channel[i]);
			checkPanpot(track[i], channel[i]);
		}
	}
}

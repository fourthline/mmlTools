/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.UndefinedTickException;

public final class MMLMidiTrackTest extends UseLoadingDLS {

	private final MabiDLS dls = MabiDLS.getInstance();

	@Test
	public void test_overlap() throws InvalidMidiDataException, UndefinedTickException {
		var track = new MMLTrack().setMML("MML@c1c2,v12rc,v13r1c;");
		var midiTrack = new MMLMidiTrack(dls.getInstByProgram(21));

		for (var eventList : track.getMMLEventList()) {
			midiTrack.add(eventList.getMMLNoteEventList());
		}

		ArrayList<MMLNoteEvent> list = new ArrayList<>();
		list.add(new MMLNoteEvent(48, 384, 0));
		list.add(new MMLNoteEvent(48, 96, 96, 12));
		list.add(new MMLNoteEvent(48, 192, 384, 8));
		list.add(new MMLNoteEvent(48, 96, 384, 13));

		assertEquals(list, midiTrack.getNoteEventList());
	}

	@Test
	public void test_noOverlap() throws InvalidMidiDataException, UndefinedTickException {
		var track = new MMLTrack().setMML("MML@c1c2,v12rc,v13r1c;");
		var midiTrack = new MMLMidiTrack(dls.getInstByProgram(0));


		for (var eventList : track.getMMLEventList()) {
			midiTrack.add(eventList.getMMLNoteEventList());
		}

		ArrayList<MMLNoteEvent> list = new ArrayList<>();
		list.add(new MMLNoteEvent(48, 384, 0));
		list.add(new MMLNoteEvent(48, 96, 96, 12));
		list.add(new MMLNoteEvent(48, 96, 384, 8));

		assertEquals(list, midiTrack.getNoteEventList());
	}
}

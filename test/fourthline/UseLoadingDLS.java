/*
 * Copyright (C) 2015-2021 たんらる
 */

package fourthline;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.LineUnavailableException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLTrack;

abstract public class UseLoadingDLS extends FileSelect {
	@BeforeClass
	public static void initializeDefaultDLS() {
		InstClass.debug = true;
		try {
			MabiDLS midi = MabiDLS.getInstance();
			if (midi.getAvailableInstByInstType(InstType.MAIN_INST_LIST).length == 0) {
				midi.initializeMIDI();
				midi.loadingDLSFile(new File(MabiDLS.DEFALUT_DLS_PATH));
			}
		} catch (IOException | MidiUnavailableException | InvalidMidiDataException | LineUnavailableException e) {
			throw new AssertionError();
		}
	}

	@AfterClass
	public static void cleanupDLS() {
		InstClass.debug = false;
	}

	protected String createStringN(char c, int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	protected MMLTrack createMMLTrack(int melody, int chord1, int chord2, int song, boolean excludeSong) {
		MMLTrack track = new MMLTrack();
		track.setMML(createStringN('a', melody), createStringN('b', chord1), createStringN('c', chord2), createStringN('d', song));
		track.setSongProgram(excludeSong?-2:-1);
		return track;
	}

}

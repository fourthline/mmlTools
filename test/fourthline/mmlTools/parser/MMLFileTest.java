/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;

import org.junit.BeforeClass;
import org.junit.Test;

import fourthline.FileSelect;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLScoreTest;
import fourthline.mmlTools.UndefinedTickException;

public class MMLFileTest extends FileSelect {

	@BeforeClass
	public static void setup() {
		MabiDLS midi = MabiDLS.getInstance();
		try {
			midi.loadingDLSFile(new File(MabiDLS.DEFALUT_DLS_PATH));
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public final void testParse() {
		try {
			MMLScore score = new MMLFile().parse(fileSelect("sample2.mml"));
			assertEquals(2, score.getTrackCount());

			// Track1 & Track2
			assertEquals("MML@cde,rrrfga,;", score.getTrack(0).getOriginalMML());
			assertEquals("Track1", score.getTrack(0).getTrackName());
			assertEquals(0, score.getTrack(0).getProgram());

			// Track3
			assertEquals("MML@aba,,;", score.getTrack(1).getOriginalMML());
			assertEquals("Track3", score.getTrack(1).getTrackName());
			assertEquals(1, score.getTrack(1).getProgram());

			InputStream inputStream = fileSelect("sample2.mmi");
			MMLScoreTest.checkMMLScoreWriteToOutputStream(score.generateAll(), inputStream);
		} catch (MMLParseException | UndefinedTickException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public final void testParse_v1() {
		try {
			MMLScore score = new MMLFile().parse(fileSelect("sample3.mml"));
			assertEquals(5, score.getTrackCount());

			// Track1
			assertEquals("MML@,,,aaa;", score.getTrack(0).getOriginalMML());
			assertEquals("Track1", score.getTrack(0).getTrackName());
			assertEquals(120, score.getTrack(0).getProgram());

			// Track2
			assertEquals("MML@,,,bbb;", score.getTrack(1).getOriginalMML());
			assertEquals("Track2", score.getTrack(1).getTrackName());
			assertEquals(121, score.getTrack(1).getProgram());

			// Track3
			assertEquals("MML@,,,ccc;", score.getTrack(2).getOriginalMML());
			assertEquals("Track3", score.getTrack(2).getTrackName());
			assertEquals(121, score.getTrack(2).getProgram());

			// Track4
			assertEquals("MML@ggg,,;", score.getTrack(3).getOriginalMML());
			assertEquals("Track4", score.getTrack(3).getTrackName());
			assertEquals(66, score.getTrack(3).getProgram());

			// Track5
			assertEquals("MML@fff,,;", score.getTrack(4).getOriginalMML());
			assertEquals("Track5", score.getTrack(4).getTrackName());
			assertEquals(66, score.getTrack(4).getProgram());

			InputStream inputStream = fileSelect("sample3.mmi");
			MMLScoreTest.checkMMLScoreWriteToOutputStream(score, inputStream);
		} catch (MMLParseException | IOException e) {
			fail(e.getMessage());
		}
	}
}

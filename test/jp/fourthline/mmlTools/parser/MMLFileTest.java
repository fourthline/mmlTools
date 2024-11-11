/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mmlTools.parser;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import jp.fourthline.FileSelect;
import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLScoreTest;

public class MMLFileTest extends FileSelect {

	@BeforeClass
	public static void setup() {
		UseLoadingDLS.initializeDefaultDLS();
	}

	@Test
	public final void testParse() throws Exception {
		MMLScore score = new MMLFile().parse(fileSelect("sample2.mml"));
		assertEquals(2, score.getTrackCount());

		// Track1 & Track2
		assertEquals("MML@cde,r2.fga,;", score.getTrack(0).getOriginalMML());
		assertEquals("Track1", score.getTrack(0).getTrackName());
		assertEquals(0, score.getTrack(0).getProgram());

		// Track3
		assertEquals("MML@aba,,;", score.getTrack(1).getOriginalMML());
		assertEquals("Track3", score.getTrack(1).getTrackName());
		assertEquals(1, score.getTrack(1).getProgram());

		InputStream inputStream = fileSelect("sample2.mmi");
		MMLScoreTest.checkMMLScoreWriteToOutputStream(score.generateAll(), inputStream);
	}

	@Test
	public final void testParse_v1() throws Exception {
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
	}

	@Test
	public final void testParse_960() throws Exception {
		MMLScore score = new MMLFile().parse(fileSelect("sample_960.mml"));
		assertEquals(3, score.getTrackCount());

		// Track1
		assertEquals("MML@aaa,,;", score.getTrack(0).getOriginalMML());
		assertEquals("Track1", score.getTrack(0).getTrackName());
		assertEquals(0, score.getTrack(0).getProgram());

		// Track2
		assertEquals("MML@bbb,,;", score.getTrack(1).getOriginalMML());
		assertEquals("Track2", score.getTrack(1).getTrackName());
		assertEquals(0, score.getTrack(1).getProgram());

		// Track3
		assertEquals("MML@ccc,,;", score.getTrack(2).getOriginalMML());
		assertEquals("Track3", score.getTrack(2).getTrackName());
		assertEquals(0, score.getTrack(2).getProgram());

		InputStream inputStream = fileSelect("sample_960.mmi");
		MMLScoreTest.checkMMLScoreWriteToOutputStream(score, inputStream);
	}

	@Test
	public void testEx1() {
		String text = "// test\nabc";
		assertEquals("abc", MMLFile.toMMLText(text));
	}

	@Test
	public void testEx2() {
		String text = "// test\r\nabc";
		assertEquals("abc", MMLFile.toMMLText(text));
	}

	@Test
	public void testEx3() {
		String text = "/* test */abc";
		assertEquals("abc", MMLFile.toMMLText(text));
	}
}

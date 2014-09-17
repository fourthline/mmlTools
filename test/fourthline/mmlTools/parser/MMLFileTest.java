/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import fourthline.FileSelect;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLScoreTest;

public class MMLFileTest extends FileSelect {

	@Test
	public final void testParse() {
		try {
			MMLScore score = new MMLFile().parse(fileSelect("sample2.mml"));
			assertEquals(1, score.getTrackCount());
			assertEquals("MML@cde,rrrfga,;", score.getTrack(0).getMML());
			assertEquals("Track1", score.getTrack(0).getTrackName());

			InputStream inputStream = fileSelect("sample2.mmi");
			MMLScoreTest.checkMMLScoreWriteToOutputStream(score, inputStream);
		} catch (MMLParseException | IOException e) {
			fail(e.getMessage());
		}
	}
}

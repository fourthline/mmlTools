/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mmlTools.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import jp.fourthline.FileSelect;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLScoreTest;

public class MMSFileTest extends FileSelect {

	@Test
	public final void testParse() {
		try {
			MMLScore score = new MMSFile().parse(fileSelect("sample1.mms"));
			assertEquals(1, score.getTrackCount());
			assertEquals("test", score.getTitle());
			assertEquals("noname", score.getAuthor());
			assertEquals("3/4", score.getBaseTime());

			InputStream inputStream = fileSelect("sample1.mmi");
			MMLScoreTest.checkMMLScoreWriteToOutputStream(score, inputStream);
		} catch (IOException | MMLParseException e) {
			fail(e.getMessage());
		}
	}
}

/*
 * Copyright (C) 2017 たんらる
 */

package jp.fourthline.mmlTools.parser;

import static org.junit.Assert.*;

import java.io.InputStream;
import org.junit.Test;

import jp.fourthline.FileSelect;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLScoreTest;

public class MidiFileTest extends FileSelect {

	@Test
	public final void testParse() throws Exception {
		MMLScore score = new MidiFile().parse(fileSelect("sample4.mid"));

		assertEquals(4, score.getTrackCount());

		InputStream inputStream = fileSelect("sample4.mmi");
		MMLScoreTest.checkMMLScoreWriteToOutputStream(score.generateAll(), inputStream);
	}

}

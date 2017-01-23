/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mmlTools.parser;

import static org.junit.Assert.*;

import java.io.InputStream;
import org.junit.Test;

import fourthline.FileSelect;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLScoreTest;

public class MidiFileTest extends FileSelect {

	@Test
	public final void testParse() throws Exception {
		MMLScore score = new MidiFile().parse(fileSelect("sample4.mid"));

		assertEquals(4, score.getTrackCount());

		InputStream inputStream = fileSelect("sample4.mmi");
		MMLScoreTest.checkMMLScoreWriteToOutputStream(score.generateAll(), inputStream);
	}

}

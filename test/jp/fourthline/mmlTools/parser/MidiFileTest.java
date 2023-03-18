/*
 * Copyright (C) 2017-2023 たんらる
 */

package jp.fourthline.mmlTools.parser;

import static org.junit.Assert.*;

import java.io.InputStream;
import org.junit.Test;

import jp.fourthline.FileSelect;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLScoreTest;

public final class MidiFileTest extends FileSelect {

	@Test
	public void testParse1() throws Exception {
		MidiFile.enableInstPatch();
		IMMLFileParser parser = new MidiFile();
		parser.setParseAttribute(MidiFile.PARSE_ALIGN, MidiFile.PARSE_ALIGN_6);
		MMLScore score = parser.parse(fileSelect("sample4.mid"));

		assertEquals(4, score.getTrackCount());

		InputStream inputStream = fileSelect("sample4.mmi");
		MMLScoreTest.checkMMLScoreWriteToOutputStream(score.generateAll(), inputStream);
	}

	@Test
	public void testParse2() throws Exception {
		MidiFile.enableInstPatch();
		IMMLFileParser parser = new MidiFile();
		parser.setParseAttribute(MidiFile.PARSE_ALIGN, MidiFile.PARSE_ALIGN_1);
		MMLScore score = parser.parse(fileSelect("sample4.mid"));

		assertEquals(4, score.getTrackCount());

		InputStream inputStream = fileSelect("sample4_1.mmi");
		MMLScoreTest.checkMMLScoreWriteToOutputStream(score.generateAll(), inputStream);
	}

	@Test
	public void testParseFormat0() throws Exception {
		MidiFile.enableInstPatch();
		IMMLFileParser parser = new MidiFile();
		parser.setParseAttribute(MidiFile.PARSE_ALIGN, MidiFile.PARSE_ALIGN_1);
		MMLScore score = parser.parse(fileSelect("sample_format0.mid"));

		assertEquals(2, score.getTrackCount());

		InputStream inputStream = fileSelect("sample_format0.mmi");
		MMLScoreTest.checkMMLScoreWriteToOutputStream(score.generateAll(), inputStream);
	}
}

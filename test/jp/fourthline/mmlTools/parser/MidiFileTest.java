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

	@Test
	public void testParseFormat0_trackSelect1() throws Exception {
		MidiFile.enableInstPatch();
		IMMLFileParser parser = IMMLFileParser.getParser(fileSelectF("sample_format0.mid"));
		var map = parser.getTrackSelectMap();
		assertEquals("{0=<Track1>, 1=<Track2>}", map.toString());

		map.get(0).setEnable(false);
		var sc = parser.parse(fileSelect("sample_format0.mid"));
		assertEquals(1, sc.getTrackCount());
		assertEquals("Track2", sc.getTrack(0).getTrackName());
		assertEquals("MML@t120r8.v12>frr16fr8.d+d,,;", sc.getTrack(0).getMabiMML());
	}

	@Test
	public void testParseFormat0_trackSelect2() throws Exception {
		MidiFile.enableInstPatch();
		IMMLFileParser parser = IMMLFileParser.getParser(fileSelectF("sample_format0.mid"));
		var map = parser.getTrackSelectMap();

		map.get(1).setEnable(false);
		var sc = parser.parse(fileSelect("sample_format0.mid"));
		assertEquals(1, sc.getTrackCount());
		assertEquals("Track1", sc.getTrack(0).getTrackName());
		assertEquals("MML@t120r16v12>cr8c+d,,;", sc.getTrack(0).getMabiMML());
	}

	@Test
	public void testParseFormat1_trackSelect1() throws Exception {
		MidiFile.enableInstPatch();
		IMMLFileParser parser = IMMLFileParser.getParser(fileSelectF("sample4.mid"));
		var map = parser.getTrackSelectMap();
		assertEquals("{1=<trackA>, 2=<Track3>, 4=<Track5>, 7=<Piano>}", map.toString());

		map.get(2).setEnable(false);
		map.get(4).setEnable(false);
		var sc = parser.parse(fileSelect("sample4.mid"));
		assertEquals(2, sc.getTrackCount());
		assertEquals("trackA", sc.getTrack(0).getTrackName());
		assertEquals("Piano", sc.getTrack(1).getTrackName());
		assertEquals("MML@t60r8t120rv12d+d+d+r24l12<eg+,r3r8v12l6<ag+g+a,r2r.v12l24<c+c+d+l12rc;", sc.getTrack(0).getOriginalMML());
		assertEquals("MML@t60r8t120r1v12<<b,r1r8v12<<g,r1r8v12n27;", sc.getTrack(1).getOriginalMML());
	}

	@Test
	public void testParseFormat1_trackSelect2() throws Exception {
		MidiFile.enableInstPatch();
		IMMLFileParser parser = IMMLFileParser.getParser(fileSelectF("sample4.mid"));
		var map = parser.getTrackSelectMap();

		map.get(1).setEnable(false);
		map.get(7).setEnable(false);
		var sc = parser.parse(fileSelect("sample4.mid"));
		assertEquals(2, sc.getTrackCount());
		assertEquals("Track3", sc.getTrack(0).getTrackName());
		assertEquals("Track5", sc.getTrack(1).getTrackName());
		assertEquals("MML@t60r8t120r1v12n30,r1r8v12<<c,;", sc.getTrack(0).getOriginalMML());
		assertEquals("MML@t60r8t120rv12c64,,;", sc.getTrack(1).getOriginalMML());
	}

	@Test
	public void testOverTrack_format0() throws Exception {
		String filename = "overTrack_format0.mid";
		IMMLFileParser parser = IMMLFileParser.getParser(fileSelectF(filename));
		try {
			parser.parse(fileSelect(filename));
		} catch (MMLParseException e) {
			assertEquals("track over: Track14", e.getMessage());
			return;
		}
		assertFalse(true);
	}

	@Test
	public void testOverTrack_format1() throws Exception {
		String filename = "overTrack_format1.mid";
		IMMLFileParser parser = IMMLFileParser.getParser(fileSelectF(filename));
		try {
			parser.parse(fileSelect(filename));
		} catch (MMLParseException e) {
			assertEquals("track over: Track16", e.getMessage());
			return;
		}
		assertFalse(true);
	}
}

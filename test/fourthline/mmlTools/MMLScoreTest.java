/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

public class MMLScoreTest {

	private void checkMMLFileOutput(MMLScore score, String expectFileName) {
		try {
			File file = new File("resources/"+expectFileName);
			System.out.println("Read: "+file.getAbsolutePath());
			FileInputStream inputStream = new FileInputStream(file);
			int size = inputStream.available();
			byte expectBuf[] = new byte[size];
			inputStream.read(expectBuf);
			inputStream.close();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			score.writeToOutputStream(outputStream);

			assertEquals(new String(expectBuf), outputStream.toString("UTF-8"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testMMLFileFormat0() {
		MMLScore score = new MMLScore();
		MMLTrack track = new MMLTrack("MML@aaa,bbb,ccc;");
		track.setTrackName("track1");
		score.addTrack(track);

		checkMMLFileOutput(score, "format0.mmi");
	}

	@Test
	public void testMMLFileFormat1() {
		MMLScore score = new MMLScore();
		MMLTrack track1 = new MMLTrack("MML@at150aa,bbb,ccc;");
		track1.setTrackName("track1");
		score.addTrack(track1);
		MMLTrack track2 = new MMLTrack("MML@aaa,bbt120b,ccc;");
		track2.setTrackName("track2");
		track2.setProgram(4);
		track2.setSongProgram(120);
		score.addTrack(track2);

		checkMMLFileOutput(score, "format1.mmi");
	}

	@Test
	public void testMMLFileFormat_r0() {
		MMLScore score = new MMLScore();
		MMLTrack track = new MMLTrack("MML@r1t180c8;");
		track.setTrackName("track1");
		score.addTrack(track);

		checkMMLFileOutput(score, "format_r0.mmi");
	}
}

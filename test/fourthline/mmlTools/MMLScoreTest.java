/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;

import fourthline.FileSelect;

public class MMLScoreTest extends FileSelect {

	private void checkMMLFileOutput(MMLScore score, String expectFileName, String expectMML[]) {
		try {
			/* MMLScore.writeToOutputStream() */
			InputStream inputStream = fileSelect(expectFileName);
			int size = inputStream.available();
			byte expectBuf[] = new byte[size];
			inputStream.read(expectBuf);
			inputStream.close();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			score.writeToOutputStream(outputStream);
			assertEquals(new String(expectBuf), outputStream.toString("UTF-8"));

			/* MMLScore.parse() */
			inputStream = fileSelect(expectFileName);
			MMLScore inputScore = new MMLScore().parse(inputStream);
			inputStream.close();
			int i = 0;
			assertEquals(expectMML.length, inputScore.getTrackCount());
			for (MMLTrack track : inputScore.getTrackList()) {
				assertEquals(expectMML[i++], track.getMMLString());
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testMMLFileFormat0() {
		MMLScore score = new MMLScore();
		MMLTrack track = new MMLTrack("MML@aaa,bbb,ccc,dd1;");
		track.setTrackName("track1");
		score.addTrack(track);

		String mml[] = { "MML@aaa,bbb,ccc,dd1;" };

		checkMMLFileOutput(score, "format0.mmi", mml);
	}

	@Test
	public void testMMLFileFormat1() {
		MMLScore score = new MMLScore();
		MMLTrack track1 = new MMLTrack("MML@at150aa1,bbb,ccc,dd1;");
		track1.setTrackName("track1");
		score.addTrack(track1);
		MMLTrack track2 = new MMLTrack("MML@aaa2,bbt120b,ccc,dd2;");
		track2.setTrackName("track2");
		track2.setProgram(4);
		track2.setSongProgram(120);
		score.addTrack(track2);

		String mml[] = { 
				"MML@at150at120a1,bbb,ccc,dt150dt120&d2.;",
				"MML@at150at120a2,bbb,ccc,dt150dt120&d;"
		};

		checkMMLFileOutput(score, "format1.mmi", mml);
	}

	@Test
	public void testMMLFileFormat_r0() {
		MMLScore score = new MMLScore();
		MMLTrack track = new MMLTrack("MML@r1t180c8;");
		track.setTrackName("track1");
		score.addTrack(track);

		String mml[] = { "MML@v0c1t180v8c8,,;" };

		checkMMLFileOutput(score, "format_r0.mmi", mml);
	}

	@Test
	public void testMMLFileFormat_r1() {
		MMLScore score = new MMLScore();
		MMLTrack track1 = new MMLTrack("MML@r1>f+1t120&f+1;");
		track1.setTrackName("track1");
		score.addTrack(track1);

		MMLTrack track2 = new MMLTrack("MML@r1r1a+1;");
		track2.setTrackName("track2");
		score.addTrack(track2);

		MMLTrack track3 = new MMLTrack("MML@d1;");
		track3.setTrackName("track3");
		score.addTrack(track3);

		String mml[] = {
				"MML@l1r>f+t120&f+,,;",
				"MML@v0l1cct120v8a+,,;",
				"MML@d1v0c1t120,,;"
		};

		checkMMLFileOutput(score, "format_r1.mmi", mml);
	}
}

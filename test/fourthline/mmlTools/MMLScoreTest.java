/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;






import fourthline.FileSelect;
import fourthline.UseLoadingDLS;
import fourthline.mmlTools.core.MMLText;
import fourthline.mmlTools.parser.IMMLFileParser;
import fourthline.mmlTools.parser.MMLParseException;

public class MMLScoreTest extends FileSelect {

	@BeforeClass
	public static void setup() {
		UseLoadingDLS.initializeDefaultDLS();
	}

	/**
	 * testLocalMMLParseでみているローカルのファイルに対して上書きします.
	 * 最適化向上した際の更新用です.
	 * 更新するばあい trueに設定.
	 */
	private static boolean overwriteToLocalMMLOption = false;

	public static void checkMMLScoreWriteToOutputStream(MMLScore score, InputStream inputStream) {
		try {
			int size = inputStream.available();
			byte expectBuf[] = new byte[size];
			inputStream.read(expectBuf);
			inputStream.close();

			// MMLScore -> mmi check
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			score.writeToOutputStream(outputStream);
			String mmiOutput = outputStream.toString("UTF-8");
			assertEquals(new String(expectBuf), mmiOutput);

			// mmi -> re-parse check
			ByteArrayInputStream bis = new ByteArrayInputStream(mmiOutput.getBytes());
			MMLScore reparseScore = new MMLScore().parse(bis);
			assertEquals(mmiOutput, new String(reparseScore.getObjectState()));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private void checkMMLFileOutput(MMLScore score, String expectFileName, String expectMML[]) {
		try {
			/* MMLScore.writeToOutputStream() */
			InputStream inputStream = fileSelect(expectFileName);
			checkMMLScoreWriteToOutputStream(score, inputStream);

			/* MMLScore.parse() */
			inputStream = fileSelect(expectFileName);
			MMLScore inputScore = new MMLScore().parse(inputStream).generateAll();
			inputStream.close();
			int i = 0;
			assertEquals(expectMML.length, inputScore.getTrackCount());
			for (MMLTrack track : inputScore.getTrackList()) {
				assertEquals(expectMML[i++], track.getMabiMML());
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testMMLFileFormat0() throws UndefinedTickException {
		MMLScore score = new MMLScore();
		MMLTrack track = new MMLTrack().setMML("MML@aaa,bbb,ccc,dd1;");
		track.setTrackName("track1");
		score.addTrack(track);
		score.getMarkerList().add(new Marker("marker1", 96));

		String mml[] = { "MML@aaa,bbb,ccc,dd1;" };

		checkMMLFileOutput(score.generateAll(), "format0.mmi", mml);
	}

	@Test
	public void testMMLFileFormat1() throws UndefinedTickException {
		MMLScore score = new MMLScore();
		MMLTrack track1 = new MMLTrack().setMML("MML@at150aa1,bbb,ccc,dd1;");
		track1.setTrackName("track1");
		score.addTrack(track1);
		MMLTrack track2 = new MMLTrack().setMML("MML@aaa2,bbt120b,ccc,dd2;");
		track2.setTrackName("track2");
		track2.setProgram(4);
		track2.setSongProgram(120);
		score.addTrack(track2);

		String mml[] = { 
				"MML@at150at120a1,bbb,ccc,dt150dt120v0d2.v8;",
				"MML@at150at120a2,bbb,ccc,dt150dt120v0dv8;"
		};

		checkMMLFileOutput(score.generateAll(), "format1.mmi", mml);
	}

	@Test
	public void testMMLFileFormat_r0() throws UndefinedTickException {
		MMLScore score = new MMLScore();
		MMLTrack track = new MMLTrack().setMML("MML@r1t180c8;");
		track.setTrackName("track1");
		score.addTrack(track);

		String mml[] = { "MML@v0c1t180v8c8,,;" };

		checkMMLFileOutput(score.generateAll(), "format_r0.mmi", mml);
	}

	@Test
	public void testMMLFileFormat_r1() throws UndefinedTickException {
		MMLScore score = new MMLScore();
		MMLTrack track1 = new MMLTrack().setMML("MML@r1>f+1t120&f+1;");
		track1.setTrackName("track1");
		score.addTrack(track1);

		MMLTrack track2 = new MMLTrack().setMML("MML@r1r1a+1;");
		track2.setTrackName("track2");
		score.addTrack(track2);

		MMLTrack track3 = new MMLTrack().setMML("MML@d1;");
		track3.setTrackName("track3");
		score.addTrack(track3);

		String mml[] = {
				"MML@l1r>f+t120v0f+v8,,;",
				"MML@v0l1cct120v8a+,,;",
				"MML@d1,,;" // 後方にあるテンポは出力しない.
		};

		checkMMLFileOutput(score.generateAll(), "format_r1.mmi", mml);
	}

	/**
	 * ファイルをparseして, 出力文字が増加していないか確認する.
	 * @param filename
	 */
	private void mmlFileParse(String filename, boolean updateOption) {
		File file = new File(filename);
		if (file.exists()) {
			IMMLFileParser fileParser = IMMLFileParser.getParser(file);
			try {
				MMLScore score = fileParser.parse(new FileInputStream(file));
				System.out.println(filename);
				score.getTrackList().forEach(t -> {
					try {
						String mml1 = t.getOriginalMML();
						String rank1 = t.mmlRankFormat();
						System.out.println("mml1: "+mml1);
						t.generate();
						String mml2 = t.getOriginalMML();
						System.out.println("mml2: "+mml2);
						String rank2 = new MMLText().setMMLText(mml2).mmlRankFormat();
						String rank3 = t.mmlRankFormat();
						if (!mml1.equals(mml2)) {
							System.out.print("#");
						}
						System.out.println(rank1 + " -> " + rank2 + ", " + rank3);
						assertTrue(mml1.length() >= mml2.length());
						assertEquals(new MMLTrack().setMML(mml1), new MMLTrack().setMML(mml2));
					} catch (UndefinedTickException e) {
						fail(e.getMessage());
					}
				});

				try {
					long t1 = System.currentTimeMillis();
					score.generateAll();
					long t2 = System.currentTimeMillis();
					System.out.println("MMLScore generateAll: "+(t2-t1)+"ms");
				} catch (UndefinedTickException e) {
					fail(e.getMessage());
				}

				if (updateOption) {
					try {
						score.generateAll();
						score.writeToOutputStream(new FileOutputStream(file));
					} catch (UndefinedTickException e) {
						fail(e.getMessage());
					}
				}
			} catch (MMLParseException | FileNotFoundException e) {}
		}
	}

	/**
	 * ローカルのファイルを読み取って, MML最適化に劣化がないかどうかを確認するテスト.
	 */
	@Test
	public void testLocalMMLParse() {
		try {
			String listFile = "localMMLFileList.txt";
			InputStream stream = fileSelect(listFile);
			if (stream == null) {
				fail("not found "+listFile);
				return;
			}
			InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
			new BufferedReader(reader).lines().forEach(s -> {
				System.out.println(s);
				mmlFileParse(s, overwriteToLocalMMLOption);
			});
		} catch (IOException e) {}
	}

	/**
	 * 指定Tickにあるノートをすべて取得する.
	 */
	@Test
	public void test_getNoteListOnTickOffset() {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@c,,rd"));
		score.addTrack(new MMLTrack().setMML("MML@rf,e"));
		score.addTrack(new MMLTrack().setMML("MML@,ra,g"));

		MMLNoteEvent expect1[] = {
				new MMLNoteEvent(48, 96, 0),
				new MMLNoteEvent(52, 96, 0),
				new MMLNoteEvent(55, 96, 0)
		};
		MMLNoteEvent expect2[] = {
				new MMLNoteEvent(50, 96, 96),
				new MMLNoteEvent(53, 96, 96),
				new MMLNoteEvent(57, 96, 96)
		};

		assertArrayEquals(expect1,
				score.getNoteListOnTickOffset(0).stream()
				.flatMap(t -> Stream.of(t))
				.filter(t -> (t != null)).toArray());
		assertArrayEquals(expect2,
				score.getNoteListOnTickOffset(96).stream()
				.flatMap(t -> Stream.of(t))
				.filter(t -> (t != null)).toArray());
		assertEquals(0,
				score.getNoteListOnTickOffset(96+96).stream()
				.flatMap(t -> Stream.of(t))
				.filter(t -> (t != null)).count());
	}

	@Test
	public void test_transpose1() throws UndefinedTickException {
		MMLScore score = new MMLScore();
		String mml    = "MML@cdcdccdd,,;";
		String expect = "MML@c+d+c+d+c+c+d+d+,,;"; // 移調された場合.

		// 普通の楽器.
		score.addTrack(new MMLTrack().setMML(mml).setProgram(0));
		// 大太鼓.
		score.addTrack(new MMLTrack().setMML(mml).setProgram(66));
		// シロフォン.
		score.addTrack(new MMLTrack().setMML(mml).setProgram(77));

		// 移調.
		score.transpose(1);
		score.generateAll();

		assertEquals(expect, score.getTrack(0).getMabiMML());
		assertEquals(mml   , score.getTrack(1).getMabiMML()); // 大太鼓のみ移調不可.
		assertEquals(expect, score.getTrack(2).getMabiMML());
	}

	private void checkTranspose(String mml, String expect, int transpose) throws UndefinedTickException {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack().setMML(mml));

		score.transpose(transpose);
		score.generateAll();

		assertEquals(expect, score.getTrack(0).getMabiMML());
	}

	@Test
	public void test_transpose2() throws UndefinedTickException {
		String mml    = "MML@o0c+,,;";
		String expect = "MML@n0,,;";
		checkTranspose(mml, expect, -1);
	}

	@Test(expected=UndefinedTickException.class)
	public void test_transpose3() throws UndefinedTickException {
		String mml    = "MML@o0c+,,;";
		String expect = "MML@n-1,,;";
		checkTranspose(mml, expect, -2);
	}

	@Test
	public void test_transpose4() throws UndefinedTickException {
		String mml    = "MML@o8b-,,;";
		String expect = "MML@o8b,,;";
		checkTranspose(mml, expect, +1);
	}

	@Test(expected=UndefinedTickException.class)
	public void test_transpose5() throws UndefinedTickException {
		String mml    = "MML@n0o8b-,,;";
		String expect = "MML@n0o9c,,;";
		checkTranspose(mml, expect, +2);
	}
}

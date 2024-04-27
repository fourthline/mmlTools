/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.mml.MMLManagerStub;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.MMLVerifyException;

public final class MMLTransposeTest {

	private IMMLManager mmlManager = new MMLManagerStub();
	private MMLScore score;

	@Before
	public void setup() {
		score = mmlManager.getMMLScore();
	}

	@Test
	public void test_transpose1() throws MMLExceptionList, MMLVerifyException {
		String mml    = "MML@cdcdccdd,,;";
		String expect = "MML@c+d+c+d+c+c+d+d+,,;"; // 移調された場合.

		// 普通の楽器.
		score.addTrack(new MMLTrack().setMML(mml).setProgram(0));
		// 大太鼓.
		score.addTrack(new MMLTrack().setMML(mml).setProgram(66));
		// シロフォン.
		score.addTrack(new MMLTrack().setMML(mml).setProgram(77));

		// 移調.
		new MMLTranspose(null, mmlManager).apply(1);
		score.generateAll();

		assertEquals(expect, score.getTrack(0).getMabiMML());
		assertEquals(mml   , score.getTrack(1).getMabiMML()); // 大太鼓のみ移調不可.
		assertEquals(expect, score.getTrack(2).getMabiMML());
	}

	private void checkTranspose(String mml, String expect, int transpose) throws MMLExceptionList, MMLVerifyException {
		score.addTrack(new MMLTrack().setMML(mml));

		new MMLTranspose(null, mmlManager).apply(transpose);
		score.generateAll();

		assertEquals(expect, score.getTrack(0).getMabiMML());
	}

	@Test
	public void test_transpose2() throws MMLExceptionList, MMLVerifyException {
		String mml    = "MML@o0c,,;";
		String expect = "MML@o0c-,,;";
		checkTranspose(mml, expect, -1);
	}

	@Test(expected=MMLExceptionList.class)
	public void test_transpose3() throws MMLExceptionList, MMLVerifyException {
		String mml    = "MML@o0c,,;";
		String expect = "MML@n-2,,;";
		checkTranspose(mml, expect, -2);
	}

	@Test
	public void test_transpose4() throws MMLExceptionList, MMLVerifyException {
		String mml    = "MML@o8b-,,;";
		String expect = "MML@o8b,,;";
		checkTranspose(mml, expect, +1);
	}

	@Test(expected=MMLExceptionList.class)
	public void test_transpose5() throws MMLExceptionList, MMLVerifyException {
		String mml    = "MML@n0o8b-,,;";
		String expect = "MML@n0o9c,,;";
		checkTranspose(mml, expect, +2);
	}
}

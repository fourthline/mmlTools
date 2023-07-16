/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

public final class MMLImportPanelTest extends UseLoadingDLS {
	private IMMLManager mmlManager = new MMLManagerStub();

	@Before
	public void setup() {
		mmlManager.getMMLScore().addTrack(new MMLTrack().setMML("MML@a;"));
		mmlManager.getMMLScore().setTitle("score");
		mmlManager.getMMLScore().setAuthor("author");
		mmlManager.getMMLScore().setBaseTime("6/8");
	}

	@Test
	public void testAddImport() {
		var score = new MMLScore();
		score.setTitle("score");
		score.addTrack(new MMLTrack().setMML("MML@aaa,bbb,ccc;"));
		MMLImportPanel panel = new MMLImportPanel(null, score, mmlManager, false);
		panel.importMMLTrack();
		assertEquals(2, mmlManager.getMMLScore().getTrackCount());
		assertEquals("score", mmlManager.getMMLScore().getTitle());
		assertEquals("author", mmlManager.getMMLScore().getAuthor());
		assertEquals("6/8", mmlManager.getMMLScore().getBaseTime());
	}

	@Test
	public void testNewImport() {
		var mmlManager = new MMLManagerStub();
		mmlManager.getMMLScore().addTrack(new MMLTrack().setMML("MML@a;"));
		var score = new MMLScore();
		score.setTitle("new score");
		score.setAuthor("new author");
		score.setBaseTime("12/16");
		score.addTrack(new MMLTrack().setMML("MML@aaa,bbb,ccc;"));
		MMLImportPanel panel = new MMLImportPanel(null, score, mmlManager, true);
		panel.importMMLTrack();
		assertEquals(1, mmlManager.getMMLScore().getTrackCount());
		assertEquals("new score", mmlManager.getMMLScore().getTitle());
		assertEquals("new author", mmlManager.getMMLScore().getAuthor());
		assertEquals("12/16", mmlManager.getMMLScore().getBaseTime());
	}
}

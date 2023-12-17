/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.MMLSeqView;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

public final class MMLInputPanelTest extends UseLoadingDLS {

	@Test
	public void test_clipboard() {
		String s = "MML@aaa,bbb,ccc,ddd;";
		MMLOutputPanel.copyToClipboard(null, s, null);
		assertEquals(s, MMLInputPanel.getClipboardString());
	}

	@Test
	public void test() {
		IMMLManager mmlManager =  new MMLSeqView(null);
		MMLScore score = mmlManager.getMMLScore();
		MMLInputPanel panel = new MMLInputPanel(null, "test track", mmlManager);

		assertEquals(1, score.getTrackCount());

		// デフォルト（新規トラック）
		panel.setMMLTrack(new MMLTrack().setMML("MML@aaa,bbb,ccc;"));
		panel.applyMMLTrack();
		assertEquals(2, score.getTrackCount());
		assertEquals("MML@aaa,bbb,ccc;", score.getTrack(1).getMabiMML());

		// 現在のトラックに上書き
		panel.setOverride(true);
		panel.setMMLTrack(new MMLTrack().setMML("MML@aaa,bbb,ccc,ddd;"));
		panel.applyMMLTrack();
		assertEquals(2, score.getTrackCount());
		assertEquals("MML@aaa,bbb,ccc,ddd;", score.getTrack(1).getMabiMML());
	}

	@Test
	public void test_excludeSongPart() {
		IMMLManager mmlManager =  new MMLSeqView(null);
		MMLScore score = mmlManager.getMMLScore();
		score.addTrack(new MMLTrack());
		MMLInputPanel panel1 = new MMLInputPanel(null, "test track", mmlManager, true);
		MMLInputPanel panel2 = new MMLInputPanel(null, "test track", mmlManager, false);

		// 現在のトラックに上書き
		panel1.setOverride(true);
		panel1.setMMLTrack(new MMLTrack().setMML("MML@aaa,bbb,ccc,ddd;"));
		panel1.applyMMLTrack();
		assertEquals("MML@aaa,bbb,ccc;", score.getTrack(0).getMabiMML());
		assertEquals(true, score.getTrack(0).isExcludeSongPart());

		// 現在のトラックに上書き
		panel2.setOverride(true);
		panel2.setMMLTrack(new MMLTrack().setMML("MML@aaa,bbb,ccc,ddd;"));
		panel2.applyMMLTrack();
		assertEquals("MML@aaa,bbb,ccc,ddd;", score.getTrack(0).getMabiMML());
		assertEquals(false, score.getTrack(0).isExcludeSongPart());
	}
}

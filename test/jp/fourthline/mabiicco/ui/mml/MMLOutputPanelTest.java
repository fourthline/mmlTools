/*
 * Copyright (C) 2021 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.mml.MMLOutputPanel;
import jp.fourthline.mmlTools.MMLTrack;

public class MMLOutputPanelTest extends UseLoadingDLS {
	/** Rank表示のテスト */
	@Test
	public void testMMLOutputPanelFrameListOfMMLTrack() {
		ArrayList<MMLTrack> list = new ArrayList<>();
		list.add(createMMLTrack(1200, 400, 400, 400, false));
		list.add(createMMLTrack(1201, 400, 400, 400, false));
		list.add(createMMLTrack(1201, 400, 400, 400, true));
		list.add(createMMLTrack(1600, 400, 400, 400, true));
		list.add(createMMLTrack(1601, 400, 400, 400, true));
		list.add(createMMLTrack(0, 0, 0, 800, false));

		MMLOutputPanel panel = new MMLOutputPanel(null, list, null);
		assertEquals("1", panel.table.getRank(0).getRank());
		assertEquals("-", panel.table.getRank(1).getRank());
		assertEquals("6`", panel.table.getRank(2).getRank());
		assertEquals("1`", panel.table.getRank(3).getRank());
		assertEquals("-", panel.table.getRank(4).getRank());
		assertEquals("9", panel.table.getRank(5).getRank());
	}

	/** 楽譜集分割のテスト */
	@Test
	public void testMMLOutputPanelSplit_01() {
		ArrayList<MMLTrack> list = new ArrayList<>();
		list.add(createMMLTrack(2400, 2400, 2400, 2400, false));

		MMLTrack e1 = createMMLTrack(1200, 800, 500, 1200, false);
		MMLTrack e2 = createMMLTrack(1200, 800, 500, 1200, false);
		MMLTrack e3 = createMMLTrack(0, 800, 500, 00, false);

		MMLOutputPanel panel = new MMLOutputPanel(null, list, null);
		MMLOutputPanel panel2 = panel.createSelectedTrackMMLSplitPanel(0);
		assertEquals(e1.getMabiMML(), panel2.outputTextList.get(0));
		assertEquals(e2.getMabiMML(), panel2.outputTextList.get(1));
		assertEquals(e3.getMabiMML(), panel2.outputTextList.get(2));
	}

	/** 楽譜集分割のテスト, 歌パート除外オプション */
	@Test
	public void testMMLOutputPanelSplit_02() {
		ArrayList<MMLTrack> list = new ArrayList<>();
		list.add(createMMLTrack(2400, 2400, 2400, 2400, true));

		MMLTrack e1 = createMMLTrack(1600, 1200, 900, 0, true);
		MMLTrack e2 = createMMLTrack(800, 1200, 900, 0, true);
		MMLTrack e3 = createMMLTrack(0, 0, 600, 0, true);

		MMLOutputPanel panel = new MMLOutputPanel(null, list, null);
		MMLOutputPanel panel2 = panel.createSelectedTrackMMLSplitPanel(0);
		assertEquals(e1.getMabiMML(), panel2.outputTextList.get(0));
		assertEquals(e2.getMabiMML(), panel2.outputTextList.get(1));
		assertEquals(e3.getMabiMML(), panel2.outputTextList.get(2));
	}
}

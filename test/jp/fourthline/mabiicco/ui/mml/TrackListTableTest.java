/*
 * Copyright (C) 2023 たんらる
 */
package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

public class TrackListTableTest extends UseLoadingDLS {

	@Test
	public void testGetTableListInfo() {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@aaa,bbb,ccccc;"));
		score.addTrack(new MMLTrack().setMML("MML@,,,eeee;"));
		var o = new TrackListTable(score.getTrackList());

		String expect = """
				#	トラック名	楽器	作曲ランク
				1		0: リュート	*Rank F ( 3, 3, 5 )
				2		0: リュート	*Rank 練習 ( 0, 0, 0, 4 )
				""";
		assertEquals(expect, o.getTableListInfo());
	}

}

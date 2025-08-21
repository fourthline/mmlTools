/*
 * Copyright (C) 2023-2024 たんらる
 */
package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.table.TrackListTable;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLText;

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

	@Test
	public void test_ui1() {
		MMLScore score = new MMLScore();
		var track = new MMLTrack().setMML("MML@aaa,bbb,ccccc;");
		track.setTrackName("track1");
		score.addTrack(track);
		var o = new TrackListTable(score.getTrackList());
		assertEquals("1", o.getValueAt(0, 0).toString());
		assertEquals("track1", o.getValueAt(0, 1));
		assertEquals("0: リュート", o.getValueAt(0, 2));
		assertEquals("*Rank F ( 3, 3, 5 )", o.getValueAt(0, 3));

		assertEquals(Integer.class, o.getColumnClass(0));
		assertEquals(String.class, o.getColumnClass(1));
		assertEquals(String.class, o.getColumnClass(2));
		assertEquals(String.class, o.getColumnClass(3));

		assertEquals(false, o.isCellEditable(0, 0));
		assertEquals(false, o.isCellEditable(0, 1));
		assertEquals(false, o.isCellEditable(0, 2));
		assertEquals(false, o.isCellEditable(0, 3));
	}

	@Test
	public void test_ui2() {
		MMLScore score = new MMLScore();
		var track = new MMLTrack().setMML("MML@aaa,bbb,ccccc;");
		track.setTrackName("track1");
		score.addTrack(track);
		var o = new TrackListTable(score.getTrackList(), true);
		assertEquals(false, o.getValueAt(0, 0));
		assertEquals("1", o.getValueAt(0, 1).toString());
		assertEquals("track1", o.getValueAt(0, 2));
		assertEquals("0: リュート", o.getValueAt(0, 3));
		assertEquals("*Rank F ( 3, 3, 5 )", o.getValueAt(0, 4));

		assertEquals(Boolean.class, o.getColumnClass(0));
		assertEquals(Integer.class, o.getColumnClass(1));
		assertEquals(String.class, o.getColumnClass(2));
		assertEquals(String.class, o.getColumnClass(3));
		assertEquals(String.class, o.getColumnClass(4));

		assertEquals(true, o.isCellEditable(0, 0));
		assertEquals(false, o.isCellEditable(0, 1));
		assertEquals(false, o.isCellEditable(0, 2));
		assertEquals(false, o.isCellEditable(0, 3));
		assertEquals(false, o.isCellEditable(0, 4));
	}

	@Test
	public void test_ui3() {
		List<MMLText> list = List.of(new MMLText().setMMLText("MML@aaa;"), new MMLText().setMMLText("MML@bbb;"));
		var track = new MMLTrack().setMML("MML@aaa,bbb,ccccc;");
		track.setTrackName("track1");

		var o = new TrackListTable(track, list);
		assertEquals("1", o.getValueAt(0, 0).toString());
		assertEquals("track1", o.getValueAt(0, 1));
		assertEquals("0: リュート", o.getValueAt(0, 2));
		assertEquals("Rank 練習 ( 3, 0, 0 )", o.getValueAt(0, 3));

		assertEquals(String.class, o.getColumnClass(0));
		assertEquals(String.class, o.getColumnClass(1));
		assertEquals(String.class, o.getColumnClass(2));
		assertEquals(String.class, o.getColumnClass(3));

		assertEquals(false, o.isCellEditable(0, 0));
		assertEquals(false, o.isCellEditable(0, 1));
		assertEquals(false, o.isCellEditable(0, 2));
		assertEquals(false, o.isCellEditable(0, 3));
	}
}

/*
 * Copyright (C) 2025 たんらる
 */
package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

public class PartListTableTest extends UseLoadingDLS {

	@Test
	public void test() {
		MMLScore score = new MMLScore();
		var track1 = new MMLTrack().setMML("MML@aaa,bbb,ccccc;");
		track1.setTrackName("track1");
		score.addTrack(track1);
		var track2 = new MMLTrack().setMML("MML@a,b,c;");
		track2.setTrackName("track2");
		score.addTrack(track2);
		var o = new PartListTable(score.getTrackList(), true, 2);
		assertEquals(false, o.getValueAt(0, 0));
		assertEquals(1, o.getValueAt(0, 1));
		assertEquals("track1", o.getValueAt(0, 2));
		assertEquals("0: リュート", o.getValueAt(0, 3));
		assertEquals(AppResource.appText("melody"), o.getValueAt(0, 4));
		assertEquals(3, o.getValueAt(0, 5));

		assertEquals(false, o.getValueAt(1, 0));
		assertEquals("", o.getValueAt(1, 1));
		assertEquals("", o.getValueAt(1, 2));
		assertEquals("", o.getValueAt(1, 3));
		assertEquals(AppResource.appText("chord1"), o.getValueAt(1, 4));
		assertEquals(3, o.getValueAt(1, 5));

		assertEquals(false, o.getValueAt(3, 0));
		assertEquals(2, o.getValueAt(3, 1));
		assertEquals("track2", o.getValueAt(3, 2));
		assertEquals("0: リュート", o.getValueAt(3, 3));
		assertEquals(AppResource.appText("melody"), o.getValueAt(3, 4));
		assertEquals(1, o.getValueAt(3, 5));

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

		// check box
		var checkBoxExpected1 = new boolean[] { true, true, true, true, true, true };
		var checkBoxExpected2 = new boolean[] { true, false, false, false, false, true };
		for (int i = 0; i < checkBoxExpected1.length; i++) {
			assertEquals(checkBoxExpected1[i], o.isCellEditable(i, 0));
		}
		assertEquals(List.of(), o.getCheckedEventList());

		o.setValueAt(true, 0, 0);
		for (int i = 0; i < checkBoxExpected1.length; i++) {
			assertEquals(checkBoxExpected1[i], o.isCellEditable(i, 0));
		}
		assertEquals(List.of(track1.getMMLEventAtIndex(0)), o.getCheckedEventList());

		// チェックボックスを最大数チェックしている場合
		o.setValueAt(true, 5, 0);
		for (int i = 0; i < checkBoxExpected1.length; i++) {
			assertEquals(checkBoxExpected2[i], o.isCellEditable(i, 0));
		}
		assertEquals(List.of(track1.getMMLEventAtIndex(0), track2.getMMLEventAtIndex(2)), o.getCheckedEventList());
	}

	@Test
	public void testSong() {
		MMLScore score = new MMLScore();
		var track1 = new MMLTrack().setMML("MML@aaa,bbb,ccccc,dddd;");
		track1.setTrackName("track1");
		track1.setProgram(120);
		score.addTrack(track1);
		var track2 = new MMLTrack().setMML("MML@a,b,c,dd;");
		track2.setTrackName("track2");
		track2.setSongProgram(100);
		score.addTrack(track2);
		var o = new PartListTable(score.getTrackList(), true, 2);
		assertEquals(false, o.getValueAt(0, 0));
		assertEquals(1, o.getValueAt(0, 1));
		assertEquals("track1", o.getValueAt(0, 2));
		assertEquals("120: 男声", o.getValueAt(0, 3));
		assertEquals(AppResource.appText("song"), o.getValueAt(0, 4));
		assertEquals(4, o.getValueAt(0, 5));

		assertEquals(false, o.getValueAt(1, 0));
		assertEquals(2, o.getValueAt(1, 1));
		assertEquals("track2", o.getValueAt(1, 2));
		assertEquals("0: リュート", o.getValueAt(1, 3));
		assertEquals(AppResource.appText("melody"), o.getValueAt(1, 4));
		assertEquals(1, o.getValueAt(1, 5));

		assertEquals(false, o.getValueAt(4, 0));
		assertEquals("", o.getValueAt(4, 1));
		assertEquals("", o.getValueAt(4, 2));
		assertEquals("100: 男性コーラス", o.getValueAt(4, 3));
		assertEquals(AppResource.appText("song"), o.getValueAt(4, 4));
		assertEquals(2, o.getValueAt(4, 5));

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

		// check box
		var checkBoxExpected1 = new boolean[] { true, true, true, true, true };
		var checkBoxExpected2 = new boolean[] { true, false, false, false, true };
		for (int i = 0; i < checkBoxExpected1.length; i++) {
			assertEquals(checkBoxExpected1[i], o.isCellEditable(i, 0));
		}
		assertEquals(List.of(), o.getCheckedEventList());

		o.setValueAt(true, 0, 0);
		for (int i = 0; i < checkBoxExpected1.length; i++) {
			assertEquals(checkBoxExpected1[i], o.isCellEditable(i, 0));
		}
		assertEquals(List.of(track1.getMMLEventAtIndex(3)), o.getCheckedEventList());

		// チェックボックスを最大数チェックしている場合
		o.setValueAt(true, 4, 0);
		for (int i = 0; i < checkBoxExpected1.length; i++) {
			assertEquals(checkBoxExpected2[i], o.isCellEditable(i, 0));
		}
		assertEquals(List.of(track1.getMMLEventAtIndex(3), track2.getMMLEventAtIndex(3)), o.getCheckedEventList());
	}
}

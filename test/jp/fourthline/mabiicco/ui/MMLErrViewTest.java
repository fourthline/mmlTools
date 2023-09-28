/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Vector;

import org.junit.Test;

import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.MMLVerifyException;

public class MMLErrViewTest {

	@Test
	public void testMakeList() {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack());
		score.addTrack(new MMLTrack());

		score.getTrack(0).setTrackName("track1");
		score.getTrack(0).getMMLEventAtIndex(2).addMMLNoteEvent(new MMLNoteEvent(50, 5, 0));

		score.getTrack(1).setTrackName("track2");
		score.getTrack(1).getMMLEventAtIndex(0).addMMLNoteEvent(new MMLNoteEvent(50, 48, 4));

		score.getTrack(0).getMMLEventAtIndex(3).addMMLNoteEvent(new MMLNoteEvent(50, 48, 0));
		score.getTrack(0).getMMLEventAtIndex(3).addMMLNoteEvent(new MMLNoteEvent(50, 48, 50));

		score.getTrack(1).getMMLEventAtIndex(1).addMMLNoteEvent(new MMLNoteEvent(111, 96, 0));

		try {
			score.generateAll();
		} catch (MMLExceptionList | MMLVerifyException e) {}

		var list = new MMLErrView(score).getDataList();

		var expect = new Vector<Vector<String>>();
		expect.add(new Vector<String>(List.of("1", "track1", "和音2", "0:00:00", "Undefined tick table: 5")));
		expect.add(new Vector<String>(List.of("1", "track1", "歌", "0:00:50", "Undefined tick table: 2")));
		expect.add(new Vector<String>(List.of("2", "track2", "メロディー", "0:00:04", "Undefined tick table: 4")));
		expect.add(new Vector<String>(List.of("2", "track2", "和音1", "0:00:00", "Illegal note: 111")));

		assertEquals(expect, list);
	}
}

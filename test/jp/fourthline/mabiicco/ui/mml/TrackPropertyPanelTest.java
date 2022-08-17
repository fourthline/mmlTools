/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mmlTools.MMLTempoEvent;
import jp.fourthline.mmlTools.MMLTrack;

public final class TrackPropertyPanelTest {
	private final IMMLManager mmlManager = new MMLManagerStub();

	@Test
	public void test_delayInst() {
		MMLTrack track = new MMLTrack();
		mmlManager.getMMLScore().addTrack(track);
		var obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "0ms"), obj.getLabelText());

		// マイナス方向のDelay
		track.setAttackDelayCorrect(-5);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "-26ms=N/A", "0:00:00", "0ms"), obj.getLabelText());

		track.setAttackDelayCorrect(-48);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "-250ms=-L8", "0:00:00", "0ms"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(60, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "-500ms=-L8", "0:00:00", "0ms"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(240, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "-125ms=-L8", "0:00:00", "0ms"), obj.getLabelText());

		// プラス方向のDelay
		track.getGlobalTempoList().add(new MMLTempoEvent(120, 0));
		track.setAttackDelayCorrect(5);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "26ms=N/A", "0:00:00", "0ms"), obj.getLabelText());

		track.setAttackDelayCorrect(48);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "250ms=L8", "0:00:00", "0ms"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(60, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "250ms=L8", "0:00:00", "0ms"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(240, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "250ms=L8", "0:00:00", "0ms"), obj.getLabelText());
	}

	@Test
	public void test_delaySong() {
		MMLTrack track = new MMLTrack();
		mmlManager.getMMLScore().addTrack(track);
		var obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "0ms"), obj.getLabelText());

		// マイナス方向のDelay
		track.setAttackSongDelayCorrect(-5);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "-26ms=N/A"), obj.getLabelText());

		track.setAttackSongDelayCorrect(-48);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "-250ms=-L8"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(60, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "-500ms=-L8"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(240, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "-125ms=-L8"), obj.getLabelText());

		// プラス方向のDelay
		track.getGlobalTempoList().add(new MMLTempoEvent(120, 0));
		track.setAttackSongDelayCorrect(5);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "26ms=N/A"), obj.getLabelText());

		track.setAttackSongDelayCorrect(48);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "250ms=L8"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(60, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "250ms=L8"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(240, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:00:00", "0ms", "0:00:00", "250ms=L8"), obj.getLabelText());
	}
}

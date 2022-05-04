/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

import jp.fourthline.mabiicco.ui.AbstractMMLManager;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.mml.TrackPropertyPanel;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTempoEvent;
import jp.fourthline.mmlTools.MMLTrack;

public final class TrackPropertyPanelTest {
	private class MMLManagerStub extends AbstractMMLManager {
		@Override
		public void setMMLScore(MMLScore score) {}

		@Override
		public int getActiveTrackIndex() {
			return 0;
		}

		@Override
		public int getActiveMMLPartIndex() {
			return 0;
		}

		@Override
		public MMLEventList getActiveMMLPart() {
			return null;
		}

		@Override
		public void updateActivePart(boolean generate) {}

		@Override
		public void generateActiveTrack() {}

		@Override
		public void updateActiveTrackProgram(int trackIndex, int program, int songProgram) {}

		@Override
		public int getActivePartProgram() {
			return 0;
		}

		@Override
		public boolean selectTrackOnExistNote(int note, int tickOffset) {
			return false;
		}

		@Override
		public void setMMLselectedTrack(MMLTrack track) {}

		@Override
		public void addMMLTrack(MMLTrack track) {}

		@Override
		public void moveTrack(int toIndex) {}

		@Override
		public void updatePianoRollView() {}

		@Override
		public void updatePianoRollView(int note) {}
	}

	private IMMLManager mmlManager = new MMLManagerStub();

	@Test
	public void test_delayInst() {
		MMLTrack track = new MMLTrack();
		mmlManager.getMMLScore().addTrack(track);
		var obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "0ms"), obj.getLabelText());

		// マイナス方向のDelay
		track.setAttackDelayCorrect(-5);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "-20ms=N/A", "0:0:0", "0ms"), obj.getLabelText());

		track.setAttackDelayCorrect(-48);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "-250ms=-L8", "0:0:0", "0ms"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(60, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "-500ms=-L8", "0:0:0", "0ms"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(240, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "-125ms=-L8", "0:0:0", "0ms"), obj.getLabelText());

		// プラス方向のDelay
		track.getGlobalTempoList().add(new MMLTempoEvent(120, 0));
		track.setAttackDelayCorrect(5);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "20ms=N/A", "0:0:0", "0ms"), obj.getLabelText());

		track.setAttackDelayCorrect(48);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "250ms=L8", "0:0:0", "0ms"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(60, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "250ms=L8", "0:0:0", "0ms"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(240, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "250ms=L8", "0:0:0", "0ms"), obj.getLabelText());
	}

	@Test
	public void test_delaySong() {
		MMLTrack track = new MMLTrack();
		mmlManager.getMMLScore().addTrack(track);
		var obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "0ms"), obj.getLabelText());

		// マイナス方向のDelay
		track.setAttackSongDelayCorrect(-5);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "-20ms=N/A"), obj.getLabelText());

		track.setAttackSongDelayCorrect(-48);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "-250ms=-L8"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(60, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "-500ms=-L8"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(240, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "-125ms=-L8"), obj.getLabelText());

		// プラス方向のDelay
		track.getGlobalTempoList().add(new MMLTempoEvent(120, 0));
		track.setAttackSongDelayCorrect(5);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "20ms=N/A"), obj.getLabelText());

		track.setAttackSongDelayCorrect(48);
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "250ms=L8"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(60, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "250ms=L8"), obj.getLabelText());

		track.getGlobalTempoList().add(new MMLTempoEvent(240, 0));
		obj = new TrackPropertyPanel(track, mmlManager);
		assertEquals(List.of("0:0:0", "0ms", "0:0:0", "250ms=L8"), obj.getLabelText());
	}
}

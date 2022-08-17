/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import jp.fourthline.mabiicco.ui.AbstractMMLManager;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

class MMLManagerStub extends AbstractMMLManager {
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
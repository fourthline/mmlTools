/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.fx;

import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

/**
 * 
 */
public final class MMLContents implements IMMLManager {

	private MMLScore mmlScore = new MMLScore();

	@Override
	public MMLScore getMMLScore() {
		return mmlScore;
	}

	@Override
	public void setMMLScore(MMLScore score) {
		this.mmlScore = score;
	}

	@Override
	public int getActiveTrackIndex() {
		return 0;
	}

	@Override
	public MMLEventList getActiveMMLPart() {
		return mmlScore.getTrack(0).getMMLEventAtIndex(0);
	}

	@Override
	public void updateActivePart(boolean generate) {}

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

}

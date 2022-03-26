/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mabiicco.ui;

import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

public abstract class AbstractMMLManager implements IMMLManager {
	protected MMLScore mmlScore = new MMLScore();

	@Override
	public MMLScore getMMLScore() {
		return mmlScore;
	}

	@Override
	public MMLTrack getActiveTrack() {
		return mmlScore.getTrack(getActiveTrackIndex());
	}

	@Override
	public int getActiveMMLPartStartOffset() {
		return getActiveTrack().getStartOffset(getActiveMMLPartIndex());
	}
}

/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui;

import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

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

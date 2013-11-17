/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLScore;

/**
 *
 */
public interface IMMLManager {
	public MMLScore getMMLScore();
	public MMLEventList getActiveMMLPart();
	public void updateActivePart();
	public void updateActiveTrackProgram(int program);
	public void updateTempoRoll();
}

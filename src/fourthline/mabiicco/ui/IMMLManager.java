/*
 * Copyright (C) 2013-2014 たんらる
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
	public void updateActiveTrackProgram(int program, int songProgram);
	public void updateTempoRoll();
	public void saveState();
	public int getActivePartProgram();
}

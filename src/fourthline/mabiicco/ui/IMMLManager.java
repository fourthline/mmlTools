/*
 * Copyright (C) 2013-2022 たんらる
 */

package fourthline.mabiicco.ui;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

/**
 *
 */
public interface IMMLManager {
	public MMLScore getMMLScore();
	public void setMMLScore(MMLScore score);
	public int getActiveTrackIndex();
	public MMLTrack getActiveTrack();
	public int getActiveMMLPartIndex();
	public int getActiveMMLPartStartOffset();
	public MMLEventList getActiveMMLPart();
	public void updateActivePart(boolean generate);
	public void generateActiveTrack();
	public void updateActiveTrackProgram(int trackIndex, int program, int songProgram);
	public int getActivePartProgram();
	public boolean selectTrackOnExistNote(int note, int tickOffset);
	public void setMMLselectedTrack(MMLTrack track);
	public void addMMLTrack(MMLTrack track);
	public void moveTrack(int toIndex);
	public void updatePianoRollView();
	public void updatePianoRollView(int note);
}

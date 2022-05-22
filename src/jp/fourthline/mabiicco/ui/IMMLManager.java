/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mabiicco.ui;

import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

/**
 *
 */
public interface IMMLManager {
	MMLScore getMMLScore();
	void setMMLScore(MMLScore score);
	int getActiveTrackIndex();
	MMLTrack getActiveTrack();
	int getActiveMMLPartIndex();
	int getActiveMMLPartStartOffset();
	MMLEventList getActiveMMLPart();
	void updateActivePart(boolean generate);
	void generateActiveTrack();
	void updateActiveTrackProgram(int trackIndex, int program, int songProgram);
	int getActivePartProgram();
	boolean selectTrackOnExistNote(int note, int tickOffset);
	void setMMLselectedTrack(MMLTrack track);
	void addMMLTrack(MMLTrack track);
	void moveTrack(int toIndex);
	void updatePianoRollView();
	void updatePianoRollView(int note);
}

/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.util.List;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLTrack;

/**
 *
 */
public interface IMMLManager {
	public List<MMLTrack> getTrackList();
	public MMLEventList getActiveMMLPart();
	public void updateActivePart();
	public void updateActiveTrackProgram(int program);
}

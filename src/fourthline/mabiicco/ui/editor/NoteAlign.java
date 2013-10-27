/*
　* Copyright (C) 2013 たんらる
　*/

package fourthline.mabiicco.ui.editor;

import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTicks;



public class NoteAlign {
	
	private String viewText;
	private int alignTick;
	
	public NoteAlign(String viewText, String tickName) throws UndefinedTickException {
		this.viewText = viewText;
		alignTick = MMLTicks.getTick(tickName);
	}
	
	public String toString() {
		return viewText;
	}
	
	public int getAlign() {
		return alignTick;
	}
}

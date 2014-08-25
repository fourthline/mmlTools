/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

public interface IFileState {
	public boolean isModified();
	public boolean canUndo();
	public boolean canRedo();
	public void saveState();

	public void setOriginalBase();
	public void setFileStateObserver(IFileStateObserver observer);
}

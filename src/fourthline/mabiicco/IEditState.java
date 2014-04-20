/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

public interface IEditState {
	public boolean hasSelectedNote();
	public boolean canPaste();
	public void paste(long startTick);
	public void selectedCut();
	public void selectedCopy();
	public void selectedDelete();

	public void setEditStateObserver(IEditStateObserver observer);
}

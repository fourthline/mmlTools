/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

public interface IEditState {
	public boolean hasSelectedNote();
	public void selectedDelete();
	
	public void setEditStateObserver(IEditStateObserver observer);
}

/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

/**
 * 編集関連の動作インタフェース.
 */
public interface IEditState {
	public boolean hasSelectedNote();
	public boolean canPaste();
	public void paste(long startTick);
	public void selectedCut();
	public void selectedCopy();
	public void selectedDelete();
	public void noteProperty();
	public void selectAll();
	public void selectPreviousAll();
	public void selectAfterAll();

	public void setEditStateObserver(IEditStateObserver observer);
}

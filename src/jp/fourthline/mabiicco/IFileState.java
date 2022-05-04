/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mabiicco;

/**
 * ファイル操作関連の動作インタフェース.
 */
public interface IFileState {
	public boolean isModified();
	public boolean canUndo();
	public boolean canRedo();
	public void saveState();
	public void revertState();

	public void setOriginalBase();
	public void setFileStateObserver(IFileStateObserver observer);
}

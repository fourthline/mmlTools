/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mabiicco;

/**
 * ファイル操作関連の動作インタフェース.
 */
public interface IFileState {
	boolean isModified();
	boolean canUndo();
	boolean canRedo();
	void saveState();
	void revertState();

	void setOriginalBase();
	void setFileStateObserver(IFileStateObserver observer);
}

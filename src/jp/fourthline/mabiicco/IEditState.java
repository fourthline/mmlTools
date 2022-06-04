/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mabiicco;

/**
 * 編集関連の動作インタフェース.
 */
public interface IEditState {
	boolean hasSelectedNote();
	boolean canPaste();
	void paste(long startTick);
	void selectedCut();
	void selectedCopy();
	void selectedDelete();
	void noteProperty();
	void selectAll();
	void selectPreviousAll();
	void selectAfterAll();
	void selectAllSamePitch();
	void setTempMute(boolean mute);
	void setTempMuteAll();

	/** 連続した複数のノートが選択されているかどうかを判定する */
	boolean hasSelectedMultipleConsecutiveNotes();

	/** 音符間の休符を削除する */
	void removeRestsBetweenNotes();

	void octaveUp();
	void octaveDown();

	void setEditStateObserver(IEditStateObserver observer);
}

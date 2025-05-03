/*
 * Copyright (C) 2014-2025 たんらる
 */

package jp.fourthline.mabiicco;

import java.awt.Point;

import jp.fourthline.mabiicco.ui.editor.EditTool;

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
	void convertTuplet();

	/** 連続した複数のノートが選択されているかどうかを判定する */
	boolean hasSelectedMultipleConsecutiveNotes();

	/** 音符間の休符を削除する */
	void removeRestsBetweenNotes();

	void octaveUp();
	void octaveDown();

	void notesModifyVelocity(Point point, boolean inc);

	void setEditStateObserver(IEditStateObserver observer);

	/** 編集ツールの変更 */
	void changeEditTool(EditTool tool);
}

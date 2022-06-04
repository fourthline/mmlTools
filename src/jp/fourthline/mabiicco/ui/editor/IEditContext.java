/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Cursor;
import java.awt.Point;

/**
 * EditModeから使用するContext.
 */
interface IEditContext {
	boolean hasSelectedNote();
	boolean onExistNote(Point point);
	boolean selectTrackOnExistNote(Point point);
	boolean isEditLengthPosition(Point point);
	EditMode changeState(EditMode nextMode);
	void newMMLNoteAndSelected(Point p);
	void detachSelectedMMLNote();
	void updateSelectedNoteAndTick(Point p, boolean updateNote, boolean alignment);
	void moveSelectedMMLNote(Point start, Point p, boolean shiftOption, boolean alignment, boolean octaveAlign);
	void cancelMove();
	void applyEditNote(boolean select);
	void setCursor(Cursor cursor);
	void areaSelectingAction(Point startPoint, Point point);
	void applyAreaSelect();
	void selectNoteByPoint(Point point, int selectModifiers);
	void showPopupMenu(Point point);
	boolean canEditStartOffset(Point point);
}

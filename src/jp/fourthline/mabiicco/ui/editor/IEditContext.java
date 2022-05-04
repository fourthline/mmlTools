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
	public boolean onExistNote(Point point);
	public boolean selectTrackOnExistNote(Point point);
	public boolean isEditLengthPosition(Point point);
	public EditMode changeState(EditMode nextMode);
	public void newMMLNoteAndSelected(Point p);
	public void detachSelectedMMLNote();
	public void updateSelectedNoteAndTick(Point p, boolean updateNote, boolean alignment);
	public void moveSelectedMMLNote(Point start, Point p, boolean shiftOption, boolean alignment);
	public void cancelMove();
	public void applyEditNote(boolean select);
	public void setCursor(Cursor cursor);
	public void areaSelectingAction(Point startPoint, Point point);
	public void applyAreaSelect();
	public void selectNoteByPoint(Point point, int selectModifiers);
	public void showPopupMenu(Point point);
	public boolean canEditStartOffset(Point point);
}

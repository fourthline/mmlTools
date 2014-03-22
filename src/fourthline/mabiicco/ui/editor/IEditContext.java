/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.Cursor;
import java.awt.Point;

/**
 * EditModeから使用するContext.
 */
interface IEditContext {
	public boolean onExistNote(Point p);
	public boolean isEditLengthPosition(Point point);
	public void changeState(EditMode nextMode);
	public void newMMLNoteAndSelected(Point p);
	public void detachSelectedMMLNote();
	public void updateSelectedNoteAndTick(Point p, boolean updateNote);
	public void moveSelectedMMLNote(Point start, Point p);
	public void cancelMove();
	public void applyEditNote(boolean select);
	public void setCursor(Cursor cursor);
	public void areaSelectingAction(Point startPoint, Point point);
	public void applyAreaSelect();
	public void selectNoteByPoint(Point point, boolean multiSelect);
}

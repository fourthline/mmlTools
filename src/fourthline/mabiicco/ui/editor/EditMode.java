/*
 * Copyright (C) 2014-2017 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;

import javax.swing.SwingUtilities;

import fourthline.mabiicco.MabiIccoProperties;

/**
 * MMLEditorの編集状態と振る舞い.
 */
enum EditMode {
	SELECT {
		@Override
		public void pressEvent(IEditContext context, MouseEvent e) {
			startPoint = e.getPoint();
			if (SwingUtilities.isRightMouseButton(e)) {
				if (context.onExistNote(startPoint)) {
					context.selectNoteByPoint(startPoint, 0);
					context.showPopupMenu(startPoint);
				} else {
					context.changeState(AREA).executeEvent(context, e);
				}
			} else if (SwingUtilities.isLeftMouseButton(e)) {
				boolean partSwitch = MabiIccoProperties.getInstance().activePartSwitch.get();
				EditMode next = INSERT;
				if (context.onExistNote(startPoint)) {
					// ノート上であれば、ノートを選択状態にする. 複数選択判定も.
					context.selectNoteByPoint(startPoint, e.getModifiersEx());
					if (context.isEditLengthPosition(startPoint)) {
						next = LENGTH;
					} else {
						next = MOVE;
					}
				} else if (partSwitch && context.selectTrackOnExistNote(startPoint)) {
					// アクティブパートを変更したときには単一選択のみ.
					context.selectNoteByPoint(startPoint, 0);
					next = SELECT;
				}
				context.changeState(next).executeEvent(context, e);
			}
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			int cursorType = Cursor.DEFAULT_CURSOR;
			Point p = e.getPoint();
			boolean onOption = (e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK + InputEvent.CTRL_DOWN_MASK);
			if (context.onExistNote(p)) {
				if (context.isEditLengthPosition(p)) {
					cursorType = Cursor.E_RESIZE_CURSOR;
				} else {
					cursorType = Cursor.MOVE_CURSOR;
				}
			} else if (onOption) {
				context.selectTrackOnExistNote(p);
			}
			context.setCursor(Cursor.getPredefinedCursor(cursorType));
		}
	},
	INSERT {
		@Override
		public void pressEvent(IEditContext context, MouseEvent e) {
			// 右クリックで、編集キャンセル
			if (SwingUtilities.isRightMouseButton(e)) {
				context.selectNoteByPoint(null, 0);
				context.changeState(SELECT).executeEvent(context, e);
			}
		}
		@Override
		public void enter(IEditContext context) {
			// 新規ノート, 作成したノートを選択.
			context.newMMLNoteAndSelected(startPoint);
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			// 選択中のNote、Note長を更新.
			context.updateSelectedNoteAndTick(e.getPoint(), true);
		}
		@Override
		public void exit(IEditContext context) {
			// ノート選択を解除.
			context.applyEditNote(false);
		}
	},
	MOVE {
		@Override
		public void pressEvent(IEditContext context, MouseEvent e) {
			// 右クリックで、編集キャンセル
			if (SwingUtilities.isRightMouseButton(e)) {
				context.cancelMove();
				context.changeState(SELECT).executeEvent(context, e);
			}
		}
		@Override
		public void enter(IEditContext context) {
			// 移動前の選択ノートリストをdetachする.
			context.detachSelectedMMLNote();
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			// 選択中のNoteを移動
			boolean shiftOption = false;
			if ( (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
				shiftOption = true;
			}
			context.moveSelectedMMLNote(startPoint, e.getPoint(), shiftOption);
		}
		@Override
		public void exit(IEditContext context) {
			// 編集後のノートを登録する.（選択状態維持）
			context.applyEditNote(true);
		}
	},
	LENGTH {
		@Override
		public void enter(IEditContext context) {
			// 単音選択
			context.selectNoteByPoint(null, 0);
			context.selectNoteByPoint(startPoint, 0);
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			// 選択中のNote長を更新.（Noteは更新しない）
			context.updateSelectedNoteAndTick(e.getPoint(), false);
		}
		@Override
		public void exit(IEditContext context) {
			// ノート選択を解除.
			context.applyEditNote(false);
		}
	},
	AREA {
		@Override
		public void enter(IEditContext context) {
			// 選択解除.
			context.selectNoteByPoint(null, 0);
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			// 範囲選択.
			context.areaSelectingAction(startPoint, e.getPoint());
		}
		@Override
		public void exit(IEditContext context) {
			// 範囲選択反映.
			context.applyAreaSelect();
		}
	};

	private static Point startPoint;

	private EditMode() {}

	public void pressEvent(IEditContext context, MouseEvent e) {}
	public void executeEvent(IEditContext context, MouseEvent e) {}
	public void releaseEvent(IEditContext context, MouseEvent e) {
		switch (this) {
		case INSERT:
		case LENGTH:
		case MOVE:
			if (SwingUtilities.isLeftMouseButton(e)) {
				context.changeState(SELECT).executeEvent(context, e);
			}
			break;
		case AREA:
			if (SwingUtilities.isRightMouseButton(e)) {
				context.changeState(SELECT).executeEvent(context, e);
			}
			break;
		case SELECT:
		default:
			break;
		}
	}
	public void enter(IEditContext context) {}
	public void exit(IEditContext context) {}
}

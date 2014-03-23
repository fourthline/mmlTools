/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

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
					// TODO: show note edit menu
				} else {
					context.changeState(EditMode.AREA);
				}
			} else if (SwingUtilities.isLeftMouseButton(e)) {
				if (context.onExistNote(startPoint)) {
					// ノート上であれば、ノートを選択状態にする. 複数選択判定も.
					context.selectNoteByPoint(startPoint, e.getModifiers());
					if (context.isEditLengthPosition(startPoint)) {
						context.changeState(LENGTH);
					} else {
						context.changeState(MOVE);
					}
				} else {
					context.changeState(INSERT);
				}
			}
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			int cursorType = Cursor.DEFAULT_CURSOR;
			Point p = e.getPoint();
			if (context.onExistNote(p)) {
				if (context.isEditLengthPosition(p)) {
					cursorType = Cursor.E_RESIZE_CURSOR;
				} else {
					cursorType = Cursor.MOVE_CURSOR;
				}
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
				context.changeState(SELECT);
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
				context.changeState(SELECT);
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
			context.moveSelectedMMLNote(startPoint, e.getPoint());
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
				context.changeState(SELECT);
			}
			break;
		case AREA:
			if (SwingUtilities.isRightMouseButton(e)) {
				context.changeState(SELECT);
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

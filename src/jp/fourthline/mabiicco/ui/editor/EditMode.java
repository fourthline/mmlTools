/*
 * Copyright (C) 2014-2025 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

import javax.swing.SwingUtilities;

import jp.fourthline.mabiicco.MabiIccoProperties;

/**
 * MMLEditorの編集状態と振る舞い.
 */
enum EditMode {
	SELECT {
		@Override
		public void pressEvent(IEditContext context, MouseEvent e) {
			startPoint = e.getPoint();
			if (!context.canEditStartOffset(startPoint)) {
				return;
			}
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
					if (context.selectNoteByPoint(startPoint, e.getModifiersEx())) {
						if (context.isEditLengthPosition(startPoint)) {
							next = LENGTH;
						} else {
							next = MOVE;
						}
					} else {
						next = SELECT;
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
			boolean shiftOption = e.isShiftDown();
			boolean ctrlOption = e.isControlDown();
			int cursorType = Cursor.DEFAULT_CURSOR;
			Point p = e.getPoint();
			if (context.onExistNote(p) && !shiftOption && !ctrlOption) {
				if (context.isEditLengthPosition(p)) {
					cursorType = Cursor.E_RESIZE_CURSOR;
				} else {
					cursorType = Cursor.MOVE_CURSOR;
				}
			} else if (shiftOption && ctrlOption && !context.hasSelectedNote()) {
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
			context.updateSelectedNoteAndTick(e.getPoint(), true, !e.isControlDown());
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
				context.cancelEdit();
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
			boolean shiftOption = e.isShiftDown();
			boolean ctrlOption = e.isControlDown();
			context.moveSelectedMMLNote(startPoint, e.getPoint(), shiftOption, !ctrlOption, ctrlOption, !shiftOption && ctrlOption);
		}
		@Override
		public void exit(IEditContext context) {
			// 編集後のノートを登録する.（選択状態維持）
			context.applyEditNote(true);
		}
	},
	LENGTH {
		@Override
		public void pressEvent(IEditContext context, MouseEvent e) {
			// 右クリックで、編集キャンセル
			if (SwingUtilities.isRightMouseButton(e)) {
				context.cancelEdit();
				context.changeState(SELECT).executeEvent(context, e);
			}
		}
		@Override
		public void enter(IEditContext context) {
			// 単音選択
//			context.selectNoteByPoint(null, 0);
//			context.selectNoteByPoint(startPoint, 0);
			// 編集前の選択ノートリストをdetachする.
			context.detachSelectedMMLNote();
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			// 選択中のNote長を更新.（Noteは更新しない）
//			context.updateSelectedNoteAndTick(e.getPoint(), false, !e.isControlDown());
			context.editLengthSelectedMMLNote(startPoint, e.getPoint(), !e.isControlDown());
		}
		@Override
		public void exit(IEditContext context) {
			// ノート選択を解除.
			context.applyEditNote(false);
		}
	},
	AREA {
		private MouseEvent firstEvent;
		@Override
		public void enter(IEditContext context) {
			firstEvent = null;
			context.detachSelectedMMLNote();
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			boolean ctrlOption = e.isControlDown();
			if (firstEvent == null) {
				firstEvent = e;
				if (!ctrlOption) {
					context.selectNoteByPoint(null, 0);
					context.detachSelectedMMLNote();
				}
			}
			// 範囲選択.
			context.areaSelectingAction(startPoint, e.getPoint());
		}
		@Override
		public void exit(IEditContext context) {
			// 範囲選択反映.
			context.applyAreaSelect();
		}
	},
	SPLIT {
		private SimpleTool splitTool = new SimpleTool((context, e) -> context.splitAction(e.getPoint()));
		@Override
		public void pressEvent(IEditContext context, MouseEvent e) {
			splitTool.pressEvent(context, e);
		}

		@Override
		public void releaseEvent(IEditContext context, MouseEvent e) {
			splitTool.releaseEvent(context, e);
		}
	},
	GLUE {
		private SimpleTool glueTool = new SimpleTool((context, e) -> context.glueAction(e.getPoint()));
		public void pressEvent(IEditContext context, MouseEvent e) {
			glueTool.pressEvent(context, e);
		}

		@Override
		public void releaseEvent(IEditContext context, MouseEvent e) {
			glueTool.releaseEvent(context, e);
		}
	};

	private static Point startPoint;

	

	EditMode() {}

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

	private static final class SimpleTool {
		private boolean inAction = false;
		private final BiConsumer<IEditContext, MouseEvent> func;
		private SimpleTool(BiConsumer<IEditContext, MouseEvent> func) {
			this.func = func;
		}

		private void pressEvent(IEditContext context, MouseEvent e) {
			boolean left = SwingUtilities.isLeftMouseButton(e);
			boolean right = SwingUtilities.isRightMouseButton(e);
			boolean middle = SwingUtilities.isMiddleMouseButton(e);
			if (left && !right && !middle) {
				inAction = true;
				func.accept(context, e);
			} else {
				inAction = false;
				context.selectNoteByPoint(null, 0);
			}
		}

		private void releaseEvent(IEditContext context, MouseEvent e) {
			if (inAction) {
				context.applyEditNote(false);
				inAction = false;
			}
		}
	}
}

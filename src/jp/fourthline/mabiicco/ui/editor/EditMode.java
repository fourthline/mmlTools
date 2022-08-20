/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.SwingUtilities;

import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mmlTools.MMLNoteEvent;

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
			boolean shiftOption = e.isShiftDown();
			boolean ctrlOption = e.isControlDown();
			int cursorType = Cursor.DEFAULT_CURSOR;
			Point p = e.getPoint();
			if (context.onExistNote(p)) {
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
			context.selectNoteByPoint(null, 0);
			context.selectNoteByPoint(startPoint, 0);
			// 編集前の選択ノートリストをdetachする.
			context.detachSelectedMMLNote();
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			// 選択中のNote長を更新.（Noteは更新しない）
			context.updateSelectedNoteAndTick(e.getPoint(), false, !e.isControlDown());
		}
		@Override
		public void exit(IEditContext context) {
			// ノート選択を解除.
			context.applyEditNote(false);
		}
	},
	AREA {
		private List<MMLNoteEvent> hadNotes; // 追加選択時の既存選択ノートのリスト
		@Override
		public void enter(IEditContext context) {
			hadNotes = null;
		}
		@Override
		public void executeEvent(IEditContext context, MouseEvent e) {
			boolean ctrlOption = e.isControlDown();
			if (hadNotes == null) {
				if (!ctrlOption) {
					context.selectNoteByPoint(null, 0);
					hadNotes = List.of();
				} else {
					// Ctrlキー押下時は追加選択する
					hadNotes = context.getSelectedNote();
				}
			}
			// 範囲選択.
			context.areaSelectingAction(startPoint, e.getPoint(), hadNotes);
		}
		@Override
		public void exit(IEditContext context) {
			// 範囲選択反映.
			context.applyAreaSelect();
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
}

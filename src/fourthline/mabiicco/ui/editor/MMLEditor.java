/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui.editor;


import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import fourthline.mabiicco.IEditState;
import fourthline.mabiicco.IEditStateObserver;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.KeyboardView;
import fourthline.mabiicco.ui.MMLNotePropertyPanel;
import fourthline.mabiicco.ui.PianoRollView;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.UndefinedTickException;


public class MMLEditor implements MouseInputListener, IEditState {

	// ノートの編集モード
	private final int EDIT_NONE = 0;
	private final int EDIT_NOTE_INSERT = 1; // ノート挿入
	private final int EDIT_NOTE_SLIDE  = 2; // ノート移動
	private final int EDIT_NOTE_LENGTH = 3; // ノートの長さ変更
	private final int RECT_NOTE_SELECT = 4; // 範囲選択
	private int editMode = EDIT_NONE;

	// 編集選択中のノート
	private ArrayList<MMLNoteEvent> selectedNote = new ArrayList<MMLNoteEvent>();

	// 編集対象のイベントリスト
	private MMLEventList editEventList;

	// 編集align (tick base)
	private int editAlign = 48;

	private IEditStateObserver editObserver;

	private Point pressedPoint;

	private PianoRollView pianoRollView;
	private KeyboardView keyboardView;
	private IMMLManager mmlManager;

	public static int DEFAULT_ALIGN_INDEX = 2;

	public static NoteAlign[] createAlignList() {
		try {
			NoteAlign list[] = {
					new NoteAlign("全音符", "1"),
					new NoteAlign("2分音符", "2"),
					new NoteAlign("4分音符", "4"),
					new NoteAlign("8分音符", "8"),
					new NoteAlign("16分音符", "16"),
					new NoteAlign("32分音符", "32"),
					new NoteAlign("64分音符", "64"),
			};

			return list;
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}

		return null;
	}

	public MMLEditor(KeyboardView keyboard, PianoRollView pianoRoll, IMMLManager mmlManager) {
		this.keyboardView = keyboard;
		this.pianoRollView = pianoRoll;
		this.mmlManager = mmlManager;

		pianoRoll.setSelectNote(selectedNote);
	}

	public void setEditAlign(int alignTick) {
		editAlign = alignTick;
	}

	/** 
	 * 編集対象とするMMLEventListを設定します.
	 * @param eventList
	 */
	public void setMMLEventList(MMLEventList eventList) {
		this.editEventList = eventList;
		selectNote(null);
	}

	private void selectNote(MMLNoteEvent noteEvent) {
		if (!selectedNote.contains(noteEvent)) {
			selectedNote.clear();
			if (noteEvent != null) {
				selectedNote.add(noteEvent);
			}
		}
	}

	/**
	 * 編集モードの判定を行います.
	 * @param note
	 * @param tickOffset
	 * @return 判定した編集モード.
	 */
	private int decisionEditMode(int note, int tickOffset) {
		MMLNoteEvent noteEvent = editEventList.searchOnTickOffset( tickOffset );

		if ( (noteEvent != null) && (noteEvent.getNote() == note) ) {
			if (noteEvent.getEndTick() <= tickOffset +(editAlign/2) ) {
				return EDIT_NOTE_LENGTH;
			} else {
				return EDIT_NOTE_SLIDE;
			}
		}

		return EDIT_NOTE_INSERT;
	}

	private int editDeltaOffset;
	/**
	 * 編集モードを開始します.
	 * @param note
	 * @param tickOffset
	 */
	private void startEditAction(int note, int tickOffset) {
		int alignedTickOffset = tickOffset - (tickOffset % editAlign);

		MMLNoteEvent noteEvent = editEventList.searchOnTickOffset( tickOffset );
		editMode = decisionEditMode(note, tickOffset);

		if ( (editMode == EDIT_NOTE_LENGTH) || (editMode == EDIT_NOTE_SLIDE) ) {
			editDeltaOffset = tickOffset - noteEvent.getTickOffset();
		} else {
			// 新規追加.
			noteEvent = new MMLNoteEvent(note, editAlign, alignedTickOffset);
		}

		keyboardView.playNote( note );
		selectNote(noteEvent);
	}

	/**
	 * 編集中モードでの表示更新を行います.
	 *  EDIT_NOTE_LENGTH: 音の長さを更新します.
	 *  EDIT_NOTE_SLIDE:  音の高さ、offsetを更新します.
	 *  EDIT_NOTE_INSERT: 音の高さを更新します.
	 * @param note
	 * @param tickOffset
	 */
	private void updateEditAction(int note, int tickOffset) {
		if (selectedNote.size() == 0) {
			return;
		}

		MMLNoteEvent editNote = selectedNote.get(0);
		if (editMode == EDIT_NOTE_LENGTH) {
			// 既存音符の編集. (一度イベントを削除します)
			editEventList.deleteMMLEvent(editNote);
			int alignedTickOffset = tickOffset - (tickOffset % editAlign);
			int newTick = (alignedTickOffset - editNote.getTickOffset()) + editAlign;
			if (newTick < 0) {
				newTick = 0;
			}
			editNote.setTick(newTick);
		}
		if (editMode == EDIT_NOTE_SLIDE) {
			// 既存音符の編集. (一度イベントを削除します)
			editEventList.deleteMMLEvent(editNote);
			editNote.setNote(note);

			int newTickOffset = (tickOffset - editDeltaOffset);
			newTickOffset -= newTickOffset % editAlign;
			editNote.setTickOffset(newTickOffset);
			keyboardView.playNote( note );
		}
		if (editMode == EDIT_NOTE_INSERT) {
			editNote.setNote(note);
			int alignedTickOffset = tickOffset - (tickOffset % editAlign);
			int newTick = (alignedTickOffset - editNote.getTickOffset()) + editAlign;
			if (newTick < 0) {
				newTick = 0;
			}
			editNote.setTick(newTick);
			keyboardView.playNote( note );
		}
	}

	private void editApply() {
		if (selectedNote.size() == 0) {
			return;
		}

		MMLNoteEvent editNote = selectedNote.get(0);
		if (editMode == EDIT_NOTE_LENGTH) {
			editEventList.addMMLNoteEvent(editNote);
		}
		if (editMode == EDIT_NOTE_SLIDE) {
			editEventList.addMMLNoteEvent(editNote);
		}
		if (editMode == EDIT_NOTE_INSERT) {
			editEventList.addMMLNoteEvent(editNote);
			selectNote(null);
		}
		keyboardView.offNote();
		mmlManager.updateActivePart();
	}

	private void updatePretarget(int note, int tickOffset) {
		Cursor cursor;

		int preEditMode = decisionEditMode(note, tickOffset);
		if (preEditMode == EDIT_NOTE_LENGTH) {
			cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
		} else if (preEditMode == EDIT_NOTE_SLIDE) {
			cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else {
			cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		}

		pianoRollView.setCursor(cursor);
	}

	private void startSelectingAction(int note, int tickOffset) {
		MMLNoteEvent noteEvent = editEventList.searchOnTickOffset( tickOffset );

		if ( (noteEvent == null) || (noteEvent.getNote() != note) ) {
			selectNote(null);
			editMode = RECT_NOTE_SELECT;
		} else {
			// TODO: Note編集メニュー
			selectNote(noteEvent);
		}
	}

	private void multiSelectingAction(Point point) {
		if (editMode == RECT_NOTE_SELECT) {
			int x1 = pressedPoint.x;
			int x2 = point.x;
			if (x1 > x2) {
				x2 = pressedPoint.x;
				x1 = point.x;
			}
			int y1 = pressedPoint.y;
			int y2 = point.y;
			if (y1 > y2) {
				y2 = pressedPoint.y;
				y1 = point.y;
			}
			Rectangle rect = new Rectangle(x1, y1, (x2-x1), (y2-y1));
			pianoRollView.setSelectingArea(rect);

			int note1 = pianoRollView.convertY2Note( y1 );
			int tickOffset1 = (int)pianoRollView.convertXtoTick( x1 );
			int note2 = pianoRollView.convertY2Note( y2 );
			int tickOffset2 = (int)pianoRollView.convertXtoTick( x2 );

			selectedNote.clear();
			for (MMLNoteEvent noteEvent : editEventList.getMMLNoteEventList()) {
				if ( (noteEvent.getNote() <= note1) && (noteEvent.getNote() >= note2) 
						&& (noteEvent.getEndTick() >= tickOffset1)
						&& (noteEvent.getTickOffset() <= tickOffset2) ) {
					selectedNote.add(noteEvent);
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 2) {
				// 左ダブルクリック: 音符上なら、その音符のプロパティ画面を表示します.
				int note = pianoRollView.convertY2Note( e.getY() );
				int tickOffset = (int)pianoRollView.convertXtoTick( e.getX() );
				MMLNoteEvent noteEvent = editEventList.searchOnTickOffset( tickOffset );
				if ( (noteEvent != null) && (noteEvent.getNote() == note) ) {
					new MMLNotePropertyPanel(noteEvent).showDialog();
					mmlManager.updateActivePart();
					pianoRollView.repaint();
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int note = pianoRollView.convertY2Note( e.getY() );
		int tickOffset = (int)pianoRollView.convertXtoTick( e.getX() );
		pressedPoint = e.getPoint();

		if (SwingUtilities.isLeftMouseButton(e)) {
			startEditAction(note, tickOffset);
		} else if (SwingUtilities.isRightMouseButton(e)) {
			startSelectingAction(note, tickOffset);
		}

		pianoRollView.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			editApply();
		} else if (SwingUtilities.isRightMouseButton(e)) {
			pianoRollView.setSelectingArea(null);
		}

		editMode = EDIT_NONE;
		pianoRollView.repaint();
		editObserver.notifyUpdateEditState();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int note = pianoRollView.convertY2Note( e.getY() );
		int tickOffset = (int)pianoRollView.convertXtoTick( e.getX() );

		if (SwingUtilities.isLeftMouseButton(e)) {
			updateEditAction(note, tickOffset);
		} else if (SwingUtilities.isRightMouseButton(e)) {
			multiSelectingAction(e.getPoint());
		}

		pianoRollView.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int note = pianoRollView.convertY2Note( e.getY() );
		int tickOffset = (int)pianoRollView.convertXtoTick( e.getX() );
		updatePretarget( note, tickOffset );
	}

	@Override
	public boolean hasSelectedNote() {
		return !(selectedNote.isEmpty());
	}

	@Override
	public void setEditStateObserver(IEditStateObserver observer) {
		this.editObserver = observer;
	}

	@Override
	public void selectedDelete() {
		for (MMLNoteEvent noteEvent : selectedNote) {
			editEventList.deleteMMLEvent(noteEvent);
		}

		selectNote(null);
		pianoRollView.repaint();
		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart();
	}
}

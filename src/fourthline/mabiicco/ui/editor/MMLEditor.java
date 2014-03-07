/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui.editor;


import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.KeyboardView;
import fourthline.mabiicco.ui.MMLNotePropertyPanel;
import fourthline.mabiicco.ui.PianoRollView;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.UndefinedTickException;


public class MMLEditor implements MouseInputListener {

	// ノートの編集モード
	private final int EDIT_NONE = 0;
	private final int EDIT_NOTE_INSERT = 1; // ノート挿入
	private final int EDIT_NOTE_SLIDE  = 2; // ノート移動
	private final int EDIT_NOTE_LENGTH = 3; // ノートの長さ変更
	private int editMode = EDIT_NONE;

	private MMLEventList eventList;

	// 編集align (tick base)
	private int editAlign = 48;

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
	}

	public void setEditAlign(int alignTick) {
		editAlign = alignTick;
	}

	/** 
	 * 編集対象とするMMLEventListを設定します.
	 * @param eventList
	 */
	public void setMMLEventList(MMLEventList eventList) {
		this.eventList = eventList;
	}

	/**
	 * 編集モードの判定を行います.
	 * @param note
	 * @param tickOffset
	 * @return 判定した編集モード.
	 */
	private int decisionEditMode(int note, int tickOffset) {
		MMLNoteEvent noteEvent = eventList.searchOnTickOffset( tickOffset );

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

		MMLNoteEvent noteEvent = eventList.searchOnTickOffset( tickOffset );
		editMode = decisionEditMode(note, tickOffset);

		if ( (editMode == EDIT_NOTE_LENGTH) || (editMode == EDIT_NOTE_SLIDE) ) {
			// 既存音符の編集. (一度イベントを削除します)
			eventList.deleteMMLEvent(noteEvent);
			editDeltaOffset = tickOffset - noteEvent.getTickOffset();
		} else {
			// 新規追加.
			noteEvent = new MMLNoteEvent(note, editAlign, alignedTickOffset);
		}

		keyboardView.playNote( note );
		pianoRollView.setEditNote(noteEvent);

		pianoRollView.repaint();
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
		MMLNoteEvent editNote = pianoRollView.getEditNote();

		if (editNote == null) {
			return;
		}
		if (editMode == EDIT_NOTE_LENGTH) {
			int alignedTickOffset = tickOffset - (tickOffset % editAlign);
			int newTick = (alignedTickOffset - editNote.getTickOffset()) + editAlign;
			if (newTick < 0) {
				newTick = 0;
			}
			editNote.setTick(newTick);
		}
		if (editMode == EDIT_NOTE_SLIDE) {
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

		pianoRollView.repaint();
	}

	private void editApply() {
		MMLNoteEvent editNote = pianoRollView.getEditNote();

		if (editNote == null) {
			return;
		}
		if (editMode == EDIT_NOTE_LENGTH) {
			eventList.addMMLNoteEvent(editNote);
		}
		if (editMode == EDIT_NOTE_SLIDE) {
			eventList.addMMLNoteEvent(editNote);
		}
		if (editMode == EDIT_NOTE_INSERT) {
			eventList.addMMLNoteEvent(editNote);
		}
		pianoRollView.setEditNote(null);
		keyboardView.offNote();
		editMode = EDIT_NONE;

		pianoRollView.repaint();
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


	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			if (e.getClickCount() == 2) {
				// 右ダブルクリック: 音符上なら、その音符のプロパティ画面を表示します.
				int note = pianoRollView.convertY2Note( e.getY() );
				int tickOffset = (int)pianoRollView.convertXtoTick( e.getX() );
				MMLNoteEvent noteEvent = eventList.searchOnTickOffset( tickOffset );
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
		if (SwingUtilities.isLeftMouseButton(e)) {
			int note = pianoRollView.convertY2Note( e.getY() );
			int tickOffset = (int)pianoRollView.convertXtoTick( e.getX() );
			startEditAction(note, tickOffset);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			editApply();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			int note = pianoRollView.convertY2Note( e.getY() );
			int tickOffset = (int)pianoRollView.convertXtoTick( e.getX() );
			updateEditAction(note, tickOffset);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int note = pianoRollView.convertY2Note( e.getY() );
		int tickOffset = (int)pianoRollView.convertXtoTick( e.getX() );
		updatePretarget( note, tickOffset );
	}

}

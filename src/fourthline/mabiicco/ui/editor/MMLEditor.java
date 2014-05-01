/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui.editor;


import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import fourthline.mabiicco.ActionDispatcher;
import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.IEditState;
import fourthline.mabiicco.IEditStateObserver;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.KeyboardView;
import fourthline.mabiicco.ui.MMLNotePropertyPanel;
import fourthline.mabiicco.ui.PianoRollView;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.UndefinedTickException;


public class MMLEditor implements MouseInputListener, IEditState, IEditContext, IEditAlign {

	private EditMode editMode = EditMode.SELECT;

	// 編集選択中のノート
	private final ArrayList<MMLNoteEvent> selectedNote = new ArrayList<>();
	// 複数ノート移動時のdetachリスト
	private final ArrayList<MMLNoteEvent> detachedNote = new ArrayList<>();

	// Cut, Copy時に保持するリスト.
	private MMLEventList clipEventList;
	// 編集対象のイベントリスト
	private MMLEventList editEventList;

	// 編集align (tick base)
	private int editAlign = 48;

	private IEditStateObserver editObserver;

	private final PianoRollView pianoRollView;
	private final KeyboardView keyboardView;
	private final IMMLManager mmlManager;

	private final JPopupMenu popupMenu = new JPopupMenu();

	public static int DEFAULT_ALIGN_INDEX = 2;

	public static NoteAlign[] createAlignList() {
		try {
			NoteAlign list[] = {
					new NoteAlign(AppResource.getText("note_1"), "1"),
					new NoteAlign(AppResource.getText("note_2"), "2"),
					new NoteAlign(AppResource.getText("note_4"), "4"),
					new NoteAlign(AppResource.getText("note_8"), "8"),
					new NoteAlign(AppResource.getText("note_16"), "16"),
					new NoteAlign(AppResource.getText("note_32"), "32"),
					new NoteAlign(AppResource.getText("note_64"), "64"),
					new NoteAlign(AppResource.getText("note_12"), "12"),
					new NoteAlign(AppResource.getText("note_24"), "24"),
					new NoteAlign(AppResource.getText("note_48"), "48"),
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

		newPopupMenu(AppResource.getText("part_change"), ActionDispatcher.PART_CHANGE);
	}

	public void setEditAlign(int alignTick) {
		editAlign = alignTick;
	}

	@Override
	public int getEditAlign() {
		return editAlign;
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
		selectNote(noteEvent, false);
	}

	private void selectNote(MMLNoteEvent noteEvent, boolean multiSelect) {
		if (noteEvent == null) {
			selectedNote.clear();
		}
		if ( (noteEvent != null) && (!selectedNote.contains(noteEvent))) {
			if (!multiSelect) {
				selectedNote.clear();
			}
			selectedNote.add(noteEvent);
		}
	}

	private void selectMultipleNote(MMLNoteEvent noteEvent1, MMLNoteEvent noteEvent2) {
		selectMultipleNote(noteEvent1, noteEvent2, true);
	}

	/**
	 * @param noteEvent1
	 * @param noteEvent2
	 * @param lookNote falseの場合は、tickOffset間にあるすべてのノートが選択される. trueの場合はnote情報もみて判定する.
	 */
	private void selectMultipleNote(MMLNoteEvent noteEvent1, MMLNoteEvent noteEvent2, boolean lookNote) {
		int note[] = {
				noteEvent1.getNote(),
				noteEvent2.getNote()
		};
		int tickOffset[] = {
				noteEvent1.getTickOffset(),
				noteEvent2.getTickOffset()
		};

		Arrays.sort(note);
		Arrays.sort(tickOffset);

		if (!lookNote) {
			note[0] = 0;
			note[1] = 96;
		}

		selectedNote.clear();
		for (MMLNoteEvent noteEvent : editEventList.getMMLNoteEventList()) {
			if ( (noteEvent.getNote() >= note[0]) && (noteEvent.getNote() <= note[1]) 
					&& (noteEvent.getEndTick() > tickOffset[0])
					&& (noteEvent.getTickOffset() <= tickOffset[1]) ) {
				selectedNote.add(noteEvent);
			}
		}
	}

	/**
	 * @param point  nullのときはクリアする.
	 */
	@Override
	public void selectNoteByPoint(Point point, int selectModifiers) {
		if (point == null) {
			selectNote(null);
		} else {
			int note = pianoRollView.convertY2Note(point.y);
			long tickOffset = pianoRollView.convertXtoTick(point.x);
			MMLNoteEvent noteEvent = editEventList.searchOnTickOffset(tickOffset);
			if (noteEvent.getNote() != note) {
				return;
			}

			if ( (selectedNote.size() == 1) && ((selectModifiers & ActionEvent.SHIFT_MASK) != 0) ) {
				selectMultipleNote(selectedNote.get(0), noteEvent, false);
			} else {
				selectNote(noteEvent, ((selectModifiers & ActionEvent.CTRL_MASK) != 0));
			}
		}
	}

	/**
	 * 指定されたPointに新しいノートを作成する.
	 * 作成されたNoteは、選択状態になる.
	 */
	@Override
	public void newMMLNoteAndSelected(Point p) {
		int note = pianoRollView.convertY2Note(p.y);
		long tickOffset = pianoRollView.convertXtoTick(p.x);
		long alignedTickOffset = tickOffset - (tickOffset % editAlign);

		MMLNoteEvent noteEvent = new MMLNoteEvent(note, editAlign, (int)alignedTickOffset);
		selectNote(noteEvent);
		keyboardView.playNote( note );
	}

	/**
	 * 選択状態のノート、ノート長を更新する（ノート挿入時）
	 */
	@Override
	public void updateSelectedNoteAndTick(Point p, boolean updateNote) {
		MMLNoteEvent noteEvent = selectedNote.get(0);
		int note = pianoRollView.convertY2Note(p.y);
		long tickOffset = pianoRollView.convertXtoTick(p.x);
		long alignedTickOffset = tickOffset - (tickOffset % editAlign);
		long newTick = (alignedTickOffset - noteEvent.getTickOffset()) + editAlign;
		if (newTick < 0) {
			newTick = 0;
		}
		if (updateNote) {
			noteEvent.setNote(note);
		}
		noteEvent.setTick((int)newTick);
		keyboardView.playNote(noteEvent.getNote());
	}

	@Override
	public void detachSelectedMMLNote() {
		detachedNote.clear();
		for (MMLNoteEvent noteEvent : selectedNote) {
			detachedNote.add(noteEvent.clone());
		}
	}
	/**
	 * 選択状態のノートを移動する
	 */
	@Override
	public void moveSelectedMMLNote(Point start, Point p, boolean shiftOption) {
		int noteDelta = pianoRollView.convertY2Note(p.y) - pianoRollView.convertY2Note(start.y);
		long tickOffsetDelta = pianoRollView.convertXtoTick(p.x) - pianoRollView.convertXtoTick(start.x);
		long alignedTickOffsetDelta = tickOffsetDelta - (tickOffsetDelta % editAlign);
		if (shiftOption) {
			alignedTickOffsetDelta = 0;
		}

		for (int i = 0; i < selectedNote.size(); i++) {
			MMLNoteEvent note1 = detachedNote.get(i);
			MMLNoteEvent note2 = selectedNote.get(i);
			note2.setNote(note1.getNote() + noteDelta);
			note2.setTickOffset(note1.getTickOffset() + (int)alignedTickOffsetDelta);
		}

		keyboardView.playNote( pianoRollView.convertY2Note(p.y) );
	}

	@Override
	public void cancelMove() {
		int i = 0;
		for (MMLNoteEvent noteEvent : selectedNote) {
			MMLNoteEvent revertNote = detachedNote.get(i++);
			noteEvent.setNote(revertNote.getNote());
			noteEvent.setTickOffset(revertNote.getTickOffset());
		}
	}

	/**
	 * 編集選択中のノートをイベントリストに反映する.
	 * @param select trueの場合は選択状態を維持する.
	 */
	@Override
	public void applyEditNote(boolean select) {
		for (MMLNoteEvent noteEvent : selectedNote) {
			editEventList.deleteMMLEvent(noteEvent);
			editEventList.addMMLNoteEvent(noteEvent);
		}
		if (!select) {
			selectNote(null);
		}
		mmlManager.updateActivePart();
		keyboardView.offNote();
	}

	@Override
	public void setCursor(Cursor cursor) {
		pianoRollView.setCursor(cursor);
	}

	@Override
	public void areaSelectingAction(Point startPoint, Point point) {
		int x1 = startPoint.x;
		int x2 = point.x;
		if (x1 > x2) {
			x2 = startPoint.x;
			x1 = point.x;
		}
		int y1 = startPoint.y;
		int y2 = point.y;
		if (y1 > y2) {
			y2 = startPoint.y;
			y1 = point.y;
		}
		Rectangle rect = new Rectangle(x1, y1, (x2-x1), (y2-y1));
		pianoRollView.setSelectingArea(rect);

		int note1 = pianoRollView.convertY2Note( y1 );
		int tickOffset1 = (int)pianoRollView.convertXtoTick( x1 );
		int note2 = pianoRollView.convertY2Note( y2 );
		int tickOffset2 = (int)pianoRollView.convertXtoTick( x2 );

		selectMultipleNote(
				new MMLNoteEvent(note1, 0, tickOffset1),
				new MMLNoteEvent(note2, 0, tickOffset2) );
	}

	@Override
	public void applyAreaSelect() {
		pianoRollView.setSelectingArea(null);
	}


	/**
	 * Editスタートポイントがノート上であるかどうかを判定する.
	 * @param point
	 * @return ノート上の場合はtrue.
	 */
	@Override
	public boolean onExistNote(Point point, boolean autoSelect) {
		int note = pianoRollView.convertY2Note( point.y );
		int tickOffset = (int)pianoRollView.convertXtoTick( point.x );
		MMLNoteEvent noteEvent = editEventList.searchOnTickOffset(tickOffset);

		if ( (noteEvent != null) && (note == noteEvent.getNote()) ) {
			return true;
		}

		if (autoSelect) {
			return mmlManager.selectTrackOnExistNote(note, tickOffset);
		}

		return false;
	}

	/**
	 * 音価編集位置にあるかどうかを判定する.
	 * @param point
	 * @return
	 */
	@Override
	public boolean isEditLengthPosition(Point point) {
		int note = pianoRollView.convertY2Note( point.y );
		int tickOffset = (int)pianoRollView.convertXtoTick( point.x );
		MMLNoteEvent noteEvent = editEventList.searchOnTickOffset( tickOffset );

		if ( (noteEvent != null) && (noteEvent.getNote() == note) ) {
			if (noteEvent.getEndTick() <= tickOffset +(editAlign/2) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Edit状態を変更する.
	 * @param nextMode
	 */
	@Override
	public EditMode changeState(EditMode nextMode) {
		if (editMode != nextMode) {
			editMode.exit(this);
			editMode = nextMode;
			editMode.enter(this);
		}

		return nextMode;
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
		editMode.pressEvent(this, e);
		pianoRollView.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		editMode.releaseEvent(this, e);
		pianoRollView.repaint();
		editObserver.notifyUpdateEditState();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		editMode.executeEvent(this, e);
		pianoRollView.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		editMode.executeEvent(this, e);
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
	public boolean canPaste() {
		if (clipEventList == null) {
			return false;
		}
		if (clipEventList.getMMLNoteEventList().size() == 0) {
			return false;
		}

		return true;
	}

	@Override
	public void paste(long startTick) {
		if (!canPaste()) {
			return;
		}

		selectNote(null);
		long offset = clipEventList.getMMLNoteEventList().get(0).getTickOffset();
		for (MMLNoteEvent noteEvent : clipEventList.getMMLNoteEventList()) {
			long tickOffset = noteEvent.getTickOffset() - offset + startTick;
			MMLNoteEvent addNote = new MMLNoteEvent(noteEvent.getNote(), noteEvent.getTick(), (int)tickOffset);
			addNote.setVelocity(noteEvent.getVelocity());
			editEventList.addMMLNoteEvent(addNote);
			selectNote(addNote, true);
		}

		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart();
	}

	@Override
	public void selectedCut() {
		clipEventList = new MMLEventList("");
		for (MMLNoteEvent noteEvent : selectedNote) {
			clipEventList.addMMLNoteEvent(noteEvent);
			editEventList.deleteMMLEvent(noteEvent);
		}

		selectNote(null);
		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart();
	}

	@Override
	public void selectedCopy() {
		clipEventList = new MMLEventList("");
		for (MMLNoteEvent noteEvent : selectedNote) {
			clipEventList.addMMLNoteEvent(noteEvent);
		}

		editObserver.notifyUpdateEditState();
	}

	@Override
	public void selectedDelete() {
		for (MMLNoteEvent noteEvent : selectedNote) {
			editEventList.deleteMMLEvent(noteEvent);
		}

		selectNote(null);
		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart();
	}

	public void changePart(MMLEventList from, MMLEventList to, boolean useSelectedNoteList, ChangePartAction action) {
		int startTick = 0;
		int endTick;
		if (useSelectedNoteList && (selectedNote.size() > 0) ) {
			MMLNoteEvent startNote = selectedNote.get(0);
			MMLNoteEvent endNote = selectedNote.get(selectedNote.size()-1);

			startTick = from.getAlignmentStartTick(to, startNote.getTickOffset());
			endTick = from.getAlignmentEndTick(to, endNote.getEndTick());
		} else {
			endTick = (int) from.getTickLength();
			int toEndTick = (int) to.getTickLength();
			if (endTick < toEndTick) {
				endTick = toEndTick;
			}			
		}

		switch (action) {
		case SWAP:
			from.swap(to, startTick, endTick);
			break;
		case MOVE:
			from.move(to, startTick, endTick);
			break;
		case COPY:
			from.copy(to, startTick, endTick);
			break;
		default:
		}
	}

	public enum ChangePartAction {
		SWAP, MOVE, COPY
	}

	private JMenuItem newPopupMenu(String name, String command) {
		JMenuItem menu = new JMenuItem(name);
		menu.addActionListener(ActionDispatcher.getInstance());
		menu.setActionCommand(command);
		popupMenu.add(menu);
		return menu;
	}

	@Override
	public void showPopupMenu(Point point) {
		if (MabiDLS.getInstance().getSequencer().isRecording()) {
			return;
		}
		int x = (int)point.getX();
		int y = (int)point.getY();
		popupMenu.show(pianoRollView, x, y);
	}
}

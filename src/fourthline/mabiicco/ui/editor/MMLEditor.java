/*
 * Copyright (C) 2013-2015 たんらる
 */

package fourthline.mabiicco.ui.editor;


import java.awt.Cursor;
import java.awt.Frame;
import java.awt.IllegalComponentStateException;
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
import fourthline.mabiicco.midi.IPlayNote;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.PianoRollView;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;


public final class MMLEditor implements MouseInputListener, IEditState, IEditContext, IEditAlign {

	private EditMode editMode = EditMode.SELECT;

	// 編集選択中のノート
	private final ArrayList<MMLNoteEvent> selectedNote = new ArrayList<>();
	// 複数ノート移動時のdetachリスト
	private final ArrayList<MMLNoteEvent> detachedNote = new ArrayList<>();

	// Cut, Copy時に保持するリスト.
	private MMLEventList clipEventList;

	// 編集align (tick base)
	private int editAlign = 48;

	private IEditStateObserver editObserver;

	private final PianoRollView pianoRollView;
	private final IPlayNote notePlayer;
	private final IMMLManager mmlManager;

	private final JPopupMenu popupMenu = new JPopupMenu();

	private final Frame parentFrame;

	public MMLEditor(Frame parentFrame, IPlayNote notePlayer, PianoRollView pianoRoll, IMMLManager mmlManager) {
		this.notePlayer = notePlayer;
		this.pianoRollView = pianoRoll;
		this.mmlManager = mmlManager;
		this.parentFrame = parentFrame;

		pianoRoll.setSelectNote(selectedNote);

		newPopupMenu(AppResource.appText("part_change"), ActionDispatcher.PART_CHANGE);
		newPopupMenu(AppResource.appText("edit.select_previous_all"), ActionDispatcher.SELECT_PREVIOUS_ALL);
		newPopupMenu(AppResource.appText("edit.select_after_all"), ActionDispatcher.SELECT_AFTER_ALL);
		newPopupMenu(AppResource.appText("menu.delete"), ActionDispatcher.DELETE, AppResource.appText("menu.delete.icon"));
		newPopupMenu(AppResource.appText("note.properties"), ActionDispatcher.NOTE_PROPERTY);
	}

	public void setEditAlign(int alignTick) {
		editAlign = alignTick;
	}

	@Override
	public int getEditAlign() {
		return editAlign;
	}

	/** 
	 * Editor reset
	 */
	public void reset() {
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
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
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
			MMLEventList editEventList = mmlManager.getActiveMMLPart();
			if (editEventList == null) {
				return;
			}
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
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
		MMLNoteEvent prevNote = editEventList.searchPrevNoteOnTickOffset(tickOffset);

		MMLNoteEvent noteEvent = new MMLNoteEvent(note, editAlign, (int)alignedTickOffset, prevNote.getVelocity());
		selectNote(noteEvent);
		notePlayer.playNote( note, noteEvent.getVelocity() );
	}

	/**
	 * 選択状態のノート、ノート長を更新する（ノート挿入時）
	 */
	@Override
	public void updateSelectedNoteAndTick(Point p, boolean updateNote) {
		if (selectedNote.size() <= 0) {
			return;
		}
		pianoRollView.onViewScrollPoint(p);
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
		notePlayer.playNote(noteEvent.getNote(), noteEvent.getVelocity());
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
		pianoRollView.onViewScrollPoint(p);
		long targetTick = pianoRollView.convertXtoTick(start.x);
		int noteDelta = pianoRollView.convertY2Note(p.y) - pianoRollView.convertY2Note(start.y);
		long tickOffsetDelta = pianoRollView.convertXtoTick(p.x) - targetTick;
		long alignedTickOffsetDelta = tickOffsetDelta - (tickOffsetDelta % editAlign);
		if (shiftOption) {
			alignedTickOffsetDelta = 0;
		}

		int velocity = detachedNote.get(0).getVelocity();
		for (int i = 0; i < selectedNote.size(); i++) {
			MMLNoteEvent note1 = detachedNote.get(i);
			MMLNoteEvent note2 = selectedNote.get(i);
			note2.setNote(note1.getNote() + noteDelta);
			note2.setTickOffset(note1.getTickOffset() + (int)alignedTickOffsetDelta);
			if ( (note1.getTickOffset() <= targetTick) && (note1.getEndTick() > targetTick) ) {
				velocity = note2.getVelocity();
			}
		}

		notePlayer.playNote( pianoRollView.convertY2Note(p.y), velocity );
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
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
		for (MMLNoteEvent noteEvent : selectedNote) {
			editEventList.deleteMMLEvent(noteEvent);
			editEventList.addMMLNoteEvent(noteEvent);
		}
		if (!select) {
			selectNote(null);
		}
		notePlayer.offNote();
		mmlManager.updateActivePart(true);
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
				new MMLNoteEvent(note1, 0, tickOffset1, 0),
				new MMLNoteEvent(note2, 0, tickOffset2, 0) );
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
	public boolean onExistNote(Point point) {
		int note = pianoRollView.convertY2Note( point.y );
		int tickOffset = (int)pianoRollView.convertXtoTick( point.x );
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return false;
		}
		MMLNoteEvent noteEvent = editEventList.searchOnTickOffset(tickOffset);

		if ( (noteEvent != null) && (note == noteEvent.getNote()) ) {
			return true;
		}

		return false;
	}

	/**
	 * pointの位置で他トラックにノートが配置されていれば、アクティブパートを変更します.
	 * @param point
	 * @return アクティブパートを変更した場合はtrue.
	 */
	@Override
	public boolean selectTrackOnExistNote(Point point) {
		if (onExistNote(point)) {
			return false;
		}
		int note = pianoRollView.convertY2Note( point.y );
		int tickOffset = (int)pianoRollView.convertXtoTick( point.x );
		return mmlManager.selectTrackOnExistNote(note, tickOffset);
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
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return false;
		}
		MMLNoteEvent noteEvent = editEventList.searchOnTickOffset( tickOffset );

		if ( (noteEvent != null) && (noteEvent.getNote() == note) ) {
			if (noteEvent.getEndTick() <= tickOffset + (noteEvent.getTick() / 5) ) {
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
				noteProperty();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

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

		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
		selectNote(null);
		int delta = (int)( startTick - clipEventList.getMMLNoteEventList().get(0).getTickOffset() );
		for (MMLNoteEvent noteEvent : clipEventList.getMMLNoteEventList()) {
			MMLNoteEvent addNote = noteEvent.clone();
			addNote.setTickOffset(noteEvent.getTickOffset() + delta);
			editEventList.addMMLNoteEvent(addNote);
			selectNote(addNote, true);
		}

		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart(true);
	}

	@Override
	public void selectedCut() {
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
		clipEventList = new MMLEventList("");
		for (MMLNoteEvent noteEvent : selectedNote) {
			clipEventList.addMMLNoteEvent(noteEvent);
			editEventList.deleteMMLEvent(noteEvent);
		}

		selectNote(null);
		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart(true);
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
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
		for (MMLNoteEvent noteEvent : selectedNote) {
			editEventList.deleteMMLEvent(noteEvent);
		}

		selectNote(null);
		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart(true);
	}

	@Override
	public void noteProperty() {
		if (selectedNote.isEmpty()) {
			return;
		}

		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList != null) {
			new MMLNotePropertyPanel(selectedNote.toArray(new MMLNoteEvent[selectedNote.size()]), editEventList).showDialog(parentFrame);
			mmlManager.updateActivePart(true);
		}
	}

	@Override
	public void selectAll() {
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList != null) {
			selectedNote.clear();
			selectedNote.addAll(editEventList.getMMLNoteEventList());
		}
	}

	@Override
	public void selectPreviousAll() {
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList != null) {
			for (MMLNoteEvent note : editEventList.getMMLNoteEventList()) {
				if (note.getTickOffset() < popupTargetNote.getTickOffset()) {
					selectedNote.add(note);
				} else {
					break;
				}
			}
		}
	}

	@Override
	public void selectAfterAll() {
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList != null) {
			for (MMLNoteEvent note : editEventList.getMMLNoteEventList()) {
				if (note.getTickOffset() > popupTargetNote.getTickOffset()) {
					selectedNote.add(note);
				}
			}
		}
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

	private JMenuItem newPopupMenu(String name, String command, String iconName) {
		JMenuItem menu = newPopupMenu(name, command);
		try {
			menu.setIcon(AppResource.getImageIcon(iconName));
		} catch (NullPointerException e) {}

		return menu;
	}

	private MMLNoteEvent popupTargetNote;
	@Override
	public void showPopupMenu(Point point) {
		if (MabiDLS.getInstance().getSequencer().isRecording()) {
			return;
		}

		int tickOffset = (int)pianoRollView.convertXtoTick( point.x );
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
		popupTargetNote = editEventList.searchOnTickOffset(tickOffset);

		if (hasSelectedNote()) {
			try {
				popupMenu.show(pianoRollView, point.x, point.y);
			} catch (IllegalComponentStateException e) {}
		}
	}
}

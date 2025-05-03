/*
 * Copyright (C) 2013-2025 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;


import java.awt.Cursor;
import java.awt.Frame;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.IEditState;
import jp.fourthline.mabiicco.IEditStateObserver;
import jp.fourthline.mabiicco.midi.IPlayNote;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.PianoRollView;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLTrack;


public final class MMLEditor implements MouseInputListener, IEditState, IEditContext, IEditAlign {

	private EditMode editMode = EditMode.SELECT;

	// 編集選択中のノート
	private final ArrayList<MMLNoteEvent> selectedNote = new ArrayList<>();
	// 編集中のdetachノートリスト
	private final ArrayList<MMLNoteEvent> detachedNote = new ArrayList<>();

	// 編集align (tick base)
	private int editAlign = 48;

	private IEditStateObserver editObserver;

	private final PianoRollView pianoRollView;
	private final IPlayNote notePlayer;
	private final IMMLManager mmlManager;

	private final JPopupMenu popupMenu = new JPopupMenu();
	private final VelocityChangeMenu velocityChangeMenu;
	private final JMenuItem createTupletMenu;

	private final Frame parentFrame;

	private final EditorAction editorAction;

	private static final String MMLEVENT_PREFIX = "MMLEVENT@";

	@Override
	public void changeEditTool(EditTool tool) {
		changeState(tool.getEditMode());
		setCursor(tool.getCursor());
	}

	public MMLEditor(Frame parentFrame, IPlayNote notePlayer, PianoRollView pianoRoll, IMMLManager mmlManager) {
		this.notePlayer = notePlayer;
		this.pianoRollView = pianoRoll;
		this.mmlManager = mmlManager;
		this.parentFrame = parentFrame;
		this.editorAction = new EditorAction(this, mmlManager, pianoRoll, notePlayer, this::tickAlign);

		pianoRoll.setSelectNote(selectedNote);
		pianoRoll.addMouseInputListener(this);

		editMode.enter(this);

		velocityChangeMenu = new VelocityChangeMenu(popupMenu,
				() -> popupTargetNote.getVelocity(),
				t -> {
					// 音量コマンド挿入.
					mmlManager.getActiveMMLPart().setVelocityCommand(popupTargetNote, t);
					mmlManager.generateActiveTrack();
				},
				t -> {
					// 選択したノートの音量を変更する.
					for (MMLNoteEvent n : selectedNote) {
						n.setVelocity(t);
					}
					mmlManager.generateActiveTrack();
				});
		newPopupMenu(AppResource.appText("part_change"), ActionDispatcher.PART_CHANGE);
		popupMenu.add(new JSeparator());
		newPopupMenu(AppResource.appText("edit.select_previous_all"), ActionDispatcher.SELECT_PREVIOUS_ALL);
		newPopupMenu(AppResource.appText("edit.select_after_all"), ActionDispatcher.SELECT_AFTER_ALL);
		newPopupMenu(AppResource.appText("edit.select_all_same_pitch"), ActionDispatcher.SELECT_ALL_SAME_PITCH);
		popupMenu.add(new JSeparator());
		newPopupMenu(AppResource.appText("edit.set_temp_mute"), ActionDispatcher.SET_TEMP_MUTE);
		newPopupMenu(AppResource.appText("edit.unset_temp_mute"), ActionDispatcher.UNSET_TEMP_MUTE);
		newPopupMenu(AppResource.appText("edit.unset_temp_mute_all"), ActionDispatcher.UNSET_TEMP_MUTE_ALL);
		popupMenu.add(new JSeparator());
		createTupletMenu = newPopupMenu(AppResource.appText("edit.convert_tuplet"), ActionDispatcher.CONVERT_TUPLET);
		newPopupMenu(AppResource.appText("menu.delete"), ActionDispatcher.DELETE, AppResource.appText("menu.delete.icon"));
		newPopupMenu(AppResource.appText("note.properties"), ActionDispatcher.NOTE_PROPERTY);

		// laf変更反映対応
		ActionDispatcher.getInstance().addUpdateUIComponent(popupMenu);
	}

	public void setEditAlign(int alignTick) {
		editAlign = alignTick;
	}

	@Override
	public int getEditAlign() {
		return editAlign;
	}

	/**
	 * スタート位置に合わせてアライメントする
	 * @param tick
	 * @return
	 */
	private long tickAlign(long tick) {
		MMLTrack t = mmlManager.getActiveTrack();
		int activePartIndex = mmlManager.getActiveMMLPartIndex();
		int startOffset = t.getStartOffset(activePartIndex);
		int commonStartOffset = t.getCommonStartOffset();
		if ( (startOffset <= commonStartOffset) && (tick >= commonStartOffset) ) {
			startOffset = commonStartOffset;
		}
		return tick - (tick - startOffset) % editAlign;
	}

	/** 
	 * Editor reset
	 */
	public void reset() {
		selectNote(null);
		if (editObserver != null) {
			editObserver.notifyUpdateEditState();
		}
	}

	public boolean selectNote(MMLNoteEvent noteEvent) {
		return selectNote(noteEvent, false);
	}

	private boolean selectNote(MMLNoteEvent noteEvent, boolean multiSelect) {
		if (noteEvent == null) {
			selectedNote.clear();
		} else if (!multiSelect) {
			if (!selectedNote.contains(noteEvent)) {
				selectedNote.clear();
				selectedNote.add(noteEvent);
			}
		} else {
			if (!selectedNote.contains(noteEvent)) {
				selectedNote.add(noteEvent);
			} else {
				selectedNote.remove(noteEvent);
			}
			return false;
		}
		return true;
	}

	/**
	 * @param noteEvent1
	 * @param noteEvent2
	 * @param lookNote falseの場合は、tickOffset間にあるすべてのノートが選択される. trueの場合はnote情報もみて判定する.
	 * @param selectMode  trueの場合は反転モードON。すでに選択されているノートの場合は選択解除する
	 * @param hadNotes    追加選択しているノートのリスト
	 */
	private void selectMultipleNote(MMLNoteEvent noteEvent1, MMLNoteEvent noteEvent2, boolean lookNote, boolean selectMode) {
		int[] note = {
				noteEvent1.getNote(),
				noteEvent2.getNote()
		};
		int[] tickOffset = {
				noteEvent1.getTickOffset(),
				noteEvent2.getTickOffset()
		};

		Arrays.sort(note);
		Arrays.sort(tickOffset);

		if (!lookNote) {
			note[0] = 0;
			note[1] = 108;
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
				if (!selectMode || !detachedNote.contains(noteEvent)) {
					selectedNote.add(noteEvent);
				}
			} else if (detachedNote.contains(noteEvent)) {
				selectedNote.add(noteEvent);
			}
		}
	}

	/**
	 * ポイント上にあるアクティブパートのノートを得る
	 * @param point
	 * @return   ポイント上にアクティブパートのノートがなければ nullを返す.
	 */
	private MMLNoteEvent pointToNote(Point point) {
		if (point != null) {
			if (point.y > pianoRollView.getTotalHeight()) {
				return null;
			}

			MMLEventList editEventList = mmlManager.getActiveMMLPart();
			if (editEventList == null) {
				return null;
			}

			int note = pianoRollView.convertY2Note(point.y);
			long tickOffset = pianoRollView.convertXtoTick(point.x);
			MMLNoteEvent noteEvent = editEventList.searchOnTickOffset(tickOffset);
			if ((noteEvent != null) && (noteEvent.getNote() == note)) {
				return noteEvent;
			}
		}
		return null;
	}

	/**
	 * @param point  nullのときはクリアする.
	 */
	@Override
	public boolean selectNoteByPoint(Point point, int selectModifiers) {
		if (point == null) {
			return selectNote(null);
		} else {
			MMLNoteEvent noteEvent = pointToNote(point);
			if (noteEvent == null) {
				return true;
			}

			if ( (selectedNote.size() == 1) && ((selectModifiers & InputEvent.SHIFT_DOWN_MASK) != 0) ) {
				selectMultipleNote(selectedNote.get(0), noteEvent, false, false);
				return false;
			} else {
				return selectNote(noteEvent, ((selectModifiers & InputEvent.CTRL_DOWN_MASK) != 0));
			}
		}
	}

	/**
	 * 指定されたPointに新しいノートを作成する.
	 * 作成されたNoteは、選択状態になる.
	 */
	@Override
	public void newMMLNoteAndSelected(Point p) {
		editorAction.newNoteAction(p);
	}

	/**
	 * 選択状態のノート、ノート長を更新する（ノート挿入時）
	 */
	@Override
	public void updateSelectedNoteAndTick(Point p, boolean updateNote, boolean alignment) {
		if (selectedNote.size() <= 0) {
			return;
		}
		pianoRollView.onViewScrollPoint(p);
		MMLNoteEvent noteEvent = selectedNote.get(0);
		int note = pianoRollView.convertY2Note(p.y);
		long tickOffset = pianoRollView.convertXtoTick(p.x);
		long alignedTickOffset = alignment ? tickAlign(tickOffset+editAlign) : tickOffset;
		long newTick = (alignedTickOffset - noteEvent.getTickOffset());
		if (newTick < 0) {
			newTick = 0;
		}
		if (updateNote) {
			noteEvent.setNote(note);
		}
		noteEvent.setTick((int)newTick);
		// ノート情報表示
		pianoRollView.setPaintNoteInfo(!alignment ? noteEvent : null);
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
	 * 
	 * --------------------------------------------------------------
	 * | shiftOption | alignment |  function                        |
	 * --------------------------------------------------------------
	 * |    false    |   false   |アライメントなしで移動する                 |
	 * |    false    |   true    |NoteAlign設定に基づいて移動する(default)|
	 * |    true     |   false   |横方向固定でオクターブ単位でノート移動する    |
	 * |    true     |   true    |横方向固定でノート変更のみで移動する       |
	 * --------------------------------------------------------------
	 */
	@Override
	public void moveSelectedMMLNote(Point start, Point p, boolean shiftOption, boolean alignment, boolean octaveAlign, boolean showInfo) {
		int startOffset = mmlManager.getActiveMMLPartStartOffset();
		pianoRollView.onViewScrollPoint(p);
		long startTick = pianoRollView.convertXtoTick(start.x);
		long targetTick = pianoRollView.convertXtoTick(p.x);
		int pivNote = pianoRollView.convertY2Note(start.y);
		int noteDelta = pianoRollView.convertY2Note(p.y) - pivNote;
		long tickOffsetDelta = targetTick - startTick;
		long alignedTickOffsetDelta = tickOffsetDelta;
		if (shiftOption) {
			alignedTickOffsetDelta = 0;
			if (octaveAlign) {
				noteDelta += (noteDelta >= 0) ? 5 : -5;
				noteDelta -= noteDelta % 12;
			}
		} else {
			if (alignment) {
				alignedTickOffsetDelta -= (tickOffsetDelta % editAlign);
			} else {
				System.out.print("");
			}
		}
		if (detachedNote.get(0).getTickOffset() + alignedTickOffsetDelta < startOffset) {
			alignedTickOffsetDelta = startOffset - detachedNote.get(0).getTickOffset();
		}

		int velocity = detachedNote.get(0).getVelocity();
		for (int i = 0; i < selectedNote.size(); i++) {
			MMLNoteEvent note1 = detachedNote.get(i);
			MMLNoteEvent note2 = selectedNote.get(i);
			note2.setNote(note1.getNote() + noteDelta);
			note2.setTickOffset(note1.getTickOffset() + (int)alignedTickOffsetDelta);
			if ( (note1.getTickOffset() <= startTick) && (note1.getEndTick() > startTick) ) {
				velocity = note2.getVelocity();
				// ノート情報表示
				pianoRollView.setPaintNoteInfo(showInfo ? note2 : null);
			}
		}

		notePlayer.playNote( pivNote + noteDelta, velocity );
	}

	@Override
	public void editLengthSelectedMMLNote(Point start, Point p, boolean alignment) {
		if (selectedNote.size() == 1) {
			updateSelectedNoteAndTick(p, false, alignment);
		} else {
			editorAction.editLengthAction(start, p, alignment, selectedNote, detachedNote);
		}
	}

	@Override
	public void cancelEdit() {
		int i = 0;
		for (MMLNoteEvent noteEvent : selectedNote) {
			MMLNoteEvent revertNote = detachedNote.get(i++);
			noteEvent.setNote(revertNote.getNote());
			noteEvent.setTickOffset(revertNote.getTickOffset());
			noteEvent.setTick(revertNote.getTick());
		}
		detachedNote.clear();
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
		selectedNote.forEach(note -> editEventList.deleteMMLEvent(note));
		selectedNote.forEach(note -> editEventList.addMMLNoteEvent(note));
		if (!select) {
			selectNote(null);
		}
		notePlayer.offNote();
		mmlManager.generateActiveTrack();
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
		int y2 = Math.min(point.y, pianoRollView.getTotalHeight());
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
				new MMLNoteEvent(note2, 0, tickOffset2, 0), true, true);
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
		return pointToNote(point) != null;
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
			return noteEvent.getEndTick() <= tickOffset + (noteEvent.getTick() / 5);
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
		if (e.getY() > pianoRollView.getTotalHeight()) {
			return;
		}
		editMode.pressEvent(this, e);
		pianoRollView.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		editMode.releaseEvent(this, e);
		pianoRollView.setPaintNoteInfo(null);
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
	public boolean isSelectedNote(MMLNoteEvent event) {
		return selectedNote.contains(event);
	}

	/** 連続した複数のノートが選択されているかどうかを判定する */
	@Override
	public boolean hasSelectedMultipleConsecutiveNotes() {
		if (selectedNote.size() >= 2) {
			List<MMLNoteEvent> activePart = mmlManager.getActiveMMLPart().getMMLNoteEventList();
			int index = 0;
			for (int i = 0; i < selectedNote.size()-1; i++) {
				for ( ; (index < activePart.size()-1) && (activePart.get(index) != selectedNote.get(i)); index++ );
				if ((index < activePart.size()-1) && (activePart.get(index+1).equals(selectedNote.get(i+1)))) {
					return true;
				}
			}
		}

		return false;
	}

	/** 音符間の休符を削除する */
	@Override
	public void removeRestsBetweenNotes() {
		List<MMLNoteEvent> activePart = mmlManager.getActiveMMLPart().getMMLNoteEventList();
		int index = 0;
		for (int i = 0; i < selectedNote.size()-1; i++) {
			for ( ; (index < activePart.size()-1) && (activePart.get(index) != selectedNote.get(i)); index++ );
			if ((index < activePart.size()-1) && (activePart.get(index+1).equals(selectedNote.get(i+1)))) {
				MMLNoteEvent note = activePart.get(index);
				note.setTick(activePart.get(index+1).getTickOffset()-note.getTickOffset());
			}
		}

		selectNote(null);
		editObserver.notifyUpdateEditState();
		mmlManager.generateActiveTrack();
	}

	/**
	 * 選択されたノートを指定数上下させる
	 * @param value
	 */
	private void selectedNoteUpDown(int value) {
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
		for (MMLNoteEvent noteEvent : selectedNote) {
			var note = noteEvent.getNote() + value;
			if ( (note < -1) || (note >= 108) ) {
				// 移動後に1つでも範囲外となる場合は処理しない.
				return;
			}
		}
		selectedNote.forEach(t -> t.setNote(t.getNote() + value));
		mmlManager.updateActivePart(true);
	}

	@Override
	public void octaveUp() {
		selectedNoteUpDown(12);
	}

	@Override
	public void octaveDown() {
		selectedNoteUpDown(-12);
	}

	@Override
	public void setEditStateObserver(IEditStateObserver observer) {
		this.editObserver = observer;
	}

	/**
	 * クリップボードからMMLデータを取得する
	 */
	private MMLEventList fromClipBoard() {
		MMLEventList clipEventList = null;
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			String str = (String) clipboard.getData(DataFlavor.stringFlavor);
			if ( (str != null) && str.startsWith(MMLEVENT_PREFIX)) {
				clipEventList = new MMLEventList(str.substring(MMLEVENT_PREFIX.length()));
			}
		} catch (UnsupportedFlavorException | IOException e) {}

		return clipEventList;
	}

	/**
	 * クリップボードへMMLデータを設定する
	 * @param eventList     設定するMMLデータ.
	 */
	private void toClipBoard(MMLEventList eventList) {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection selection = new StringSelection(MMLEVENT_PREFIX + eventList.getInternalMMLString());
			clipboard.setContents(selection, selection);
		} catch (MMLExceptionList e) {}
	}

	@Override 
	public boolean canPaste() {
		MMLEventList clipEventList = fromClipBoard();
		if (clipEventList == null) {
			return false;
		}
		return clipEventList.getMMLNoteEventList().size() != 0;
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
		MMLEventList clipEventList = fromClipBoard();
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
		MMLEventList clipEventList = new MMLEventList("");
		for (MMLNoteEvent noteEvent : selectedNote) {
			clipEventList.addMMLNoteEvent(noteEvent);
			editEventList.deleteMMLEvent(noteEvent);
		}

		toClipBoard(clipEventList);
		selectNote(null);
		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart(true);
	}

	@Override
	public void selectedCopy() {
		MMLEventList clipEventList = new MMLEventList("");
		for (MMLNoteEvent noteEvent : selectedNote) {
			clipEventList.addMMLNoteEvent(noteEvent);
		}

		toClipBoard(clipEventList);
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
			mmlManager.generateActiveTrack();
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
			selectedNote.clear();
			for (MMLNoteEvent note : editEventList.getMMLNoteEventList()) {
				if (note.getTickOffset() <= popupTargetNote.getTickOffset()) {
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
			selectedNote.clear();
			for (MMLNoteEvent note : editEventList.getMMLNoteEventList()) {
				if (note.getTickOffset() >= popupTargetNote.getTickOffset()) {
					selectedNote.add(note);
				}
			}
		}
	}

	@Override
	public void selectAllSamePitch() {
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList != null) {
			selectedNote.clear();
			for (MMLNoteEvent note : editEventList.getMMLNoteEventList()) {
				if (note.getNote() == popupTargetNote.getNote()) {
					selectedNote.add(note);
				}
			}
		}
	}

	@Override
	public void setTempMute(boolean mute) {
		for (MMLNoteEvent noteEvent : selectedNote) {
			noteEvent.setMute(mute);
		}
		pianoRollView.repaint();
	}

	@Override
	public void setTempMuteAll() {
		for (MMLTrack track : mmlManager.getMMLScore().getTrackList()) {
			for (var eventList : track.getMMLEventList()) {
				for (var notes : eventList.getMMLNoteEventList()) {
					notes.setMute(false);
				}
			}
		}
		pianoRollView.repaint();
	}

	@Override
	public void convertTuplet() {
		MMLEventList editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return;
		}
		int size = selectedNote.size();
		if (size < 3) {
			return;
		}

		selectedNote.sort(Comparator.comparingInt(t -> t.getTickOffset()));
		int startTick = selectedNote.get(0).getTickOffset();
		int tick = selectedNote.get(size-1).getEndTick() - startTick;
		for (MMLNoteEvent noteEvent : selectedNote) {
			editEventList.deleteMMLEvent(noteEvent);
		}
		for (int i = 0; i < size; i++) {
			var noteEvent = selectedNote.get(i);
			int t1 = tick * i / size;
			int t2 = tick * (i+1) / size;
			noteEvent.setTickOffset(t1+startTick);
			noteEvent.setTick(t2-t1);
			editEventList.addMMLNoteEvent(noteEvent);
		}

		selectNote(null);
		editObserver.notifyUpdateEditState();
		mmlManager.updateActivePart(true);
	}

	public record TickRange(int start, int end) {}
	public TickRange selectedRange(MMLEventList from, MMLEventList to) {
		if (selectedNote.size() == 0) {
			return null;
		}
		MMLNoteEvent startNote = selectedNote.stream().min(Comparator.comparingInt(t -> t.getTickOffset())).get();
		MMLNoteEvent endNote = selectedNote.stream().max(Comparator.comparingInt(t -> t.getEndTick())).get();
		int startTick = from.getAlignmentStartTick(to, startNote.getTickOffset());
		int endTick = from.getAlignmentEndTick(to, endNote.getEndTick());
		return new TickRange(startTick, endTick);
	}

	public void changePart(MMLEventList from, MMLEventList to, boolean useSelectedNoteList, ChangePartAction action) {
		int startTick = 0;
		int endTick;
		if (useSelectedNoteList && (selectedNote.size() > 0) ) {
			var range = selectedRange(from, to);
			startTick = range.start();
			endTick = range.end();
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
		velocityChangeMenu.setValue(popupTargetNote.getVelocity());
		createTupletMenu.setEnabled(selectedNote.size() >= 3);

		if (hasSelectedNote()) {
			try {
				popupMenu.show(pianoRollView, point.x, point.y);
			} catch (IllegalComponentStateException e) {}
		}
	}

	/**
	 * 指定したポイントがスタート位置よりあとかどうかを判定する
	 */
	@Override
	public boolean canEditStartOffset(Point point) {
		int startOffset = mmlManager.getActiveMMLPartStartOffset();
		int tickOffset = (int)pianoRollView.convertXtoTick( point.x );
		return (startOffset <= tickOffset);
	}

	/**
	 * ノート音量の変更
	 * 選択されたノートの音量を変更する. 選択されたノートがなければ、ポイント上のノートの音量を変更する.
	 * @param point
	 * @param inc
	 */
	public void notesModifyVelocity(Point point, boolean inc) {
		MMLNoteEvent noteEvent = pointToNote(point);
		if (!selectedNote.isEmpty()) {
			selectedNote.forEach(t -> t.modifyVelocity(inc));
			mmlManager.updateActivePart(true);
		} else if (noteEvent != null) {
			noteEvent.modifyVelocity(inc);
			mmlManager.updateActivePart(true);
		}
	}

	@Override
	public void glueAction(Point p) {
		editorAction.glueAction(p);
	}

	@Override
	public void splitAction(Point p) {
		editorAction.splitAction(p);
	}
}

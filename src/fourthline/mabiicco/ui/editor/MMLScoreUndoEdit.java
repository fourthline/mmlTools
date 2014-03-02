/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.util.Stack;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fourthline.mabiicco.IFileState;
import fourthline.mabiicco.IFileStateObserver;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLScore;

public class MMLScoreUndoEdit extends AbstractUndoableEdit implements IFileState {
	private static final long serialVersionUID = 4093930608712571204L;

	private IFileStateObserver fileStateObserver = null;

	private static final int MAX_UNDO = 20;
	private Stack<byte[]> undoState = new Stack<byte[]>();
	private Stack<byte[]> redoState = new Stack<byte[]>();

	private IMMLManager mmlManager;
	private int originalIndex = 1; /** undo/redo範囲外になった場合は 0. 初期位置は 1 */

	public MMLScoreUndoEdit(IMMLManager mmlManager) {
		this.mmlManager = mmlManager;
	}

	public void initState() {
		undoState.clear();
		redoState.clear();
		originalIndex = 1;

		saveState();
	}

	public void saveState() {
		MMLScore score = mmlManager.getMMLScore();
		undoState.push( score.getObjectState() );
		redoState.clear();

		if (undoState.size() > MAX_UNDO) {
			undoState.remove(0);
			originalIndex = 0;
		}

		if (fileStateObserver != null)
			fileStateObserver.notifyUpdateFileState();

		System.out.println("saveState() "+undoState.size());
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();

		MMLScore score = mmlManager.getMMLScore();
		if (canUndo()) {
			byte nextState[] = undoState.pop();;
			score.putObjectState(undoState.lastElement());
			redoState.push(nextState);
			if (fileStateObserver != null)
				fileStateObserver.notifyUpdateFileState();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();

		MMLScore score = mmlManager.getMMLScore();
		if (canRedo()) {
			byte state[] = redoState.pop();
			score.putObjectState(state);
			undoState.push(state);
			if (fileStateObserver != null)
				fileStateObserver.notifyUpdateFileState();
		}
	}

	@Override
	public boolean canUndo() {
		if (undoState.size() > 1) {
			return true;
		}

		return false;
	}

	@Override
	public boolean canRedo() {
		return (!redoState.empty());
	}

	@Override
	public boolean isModified() {
		if ( originalIndex != (undoState.size()) ) {
			return true;
		}

		return false;
	}

	@Override
	public void setOriginalBase() {
		if (originalIndex > 0) {
			originalIndex = undoState.size();
		}
	}

	@Override
	public void setFileStateObserver(IFileStateObserver observer) {
		this.fileStateObserver = observer;
	}
}

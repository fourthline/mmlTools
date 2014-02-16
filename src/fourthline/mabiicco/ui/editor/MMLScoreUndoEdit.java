/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.util.Stack;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLScore;

public class MMLScoreUndoEdit extends AbstractUndoableEdit {
	private static final long serialVersionUID = 4093930608712571204L;

	private static final int MAX_UNDO = 20;
	private Stack<byte[]> undoState = new Stack<byte[]>();
	private Stack<byte[]> redoState = new Stack<byte[]>();

	private IMMLManager mmlManager;

	public MMLScoreUndoEdit(IMMLManager mmlManager) {
		this.mmlManager = mmlManager;
	}

	public void initState() {
		undoState.clear();
		redoState.clear();

		saveState();
	}

	public void saveState() {
		MMLScore score = mmlManager.getMMLScore();
		undoState.push( score.getObjectState() );
		redoState.clear();

		if (undoState.size() > MAX_UNDO) {
			undoState.remove(0);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();

		MMLScore score = mmlManager.getMMLScore();
		if (canUndo()) {
			byte nextState[] = undoState.pop();;
			score.putObjectState(undoState.lastElement());
			redoState.push(nextState);
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
}

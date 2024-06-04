/*
 * Copyright (C) 2014-2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Stack;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import jp.fourthline.mabiicco.IFileState;
import jp.fourthline.mabiicco.IFileStateObserver;
import jp.fourthline.mabiicco.Utils;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mmlTools.MMLScore;

public final class MMLScoreUndoEdit extends AbstractUndoableEdit implements IFileState {
	private static final long serialVersionUID = 4093930608712571204L;

	private IFileStateObserver fileStateObserver = null;

	private static final int MAX_UNDO = 20;
	private final Stack<byte[]> undoState = new Stack<>();
	private final Stack<byte[]> redoState = new Stack<>();

	private final IMMLManager mmlManager;
	private int originalIndex = 0; /** オリジナル位置. undo/redo範囲外になった場合は 負値. 0~size-1 */

	public MMLScoreUndoEdit(IMMLManager mmlManager) {
		this.mmlManager = mmlManager;
	}

	public void initState() {
		undoState.clear();
		redoState.clear();
		originalIndex = 0;

		saveState();
	}

	@Override
	public void saveState() {
		MMLScore score = mmlManager.getMMLScore();
		byte[] state = score.getObjectState();
		if ( !undoState.empty() && Arrays.equals(state, undoState.lastElement()) ) {
			return;
		}

		undoState.push(state);
		redoState.clear();

		if (undoState.size() > MAX_UNDO) {
			undoState.remove(0);
			originalIndex = -1;
		}

		if (fileStateObserver != null)
			fileStateObserver.notifyUpdateFileState();

		System.out.println("saveState() "+undoState.size());
	}

	@Override
	public void revertState() {
		MMLScore score = mmlManager.getMMLScore();
		score.putObjectState(undoState.lastElement());
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();

		MMLScore score = mmlManager.getMMLScore();
		if (canUndo()) {
			byte[] nextState = undoState.pop();
			score.putObjectState(undoState.lastElement());
			redoState.push(nextState);
			makeBackup();
			if (fileStateObserver != null)
				fileStateObserver.notifyUpdateFileState();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();

		MMLScore score = mmlManager.getMMLScore();
		if (canRedo()) {
			byte[] state = redoState.pop();
			score.putObjectState(state);
			undoState.push(state);
			makeBackup();
			if (fileStateObserver != null)
				fileStateObserver.notifyUpdateFileState();
		}
	}

	@Override
	public boolean canUndo() {
		return undoState.size() > 1;
	}

	@Override
	public boolean canRedo() {
		return (!redoState.empty());
	}

	@Override
	public boolean isModified() {
		return originalIndex != (undoState.size() - 1);
	}

	@Override
	public void setOriginalBase() {
		originalIndex = undoState.size() - 1;
	}

	@Override
	public void setFileStateObserver(IFileStateObserver observer) {
		this.fileStateObserver = observer;
	}

	private String makeBackup() {
		String str = null;
		try {
			str = Utils.compress(makeBackupString());
		} catch (IOException e) {
			str = null;
		}
		return str;
	}

	private void writeStack(PrintStream out, Stack<byte[]> data) throws IOException {
		out.println(data.size());
		for (byte[] datum : data) {
			out.println(Base64.getEncoder().encodeToString(datum));
		}
	}

	private void readStack(BufferedReader in, Stack<byte[]> data) throws IOException {
		data.clear();
		int count = Integer.parseInt(in.readLine());
		for (int i = 0; i < count; i++) {
			data.add( Base64.getDecoder().decode( in.readLine() ));
		}
	}

	private String makeBackupString() throws IOException {
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		PrintStream pstream = new PrintStream(bstream, false, StandardCharsets.UTF_8);
		pstream.println(serialVersionUID);

		// undoState@Stack<byte[]>
		writeStack(pstream, undoState);

		// redoState@Stack<byte[]>
		writeStack(pstream, redoState);

		// originalIndex@int
		pstream.println(originalIndex);

		pstream.close();
		return bstream.toString();
	}

	private boolean parseBackupString(String s) throws IOException, NumberFormatException {
		BufferedReader breader = new BufferedReader(new StringReader(s));
		long serial = Long.parseLong( breader.readLine() );
		if (serial != serialVersionUID) {
			return false;
		}

		// undoState@Stack<byte[]>
		readStack(breader, undoState);

		// redoState@Stack<byte[]>
		readStack(breader, redoState);

		// originalIndex@int
		originalIndex = Integer.parseInt( breader.readLine() );

		return true;
	}

	public boolean recover(String s) {
		try {
			var data = Utils.decompress(s);
			if (data == null) {
				return false;
			}
			boolean result = parseBackupString(new String(data));
			makeBackup();
			return result;
		} catch (NumberFormatException | IOException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	public String getBackupString() {
		return makeBackup();
	}
}

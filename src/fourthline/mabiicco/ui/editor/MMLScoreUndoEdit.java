/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.Stack;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fourthline.mabiicco.IFileState;
import fourthline.mabiicco.IFileStateObserver;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLScore;

public final class MMLScoreUndoEdit extends AbstractUndoableEdit implements IFileState {
	private static final long serialVersionUID = 4093930608712571204L;

	private IFileStateObserver fileStateObserver = null;

	private static final int MAX_UNDO = 20;
	private final Stack<byte[]> undoState = new Stack<>();
	private final Stack<byte[]> redoState = new Stack<>();

	private final IMMLManager mmlManager;
	private int originalIndex = 0; /** オリジナル位置. undo/redo範囲外になった場合は 負値. 0~size-1 */

	private String backupString = null;

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
		byte state[] = score.getObjectState();
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
		makeBackup();
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
			byte nextState[] = undoState.pop();
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
			byte state[] = redoState.pop();
			score.putObjectState(state);
			undoState.push(state);
			makeBackup();
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
		if ( originalIndex == (undoState.size()-1) ) {
			return false;
		}

		return true;
	}

	@Override
	public void setOriginalBase() {
		originalIndex = undoState.size() - 1;
	}

	@Override
	public void setFileStateObserver(IFileStateObserver observer) {
		this.fileStateObserver = observer;
	}

	private void makeBackup() {
		try {
			backupString = compress(makeBackupString());
		} catch (IOException e) {
			backupString = null;
		}
	}

	private void writeStack(PrintStream out, Stack<byte[]> data) throws IOException {
		out.println(data.size());
		for (int i = 0; i < data.size(); i++) {
			out.println( Base64.getEncoder().encodeToString( data.get(i) ));
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
		PrintStream pstream = new PrintStream(bstream, false, "UTF-8");
		pstream.println(serialVersionUID);

		// undoState@Stack<byte[]>
		writeStack(pstream, undoState);

		// redoState@Stack<byte[]>
		writeStack(pstream, redoState);

		// originalIndex@int
		pstream.println(originalIndex);

		pstream.close();
		return new String( bstream.toByteArray() );
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

	private String compress(String s) {
		try {
			ByteArrayOutputStream bstream = new ByteArrayOutputStream();
			GZIPOutputStream out = new GZIPOutputStream( bstream );
			out.write(s.getBytes());
			out.close();
			bstream.close();
			return Base64.getEncoder().encodeToString(bstream.toByteArray());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	private String decompress(String s) {
		try {
			GZIPInputStream in = new GZIPInputStream(
					new ByteArrayInputStream(
							Base64.getDecoder().decode(s.getBytes())));
			ByteArrayOutputStream bstream = new ByteArrayOutputStream();
			while (in.available() != 0) {
				int c = in.read();
				if (c >= 0) {
					bstream.write(c);
				}
			}
			bstream.close();
			return new String(bstream.toByteArray());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public boolean recover(String s) {
		try {
			boolean result = parseBackupString(decompress(s));
			makeBackup();
			return result;
		} catch (NumberFormatException | IOException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	public String getBackupString() {
		return backupString;
	}
}

/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Point;
import java.util.List;
import java.util.function.LongFunction;

import jp.fourthline.mabiicco.midi.IPlayNote;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.PianoRollView;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.core.MMLTicks;

public final class EditorAction {
	private final MMLEditor mmlEditor;
	private final IMMLManager mmlManager;
	private final PianoRollView pianoRollView;
	private final IPlayNote notePlayer;
	private final LongFunction<Long> tickAlignFunc;

	private int note;
	private long tickOffset;
	private long alignedTickOffset;
	private MMLEventList editEventList;

	public EditorAction(MMLEditor mmlEditor, IMMLManager mmlManager, PianoRollView pianoRollView, IPlayNote notePlayer, LongFunction<Long> tickAlignFunc) {
		this.mmlEditor = mmlEditor;
		this.mmlManager = mmlManager;
		this.pianoRollView = pianoRollView;
		this.notePlayer = notePlayer;
		this.tickAlignFunc = tickAlignFunc;
	}

	private boolean prepare(Point p) {
		note = pianoRollView.convertY2Note(p.y);
		tickOffset = pianoRollView.convertXtoTick(p.x);
		alignedTickOffset = tickAlignFunc.apply(tickOffset);
		editEventList = mmlManager.getActiveMMLPart();
		if (editEventList == null) {
			return false;
		}
		return true;
	}

	public void newNoteAction(Point p) {
		if (prepare(p)) {
			MMLNoteEvent prevNote = editEventList.searchPrevNoteOnTickOffset(tickOffset);
			MMLNoteEvent noteEvent = new MMLNoteEvent(note, mmlEditor.getEditAlign(), (int)alignedTickOffset);
			if (prevNote != null) {
				noteEvent.setVelocity(prevNote.getVelocity());
			}
			mmlEditor.selectNote(noteEvent);
			notePlayer.playNote( note, noteEvent.getVelocity() );
		}
	}

	public void splitAction(Point p) {
		// 中間位置でとる
		p.x += pianoRollView.convertTicktoX(mmlEditor.getEditAlign()) >> 1;
		if (prepare(p)) {
			MMLNoteEvent prevNote = editEventList.searchOnTickOffset(tickOffset);
			if ((prevNote != null) && (prevNote.getNote() == note)) {
				int tick = prevNote.getEndTick() - (int)alignedTickOffset;
				MMLNoteEvent noteEvent = new MMLNoteEvent(note, tick, (int)alignedTickOffset);
				noteEvent.setVelocity(prevNote.getVelocity());
				mmlEditor.selectNote(noteEvent);
				notePlayer.playNote( note, noteEvent.getVelocity() );
			}
		}
	}

	public void glueAction(Point p) {
		if (prepare(p)) {
			MMLNoteEvent prevNote = editEventList.searchOnTickOffset(tickOffset);
			MMLNoteEvent nextNote = editEventList.searchOnTickOffsetNextNote(tickOffset);
			if ((prevNote != null) && (nextNote != null) && (prevNote.getNote() == nextNote.getNote()) && (note == nextNote.getNote())) {
				int tick = nextNote.getEndTick() - prevNote.getTickOffset();
				MMLNoteEvent noteEvent = new MMLNoteEvent(note, tick, prevNote.getTickOffset());
				noteEvent.setVelocity(prevNote.getVelocity());
				mmlEditor.selectNote(noteEvent);
				notePlayer.playNote( note, noteEvent.getVelocity() );
			}
		}
	}

	/**
	 * 複数ノートの長さ編集
	 * @param start
	 * @param p
	 * @param alignment
	 * @param selectedNote
	 * @param detachedNote
	 */
	public void editLengthAction(Point start, Point p, boolean alignment, List<MMLNoteEvent> selectedNote, List<MMLNoteEvent> detachedNote) {
		if (prepare(p)) {
			pianoRollView.onViewScrollPoint(p);
			long startTick = pianoRollView.convertXtoTick(start.x);
			long tickOffsetDelta = tickOffset - startTick;

			for (int i = 0; i < selectedNote.size(); i++) {
				MMLNoteEvent note1 = detachedNote.get(i);
				MMLNoteEvent note2 = selectedNote.get(i);
				long newTickOffset = note1.getEndTick() + tickOffsetDelta;
				if (alignment) {
					newTickOffset = tickAlignFunc.apply(newTickOffset);
				}
				int newTick = (int)newTickOffset - note1.getTickOffset();
				// 最小Tick
				newTick = Math.max(newTick, MMLTicks.minimumTick());
				// 後続のTickOffsetを超えない
				var nextNote = editEventList.searchOnTickOffsetNextNote(note1.getTickOffset());
				if (nextNote != null) {
					newTick = Math.min(newTick, nextNote.getTickOffset() - note1.getTickOffset());
				}
				note2.setTick(newTick);
				if ( (note1.getTickOffset() <= startTick) && (note1.getEndTick() > startTick) ) {
					// ノート情報表示
					pianoRollView.setPaintNoteInfo(!alignment ? note2 : null);
					notePlayer.playNote(note1.getNote(), note1.getVelocity());
				}
			}
		}
	}
}

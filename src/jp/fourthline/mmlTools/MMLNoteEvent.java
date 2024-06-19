/*
 * Copyright (C) 2013-2023 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.Objects;

import jp.fourthline.mmlTools.core.IllegalTickOffsetException;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.TuningBase;
import jp.fourthline.mmlTools.core.MMLException;

public final class MMLNoteEvent extends MMLEvent implements Cloneable {
	private static final long serialVersionUID = 4372538748155995529L;

	public static final int INIT_VOL = 8;
	public static final int MAX_VOL = 15; 
	private int note;
	private int tick;
	private TuningBase tuningBase = null;
	private int velocity;
	private int[] indexOfMMLString = null; // { startIndex, endIndex }

	private boolean mute = false; // 一時ミュート

	public MMLNoteEvent(int note, int tickLength, int tickOffset) {
		this(note, tickLength, tickOffset, INIT_VOL);
	}

	public MMLNoteEvent(int note, int tickLength, int tickOffset, int velocity) {
		super(tickOffset);
		if (tickOffset + tickLength >= MMLEvent.MAX_TICK) {
			throw new IllegalTickOffsetException(tickOffset + tickLength);
		}
		if ( (velocity < 0) || (velocity > MAX_VOL) ) {
			throw new IllegalArgumentException("velocity  "+velocity);
		}

		this.note = note;
		this.tick = tickLength;
		this.velocity = velocity;
	}

	public int getNote() {
		return this.note;
	}

	public void setNote(int note) {
		this.note = note;
	}

	public int getTick() {
		return this.tick;
	}

	public int getEndTick() {
		return getTickOffset() + getTick();
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public boolean isTuningNote() {
		return (tuningBase != null);
	}

	public TuningBase getTuningBase() {
		return tuningBase;
	}

	public void setTuningNote(TuningBase base) {
		this.tuningBase = base;
	}

	public int getVelocity() {
		return velocity;
	}

	public void setVelocity(int velocity) {
		if (velocity < 0) {
			velocity = 0;
		} else if (velocity > MAX_VOL) {
			velocity = MAX_VOL;
		}
		this.velocity = velocity;
	}

	public void modifyVelocity(boolean inc) {
		setVelocity(velocity + (inc ? 1 : -1));
	}

	public int[] getIndexOfMMLString() {
		return indexOfMMLString;
	}

	public void setIndexOfMMLString(int[] index) {
		this.indexOfMMLString = index;
	}

	public boolean isMute() {
		return mute;
	}

	public void setMute(boolean mute) {
		this.mute = mute;
	}

	@Override
	public String toString() {
		return "[Note] note: " + note + ", tick: " + tick + ", offset: " + getTickOffset() + ", velocity: " + velocity;
	}

	private static final String[] noteNameTable = {
			"c", "c+", "d", "d+", "e", "f", "f+", "g", "g+", "a", "a+", "b"
	};
	private String getNoteName() throws MMLException {
		if (note == -1) {
			return "c-";
		}
		return noteNameTable[ note%noteNameTable.length ];
	}

	@Override
	public String toMMLString() throws MMLException {
		return toMMLString(0);
	}

	private String toMMLString(int prevEndTickOffset) throws MMLException {
		if ( (note < -1) || (note >= 108) ) {
			throw MMLException.createIllegalNote(note);
		}
		String noteName = getNoteName();
		int actTick = tick;
		if (getTickOffset() < prevEndTickOffset) {
			actTick -= prevEndTickOffset - getTickOffset();
		}
		MMLTicks mmlTick = new MMLTicks(noteName, actTick);
		if (tuningBase != null) {
			return mmlTick.toMMLTextByBase(tuningBase);
		} else {
			return mmlTick.toMMLText();
		}
	}

	public String toMMLString(MMLNoteEvent prevNoteEvent) throws MMLException {
		if ( (note < -1) || (note >= 108) ) {
			throw MMLException.createIllegalNote(note);
		}
		StringBuilder sb = new StringBuilder();

		// 前のノートとの差を見て、休符を挿入する.
		sb.append( createMMLSpaceString(prevNoteEvent) );

		// 前のノートとのオクターブ差分をみて、オクターブ変化を挿入する.
		sb.append( changeOctaveinMMLString(prevNoteEvent.getOctave()) );

		// 前のノートとの音量が違う場合、音量コマンドを挿入する.
		if (prevNoteEvent.getVelocity() != velocity) {
			sb.append("v").append(velocity);
		}

		sb.append( toMMLString(prevNoteEvent.getEndTick()) );

		return sb.toString();
	}

	/**
	 * 前のNoteEvent間にある休符のMML文字列を生成します.
	 * @param prevNoteEvent
	 * @return
	 */
	private String createMMLSpaceString(MMLNoteEvent prevNoteEvent) throws MMLException {
		int noteSpaceTick = getTickOffset() - prevNoteEvent.getEndTick();
		if ( noteSpaceTick > 0 ) {
			MMLTicks mmlTick = new MMLTicks("r", noteSpaceTick, false);
			return mmlTick.toMMLText();
		}

		return "";
	}

	/**
	 * ノートの高さにあわせて、MMLテキスト上でのオクターブ変更を行います.
	 * @param prevOctave
	 * @return
	 */
	public String changeOctaveinMMLString(int prevOctave) {
		String s = "";
		final String increaseOctave = ">>>>>>>>";
		final String decreaseOctave = "<<<<<<<<";
		int changeOctave = prevOctave - getOctave();
		if (changeOctave > 0) {
			s = decreaseOctave.substring(0, changeOctave);
		} else if (changeOctave < 0) {
			s = increaseOctave.substring(0, -changeOctave);
		}

		return s;
	}

	public int getOctave() {
		if (note < 0) {
			return 0;
		}
		return (note /12);
	}

	@Override
	public MMLNoteEvent clone() {
		try {
			return (MMLNoteEvent) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e.getMessage());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MMLNoteEvent)) {
			return false;
		}

		MMLNoteEvent noteEvent = (MMLNoteEvent) obj;
		return (this.tick == noteEvent.tick) &&
				(this.note == noteEvent.note) &&
				(this.velocity == noteEvent.velocity) &&
				(super.equals(noteEvent)) &&
				(Objects.equals(this.tuningBase, noteEvent.tuningBase));
	}

	public static String keyToAbsNote(int key) {
		try {
			var note = new MMLNoteEvent(key, 0, 0);
			var oct = note.getOctave();
			var name = note.getNoteName();
			return "O" + oct + name;
		} catch (MMLException e) {
			e.printStackTrace();
		}
		return "";
	}
}

/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import fourthline.mmlTools.core.MMLTicks;

public class MMLNoteEvent extends MMLEvent {

	public static final int NO_VEL = -1;
	private int note;
	private int tick;
	private boolean isTuningNote = false;
	private int velocity = NO_VEL; // 0以上であれば、このノートから音量を変更する.

	public MMLNoteEvent(int note, int tickLength, int tickOffset) {
		super(tickOffset);

		this.note = note;
		this.tick = tickLength;
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
		return isTuningNote;
	}

	public void setTuningNote(boolean isTuningNote) {
		this.isTuningNote = isTuningNote;
	}

	public int getVelocity() {
		return velocity;
	}

	public String getVelocityString() {
		return "v" + velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	@Override
	public String toString() {
		return "[Note] note: " + note + ", tick: " + tick + ", offset: " + getTickOffset() + ", velocity: " + velocity;
	}

	private final String noteNameTable[] = {
			"c", "c+", "d", "d+", "e", "f", "f+", "g", "g+", "a", "a+", "b"
	};
	private String getNoteName() {
		return noteNameTable[ note%noteNameTable.length ];
	}

	@Override
	public String toMMLString() {
		String noteName = getNoteName();
		MMLTicks mmlTick = new MMLTicks(noteName, tick);
		if (isTuningNote) {
			return mmlTick.toStringByL64();
		} else {
			return mmlTick.toString();
		}
	}

	public String toMMLString(MMLNoteEvent prevNoteEvent) {
		StringBuilder sb = new StringBuilder();

		// 前のノートとの差を見て、休符を挿入する.
		sb.append( createMMLSpaceString(prevNoteEvent) );

		// 前のノートとのオクターブ差分をみて、オクターブ変化を挿入する.
		sb.append( changeOctaveinMMLString(prevNoteEvent.getOctave()) );

		sb.append( toMMLString() );

		return sb.toString();
	}

	/**
	 * 前のNoteEvent間にある休符のMML文字列を生成します.
	 * @param prevNoteEvent
	 * @return
	 */
	private String createMMLSpaceString(MMLNoteEvent prevNoteEvent) {
		int noteSpaceTick = getTickOffset() - prevNoteEvent.getEndTick();
		if ( noteSpaceTick > 0 ) {
			MMLTicks mmlTick = new MMLTicks("r", noteSpaceTick, false);
			return mmlTick.toString();
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
		return (note /12);
	}
}

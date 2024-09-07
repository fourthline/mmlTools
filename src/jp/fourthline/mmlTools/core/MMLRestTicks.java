/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mmlTools.core;

import jp.fourthline.mmlTools.MMLNoteEvent;

/**
 * 打楽器モーション補正の休符作成用
 */
public class MMLRestTicks extends MMLTicks {

	private static final String REST = "r";
	private static final String ALT = "c";
	private static final double DIV = 4294967295.0;

	private final MMLNoteEvent prevNoteEvent;
	private boolean replaced = false;
	private boolean lastReplaced = false;


	public MMLRestTicks(int tick) {
		this(tick, null);
	}

	public MMLRestTicks(int tick, MMLNoteEvent prevNoteEvent) {
		super(REST, tick, false);
		this.prevNoteEvent = prevNoteEvent;
	}

	int calcDivTick(int tempo) {
		return (int) Math.ceil( DIV * tempo / 60000000.0 );
	}

	private void vZero(StringBuilder sb) {
		int index = sb.lastIndexOf(REST);
		sb.replace(index, index+1, ALT);
		if (prevNoteEvent != null) {
			if (prevNoteEvent.getVelocity() > 0) {
				sb.insert(index, "v0");
				prevNoteEvent.setVelocity(0);
			}
		}
	}

	public MMLNoteEvent getPrevNoteEvent() {
		return prevNoteEvent;
	}

	/**
	 * 打楽器モーション補正ありで休符のMML文字列を作成する.
	 * @param tempo
	 * @return
	 * @throws MMLException
	 */
	public String toMMLTextWithMotionFix(int tempo) throws MMLException {
		int remTick = tick;
		StringBuilder sb = new StringBuilder();

		int tickCount = 0;
		int divTick = calcDivTick(tempo);

		// "1."
		int mTick = getTick("1.");
		int tick1 = getTick("1");
		while (remTick > (tick1*2)) {
			sb.append( mmlNotePart("1.") );

			tickCount += mTick;
			if (tickCount >= divTick) {
				tickCount = mTick;
				vZero(sb);
				replaced = true;
			}
			remTick -= mTick;
		}

		tickCount += remTick;
		String str = makeMMLText(sb, remTick);
		if (tickCount >= divTick) {
			sb = new StringBuilder( str );
			vZero(sb);
			str = sb.toString();
			replaced = lastReplaced = true;
		}
		return str;
	}

	/**
	 * V0置換を行ったかどうか
	 */
	public boolean isReplaced() {
		return replaced;
	}

	/**
	 * 最後の文字に対してV0置換を行ったかどうか。次がテンポ指定ならテンポでのV0置換不要。
	 */
	public boolean isLastReplaced() {
		return lastReplaced;
	}
}

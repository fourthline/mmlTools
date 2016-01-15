/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fourthline.mmlTools.core.UndefinedTickException;


/**
 * MMLEvent
 */
public abstract class MMLEvent implements Serializable {
	private static final long serialVersionUID = -6142467143073639266L;

	// イベントの開始オフセット
	private int tickOffset;

	protected MMLEvent(int tickOffset) {
		this.tickOffset = tickOffset;
	}

	public void setTickOffset(int tickOffset) {
		this.tickOffset = tickOffset;
	}

	public int getTickOffset() {
		return this.tickOffset;
	}

	public abstract String toString();

	public abstract String toMMLString() throws UndefinedTickException;


	/**
	 * tick長の空白を挿入します.
	 * @param list 空白を挿入するMMLEventリスト. MMLEventList以外のList構造も可.
	 * @param startTick 挿入する位置
	 * @param tick 挿入するtick長
	 */
	public static void insertTick(List<? extends MMLEvent> list, int startTick, int tick) {
		for (MMLEvent event : list) {
			int noteTick = event.getTickOffset();
			if (noteTick >= startTick) {
				event.setTickOffset(noteTick + tick);
			}
		}
	}

	/**
	 * tick長の部分を削除して詰めます.
	 * @param list 削除するMMLEventリスト. MMLEventList以外のList構造も可.
	 * @param startTick 削除する位置
	 * @param tick 削除するtick長
	 */
	public static void removeTick(List<? extends MMLEvent> list, int startTick, int tick) {
		ArrayList<MMLEvent> deleteEvent = new ArrayList<>();
		for (MMLEvent event : list) {
			int eventTick = event.getTickOffset();
			if (eventTick >= startTick) {
				if (eventTick < startTick+tick) {
					// 削除リストに加えておく.
					deleteEvent.add(event);
				} else {
					event.setTickOffset(eventTick - tick);
				}
			}
		}

		for (MMLEvent event : deleteEvent) {
			list.remove(event);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MMLEvent)) {
			return false;
		}

		MMLEvent event = (MMLEvent) obj;
		if (this.tickOffset == event.tickOffset) {
			return true;
		}
		return false;
	}
}

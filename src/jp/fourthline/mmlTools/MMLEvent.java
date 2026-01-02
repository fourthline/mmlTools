/*
 * Copyright (C) 2013-2026 たんらる
 */

package jp.fourthline.mmlTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.fourthline.mmlTools.core.IllegalTickOffsetException;
import jp.fourthline.mmlTools.core.MMLException;


/**
 * MMLEvent
 */
public abstract class MMLEvent implements Serializable {
	private static final long serialVersionUID = -6142467143073639266L;

	public static final int MAX_TICK = 3840000;

	// イベントの開始オフセット
	private int tickOffset;

	protected MMLEvent(int tickOffset) {
		if ( (tickOffset <= -MAX_TICK) || (tickOffset >= MAX_TICK)) {
			// 負数側はdelayで使用している範囲もある.
			throw new IllegalTickOffsetException(tickOffset);
		}
		this.tickOffset = tickOffset;
	}

	public void setTickOffset(int tickOffset) {
		if ( (tickOffset < 0) || (tickOffset >= MAX_TICK) ) {
			throw new IllegalTickOffsetException(tickOffset);
		}
		this.tickOffset = tickOffset;
	}

	public int getTickOffset() {
		return this.tickOffset;
	}

	public abstract String toString();

	public abstract String toMMLString() throws MMLException;

	/**
	 * 挿入位置に基づいてTick位置を変換する
	 */
	private static int shiftForInsert(int pos, int start, int tick) {
		if (pos >= start) {
			return pos + tick;
		}
		return pos;
	}

	/**
	 * 削除区間に基づいてTick位置を変換する
	 */
	private static int shiftForRemove(int pos, int start, int end, int tick) {
		if (pos >= end) {
			return pos - tick;
		} else if (pos > start) {
			return start;
		}
		return pos;
	}

	/**
	 * tick長分を挿入する
	 * @param list
	 * @param startTick
	 * @param tick
	 */
	public static void insertTick(List<? extends MMLEvent> list, int startTick, int tick) {
		for (MMLEvent event : list) {
			int oldStart = event.getTickOffset();

			if (event instanceof MMLNoteEvent note) {
				if (oldStart + note.getTick() <= startTick) continue;
			} else {
				if (oldStart < startTick) continue;
			}

			int newStart = shiftForInsert(oldStart, startTick, tick);

			if (event instanceof MMLNoteEvent note) {
				int oldEnd = oldStart + note.getTick();
				int newEnd = shiftForInsert(oldEnd, startTick, tick);

				note.setTickOffset(newStart);
				note.setTick(newEnd - newStart);
			} else {
				event.setTickOffset(newStart);
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
		int endTick = startTick + tick;
		List<MMLEvent> deleteEvent = new ArrayList<>();

		for (MMLEvent event : list) {
			int oldStart = event.getTickOffset();

			if (event instanceof MMLNoteEvent note) {
				if (oldStart + note.getTick() <= startTick) continue;
			} else {
				if (oldStart < startTick) continue;
			}

			int newStart = shiftForRemove(oldStart, startTick, endTick, tick);

			if (event instanceof MMLNoteEvent note) {
				int oldEnd = oldStart + note.getTick();
				int newEnd = shiftForRemove(oldEnd, startTick, endTick, tick);

				int newTick = newEnd - newStart;
				if (newTick <= 0) {
					deleteEvent.add(note);
				} else {
					note.setTickOffset(newStart);
					note.setTick(newTick);
				}
			} else {
				if (oldStart < endTick) {
					deleteEvent.add(event);
				} else {
					event.setTickOffset(newStart);
				}
			}
		}

		// removeAllだとうまくいかないので注意.
		for (MMLEvent event : deleteEvent) {
			list.remove(event);
		}
	}

	public static <T> void updateMapByAddMeasure(Map<Integer, T> map, int measurePosition) {
		var newMap = new LinkedHashMap<Integer, T>();
		map.forEach((key, value) -> {
			if (key >= measurePosition) {
				newMap.put(key + 1, value);
			} else {
				newMap.put(key, value);
			}
		});
		map.clear();
		map.putAll(newMap);
	}

	public static <T> void updateMapByRemoveMeasure(Map<Integer, T> map, int measurePosition) {
		var newMap = new LinkedHashMap<Integer, T>();
		map.forEach((key, value) -> {
			if (key > measurePosition) {
				newMap.put(key - 1, value);
			} else if (key < measurePosition) {
				newMap.put(key, value);
			}
		});
		map.clear();
		map.putAll(newMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MMLEvent event) {
			return this.tickOffset == event.tickOffset;
		}
		return false;
	}
}

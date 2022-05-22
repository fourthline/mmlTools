/*
 * Copyright (C) 2015 たんらる
 */

package jp.fourthline.mabiicco.midi;

public interface IPlayNote {
	void playNote(int note, int velocity);
	void playNote(int[] note, int velocity);
	void offNote();
}

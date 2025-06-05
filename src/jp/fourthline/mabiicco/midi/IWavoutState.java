/*
　* Copyright (C) 2017-2025 たんらる
　*/

package jp.fourthline.mabiicco.midi;

public interface IWavoutState {
	long getTime();
	long getLen();
	void setSoundDataLine(ISoundDataLine soundDataLine);
	void startDataLine();
}

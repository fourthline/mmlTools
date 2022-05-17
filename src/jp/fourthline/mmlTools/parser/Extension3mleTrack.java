/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mmlTools.parser;

import jp.fourthline.mabiicco.midi.InstClass;


/**
 * "[3MLE EXTENSION]" Track
 */
public final class Extension3mleTrack {
	private final int instrument;
	private final int panpot;
	private final int startMarker;
	private int trackCount;
	private int trackLimit;
	private final int group;
	private final String trackName;

	public Extension3mleTrack(int instrument, int group, int panpot, String trackName, int startMarker) {
		this.instrument = instrument;
		this.group = group;
		this.panpot = panpot;
		this.startMarker = startMarker;
		this.trackName = trackName;
		this.trackCount = 1;
		this.trackLimit = 0;
		for (boolean b : InstClass.getEnablePartByProgram(instrument-1)) {
			if (b) trackLimit++;
		}
	}

	public boolean isLimit() {
		return (trackCount >= trackLimit);
	}

	public void addTrack() {
		trackCount++;
	}

	public int getInstrument() {
		return instrument;
	}

	public int getGroup() {
		return group;
	}

	public int getPanpot() {
		return panpot;
	}

	public int getStartMarker() {
		return startMarker;
	}

	public int getTrackCount() {
		return trackCount;
	}

	public String getTrackName() {
		return trackName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		sb.append(group+" ");
		sb.append(instrument+" ");
		sb.append(panpot+" ");
		sb.append(trackCount+" ");
		sb.append(trackName + " ]");
		return sb.toString();
	}
}

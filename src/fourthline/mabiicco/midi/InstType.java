/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.midi;

public enum InstType {
	NONE(false, false, false, false),
	NORMAL(true, true, true, false),
	DRUMS(true, false, false, false),
	VOICE(false, false, false, true);

	public static InstType getInstType(String s) {
		if (s.equals("0")) {
			return NONE;
		}
		if (s.equals("N")) {
			return NORMAL;
		}
		if (s.equals("D")) {
			return DRUMS;
		}
		if (s.equals("V")) {
			return VOICE;
		}

		return null;
	}

	public static final int VOICE_PLAYBACK_CHANNEL = 10;

	private final boolean enablePart[];
	private InstType(boolean melody, boolean chord1, boolean chord2, boolean songEx) {
		enablePart = new boolean[] {
				melody, chord1, chord2, songEx
		};
	}

	public boolean[] getEnablePart() {
		return enablePart;
	}
}

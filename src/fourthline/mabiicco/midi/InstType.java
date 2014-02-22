/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.midi;

public enum InstType {
	NORMAL {
	},
	DRUMS {
	},
	VOICE {
	};

	public static InstType getInstType(String s) {
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
}

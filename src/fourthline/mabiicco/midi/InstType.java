/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.midi;

import java.util.EnumSet;

public enum InstType {
	NONE(false, false, false, false, true, true),
	NORMAL(true, true, true, false, true, true),
	/**
	 * 移調が出来ない打楽器.
	 */
	DRUMS(true, false, false, false, false, false),
	/**
	 * 移調が可能な打楽器（鍵盤打楽器）.
	 */
	KPUR(true, false, false, false, false, true),
	VOICE(false, false, false, true, true, true),
	CHORUS(false, false, false, true, true, true);

	public static EnumSet<InstType> getMainInstTypes() {
		return EnumSet.of(InstType.NORMAL, InstType.DRUMS, InstType.KPUR, InstType.VOICE);
	}

	public static EnumSet<InstType> getChorusInstTypes() {
		return EnumSet.of(InstType.CHORUS);
	}

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
		if (s.equals("C")) {
			return CHORUS;
		}
		if (s.equals("K")) {
			return KPUR;
		}

		return null;
	}

	public static final int VOICE_PLAYBACK_CHANNEL = 10;

	private final boolean enablePart[];
	private final boolean allowV15;
	private final boolean allowTranspose;
	private InstType(boolean melody, boolean chord1, boolean chord2, boolean songEx, boolean allowV15, boolean allowTranspose) {
		enablePart = new boolean[] {
				melody, chord1, chord2, songEx
		};
		this.allowV15 = allowV15;
		this.allowTranspose = allowTranspose;
	}

	public boolean[] getEnablePart() {
		return enablePart;
	}

	public boolean allowTranspose() {
		return allowTranspose;
	}

	public int convertVelocityMML2Midi(int mml_velocity) {
		if (allowV15) {
			// 通常の楽器.
			if (mml_velocity > 15) {
				mml_velocity = 15;
			}
			return (mml_velocity * 8);
		} else {
			// 打楽器系の楽器はv11がMAX.
			if (mml_velocity > 11) {
				mml_velocity = 11;
			}
			return (mml_velocity * 11);
		}
	}
}

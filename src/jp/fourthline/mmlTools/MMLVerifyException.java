/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mmlTools;


public final class MMLVerifyException extends Exception {
	private static final long serialVersionUID = 5820865256091161012L;

	private final MMLTrack track;
	public MMLVerifyException(MMLTrack track) {
		super("Verify error.");
		this.track = track;
	}

	public MMLTrack getTrack() {
		return track;
	}
}

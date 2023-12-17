/*
 * Copyright (C) 2014-2023 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.util.Arrays;
import java.util.List;

/**
 * 楽器種別
 */
public interface InstType {
	/**
	 * 有効なパート情報を取得する. 
	 * @return サイズは4の配列.
	 */
	boolean[] getEnablePart();

	/**
	 * @return 移調可能な場合は true, 移調できない場合は falseを返す.
	 */
	boolean allowTranspose();

	/**
	 * @return 和音パートにテンポ出力を許容するかどうかを返す.
	 */
	boolean allowTempoChordPart();

	/**
	 * mmlのV指定からMidiの音量値に変換する.
	 * @param mml_velocity
	 * @return midiの音量値.
	 */
	int convertVelocityMML2Midi(int mml_velocity);

	/**
	 * 歌パートを除外するオプションを許可するか.
	 * @return
	 */
	boolean allowExcludeSongPart();

	/** 使用不可な楽器 */
	InstType NONE = new NoneType();

	/** 通常の楽器 [ melody, chord1, chord2 ]. */
	InstType NORMAL = new NormalType();

	/** 打楽器楽器 [ melody ], 移調できない. */
	InstType PERCUSSION = new PercussionType(false);

	/** 打楽器楽器 [ melody ], 移調できる. (シロフォン) */
	InstType KPUR = new PercussionType(true);

	/** 歌 [ song ]. */
	InstType VOICE = new SongType();

	/** コーラス [ song ]. */
	InstType CHORUS = new SongType();

	/** ドラム [ melody, chord1, chord2 ], 移調できない. */
	InstType DRUMS = new DrumsType();

	/**
	 * 単独で使用可能なメインの楽器のリスト.
	 */
	List<InstType> MAIN_INST_LIST = Arrays.asList(NORMAL, PERCUSSION, KPUR, VOICE, DRUMS);

	/**
	 * 単独で使用不能なサブの楽器のリスト.
	 */
	List<InstType> SUB_INST_LIST = List.of(CHORUS);

	static InstType getInstType(String s) {
		switch (s) {
		case "0": return NONE;
		case "N": return NORMAL;
		case "P": return PERCUSSION;
		case "V": return VOICE;
		case "C": return CHORUS;
		case "K": return KPUR;
		case "D": return DRUMS;
		default : throw new AssertionError();
		}
	}

	int VOICE_PLAYBACK_CHANNEL = 10;

	public static class NoneType extends NormalType {
		private NoneType() {
			super(NormalType.NONE_PART, false, false, false);
		}

		@Override
		public int convertVelocityMML2Midi(int mml_velocity) {
			return 0;
		}
	}

	/**
	 * 移調可能な通常音量の音源. [ melody, chord1, chord2 ], 移調可能.
	 */
	public static class NormalType implements InstType {
		private static final boolean[] NONE_PART = new boolean[] { false, false, false, false };
		private static final boolean[] ONE_PART = new boolean[] { true, false, false, false };
		private static final boolean[] THREE_PART = new boolean[] { true, true, true, false };
		private static final boolean[] SONG_PART = new boolean[] { false, false, false, true };

		private final boolean[] enablePart;
		private final boolean allowTranspose;
		private final boolean allowTempoChordPart;
		private final boolean allowExcludeSongPart;

		private NormalType() {
			this(NormalType.THREE_PART, true, true, true);
		}

		private NormalType(boolean[] enablePart, boolean allowTranspose, boolean allowTempoChordPart, boolean allowExcludeSongPart) {
			this.enablePart = enablePart;
			this.allowTranspose = allowTranspose;
			this.allowTempoChordPart = allowTempoChordPart;
			this.allowExcludeSongPart = allowExcludeSongPart;
		}

		@Override
		public final boolean[] getEnablePart() {
			return this.enablePart;
		}

		@Override
		public final boolean allowTranspose() {
			return this.allowTranspose;
		}

		@Override
		public final boolean allowTempoChordPart() {
			return allowTempoChordPart;
		}

		@Override
		public int convertVelocityMML2Midi(int mml_velocity) {
			// 通常の楽器.
			if (mml_velocity > 15) {
				mml_velocity = 15;
			} else if (mml_velocity < 0) {
				mml_velocity = 0;
			}
			return (mml_velocity * 8);
		}

		@Override
		public boolean allowExcludeSongPart() {
			return allowExcludeSongPart;
		}
	}

	/**
	 * 歌. [ song ], 移調可能.
	 */
	public static class SongType extends NormalType {
		private SongType() {
			super(NormalType.SONG_PART, true, false, false);
		}
	}

	/**
	 * ドラム. [ melody, chord1, chord2 ], 移調不可.
	 */
	public static class DrumsType extends NormalType {
		private DrumsType() {
			super(NormalType.THREE_PART, false, true, true);
		}
	}

	/**
	 * 打楽器楽器 [ melody ], 移調可否を指定する.
	 */
	public static class PercussionType extends NormalType {
		private PercussionType(boolean allowTranspose) {
			super(NormalType.ONE_PART, allowTranspose, false, true);
		}

		@Override
		public int convertVelocityMML2Midi(int mml_velocity) {
			// 打楽器系の楽器はv11がMAX.
			if (mml_velocity > 11) {
				mml_velocity = 11;
			} else if (mml_velocity < 0) {
				mml_velocity = 0;
			}
			return (mml_velocity * 11);
		}
	}
}

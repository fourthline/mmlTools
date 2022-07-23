/*
 * Copyright (C) 2014-2022 たんらる
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

	/** 使用不可な楽器 */
	InstType NONE = new NoneType();

	/** 通常の楽器 [ melody, chord1, chord2 ]. */
	InstType NORMAL = new NormalType();

	/** 打楽器楽器 [ melody ], 移調できない. */
	InstType DRUMS = new PercussionType(false);

	/** 打楽器楽器 [ melody ], 移調できる. (シロフォン) */
	InstType KPUR = new PercussionType(true);

	/** 歌 [ song ]. */
	InstType VOICE = new SongType();

	/** コーラス [ song ]. */
	InstType CHORUS = new SongType();

	/**
	 * 単独で使用可能なメインの楽器のリスト.
	 */
	List<InstType> MAIN_INST_LIST = Arrays.asList(NORMAL, DRUMS, KPUR, VOICE);

	/**
	 * 単独で使用不能なサブの楽器のリスト.
	 */
	List<InstType> SUB_INST_LIST = List.of(CHORUS);

	static InstType getInstType(String s) {
		switch (s) {
		case "0": return NONE;
		case "N": return NORMAL;
		case "D": return DRUMS;
		case "V": return VOICE;
		case "C": return CHORUS;
		case "K": return KPUR;
		default : throw new AssertionError();
		}
	}

	int VOICE_PLAYBACK_CHANNEL = 10;

	class NoneType implements InstType {
		private final boolean[] enablePart = new boolean[] { false, false, false, false };

		@Override
		public boolean[] getEnablePart() {
			return this.enablePart;
		}

		@Override
		public boolean allowTranspose() {
			return true;
		}

		@Override
		public int convertVelocityMML2Midi(int mml_velocity) {
			return 0;
		}

		@Override
		public boolean allowTempoChordPart() {
			return false;
		}
	}

	/**
	 * 移調可能な通常音量の音源. [ melody, chord1, chord2 ] or [ song ]
	 */
	public class NormalType implements InstType {
		private final boolean[] enablePart;

		private NormalType() {
			this(true);
		}

		private NormalType(boolean isNormal) {
			if (isNormal) {
				this.enablePart = new boolean[] { true, true, true, false };
			} else {
				this.enablePart = new boolean[] { false, false, false, true };
			}
		}

		@Override
		public boolean[] getEnablePart() {
			return this.enablePart;
		}

		@Override
		public boolean allowTranspose() {
			return true;
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
		public boolean allowTempoChordPart() {
			return true;
		}
	}

	public class SongType extends NormalType {
		private SongType() {
			super(false);
		}
	}

	/**
	 * 打楽器楽器 [ melody ], 移調可否を指定する.
	 */
	public class PercussionType implements InstType {
		private final boolean[] enablePart = new boolean[] { true, false, false, false };
		private final boolean allowTranspose;

		private PercussionType(boolean allowTranspose) {
			this.allowTranspose = allowTranspose;
		}

		@Override
		public boolean[] getEnablePart() {
			return this.enablePart;
		}

		@Override
		public boolean allowTranspose() {
			return this.allowTranspose;
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

		@Override
		public boolean allowTempoChordPart() {
			return false;
		}
	}
}

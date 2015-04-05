/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mabiicco.midi;

import java.util.Arrays;
import java.util.List;


public interface InstType {
	/**
	 * 有効なパート情報を取得する. 
	 * @return サイズは4の配列.
	 */
	public boolean[] getEnablePart();

	/**
	 * @return 移調可能な場合は true, 移調できない場合は falseを返す.
	 */
	public boolean allowTranspose();

	/**
	 * mmlのV指定からMidiの音量値に変換する.
	 * @param mml_velocity
	 * @return midiの音量値.
	 */
	public int convertVelocityMML2Midi(int mml_velocity);

	/** 使用不可な楽器 */
	public InstType NONE = new NoneType();

	/** 通常の楽器 [ melody, chord1, chord2 ]. */
	public InstType NORMAL = new NormalType(true);

	/** 打楽器楽器 [ melody ], 移調できない. */
	public InstType DRUMS = new PercussionType(false);

	/** 打楽器楽器 [ melody ], 移調できる. (シロフォン) */
	public InstType KPUR = new PercussionType(true);

	/** 歌 [ song ]. */
	public InstType VOICE = new NormalType(false);

	/** コーラス [ song ]. */
	public InstType CHORUS = new NormalType(false);

	/**
	 * 単独で使用可能なメインの楽器のリスト.
	 */
	public List<InstType> MAIN_INST_LIST = Arrays.asList(NORMAL, DRUMS, KPUR, VOICE);

	/**
	 * 単独で使用不能なサブの楽器のリスト.
	 */
	public List<InstType> SUB_INST_LIST = Arrays.asList(CHORUS);

	public static InstType getInstType(String s) {
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

	public static final int VOICE_PLAYBACK_CHANNEL = 10;

	class NoneType implements InstType {
		private final boolean enablePart[] = new boolean[] { false, false, false, false };

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
	}

	/**
	 * 移調可能な通常音量の音源. [ melody, chord1, chord2 ] or [ song ]
	 */
	class NormalType implements InstType {
		private final boolean enablePart[];

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
	}

	/**
	 * 打楽器楽器 [ melody ], 移調可否を指定する.
	 */
	class PercussionType implements InstType {
		private final boolean enablePart[] = new boolean[] { true, false, false, false };
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
	}
}

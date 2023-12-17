/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mabiicco.midi;

import static jp.fourthline.mabiicco.AppResource.appText;

import jp.fourthline.mabiicco.ui.SettingButtonGroupItem;

public enum SoundEnv implements SettingButtonGroupItem {
	MABINOGI("mabinogi", true, "instrument", 0, true),
	ARCHEAGE("archeage", false, "aaInstrument", 1, true),
	OTHER("other", false, null, 1, false);

	private final String name;
	private final boolean useDLS;

	/** 楽器情報のファイル接頭名称 */
	private final String instrumentName;

	/** ピアノロール表示のオクターブ変化量 */
	private final int octDelta;

	/** デフォルトのサウンドバンクロード時に楽器の名前変換を行うかどうか */
	private final boolean nameConvertForDefaultSoundBank;

	SoundEnv(String name, boolean useDLS, String instrumentName, int octDelta, boolean nameConvertForDefaultSoundBank) {
		this.name = appText("menu.sound_env." + name);
		this.useDLS = useDLS;
		this.instrumentName = instrumentName;
		this.octDelta = octDelta;
		this.nameConvertForDefaultSoundBank = nameConvertForDefaultSoundBank;
	}

	@Override
	public String getButtonName() {
		return name;
	}

	public boolean useDLS() {
		return useDLS;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public int pianoRollOctDelta() {
		return octDelta;
	}

	public boolean nameConvertForDefaultSoundBank() {
		return nameConvertForDefaultSoundBank;
	}
}

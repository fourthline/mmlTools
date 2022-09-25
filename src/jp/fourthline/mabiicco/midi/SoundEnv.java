/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.midi;

import static jp.fourthline.mabiicco.AppResource.appText;

import jp.fourthline.mabiicco.ui.SettingButtonGroupItem;

public enum SoundEnv implements SettingButtonGroupItem {
	MABINOGI("mabinogi", true, "instrument", 0),
	ARCHEAGE("archeage", false, "aaInstrument", 1);

	private final String name;
	private final boolean useDLS;

	/** 楽器情報のファイル接頭名称 */
	private final String instrumentName;

	/** ピアノロール表示のオクターブ変化量 */
	private final int octDelta;

	SoundEnv(String name, boolean useDLS, String instrumentName, int octDelta) {
		this.name = appText("menu.sound_env." + name);
		this.useDLS = useDLS;
		this.instrumentName = instrumentName;
		this.octDelta = octDelta;
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
}

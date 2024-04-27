/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.SettingButtonGroupItem;

public enum RangeMode implements SettingButtonGroupItem {
	SELECTED_PART("paintMode.active_part", false, false),
	SELECTED_TRACK("paintMode.active_track", true, false),
	ALL_TRACK("paintMode.all_track", true, true)
	;
	private final String buttonName;
	private final boolean curTrack;
	private final boolean all;
	private RangeMode(String name, boolean curTrack, boolean all) {
		this.buttonName = name;
		this.curTrack = curTrack;
		this.all = all;
	}

	public interface PartAction {
		void action(int trackIndex, int partIndex);
	}

	public void action(IMMLManager mmlManager, PartAction func) {
		int trackIndex = mmlManager.getActiveTrackIndex();
		int partIndex = mmlManager.getActiveMMLPartIndex();

		int trackCount = mmlManager.getMMLScore().getTrackCount();
		int partCount = mmlManager.getMMLScore().getTrack(trackIndex).getMMLEventList().size();

		// Other Track
		if (all) {
			for (int i = 0; i < trackCount; i++) {
				if (trackIndex != i) {
					if (mmlManager.getMMLScore().getTrack(i).isVisible()) {
						for (int p = 0; p < partCount; p++) {
							func.action(i, p);
						}
					}
				}
			}
		}

		// Active Track & Active Part以外
		if (curTrack) {
			if (mmlManager.getMMLScore().getTrack(trackIndex).isVisible()) {
				for (int i = 0; i < partCount; i++) {
					if (partIndex != i) {
						func.action(trackIndex, i);
					}
				}
			}
		}

		// Active Part
		func.action(trackIndex, partIndex);
	}

	@Override
	public String getButtonName() {
		return buttonName;
	}
}
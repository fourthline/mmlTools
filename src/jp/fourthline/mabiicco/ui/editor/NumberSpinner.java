/*
 * Copyright (C) 2015-2021 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * 
 */
public final class NumberSpinner {
	private NumberSpinner() {}

	public static JSpinner createSpinner(int initial, int min, int max, int step) {
		if (initial < min) {
			initial = min;
		} else if (initial > max) {
			initial = max;
		}
		SpinnerNumberModel model = new SpinnerNumberModel(initial, min, max, step);
		JSpinner spinner = new JSpinner(model);

		spinner.addMouseWheelListener(e -> {
			if (!spinner.isEnabled()) {
				return;
			}
			try {
				if (e.getWheelRotation() < 0) {
					spinner.setValue(model.getNextValue());
				} else {
					spinner.setValue(model.getPreviousValue());
				}
			} catch (IllegalArgumentException exception) {}
		});

		return spinner;
	}
}

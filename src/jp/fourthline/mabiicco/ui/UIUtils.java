/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public final class UIUtils {
	private UIUtils() {}

	/**
	 * JSpinnerにフォーカス設定する
	 * @param spinner
	 */
	public static void setDefaultFocus(JSpinner spinner) {
		setDefaultFocus(((JSpinner.NumberEditor)spinner.getEditor()).getTextField());
	}

	/**
	 * JTextFieldにフォーカス設定する
	 * @param textField
	 */
	public static void setDefaultFocus(JTextField textField) {
		textField.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorRemoved(AncestorEvent event) {}

			@Override
			public void ancestorMoved(AncestorEvent event) {}

			@Override
			public void ancestorAdded(AncestorEvent event) {
				textField.requestFocusInWindow();
				textField.selectAll();
			}
		});
	}
}

/*
 * Copyright (C) 2022-2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import jp.fourthline.mabiicco.ui.UIUtils;


public abstract sealed class AbstractNumberDialogAction permits MMLTranspose, UserViewWidthDialog {
	protected final Frame parentFrame;
	private final String title;
	protected final JPanel cPanel;
	private final JSpinner spinner;

	/**
	 * 単一の数値入力Spinnerのダイアログ表示をして, 処理を実行する.
	 * @param parentFrame  親Frame
	 * @param title        タイトル
	 * @param message      ダイアログに表示するメッセージ
	 * @param initial      数値初期値
	 * @param min          数値の最小値
	 * @param max          数値の最大値
	 * @param step         数値の変動ステップ
	 */
	protected AbstractNumberDialogAction(Frame parentFrame, String title, String message, int initial, int min, int max, int step) {
		this.parentFrame = parentFrame;
		this.title = title;

		JPanel panel = new JPanel();
		panel.add(new JLabel(message));
		spinner = NumberSpinner.createSpinner(initial, min, max, step);
		spinner.setFocusable(false);
		UIUtils.setDefaultFocus(spinner);
		panel.add(spinner);
		cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);
	}

	public void showDialog() {
		int status = JOptionPane.showConfirmDialog(parentFrame, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
			int v = ((Integer) spinner.getValue()).intValue();
			apply(v);
		}
	}

	/**
	 * OKボタンを押されたときの処理
	 * @param v
	 */
	public abstract void apply(int v);
}

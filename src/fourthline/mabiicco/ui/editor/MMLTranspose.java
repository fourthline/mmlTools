/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;


public final class MMLTranspose {
	public MMLTranspose() {
	}

	public void execute(Frame parentFrame, IMMLManager mmlManager) {
		int transpose = showTransposeDialog(parentFrame);
		if (transpose == 0) {
			return;
		}

		mmlManager.getMMLScore().transpose(transpose);
		mmlManager.updateActivePart(true);
	}

	private int showTransposeDialog(Frame parentFrame) {
		String title = AppResource.appText("edit.transpose");
		JPanel panel = new JPanel();
		panel.add(new JLabel(AppResource.appText("edit.transpose.text")));
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0, -12, 12, 1));
		spinner.setFocusable(false);
		panel.add(spinner);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);

		int status = JOptionPane.showConfirmDialog(parentFrame, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
			return ((Integer) spinner.getValue()).intValue();
		}

		return 0;
	}
}

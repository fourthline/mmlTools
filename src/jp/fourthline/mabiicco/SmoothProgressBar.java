/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JProgressBar;
import javax.swing.Timer;

public class SmoothProgressBar extends JProgressBar implements ActionListener {
	private static final long serialVersionUID = 891766922903993231L;
	private final Timer timer;
	private int targetValue = 0;

	public SmoothProgressBar() {
		super();
		timer = new Timer(10, this);
	}

	@Override
	public void setValue(int value) {
		synchronized (this) {
			targetValue = value;
			if (!timer.isRunning()) {
				timer.start();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var value = getValue();
		synchronized (this) {
			if (targetValue > value) {
				value++;
			} else if (targetValue < value) {
				value--;
			} else {
				timer.stop();
				return;
			}
			super.setValue(value);
		}
	}
}

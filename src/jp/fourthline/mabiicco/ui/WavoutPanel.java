/*
 * Copyright (C) 2017-2025 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static jp.fourthline.mabiicco.AppResource.appText;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.Future;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.MabiIccoExecutor;
import jp.fourthline.mabiicco.midi.MabiDLS;

import javax.swing.BoxLayout;
import javax.swing.JButton;

public final class WavoutPanel extends JPanel {

	private static final long serialVersionUID = -4756346595351421861L;
	private final JDialog dialog;
	private final MainFrame parentFrame;
	private final IMMLManager mmlManager;
	private final File file;

	private final JButton startButton = new JButton(appText("wavout.start"));
	private final JButton cancelButton = new JButton(appText("wavout.cancel"));
	private final JProgressBar progress = new JProgressBar();

	private Future<?> future;
	private final long totalTime;
	private final long totalBytes;

	private final long MAX_SIZE = 128 * 1024 * 1024;

	public WavoutPanel(MainFrame parentFrame, IMMLManager mmlManager, File file) {
		this.dialog = new JDialog(parentFrame, appText("wavout"), true);
		this.parentFrame = parentFrame;
		this.mmlManager = mmlManager;
		this.file = file;
		this.totalTime = mmlManager.getMMLScore().getTotalTime();
		this.totalBytes = (long)(totalTime * 44.1 * 4);
		initializePanel();
	}

	private void initializePanel() {
		setLayout(new BorderLayout());
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		DecimalFormat df = new DecimalFormat("#.0");
		progress.setMaximum((int)totalBytes);
		updateProgress(0);
		progress.setStringPainted(true);

		p1.add(new JLabel("File: "+file.getName()));
		p1.add(new JLabel("Size: "+df.format((double)totalBytes/1024.0/1024.0)+"MB"));
		p1.add(progress);

		startButton.addActionListener(t -> startWavout());
		cancelButton.addActionListener(t -> stopWavout());
		JPanel p2 = new JPanel();
		p2.add(startButton);
		p2.add(cancelButton);

		add(p1, BorderLayout.NORTH);
		add(p2, BorderLayout.SOUTH);

		if (totalBytes > MAX_SIZE) {
			startButton.setEnabled(false);
		}
	}

	private void updateProgress(int now) {
		progress.setValue(now);
		var f = NumberFormat.getInstance();
		now = Math.min(now, (int)totalBytes);
		progress.setString(f.format(now>>10) + "/" + f.format(totalBytes>>10));
	}

	private void startWavout() {
		System.out.println("startWavout(): "+totalBytes);
		startButton.setEnabled(false);
		parentFrame.disableNoplayItems();
		var dls = MabiDLS.getInstance();
		try {
			dls.startWavout(mmlManager.getMMLScore(), file, this::stopWavout, (int)totalBytes);
		} catch (IOException e) {
			dls.stopWavout();
			JOptionPane.showMessageDialog(parentFrame, e.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		future = MabiIccoExecutor.getInstance().scheduleWithFixedDelay(() -> {
			int len = (int) dls.getWavout().getLen();
			updateProgress(len);
		}, 100);
	}

	/**
	 * ダイアログを表示する.
	 */
	public void showDialog() {
		dialog.getContentPane().add(this);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parentFrame);
		dialog.setVisible(true);
	}

	private void stopWavout() {
		parentFrame.enableNoplayItems();
		MabiDLS.getInstance().stopWavout();
		dialog.setVisible(false);
		if (future != null) {
			ActionDispatcher.getInstance().showTime("wavout", MabiDLS.getInstance().getWavout().getTime());
			future.cancel(false);
			future = null;
		}
	}
}

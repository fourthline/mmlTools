/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import fourthline.mabiicco.ActionDispatcher;
import fourthline.mabiicco.midi.MabiDLS;

import static fourthline.mabiicco.AppResource.appText;

public final class WavoutPanel extends JPanel {

	private static final long serialVersionUID = -4756346595351421861L;
	private final JDialog dialog;
	private final MainFrame parentFrame;
	private final IMMLManager mmlManager;
	private final File file;

	private final JButton startButton = new JButton(appText("wavout.start"));
	private final JButton cancelButton = new JButton(appText("wavout.cancel"));
	private final JProgressBar progress = new JProgressBar();

	private boolean run = false;

	public WavoutPanel(MainFrame parentFrame, IMMLManager mmlManager, File file) {
		this.dialog = new JDialog(parentFrame, appText("wavout"), true);
		this.parentFrame = parentFrame;
		this.mmlManager = mmlManager;
		this.file = file;
		initializePanel();
	}

	private void initializePanel() {
		setLayout(new BorderLayout());
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		DecimalFormat df = new DecimalFormat("#.0");
		long totalTime = mmlManager.getMMLScore().getTotalTime()+1000;
		long totalBytes = (long)(totalTime * 44.1 * 4);
		progress.setMaximum((int)totalBytes);
		progress.setValue(0);

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
	}

	private void startWavout() {
		run = true;
		startButton.setEnabled(false);
		parentFrame.disableNoplayItems();
		MabiDLS.getInstance().startWavout(mmlManager.getMMLScore(), file, this::stopWavout);
		new Thread(() -> {
			while (run) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int len = (int) MabiDLS.getInstance().getWavout().getLen();
				progress.setValue(len);
			}
		}).start();
	}

	/**
	 * ダイアログを表示する.
	 */
	public void showDialog() {
		dialog.getContentPane().add(this);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void stopWavout() {
		parentFrame.enableNoplayItems();
		MabiDLS.getInstance().stopWavout();
		dialog.setVisible(false);
		if (run) {
			ActionDispatcher.getInstance().showTime("wavout", MabiDLS.getInstance().getWavout().getTime());
		}
		run = false;
	}
}

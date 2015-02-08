/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.mml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.JScrollPane;

import fourthline.mabiicco.AppResource;
import fourthline.mmlTools.MMLTrack;

public final class MMLOutputPanel extends JPanel {
	private static final long serialVersionUID = 8558159209741558854L;
	private TrackListTable table;
	private final JDialog dialog;
	private final Frame parentFrame;

	private List<MMLTrack> trackList;

	public MMLOutputPanel(Frame parentFrame) {
		this.dialog = null;
		this.parentFrame = parentFrame;
		initializePanel(null);
	}

	public MMLOutputPanel(Frame parentFrame, List<MMLTrack> trackList) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("mml.output"), true);
		this.parentFrame = parentFrame;
		initializePanel(trackList);
	}

	private void initializePanel(List<MMLTrack> trackList) {
		this.trackList = trackList;
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		JPanel p = new JPanel();
		p.setLayout(null);

		JButton copyButton = new JButton(AppResource.appText("mml.output.copyButton"));
		copyButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(copyButton);
		copyButton.addActionListener((event) -> {
			currentSelectedTrackMMLOutput();
		});

		JButton closeButton = new JButton(AppResource.appText("mml.output.closeButton"));
		closeButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((event) -> {
			dialog.setVisible(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 372, 169);
		p.add(scrollPane);

		table = new TrackListTable(trackList);
		scrollPane.setViewportView(table);

		InputMap imap = dialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		dialog.getRootPane().getActionMap().put("close-it", new AbstractAction() {
			private static final long serialVersionUID = -4495368209645211523L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}});

		add(buttonPanel, BorderLayout.SOUTH);
		add(p, BorderLayout.CENTER);
	}

	public static void copyToClipboard(Frame parent, String text) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();
		clip.setContents(new StringSelection(text), null);
		JOptionPane.showMessageDialog(parent, AppResource.appText("mml.output.done"), AppResource.getAppTitle(), JOptionPane.PLAIN_MESSAGE);
	}

	private void currentSelectedTrackMMLOutput() {
		int row = table.getSelectedRow();
		String mmlText = trackList.get(row).getMabiMML();
		copyToClipboard(parentFrame, mmlText);

		row++;
		if (row >= trackList.size()) {
			row = 0;
		}
		table.setRowSelectionInterval(row, row);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(390, 220);
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
}

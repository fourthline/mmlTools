/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.mml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;


public final class MMLImportPanel extends JPanel {
	private static final long serialVersionUID = -1504636951822574399L;
	private TrackListTable table;
	private final JDialog dialog;

	private List<MMLTrack> trackList;
	private IMMLManager mmlManager;
	private int possibleImportTrackCount;

	/**
	 * @wbp.parser.constructor
	 */
	public MMLImportPanel(Frame parentFrame) {
		this.dialog = null;
		initializePanel(null);
	}

	public MMLImportPanel(Frame parentFrame, MMLScore score, IMMLManager mmlManager) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("mml.input.import"), true);
		this.trackList = score.getTrackList();
		this.mmlManager = mmlManager;
		possibleImportTrackCount = MMLScore.MAX_TRACK - mmlManager.getMMLScore().getTrackCount();
		if (score.getTrackCount() < possibleImportTrackCount) {
			possibleImportTrackCount = score.getTrackCount();
		}
		initializePanel(trackList);
	}

	private void initializePanel(List<MMLTrack> trackList) {
		this.trackList = trackList;
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		JPanel p = new JPanel();
		p.setLayout(null);

		JButton importButton = new JButton(AppResource.appText("mml.input.import"));
		importButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(importButton);
		importButton.addActionListener((event) -> {
			importMMLTrack();
			dialog.setVisible(false);
		});

		JButton closeButton = new JButton(AppResource.appText("mml.output.closeButton"));
		closeButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((event) -> {
			dialog.setVisible(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 422, 169);
		p.add(scrollPane);

		table = new TrackListTable(trackList, true);
		table.setInitialCheck(possibleImportTrackCount);
		scrollPane.setViewportView(table);

		JLabel lblNewLabel = new JLabel(AppResource.appText("mml.input.import.possibleImport")+": "+possibleImportTrackCount);
		lblNewLabel.setBounds(22, 189, 300, 13);
		p.add(lblNewLabel);

		table.setDefaultEditor(Object.class, null);

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

	private void importMMLTrack() {
		MMLScore targetScore = mmlManager.getMMLScore();
		boolean checkList[] = table.getCheckList();
		for (int i = 0; i < trackList.size(); i++) {
			if (checkList[i]) {
				targetScore.addTrack(trackList.get(i));
			}
		}
		mmlManager.updateActivePart(false);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(440, 240);
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

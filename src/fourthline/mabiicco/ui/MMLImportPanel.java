/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

import fourthline.mabiicco.AppResource;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;


public final class MMLImportPanel extends JPanel {
	private static final long serialVersionUID = -1504636951822574399L;
	private TrackListTable table;
	private final JDialog dialog;
	private final JButton importButton = new JButton(AppResource.appText("mml.input.import"));

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
		setLayout(null);

		importButton.setBounds(142, 214, 90, 29);
		add(importButton);
		importButton.addActionListener((event) -> {
			importMMLTrack();
			dialog.setVisible(false);
		});

		JButton closeButton = new JButton(AppResource.appText("mml.output.closeButton"));
		closeButton.setBounds(258, 214, 90, 29);
		add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((event) -> {
			dialog.setVisible(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 422, 169);
		add(scrollPane);

		table = new TrackListTable(trackList, true);
		table.setInitialCheck(possibleImportTrackCount);
		scrollPane.setViewportView(table);

		JLabel lblNewLabel = new JLabel(AppResource.appText("mml.input.import.possibleImport")+": "+possibleImportTrackCount);
		lblNewLabel.setBounds(22, 189, 300, 13);
		add(lblNewLabel);

		table.setDefaultEditor(Object.class, null);
	}

	private void importMMLTrack() {
		MMLScore targetScore = mmlManager.getMMLScore();
		boolean checkList[] = table.getCheckList();
		for (int i = 0; i < trackList.size(); i++) {
			if (checkList[i]) {
				targetScore.addTrack(trackList.get(i));
			}
		}
		mmlManager.updateActivePart();
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

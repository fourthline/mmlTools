/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

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

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;


public final class MMLImportPanel extends JPanel {
	private static final long serialVersionUID = -1504636951822574399L;
	private TrackListTable table;
	private final JDialog dialog;
	private final Frame parentFrame;

	private final MMLScore importedScore;
	private final IMMLManager mmlManager;
	private final int possibleImportTrackCount;
	private final boolean newImport;

	private final JLabel lblNewLabel = new JLabel(AppResource.appText("mml.input.import.possibleImport")+": XX/XX");

	private final Dimension prefSize = new Dimension(460, 300);

	/**
	 * インポートパネルを生成する
	 * @param parentFrame
	 * @param score
	 * @param mmlManager
	 * @param newImport     新規のファイルとしてインポートする. インポート時に既存トラックは削除される.
	 */
	public MMLImportPanel(Frame parentFrame, MMLScore score, IMMLManager mmlManager, boolean newImport) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("mml.input.import"), true);
		this.importedScore = score;
		this.mmlManager = mmlManager;
		this.parentFrame = parentFrame;
		this.newImport = newImport;
		this.possibleImportTrackCount = newImport ?
				score.getTrackCount() :
					Math.min(MMLScore.MAX_TRACK - mmlManager.getMMLScore().getTrackCount(),
							score.getTrackCount());
		initializePanel(score.getTrackList());
	}

	private void initializePanel(List<MMLTrack> trackList) {
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
		scrollPane.setBounds(12, 10, 422, 229);
		p.add(scrollPane);

		table = new TrackListTable(trackList, true);
		table.setInitialCheck(possibleImportTrackCount);
		table.addPropertyChangeListener(evt -> {
			importButton.setEnabled( (table.getCheckCount() > 0) && (table.getCheckCount() <= possibleImportTrackCount) );
			updateLabel();
		});
		scrollPane.setViewportView(table);

		lblNewLabel.setBounds(22, 242, 300, 14);
		updateLabel();
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

	private void updateLabel() {
		lblNewLabel.setText(AppResource.appText("mml.input.import.possibleImport")+": "+table.getCheckCount()+"/"+possibleImportTrackCount);
	}

	void importMMLTrack() {
		var trackList = importedScore.getTrackList();
		MMLScore targetScore = mmlManager.getMMLScore();
		if (newImport) {
			for (int i = 0; i < targetScore.getTrackCount(); i++) {
				targetScore.removeTrack(0);
			}
			targetScore.setAuthor(importedScore.getAuthor());
			targetScore.setTitle(importedScore.getTitle());
			targetScore.setBaseTime(importedScore.getBaseTime());
		}
		boolean[] checkList = table.getCheckList();
		for (int i = 0; i < trackList.size(); i++) {
			if (checkList[i]) {
				targetScore.addTrack(trackList.get(i));
			}
		}
		mmlManager.updateActivePart(false);
	}

	@Override
	public Dimension getPreferredSize() {
		return prefSize;
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
}

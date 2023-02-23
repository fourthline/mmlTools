/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
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

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.ComposeRank;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLText;

public final class MMLOutputPanel extends JPanel {
	private static final long serialVersionUID = 8558159209741558854L;
	final TrackListTable table;
	private final JDialog dialog;
	private final Window parentFrame;
	private final JButton splitButton = new JButton(AppResource.appText("mml.output.split"));

	private final List<MMLTrack> trackList;
	private MMLScore score;
	final List<String> outputTextList = new ArrayList<>();
	private final String trackName;

	private final Dimension prefSize = new Dimension(480, 340);

	public MMLOutputPanel(Frame parentFrame) {
		this.dialog = null;
		this.parentFrame = parentFrame;
		this.table = null;
		this.trackList = null;
		this.trackName = null;
		initializePanel(false);
	}

	public MMLOutputPanel(Frame parentFrame, List<MMLTrack> trackList, MMLScore score) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("mml.output"), true);
		this.parentFrame = parentFrame;
		this.table = new TrackListTable(trackList);
		this.trackList = trackList;
		this.trackName = null;
		for (MMLTrack track : trackList) {
			outputTextList.add(track.getMabiMML());
		}
		this.score = score;
		initializePanel(true);
	}

	/**
	 * 楽譜集生成用
	 */
	private MMLOutputPanel(Dialog parent, MMLTrack track, List<MMLText> textList, MMLScore score) {
		this.dialog = new JDialog(parent, AppResource.appText("mml.output.split"), true);
		this.parentFrame = parent;
		this.table = new TrackListTable(track, textList);
		for (MMLText mmlText : textList) {
			outputTextList.add(mmlText.getMML());
		}
		this.trackList = null;
		this.trackName = track.getTrackName();
		this.score = score;
		initializePanel(false);
	}

	private void initializePanel(boolean splitFunc) {
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		JPanel p = new JPanel();
		p.setLayout(null);

		JButton tableListInfoButton = new JButton(AppResource.getImageIcon("/img/list.png"));
		tableListInfoButton.setToolTipText(AppResource.appText("mml.output.tableListInfoButton"));
		tableListInfoButton.setMargin(new Insets(5, 10, 5, 10));
		tableListInfoButton.setFocusable(false);
		buttonPanel.add(tableListInfoButton);
		tableListInfoButton.addActionListener((event) -> tableListOutput());

		JButton nameButton = new JButton(AppResource.appText("mml.output.nameButton"));
		nameButton.setMargin(new Insets(5, 10, 5, 10));
		nameButton.setFocusable(false);
		buttonPanel.add(nameButton);
		nameButton.addActionListener((event) -> currentSelectedTrackNameOutput());

		JButton copyButton = new JButton(AppResource.appText("mml.output.copyButton"));
		copyButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(copyButton);
		copyButton.addActionListener((event) -> currentSelectedTrackMMLOutput());

		if (splitFunc) {
			splitButton.setMargin(new Insets(5, 10, 5, 10));
			buttonPanel.add(splitButton);
			splitButton.addActionListener((event) -> currentSelectedTrackMMLSplitOutput());
			table.getSelectionModel().addListSelectionListener(t -> checkSplitCopy());
			checkSplitCopy();
		}

		JButton closeButton = new JButton(AppResource.appText("mml.output.closeButton"));
		closeButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((event) -> dialog.setVisible(false));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 460, 280);
		p.add(scrollPane);

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

	public static void copyToClipboard(Window parent, String text, String message) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();
		clip.setContents(new StringSelection(text), null);
		if (parent != null) {
			JOptionPane.showMessageDialog(parent, message, AppResource.getAppTitle(), JOptionPane.PLAIN_MESSAGE);
		}
	}


	private void nextSelect(int row) {
		row++;
		if (row >= table.getRowCount()) {
			row = 0;
		}
		table.setRowSelectionInterval(row, row);
	}

	/**
	 * テーブル情報をコピーする
	 */
	private void tableListOutput() {
		String text = table.getTableListInfo();
		copyToClipboard(parentFrame, text, AppResource.appText("mml.output.table_list_done"));
	}

	/**
	 * 現在のトラック名をコピーする
	 */
	private void currentSelectedTrackNameOutput() {
		String text;
		if (trackList != null) {
			int row = table.getSelectedRow();
			text = trackList.get(row).getTrackName();
		} else if (trackName != null) {
			text = trackName;
		} else {
			return;
		}
		if (score != null) {
			String scoreName = score.getTitle();
			if (!scoreName.isEmpty()) {
				text = scoreName + "/" + text;
			}
		}
		copyToClipboard(parentFrame, text, AppResource.appText("mml.output.name_done")+"\n\""+text+"\"");
	}

	/**
	 * 現在のトラックのMMLをコピーする
	 */
	private void currentSelectedTrackMMLOutput() {
		int row = table.getSelectedRow();
		String mmlText = outputTextList.get(row);
		copyToClipboard(parentFrame, mmlText, AppResource.appText("mml.output.done"));
		nextSelect(row);
	}

	/**
	 * 現在のトラックを分割コピーするダイアログを表示する
	 */
	private void currentSelectedTrackMMLSplitOutput() {
		int row = table.getSelectedRow();
		createSelectedTrackMMLSplitPanel(row).showDialog();
		nextSelect(row);
	}

	/**
	 * 楽譜集分割用のパネルを作成する
	 */
	MMLOutputPanel createSelectedTrackMMLSplitPanel(int row) {
		MMLTrack track = trackList.get(row);
		MMLText mmlText = new MMLText().setMMLText( outputTextList.get(row) );
		mmlText.setExcludeSongPart(track.isExcludeSongPart());
		ComposeRank topRank = !track.isExcludeSongPart() ? ComposeRank.getTopRank() : ComposeRank.getTopExcludeSongRank();
		return new MMLOutputPanel(dialog, track, mmlText.splitMML(topRank), score);
	}

	private void checkSplitCopy() {
		boolean b = table.selectedRowCanSplit();
		splitButton.setEnabled(b);
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

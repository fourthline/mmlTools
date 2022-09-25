/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.AAMMLExport;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;


public final class MMLExportPanel extends JPanel {
	private static final long serialVersionUID = -1504636951822574399L;
	private TrackListTable table;
	private final JDialog dialog;
	private final Frame parentFrame;
	private final MMLScore score;
	private final Supplier<File> fileSupplier;

	private final Dimension prefSize = new Dimension(440, 360);
	private final JLabel outputTextCountLabel = new JLabel();
	private String outputText;
	private JButton fileExportButton;
	private JButton copyButton;

	/**
	 * エクスポートパネルを生成する
	 * @param parentFrame
	 * @param score
	 * @param mmlManager
	 */
	public MMLExportPanel(Frame parentFrame, MMLScore score, Supplier<File> fileSupplier) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("mml.export"), true);
		this.score = score;
		this.parentFrame = parentFrame;
		this.fileSupplier = fileSupplier;
		initializePanel(score.getTrackList());
	}

	private void initializePanel(List<MMLTrack> trackList) {
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		JPanel p = new JPanel();
		p.setLayout(null);

		// button panel (south)
		fileExportButton = new JButton(AppResource.appText("mml.export.file"));
		fileExportButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(fileExportButton);
		fileExportButton.addActionListener((t) -> {
			exportFileMMLTrack();
		});

		copyButton = new JButton(AppResource.appText("mml.export.clipboard"));
		copyButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(copyButton);
		copyButton.addActionListener(t -> {
			exportClipboardMMLTrack();
		});

		JButton closeButton = new JButton(AppResource.appText("mml.output.closeButton"));
		closeButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((t) -> {
			dialog.setVisible(false);
		});

		// track table (center)
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 422, 229);
		p.add(scrollPane);

		table = new TrackListTable(trackList, true);
		table.setInitialCheck(1);
		scrollPane.setViewportView(table);

		outputTextCountLabel.setBounds(22, 242, 300, 14);
		p.add(outputTextCountLabel);

		table.setDefaultEditor(Object.class, null);
		table.addPropertyChangeListener(t -> updateText());

		InputMap imap = dialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		dialog.getRootPane().getActionMap().put("close-it", new AbstractAction() {
			private static final long serialVersionUID = -4495368209645211523L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}});

		// format panel (north)
		JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JRadioButton typeAA = new JRadioButton(AppResource.appText("mml.export.archeage"));
		formatPanel.add(typeAA);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(typeAA);
		typeAA.setSelected(true);

		add(buttonPanel, BorderLayout.SOUTH);
		add(p, BorderLayout.CENTER);
		add(formatPanel, BorderLayout.NORTH);

		// 初回更新
		updateText();
	}

	private void updateText() {
		var trackList = score.getTrackList();
		boolean[] checkList = table.getCheckList();
		List<MMLEventList> eventList = new ArrayList<>();
		for (int i = 0; i < trackList.size(); i++) {
			if (checkList[i]) {
				eventList.addAll(trackList.get(i).getMMLEventList());
			}
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		var export = new AAMMLExport();
		outputText = export.convertMML(eventList, score.getTempoEventList());
		outputTextCountLabel.setText(Integer.toString(outputText.length()) + " (" + export.getPartCount() + ")");
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		// 出力できるものがない場合は出力系ボタンを無効にする
		boolean b = (outputText.length() > 0);
		fileExportButton.setEnabled(b);
		copyButton.setEnabled(b);
	}

	private void exportFileMMLTrack() {
		boolean loop = true;
		File file = fileSupplier.get();
		while (loop && (file != null)) {
			try {
				FileOutputStream stream = new FileOutputStream(file);
				stream.write(outputText.getBytes());
				stream.close();
				JOptionPane.showMessageDialog(parentFrame, AppResource.appText("mml.export.archeage_done")+"\n"+file.getAbsolutePath());
				loop = false;
			} catch (IOException e) {
				String message = e.getLocalizedMessage();
				message += "\n" + AppResource.appText("mml.export.retry");
				int ret = JOptionPane.showConfirmDialog(parentFrame, message, "ERROR", JOptionPane.ERROR_MESSAGE);
				if (ret == JOptionPane.NO_OPTION) {
					loop = false;
				}
			}
		}
	}

	private void exportClipboardMMLTrack() {
		MMLOutputPanel.copyToClipboard(parentFrame, outputText, AppResource.appText("mml.output.done"));
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

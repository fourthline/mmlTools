/*
 * Copyright (C) 2022-2025 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.AAMMLExport;
import jp.fourthline.mmlTools.MMLConverter;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;


public final class MMLExportPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -1504636951822574399L;
	private TrackListTable table;
	private final JDialog dialog;
	private final Frame parentFrame;
	private final MMLScore score;
	private final Supplier<File> fileSupplier;

	private final Dimension prefSize = new Dimension(460, 360);
	private final JLabel outputTextCountLabel = new JLabel();
	private String outputText;
	private JButton fileExportButton;
	private JButton copyButton;

	private final JButton errDetailButton = new JButton(AppResource.appText("Err Detail"));

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
		fileExportButton.addActionListener((t) -> exportFileMMLTrack());

		copyButton = new JButton(AppResource.appText("mml.export.clipboard"));
		copyButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(copyButton);
		copyButton.addActionListener(t -> exportClipboardMMLTrack());

		errDetailButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(errDetailButton);
		errDetailButton.addActionListener(ActionDispatcher.getInstance());
		errDetailButton.setActionCommand(ActionDispatcher.MML_ERR_LIST);
		errDetailButton.setEnabled(false);

		JButton closeButton = new JButton(AppResource.appText("mml.output.closeButton"));
		closeButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((t) -> dialog.setVisible(false));

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

		UIUtils.dialogCloseAction(dialog);

		// format panel (north)
		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.Y_AXIS));
		formatPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		var typeAA = new MMLConverterSelectButton("mml.export.archeage", () -> new AAMMLExport(), "mml.export.archeage_done");
		formatPanel.add(typeAA);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(typeAA);

		typeAA.setSelected(true);
		sup = typeAA;

		add(buttonPanel, BorderLayout.SOUTH);
		add(p, BorderLayout.CENTER);
		add(formatPanel, BorderLayout.NORTH);

		// 初回更新
		updateText();
	}

	public static class MMLConverterSelectButton extends JRadioButton {
		private static final long serialVersionUID = -6467870455274074888L;
		private final Supplier<MMLConverter> converter;
		private final String doneMessage;
		public MMLConverterSelectButton(String name, Supplier<MMLConverter> converter, String doneMessage) {
			super(AppResource.appText(name));
			this.converter = converter;
			this.doneMessage = AppResource.appText(doneMessage);
		}
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

		if (sup != null) {
			MMLConverter export = sup.converter.get();
			errDetailButton.setEnabled(false);
			boolean valid = true;
			outputText = "";
			try {
				outputText = export.convertMML(eventList, score.getTempoEventList());
				outputTextCountLabel.setText(Integer.toString(outputText.length()) + " (" + export.getPartCount() + ")");
			} catch (MMLExceptionList e) {
				score.getMMLErr().clear();
				score.getMMLErr().addAll(e.getErr());
				errDetailButton.setEnabled(true);
				parentFrame.repaint();
				outputTextCountLabel.setText("Error");
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			// 出力できるものがない場合は出力系ボタンを無効にする
			boolean b = (outputText.length() > 0) && valid;
			fileExportButton.setEnabled(b);
			copyButton.setEnabled(b);
		}
	}

	private void exportFileMMLTrack() {
		boolean loop = true;
		File file = fileSupplier.get();
		while (loop && (file != null)) {
			try (FileOutputStream stream = new FileOutputStream(file)) {
				stream.write(outputText.getBytes());
				JOptionPane.showMessageDialog(parentFrame, sup.doneMessage + "\n"+file.getAbsolutePath());
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

	private MMLConverterSelectButton sup;
	@Override
	public void actionPerformed(ActionEvent e) {
		var source = e.getSource();
		if (source instanceof MMLConverterSelectButton s) {
			sup = s;
			updateText();
		}
	}
}

/*
 * Copyright (C) 2022-2025 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mabiicco.ui.table.PartListTable;
import jp.fourthline.mmlTools.MMLConverter;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLTickTable.Switch;


public final class MMLExportPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -1504636951822574399L;
	private PartListTable table;
	private final JDialog dialog;
	private final Frame parentFrame;
	private final MMLScore score;
	private final Supplier<File> fileSupplier;

	private final Dimension prefSize = new Dimension(460, 400);
	private final JLabel outputTextCountLabel = new JLabel();
	private String outputText;
	private JButton fileExportButton;
	private JButton copyButton;

	private final JButton errDetailButton = new JButton(AppResource.appText("Err Detail"));

	private final JCheckBox allowNopt = new JCheckBox(AppResource.appText("mml.export.options.allowNopt"));
	private final JCheckBox allTempoPart = new JCheckBox(AppResource.appText("mml.export.options.allTempoPart"));

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

		UIUtils.dialogCloseAction(dialog);

		// format panel (north)
		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.Y_AXIS));
		formatPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		var typeMb = new MMLConverterSelectButton("mml.export.mabi_mobile", () -> new MMLConverter(Switch.MB), "mml.export.mabi_mobile_done");
		formatPanel.add(typeMb);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(typeMb);

		typeMb.addActionListener(this);

		typeMb.setSelected(true);
		sup = typeMb;

		table = new PartListTable(trackList, true, 6);
		scrollPane.setViewportView(table);

		outputTextCountLabel.setBounds(22, 242, 300, 14);
		p.add(outputTextCountLabel);

		table.setDefaultEditor(Object.class, null);
		table.addPropertyChangeListener(t -> updateText());

		// Option
		var optionPanel = UIUtils.createTitledPanel("mml.export.options");
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 20, 2, 20), optionPanel.getBorder()));
		optionPanel.add(allowNopt);
		optionPanel.add(allTempoPart);
		allowNopt.setFocusable(false);
		allTempoPart.setFocusable(false);
		allowNopt.addActionListener(t -> updateText());
		allTempoPart.addActionListener(t -> updateText());
		var wP = new JPanel(new BorderLayout());
		wP.add(formatPanel, BorderLayout.CENTER);
		wP.add(optionPanel, BorderLayout.SOUTH);
	
		// panel
		add(buttonPanel, BorderLayout.SOUTH);
		add(p, BorderLayout.CENTER);
		add(wP, BorderLayout.NORTH);

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

	private String lengthText(String mml) {
		StringBuilder sb = new StringBuilder();
		if (mml.startsWith("MML@")) {
			mml = mml.replaceAll(";", "");
			var t = mml.substring(4).split(",");
			for (int i = 0; i < t.length; i++) {
				if (i != 0) {
					sb.append(", ");
				}
				sb.append(t[i].length());
			}
		}
		return sb.toString();
	}

	private void updateText() {
		var list = table.getCheckedEventList();
		table.repaint();

		if (sup != null) {
			MMLConverter export = sup.converter.get();
			export.setOption(allTempoPart.isSelected(), allowNopt.isSelected());
			boolean valid = true;
			errDetailButton.setEnabled(false);
			outputText = "";
			score.getMMLErr().clear();
			try {
				outputText = export.convertMML(list);
				if (outputText == null) {
					outputText = "";
					outputTextCountLabel.setText("Verify Error");
				} else {
					outputTextCountLabel.setText(lengthText(outputText));
				}
			} catch (MMLExceptionList e) {
				score.getMMLErr().addAll(e.getErr());
				errDetailButton.setEnabled(true);
				outputTextCountLabel.setText("Error");
			}

			parentFrame.repaint();

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

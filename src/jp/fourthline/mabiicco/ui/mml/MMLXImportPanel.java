/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.core.MMLText;

public final class MMLXImportPanel extends JPanel {

	private static final long serialVersionUID = -4785719857155935699L;
	private final static int MAX_N = 10;
	private final JTable table;
	private final DefaultTableModel tableModel;
	private final List<MMLText> textList;
	private final JLabel rankText = new JLabel();

	private final JDialog dialog;
	private final Frame parentFrame;
	private final String trackName;
	IMMLManager mmlManager;

	private final JButton pasteButton = new JButton(AppResource.appText("mml_x_import.paste"));
	private final JButton deleteButton = new JButton(AppResource.appText("mml_x_import.delete"));
	private final JButton convertButton = new JButton(AppResource.appText("mml_x_import.merge"));
	private final JButton cancelButton = new JButton(AppResource.appText("mml_x_import.cancel"));
	private final JCheckBox excludeSongCheckBox = new JCheckBox(AppResource.appText("instrument.excludeSongPart"));

	public MMLXImportPanel(Frame parentFrame, String trackName, IMMLManager mmlManager) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("menu.mml_x_import"), true);
		UIUtils.dialogCloseAction(dialog);

		this.parentFrame = parentFrame;
		this.trackName = trackName;
		this.mmlManager = mmlManager;

		textList = new ArrayList<MMLText>();
		IntStream.range(0, MAX_N).forEach(i -> textList.add(new MMLText()));
		String columnNames[] = { "#", AppResource.appText("mml.output.rank") };
		tableModel = new DefaultTableModel(columnNames, MAX_N);
		table = new JTable(tableModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(40);
		table.getColumnModel().getColumn(1).setPreferredWidth(240);table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setRequestFocusEnabled(false);
		table.setFocusable(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionInterval(0, 0);

		initializePanel();
		updateData();
	}

	private void initializePanel() {
		setLayout(new BorderLayout());

		pasteButton.addActionListener(t -> {
			var str = MMLInputPanel.getClipboardString();
			var mml = new MMLText().setMMLText(str);
			if (!mml.isEmpty()) {
				int index = table.getSelectedRow();
				textList.get(index).setMMLText(str);
				updateData();
				nextSelect();
			}
		});
		deleteButton.addActionListener(t -> {
			textList.set(table.getSelectedRow(), new MMLText());
			updateData();
		});
		convertButton.addActionListener(t -> {
			dialog.setVisible(false);
			MMLInputPanel mmlInputDialog = new MMLInputPanel(parentFrame, trackName, mmlManager, excludeSongCheckBox.isSelected());
			mmlInputDialog.showDialog(textJoin().getMML());
		});
		cancelButton.addActionListener(t -> {
			dialog.setVisible(false);
		});
		excludeSongCheckBox.addActionListener(t -> updateData());

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(table.getPreferredSize().width, table.getPreferredSize().height + 60));
		scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel1 = new JPanel();
		JPanel buttonPanel2 = new JPanel();
		buttonPanel1.add(pasteButton);
		buttonPanel1.add(deleteButton);
		buttonPanel2.add(convertButton);
		buttonPanel2.add(cancelButton);

		JPanel mainPanel = UIUtils.createTitledPanel("mml_x_import.split", new BorderLayout());
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(buttonPanel1, BorderLayout.SOUTH);
		JPanel mainWrapPanel = new JPanel();
		mainWrapPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		mainWrapPanel.add(mainPanel);

		JPanel footPanel = new JPanel(new BorderLayout());
		JPanel rankPanel = new JPanel();
		rankPanel.add(rankText);
		JPanel checkButtonPanel = new JPanel();
		checkButtonPanel.add(excludeSongCheckBox);
		footPanel.add(rankPanel, BorderLayout.NORTH);
		footPanel.add(checkButtonPanel, BorderLayout.CENTER);
		footPanel.add(buttonPanel2, BorderLayout.SOUTH);

		add(mainWrapPanel, BorderLayout.CENTER);
		add(footPanel, BorderLayout.SOUTH);
	}

	MMLText textJoin() {
		var r = new MMLText();
		for (var text : textList) {
			r.join(text);
		}
		return r;
	}

	private void updateData() {
		boolean exludeSongPart = excludeSongCheckBox.isSelected();

		for (int i = 0; i < textList.size(); i++) {
			tableModel.setValueAt(i+1, i, 0);
			var text = textList.get(i);
			text.setExcludeSongPart(exludeSongPart);
			var value = text.isEmpty() ? "-" : text.mmlRankFormat();
			tableModel.setValueAt(value, i, 1);
		}

		var r = textJoin();
		r.setExcludeSongPart(exludeSongPart);
		rankText.setText(r.mmlRankFormat());
		convertButton.setEnabled(!r.isEmpty());
	}

	private void nextSelect() {
		int row = table.getSelectedRow();
		if (row + 1 < table.getRowCount()) {
			row++;
			table.setRowSelectionInterval(row, row);
		}
	}

	List<MMLText> getTextList() {
		return textList;
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

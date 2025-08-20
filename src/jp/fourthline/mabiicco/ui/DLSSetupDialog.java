/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.ui;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.FixFileChooser;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.midi.DLSLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DLSSetupDialog extends JDialog {
	private static final long serialVersionUID = -2667995940954447811L;

	private final FileTableModel tableModel;
	private final JButton removeButton = new JButton(AppResource.appText("remove"));
	private boolean changed = false;

	private static class FileTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -6882861566466408372L;
		private final String[] columnNames = { AppResource.appText("file.dls") };
		private final List<File> files;

		public FileTableModel(List<File> files) {
			super();
			this.files = new ArrayList<>(files);
		}

		@Override
		public int getRowCount() {
			return files.size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return files.get(rowIndex).getAbsolutePath();
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		private void reset(List<File> fileList) {
			this.files.clear();
			this.files.addAll(new DLSLoader(fileList).getFileList());
			fireTableDataChanged();
		}

		private void addRow(List<File> fileList) {
			for (var file : fileList) {
				if (!this.files.contains(file)) {
					this.files.add(file);
				}
			}
			fireTableDataChanged();
		}

		private void removeRow(int row) {
			if ((row >= 0) && (row < files.size())) {
				files.remove(row);
			}
			fireTableDataChanged();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DLSSetupDialog(JFrame parent) {
		super(parent, AppResource.appText("menu.select_dls"), true);

		// Table
		tableModel = new FileTableModel(MabiIccoProperties.getInstance().getDlsFile());
		var table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				removeButton.setEnabled(table.getSelectedRow() != -1);
			}
		});

		// Buttons
		JButton addButton = new JButton(AppResource.appText("add"));
		addButton.addActionListener(e -> addDLSFile(showAddDLSFileDialog()));
		removeButton.setEnabled(false);
		removeButton.addActionListener(e -> removeRow(table.getSelectedRow()));
		JButton defaultButton = new JButton(AppResource.appText("default"));
		defaultButton.addActionListener(e -> setDefault());
		addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		defaultButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(addButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(defaultButton);

		// Layout
		var hint = "<html>" + AppResource.appText("start.selectDLS.hint").replace("\n", "<br>") + "</html>";
		JScrollPane scrollPane = new JScrollPane(table);
		JPanel centerPanel = UIUtils.createTitledPanel(hint, new BorderLayout(5, 5), 10);
		centerPanel.add(scrollPane, BorderLayout.CENTER);
		centerPanel.add(buttonPanel, BorderLayout.EAST);

		// OK/Cancel
		JButton okButton = new JButton(AppResource.appText("ok"));
		JButton cancelButton = new JButton(AppResource.appText("cancel"));
		okButton.addActionListener(e -> onOK());
		cancelButton.addActionListener(e -> onCancel());

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(okButton);
		bottomPanel.add(cancelButton);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		pack();
		setSize(new Dimension(600, 300));
		setLocationRelativeTo(parent);
	}

	void addDLSFile(List<File> files) {
		if (files != null) {
			tableModel.addRow(files);
		}
	}

	private List<File> showAddDLSFileDialog() {
		JFileChooser fileChooser = new FixFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileNameExtensionFilter("DLS File (*.dls)", "dls"));
		String recentPath = MabiIccoProperties.getInstance().getRecentFile();
		if (recentPath != null && !recentPath.isEmpty()) {
			fileChooser.setCurrentDirectory(new File(recentPath).getParentFile());
		}

		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			return Arrays.asList(fileChooser.getSelectedFiles());
		}
		return null;
	}

	void removeRow(int selectedRow) {
		if (selectedRow != -1) {
			tableModel.removeRow(selectedRow);
		}
	}

	void setDefault() {
		tableModel.reset(MabiIccoProperties.getInstance().getDlsDefaultFile());
	}

	void onOK() {
		var loader = new DLSLoader(tableModel.files);
		MabiIccoProperties.getInstance().setDlsFile(loader.getFileList());
		changed = true;

		dispose();
	}

	private void onCancel() {
		dispose();
	}

	List<File> getFileList() {
		return tableModel.files;
	}

	/**
	 * Show the dialog.
	 * @return true if the DLS settings were changed, false otherwise.
	 */
	public boolean showDialog() {
		setVisible(true);
		return changed;
	}
}

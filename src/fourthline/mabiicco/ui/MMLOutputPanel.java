/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;
import java.awt.Frame;
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
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;


import javax.swing.table.TableColumnModel;
import javax.swing.JScrollPane;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTools;

public class MMLOutputPanel extends JPanel {
	private static final long serialVersionUID = 8558159209741558854L;
	private JTable table;
	private final JDialog dialog;
	private final JButton copyButton = new JButton(AppResource.getText("mml.output.copyButton"));

	private List<MMLTrack> trackList;

	public MMLOutputPanel(Frame parentFrame) {
		this.dialog = null;
		initializePanel(null);
	}

	public MMLOutputPanel(Frame parentFrame, List<MMLTrack> trackList) {
		this.dialog = new JDialog(parentFrame, AppResource.getText("mml.output"), true);
		initializePanel(trackList);
	}

	private void initializePanel(List<MMLTrack> trackList) {
		this.trackList = trackList;
		setLayout(null);

		copyButton.setBounds(141, 189, 90, 29);
		add(copyButton);
		copyButton.addActionListener((event) -> {
			currentSelectedPartMMLOutput();
		});

		JButton closeButton = new JButton(AppResource.getText("mml.output.closeButton"));
		closeButton.setBounds(257, 189, 90, 29);
		add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((event) -> {
			dialog.setVisible(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 372, 169);
		add(scrollPane);

		table = createJTableFromMMLTrack(trackList);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);

		table.setDefaultEditor(Object.class, null);
		table.setFocusable(false);
		table.setRowSelectionInterval(0, 0);

		InputMap imap = dialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		dialog.getRootPane().getActionMap().put("close-it", new AbstractAction() {
			private static final long serialVersionUID = -4495368209645211523L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}});
	}

	public static JTable createJTableFromMMLTrack(List<MMLTrack> trackList) {
		String columnNames[] = {
				AppResource.getText("mml.output.trackName"),
				AppResource.getText("mml.output.instrument"),
				AppResource.getText("mml.output.rank")
		};
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
		JTable jTable = new JTable(tableModel);
		if (trackList != null) {
			for (MMLTrack track : trackList) {
				MMLTools tools = new MMLTools( track.getMMLString() );
				InstClass inst = MabiDLS.getInstance().getInstByProgram(track.getProgram());
				String rowData[] = {
						track.getTrackName(), inst.toString(), tools.mmlRankFormat()
				};
				tableModel.addRow(rowData);
			}
		}

		TableColumnModel columnModel = jTable.getColumnModel();
		columnModel.getColumn(2).setPreferredWidth(180);

		return jTable;
	}

	private void copyToClipboard(String text) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();
		clip.setContents(new StringSelection(text), null);
	}

	private void currentSelectedPartMMLOutput() {
		int row = table.getSelectedRow();
		String mmlText = trackList.get(row).getMMLString();
		copyToClipboard(mmlText);
		JOptionPane.showMessageDialog(this, AppResource.getText("mml.output.done"));

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

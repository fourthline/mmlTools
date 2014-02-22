/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTools;

public class MMLOutputPanel extends JPanel {
	private static final long serialVersionUID = 8558159209741558854L;
	private JTable table;
	private JDialog dialog = new JDialog((Dialog)null, "クリップボードへ出力", true);
	JButton copyButton = new JButton("MMLコピー");

	private MMLTrack trackList[];

	public MMLOutputPanel() {
		initializePanel(null);
	}

	public MMLOutputPanel(MMLTrack trackList[]) {
		initializePanel(trackList);
	}

	private void initializePanel(MMLTrack trackList[]) {
		this.trackList = trackList;
		setLayout(null);

		copyButton.setBounds(141, 189, 84, 29);
		add(copyButton);
		copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				currentSelectedPartMMLOutput();
			}
		});

		JButton closeButton = new JButton("閉じる");
		closeButton.setBounds(257, 189, 84, 29);
		add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 345, 169);
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

	private JTable createJTableFromMMLTrack(MMLTrack trackList[]) {
		String columnNames[] = {
				"トラック名", "楽器", "作曲ランク"
		};
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
		JTable jTable = new JTable(tableModel);

		for (MMLTrack track : trackList) {
			MMLTools tools = new MMLTools( track.getMMLString() );
			InstClass inst = MabiDLS.getInstance().getInstByProgram(track.getProgram());
			String rowData[] = {
					track.getTrackName(), inst.toString(), tools.mmlRankFormat()
			};
			tableModel.addRow(rowData);
		}

		TableColumnModel columnModel = jTable.getColumnModel();
		columnModel.getColumn(2).setPreferredWidth(140);

		return jTable;
	}

	private void copyToClipboard(String text) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();
		clip.setContents(new StringSelection(text), null);
	}

	private void currentSelectedPartMMLOutput() {
		int row = table.getSelectedRow();
		String mmlText = trackList[row].getMMLString();
		copyToClipboard(mmlText);
		JOptionPane.showMessageDialog(this, "クリップボードにコピーしました.");

		row++;
		if (row >= trackList.length) {
			row = 0;
		}
		table.setRowSelectionInterval(row, row);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(370, 240);
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

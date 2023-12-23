/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.parser.IMMLFileParser;
import jp.fourthline.mmlTools.parser.MidiFile;

public final class ParsePropertiesDialog extends JPanel {
	private static final long serialVersionUID = -6502564484362288491L;

	private final Frame parentFrame;
	private final List<JCheckBox> checkBoxList = new ArrayList<>();
	private final boolean noOption;
	private final IMMLFileParser parser;
	private final Map<String, JComboBox<?>> comboMap = new HashMap<>();

	public ParsePropertiesDialog(Frame parentFrame, IMMLFileParser parser) {
		this.parentFrame = parentFrame;
		this.parser = parser;
		noOption = (parser.getParseProperties() == null) && (parser.getParseAttributes() == null);
		if (!noOption) {
			initializePanel();
		}
	}

	private void initializePanel() {
		JPanel attrPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		setLayout(new BorderLayout());
		if (parser.getParseProperties() != null) {
			for (String key : parser.getParseProperties().keySet()) {
				JCheckBox checkBox = new JCheckBox(AppResource.appText(key), parser.getParseProperties().get(key));
				attrPanel.add(checkBox);
				checkBoxList.add(checkBox);
			}
		}
		if (parser.getParseAttributes() != null) {
			for (String key : parser.getParseAttributes().keySet()) {
				var items = parser.getParseAttributes().get(key).stream().map(t -> AppResource.appText(t)).toArray(String[]::new);
				JComboBox<String> c = new JComboBox<>(items);
				c.setFocusable(false);
				JLabel label = new JLabel(AppResource.appText(key));
				JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
				p.add(label);
				p.add(c);
				attrPanel.add(p);
				attrPanel.add(new JPanel());   // JComboBoxの性能劣化対策
				comboMap.put(key, c);
			}
		}
		if (parser.getTrackSelectMap() != null) {
			Vector<Object> colH = new Vector<>();
			colH.add(""); colH.add("#"); colH.add("Track");
			Vector<Vector<Object>> data = new Vector<>();
			int index = 0;
			for (var key : parser.getTrackSelectMap().keySet()) {
				var trackSelect = parser.getTrackSelectMap().get(key);
				Vector<Object> rowData = new Vector<>();
				rowData.add(trackSelect.isEnabled());
				rowData.add(++index);
				rowData.add(trackSelect.toString());
				data.add(rowData);
			}
			JTable table = new JTable((new DefaultTableModel(data, colH) {
				private static final long serialVersionUID = -530181826789266153L;
				@Override
				public Class<?> getColumnClass(int col){
					return getValueAt(0, col).getClass();
				}
				@Override
				public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
					super.setValueAt(aValue, rowIndex, columnIndex);
					if (columnIndex == 0) {
						// チェックボックスの内容を反映する
						var key = parser.getTrackSelectMap().keySet().toArray()[rowIndex];
						parser.getTrackSelectMap().get(key).setEnable(aValue.equals(Boolean.TRUE));
					}
				}
			}));
			table.getColumnModel().getColumn(0).setPreferredWidth(0);
			table.getColumnModel().getColumn(1).setPreferredWidth(0);
			table.getColumnModel().getColumn(2).setPreferredWidth(200);
			table.setRowSelectionAllowed(false);
			table.getTableHeader().setReorderingAllowed(false);
			table.setFocusable(false);

			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(300, 240));
			JPanel trackPanel = new JPanel(new BorderLayout());
			trackPanel.add(scrollPane, BorderLayout.CENTER);

			// すべて選択, すべて解除ボタン
			JPanel panelT = new JPanel();
			JButton allSelectButton = new JButton(AppResource.appText("menu.selectAll"));
			allSelectButton.addActionListener(t -> {
				int count = table.getModel().getRowCount();
				for (int i = 0; i < count; i++) {
					table.getModel().setValueAt(true, i, 0);
				}
				repaint();
			});
			panelT.add(allSelectButton);

			JButton allClearButton = new JButton(AppResource.appText("menu.selectClear"));
			allClearButton.addActionListener(t -> {
				int count = table.getModel().getRowCount();
				for (int i = 0; i < count; i++) {
					table.getModel().setValueAt(false, i, 0);
				}
				repaint();
			});
			panelT.add(allClearButton);
			trackPanel.add(panelT, BorderLayout.NORTH);

			add(trackPanel, BorderLayout.EAST);
		}

		add(attrPanel, BorderLayout.CENTER);
	}

	private void apply() {
		var keyList = parser.getParseProperties().keySet().toArray(new String[0]);
		for (int i = 0; i < parser.getParseProperties().size(); i++) {
			parser.getParseProperties().put(keyList[i], checkBoxList.get(i).isSelected());
		}
		comboMap.entrySet().forEach(t -> {
			var key = t.getKey();
			var valueArray = parser.getParseAttributes().get(key).toArray(String[]::new);
			String value = valueArray[t.getValue().getSelectedIndex()];
			parser.setParseAttribute(key, value);
		});
	}

	/**
	 * ダイアログを表示する.
	 */
	public boolean showDialog() {
		if (noOption) {
			return true;
		}
		String title = parser.getName();
		if (!title.isEmpty()) {
			title += " ";
		}
		title += AppResource.appText("parse.dialogTitle");
		int ret = JOptionPane.showConfirmDialog(parentFrame, this, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (ret == JOptionPane.OK_OPTION) {
			apply();
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		new ParsePropertiesDialog(null, new MidiFile()).showDialog();
	}
}

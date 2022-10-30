/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
		setLayout(new GridLayout(0, 1, 0, 0));
		if (parser.getParseProperties() != null) {
			for (String key : parser.getParseProperties().keySet()) {
				JCheckBox checkBox = new JCheckBox(AppResource.appText(key), parser.getParseProperties().get(key));
				add(checkBox);
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
				add(p);
				add(new JPanel());   // JComboBoxの性能劣化対策
				comboMap.put(key, c);
			}
		}
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
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		new ParsePropertiesDialog(null, new MidiFile()).showDialog();
	}
}

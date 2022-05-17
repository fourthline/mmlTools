/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.parser.IMMLFileParser;
import jp.fourthline.mmlTools.parser.MidiFile;

public final class ParsePropertiesDialog extends JPanel {
	private static final long serialVersionUID = -6502564484362288491L;

	private final Frame parentFrame;
	private final String name;
	private final List<JCheckBox> checkBoxList = new ArrayList<>();
	private final Map<String, Boolean> parseProperties;
	private final boolean noOption;

	public ParsePropertiesDialog(Frame parentFrame, IMMLFileParser parser) {
		this.parentFrame = parentFrame;
		this.parseProperties = parser.getParseProperties();
		this.name = parser.getName();
		noOption = (this.parseProperties == null);
		if (!noOption) {
			initializePanel();
		}
	}

	private void initializePanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (String key : parseProperties.keySet()) {
			JCheckBox checkBox = new JCheckBox(AppResource.appText(key), parseProperties.get(key));
			add(checkBox);
			checkBoxList.add(checkBox);
		}
	}

	/**
	 * ダイアログを表示する.
	 */
	public boolean showDialog() {
		if (noOption) {
			return true;
		}
		String title = this.name;
		if (!title.isEmpty()) {
			title += " ";
		}
		title += AppResource.appText("parse.dialogTitle");
		int ret = JOptionPane.showConfirmDialog(parentFrame, this, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (ret == JOptionPane.OK_OPTION) {
			var keyList = parseProperties.keySet().toArray(new String[0]);
			for (int i = 0; i < parseProperties.size(); i++) {
				parseProperties.put(keyList[i], checkBoxList.get(i).isSelected());
			}
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		new ParsePropertiesDialog(null, new MidiFile()).showDialog();
	}
}

/*
 * Copyright (C) 2014-2025 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.InstType.DrumsType;
import jp.fourthline.mabiicco.midi.InstType.NormalType;
import jp.fourthline.mabiicco.midi.InstType.PercussionType;
import jp.fourthline.mabiicco.midi.InstType.SongType;
import jp.fourthline.mabiicco.midi.MabiDLS;

public final class About {
	private String readNotice() {
		try {
			var stream = About.class.getResourceAsStream("/NOTICE");
			if (stream == null) {
				stream = new FileInputStream("NOTICE");
			}
			var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			var str = reader.lines().collect(Collectors.joining("\n"));
			stream.close();
			return str;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void show(Frame parentFrame) {
		String title = AppResource.appText("menu.about");
		StringBuilder sb = new StringBuilder();
		sb.append("Version:  ").append(AppResource.getVersionText());
		sb.append('\n').append("Runtime: ").append(AppResource.getRuntimeVersion());
		JOptionPane.showMessageDialog(parentFrame, sb.toString(), title, JOptionPane.PLAIN_MESSAGE);
	}

	public void showLicenses(Frame parentFrame) {
		String title = AppResource.appText("3rd_licenses");
		JPanel mainPanel = new JPanel(new BorderLayout());
		JTabbedPane tab = new JTabbedPane();
		mainPanel.add(tab, BorderLayout.CENTER);

		// NOTICE
		String notice = readNotice();
		String[] text = notice.split("================================================================================\n=");

		// libraries
		for (int i = 1; i < text.length; i++) {
			String name = text[i].substring(0, text[i].indexOf('\n')).trim();
			JTextArea textArea = new JTextArea(text[i]);
			textArea.setEditable(false);
			textArea.setCaretPosition(0);
			tab.add(name, new JScrollPane(textArea));
		}

		mainPanel.setPreferredSize(new Dimension(600, 400));
		JOptionPane.showMessageDialog(parentFrame, mainPanel, title, JOptionPane.PLAIN_MESSAGE);
	}

	@SuppressWarnings("deprecation")
	private String getAccText(List<KeyStroke> accList) {
		StringBuilder accText = new StringBuilder();
		accList.forEach(acc -> {
			if (acc != null) {
				if (accText.length() > 0) {
					accText.append(", ");
				}
				int modifiers = acc.getModifiers();
				if (modifiers > 0) {
					accText.append(KeyEvent.getKeyModifiersText(modifiers));
					accText.append("+");
				}
				int keyCode = acc.getKeyCode();
				if (keyCode != 0) {
					accText.append(KeyEvent.getKeyText(keyCode));
				} else {
					accText.append(acc.getKeyChar());
				}
			}
		});
		return accText.toString();
	}

	/**
	 * ショートカットキー情報の表示
	 * @param parentFrame
	 * @param map
	 */
	public void showShortcutInfo(Frame parentFrame, Map<String, List<KeyStroke>> map) {
		Vector<String> column = new Vector<>();
		column.add(AppResource.appText("shortcut.table.key"));
		column.add(AppResource.appText("shortcut.table.function"));

		Vector<Vector<String>> list = new Vector<>();
		map.entrySet().forEach(t -> {
			var key = t.getKey();
			var value = t.getValue();
			var v = new Vector<String>();
			v.add(getAccText(value));
			v.add(key);
			list.add(v);
		});

		JTable table = UIUtils.createTable(list, column);
		table.getColumnModel().getColumn(0).setMinWidth(200);
		table.getColumnModel().getColumn(0).setMaxWidth(200);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		String title = AppResource.appText("menu.shortcutInfo");
		JOptionPane.showMessageDialog(parentFrame, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
	}

	private Icon getInstIcon(InstClass inst) {
		var type = inst.getType();
		if (type instanceof SongType) {
			return ListItem.S_ICON;
		} else if (type instanceof DrumsType) {
			return ListItem.D_ICON;
		} else if (type instanceof PercussionType) {
			return ListItem.P_ICON;
		} else if (type instanceof NormalType) {
			return ListItem.N_ICON;
		}
		return ListItem.B_ICON;
	}

	/**
	 * DLS情報の表示
	 * @param parentFrame
	 */
	public void showInstList(Frame parentFrame) {
		var dls = MabiDLS.getInstance();
		var tm = new DefaultMutableTreeNode();
		var instList = dls.getAllInst();
		dls.getInstsMap().forEach((key, value) -> {
			var t1 = new ListItem(key, ListItem.DLS_ICON);
			value.forEach(v -> {
				var exist = instList.contains(v);
				var t2 = new ListItem(v, exist, getInstIcon(v));
				t1.add(t2);
				if (exist) {
					t2.add(new ListItem(v.getMidiName(), ListItem.B_ICON));
					t2.add(new ListItem("transposable: " + v.getType().allowTranspose(), null));
					for (int i = v.getUpperNote(); i >= v.getLowerNote(); i--) {
						if (v.isValid(i)) {
							t2.add(new ListItem(String.format("%s: %s%s", i, v.getAttention(i), v.isOverlap(i)?", overlap":""), null));
						}
					}
				}
			});
			tm.add(t1);
		});
		JTree tree = new JTree(tm);
		tree.setCellRenderer(new DLSInfoRenderer());
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		String title = AppResource.appText("menu.instList");
		JOptionPane.showMessageDialog(parentFrame, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
	}

	private static class DLSInfoRenderer extends JLabel implements TreeCellRenderer {
		private static final long serialVersionUID = 4935457943334555902L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
			setText(stringValue);
			if (value instanceof ListItem node) {
				setIcon(node.icon);
			} else {
				setIcon(null);
			}
			return this;
		}
	}

	private static class ListItem extends DefaultMutableTreeNode {
		private static final long serialVersionUID = -3147229489704024291L;
		private static final Icon N_ICON = new CircleIcon(8, Color.BLUE);
		private static final Icon P_ICON = new CircleIcon(8, Color.ORANGE);
		private static final Icon S_ICON = new CircleIcon(8, Color.GREEN);
		private static final Icon D_ICON = new CircleIcon(8, Color.MAGENTA);
		private static final Icon B_ICON = new CircleIcon(8, Color.WHITE, Color.GRAY);
		private static final Icon DLS_ICON = AppResource.getImageIcon("/img/dls_icon.png");
		private final Icon icon;

		private ListItem(Object obj, Icon icon) {
			this(obj, true, icon);
		}

		private ListItem(Object obj, boolean b, Icon icon) {
			super(obj, b);
			this.icon = icon;
		}
	}
}

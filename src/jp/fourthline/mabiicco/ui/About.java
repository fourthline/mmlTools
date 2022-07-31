/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Map;
import java.util.Vector;

import javax.sound.midi.Instrument;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.InstType.NormalType;
import jp.fourthline.mabiicco.midi.InstType.PercussionType;
import jp.fourthline.mabiicco.midi.InstType.SongType;
import jp.fourthline.mabiicco.midi.MabiDLS;

public final class About {
	public void show(Frame parentFrame) {
		String title = AppResource.appText("menu.about");
		StringBuilder sb = new StringBuilder();
		sb.append("Version:  ").append(AppResource.getVersionText());
		sb.append('\n').append("Runtime: ").append(AppResource.getRuntimeVersion());
		JOptionPane.showMessageDialog(parentFrame, sb.toString(), title, JOptionPane.PLAIN_MESSAGE);
	}


	@SuppressWarnings("deprecation")
	private String getAccText(KeyStroke acc) {
		String accText = "";
		if (acc != null) {
			int modifiers = acc.getModifiers();
			if (modifiers > 0) {
				accText = KeyEvent.getKeyModifiersText(modifiers);
				accText += "+";
			}
			int keyCode = acc.getKeyCode();
			if (keyCode != 0) {
				accText += KeyEvent.getKeyText(keyCode);
			} else {
				accText += acc.getKeyChar();
			}
		}
		return accText;
	}

	/**
	 * ショートカットキー情報の表示
	 * @param parentFrame
	 * @param map
	 */
	public void showShortcutInfo(Frame parentFrame, Map<KeyStroke, String> map) {
		Vector<String> column = new Vector<>();
		column.add(AppResource.appText("shortcut.table.key"));
		column.add(AppResource.appText("shortcut.table.function"));

		Vector<Vector<String>> list = new Vector<>();
		map.entrySet().forEach(t -> {
			var key = t.getKey();
			var value = t.getValue();
			var v = new Vector<String>();
			v.add(getAccText(key));
			v.add(value);
			list.add(v);
		});

		JTable table = new JTable(new DefaultTableModel(list, column) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		table.getColumnModel().getColumn(0).setMinWidth(200);
		table.getColumnModel().getColumn(0).setMaxWidth(200);
		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setRequestFocusEnabled(false);
		table.setFocusable(false);
		table.setRowSelectionAllowed(false);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		String title = AppResource.appText("menu.shortcutInfo");
		JOptionPane.showMessageDialog(parentFrame, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
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
			var t1 = new DefaultMutableTreeNode(key);
			value.forEach(v -> {
				var exist = instList.contains(v);
				var t2 = new DefaultMutableTreeNode(v, exist);
				t1.add(t2);
				if (exist) {
					t2.add(new DefaultMutableTreeNode(v.getInstrument()));
					for (int i = v.getUpperNote(); i >= v.getLowerNote(); i--) {
						if (v.isValid(i)) {
							t2.add(new DefaultMutableTreeNode(String.format("%s: %s%s", i, v.getAttention(i), v.isOverlap(i)?", overlap":"")));
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
		private static final Icon N_ICON = new CircleIcon(10, Color.BLUE, null);
		private static final Icon P_ICON = new CircleIcon(10, Color.ORANGE, null);
		private static final Icon S_ICON = new CircleIcon(10, Color.GREEN, null);
		private static final Icon B_ICON = new CircleIcon(8, null, Color.GRAY);
		private static final Icon DLS_ICON = AppResource.getImageIcon("/img/dls_icon.png");

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
			setText(stringValue);
			setIcon(null);
			if (value instanceof DefaultMutableTreeNode node) {
				Object obj = node.getUserObject();
				if (obj instanceof File) {
					setIcon(DLS_ICON);
				} else if (obj instanceof InstClass inst) {
					if (inst.getType() instanceof SongType) {
						setIcon(S_ICON);
					} else if (inst.getType() instanceof NormalType) {
						setIcon(N_ICON);
					} else if (inst.getType() instanceof PercussionType) {
						setIcon(P_ICON);
					}
				} else if (obj instanceof Instrument) {
					setIcon(B_ICON);
				}
			}
			return this;
		}
	}
}

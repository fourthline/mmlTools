/*
 * Copyright (C) 2022-2024 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static jp.fourthline.mabiicco.AppResource.appText;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.MabiIccoProperties.EnumProperty;

public final class UIUtils {
	private UIUtils() {}

	/**
	 * JSpinnerにフォーカス設定する
	 * @param spinner
	 */
	public static void setDefaultFocus(JSpinner spinner) {
		setDefaultFocus(((JSpinner.NumberEditor)spinner.getEditor()).getTextField());
	}

	/**
	 * JTextFieldにフォーカス設定する
	 * @param textField
	 */
	public static void setDefaultFocus(JTextField textField) {
		textField.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorRemoved(AncestorEvent event) {}

			@Override
			public void ancestorMoved(AncestorEvent event) {}

			@Override
			public void ancestorAdded(AncestorEvent event) {
				textField.requestFocusInWindow();
				textField.selectAll();
			}
		});
	}

	private static final class ScrollChainListener implements ChangeListener {
		private final JViewport target;

		private ScrollChainListener(JViewport targetViewport) {
			this.target = targetViewport;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() instanceof JViewport viewport) {
				var p = viewport.getViewPosition();
				var p2 = target.getViewPosition();
				p2.x = p.x;
				target.setViewPosition(p2);
			}
		}
	}

	/**
	 * 2個のJScrollPaneのViewportの位置を同じように動かす.
	 * @param sc1
	 * @param sc2
	 */
	public static void scrollChain(JScrollPane sc1, JScrollPane sc2) {
		sc1.getViewport().addChangeListener(new ScrollChainListener(sc2.getViewport()));
		sc2.getViewport().addChangeListener(new ScrollChainListener(sc1.getViewport()));
	}


	public static void drawRect(Graphics2D g, Color rectColor, Color fillColor, int x, int y, int width, int height) {
		g.setColor(fillColor);
		if (width != 0) {
			g.fillRect(x+1, y+1, width, height-1);
		} else {
			g.drawLine(x+1, y+1, x+1, y+height-1);
		}
		g.setColor(rectColor);
		g.drawLine(x+1, y+1, x+1, y+height-1);
		g.drawLine(x+width+1, y+height-1, x+width+1, y+1);
		g.drawLine(x+2, y, x+width, y);
		g.drawLine(x+width, y+height, x+2, y+height);
	}

	/**
	 * ラジオボタンのメニューグループを作成する
	 * @param settingMenu
	 * @param menuName
	 * @param prop
	 */
	public static void createGroupMenu(JComponent settingMenu, String menuName, EnumProperty<? extends SettingButtonGroupItem> prop) {
		var listener = ActionDispatcher.getInstance();
		JMenu menu = new JMenu(appText(menuName));
		settingMenu.add(menu);

		ButtonGroup group = new ButtonGroup();
		var defaultValue = prop.getDefault();
		for (SettingButtonGroupItem item : prop.getValues()) {
			String name = appText(item.getButtonName());
			if (item == defaultValue) {
				name += " (default)";
			}
			GroupRadioMenuItemWith<SettingButtonGroupItem> itemMenu = new GroupRadioMenuItemWith<>(name, item);
			itemMenu.setActionCommand(ActionDispatcher.CHANGE_ACTION);
			itemMenu.addActionListener(listener);
			itemMenu.setSelected(item.equals(prop.get()));
			menu.add(itemMenu);
			group.add(itemMenu);
		}
	}

	public static void createGroupMenu(JComponent menu, SettingButtonGroupItem[] items, ActionListener listener, SettingButtonGroupItem initialItem) {
		ButtonGroup group = new ButtonGroup();
		for (SettingButtonGroupItem item : items) {
			String name = appText(item.getButtonName());
			GroupRadioMenuItemWith<SettingButtonGroupItem> itemMenu = new GroupRadioMenuItemWith<>(name, item);
			itemMenu.addActionListener(listener);
			itemMenu.setSelected(item.equals(initialItem));
			menu.add(itemMenu);
			group.add(itemMenu);
		}
	}

	public static List<JMenuItem> createGroupActionMenu(JComponent menu, SettingButtonGroupItem[] items, Function<String, String> nameConvert, String command) {
		var list = new ArrayList<JMenuItem>();
		var listener = ActionDispatcher.getInstance();
		for (SettingButtonGroupItem item : items) {
			String name = nameConvert.apply(appText(item.getButtonName()));
			GroupMenuItemWith<SettingButtonGroupItem> itemMenu = new GroupMenuItemWith<>(name, item);
			itemMenu.setActionCommand(command);
			itemMenu.addActionListener(listener);
			menu.add(itemMenu);
			list.add(itemMenu);
		}
		return list;
	}

	private static class GroupRadioMenuItemWith<T> extends JRadioButtonMenuItem implements Supplier<T> {
		private static final long serialVersionUID = -7786833458520626015L;
		private final T obj;
		private GroupRadioMenuItemWith(String text, T obj) {
			super(text);
			this.obj = obj;
		}
		@Override
		public T get() {
			return obj;
		}
	}

	private static class GroupMenuItemWith<T> extends JMenuItem implements Supplier<T> {
		private static final long serialVersionUID = -3983687243046433624L;
		private final T obj;
		private GroupMenuItemWith(String text, T obj) {
			super(text);
			this.obj = obj;
		}
		@Override
		public T get() {
			return obj;
		}
	}

	public static JToolBar createToolBar() {
		return new MToolBar();
	}

	public static JToolBar createToolBar(int orientation) {
		return new MToolBar(orientation);
	}

	/**
	 * lafを切り替えたときに移動できてしまうようになってしまうことがあるので、その対策をしたツールバー.
	 */
	private static final class MToolBar extends JToolBar {
		private static final long serialVersionUID = 3018958172538601500L;

		public MToolBar() {
			super();
			setFloatable(false);
		}
		public MToolBar(int orientation) {
			super(orientation);
			setFloatable(false);
		}

		@Override
		public void updateUI() {
			super.updateUI();
			setFloatable(false);
		}
	}

	public static void dialogCloseAction(JDialog dialog) {
		dialogCloseAction(dialog, () -> {});
	}

	public static void dialogCloseAction(JDialog dialog, Runnable f) {
		InputMap imap = dialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		dialog.getRootPane().getActionMap().put("close-it", new AbstractAction() {
			private static final long serialVersionUID = 9185214975506783931L;
			@Override
			public void actionPerformed(ActionEvent arg) {
				f.run();
				dialog.setVisible(false);
			}});
	}

	public static JPanel createTitledPanel(String title) {
		return createTitledPanel(title, null);
	}

	public static JPanel createTitledPanel(String title, LayoutManager layout) {
		var panel = new JPanel(layout);
		panel.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 1, true), AppResource.appText(title), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		return panel;
	}

	/**
	 *  表示位置が正しい値になっていない場合がある問題の対策.
	 *    大本からの全体を invokeLaterで実行しても効かない.
	 * @param viewport
	 * @param p
	 */
	public static void viewportSetPositionWorkaround(JViewport viewport, Point p) {
		viewport.setViewPosition(p);
		int count = 0;
		while ( (viewport.getViewPosition().x != p.x) || (viewport.getViewPosition().y != p.y)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			viewport.setViewPosition(p);
			if (++count > 100) break;
		}
	}

	@SuppressWarnings("rawtypes")
	public static JTable createTable(Vector<? extends Vector> list, Vector<?> column) {
		JTable table = new JTable(new DefaultTableModel(list, column) {
			private static final long serialVersionUID = 746461323527083725L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setRequestFocusEnabled(false);
		table.setFocusable(false);
		table.setRowSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return table;
	}

	/**
	 * テキストアンチエイリアスの既定値を有効にするため, デフォルト値を取得して設定する.
	 * @param g
	 */
	public static void setRenderingHint(Graphics2D g) {
		var o = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
		if (o instanceof Map<?,?> map) {
			map.forEach((a, b) -> {
				if (a instanceof RenderingHints.Key key) {
					g.setRenderingHint(key, b);
				}
			});
		}
	}

	public static JCheckBox createCheckBox(String name, boolean tooltip) {
		var o = new JCheckBox(appText(name));
		if (tooltip) {
			o.setToolTipText(appText(name + ".detail"));
		}
		return o;
	}
}

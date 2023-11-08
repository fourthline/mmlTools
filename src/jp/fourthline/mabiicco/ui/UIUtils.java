/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static jp.fourthline.mabiicco.AppResource.appText;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.Supplier;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.fourthline.mabiicco.ActionDispatcher;
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
			GroupMenuItemWith<SettingButtonGroupItem> itemMenu = new GroupMenuItemWith<>(name, item);
			itemMenu.setActionCommand(ActionDispatcher.CHANGE_ACTION);
			itemMenu.addActionListener(listener);
			itemMenu.setSelected(item.equals(prop.get()));
			menu.add(itemMenu);
			group.add(itemMenu);
		}
	}

	private static class GroupMenuItemWith<T> extends JRadioButtonMenuItem implements Supplier<T> {
		private static final long serialVersionUID = -7786833458520626015L;
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
}

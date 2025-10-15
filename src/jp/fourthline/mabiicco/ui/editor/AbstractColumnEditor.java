/*
 * Copyright (C) 2022-2025 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Function;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;

public abstract class AbstractColumnEditor implements IMarkerEditor, ActionListener {

	protected abstract void viewTargetMarker(AbstractButton menu, boolean b);

	protected JMenuItem newMenuItem(String name, String actionCommand) {
		return newMenu(name, actionCommand, JMenuItem::new);
	}

	protected <T extends AbstractButton> T newMenu(String name, Function<String, T> c) {
		return newMenu(name, null, c);
	}

	protected <T extends AbstractButton> T newMenu(String name, String actionCommand, Function<String, T> c) {
		T menu = c.apply(name);
		menu.addActionListener(this);
		if (actionCommand != null) {
			menu.setActionCommand(actionCommand);
		}
		menu.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				viewTargetMarker(menu, false);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				viewTargetMarker(menu, false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				viewTargetMarker(menu, true);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				viewTargetMarker(menu, true);
			}

			@Override
			public void mouseClicked(MouseEvent e) {}
		});
		return menu;
	}
}

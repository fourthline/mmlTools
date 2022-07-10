/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;

public abstract class AbstractColumnEditor implements IMarkerEditor, ActionListener {

	protected abstract void viewTargetMarker(JMenuItem menu, boolean b);

	protected JMenuItem newMenuItem(String name, String actionCommand) {
		JMenuItem menu = new JMenuItem(name);
		menu.addActionListener(this);
		menu.setActionCommand(actionCommand);
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

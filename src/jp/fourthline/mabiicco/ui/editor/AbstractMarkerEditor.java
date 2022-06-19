/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.IViewTargetMarker;
import jp.fourthline.mmlTools.MMLEvent;

/**
 * Marker (for MMLEvent) Editor.
 *
 * <pre>
 *   {@code edit.insert_<suffix>}
 *   {@code edit.edit_<suffix>}
 *   {@code edit.delete_<suffix>}
 *   {@code edit.label_<suffix>}
 * </pre>
 * @param <T>
 * @see MMLTempoEditor
 * @see MarkerEditor
 */
abstract public class AbstractMarkerEditor<T extends MMLEvent> implements IMarkerEditor, ActionListener {

	private final JMenuItem insertMenu;
	private final JMenuItem editMenu;
	private final JMenuItem deleteMenu;

	protected final String suffix;
	protected final String insertCommand;
	protected final String editCommand;
	protected final String deleteCommand;

	private final IEditAlign editAlign;
	protected final IMMLManager mmlManager;
	private final IViewTargetMarker viewTargetMarker;

	protected T targetEvent;
	protected int targetTick;

	public AbstractMarkerEditor(String suffix, IMMLManager mmlManager, IEditAlign editAlign, IViewTargetMarker viewTargetMarker) {
		this.suffix = suffix;
		this.insertCommand = "insert_" + suffix;
		this.editCommand   = "edit_" + suffix;
		this.deleteCommand = "delete_" + suffix;
		this.mmlManager = mmlManager;
		this.editAlign = editAlign;
		this.viewTargetMarker = viewTargetMarker;

		insertMenu = newMenuItem(AppResource.appText("edit."+insertCommand));
		insertMenu.setActionCommand(insertCommand);
		editMenu = newMenuItem(AppResource.appText("edit."+editCommand));
		editMenu.setActionCommand(editCommand);
		deleteMenu = newMenuItem(AppResource.appText("edit."+deleteCommand));
		deleteMenu.setActionCommand(deleteCommand);
	}

	@Override
	public List<JMenuItem> getMenuItems() {
		return Arrays.asList(insertMenu, editMenu, deleteMenu);
	}

	private void viewTargetMarker(JMenuItem menu, boolean b) {
		if (!b || !menu.isEnabled()) {
			viewTargetMarker.PaintOff();
		} else if (targetEvent != null) {
			viewTargetMarker.PaintOnTarget(targetEvent.getTickOffset());
		} else {
			viewTargetMarker.PaintOnTarget(targetTick);
		}
	}

	protected JMenuItem newMenuItem(String name) {
		JMenuItem menu = new JMenuItem(name);
		menu.addActionListener(this);
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

	@Override
	public void activateEditMenuItem(int baseTick, int delta) {
		this.targetTick = baseTick - (baseTick % this.editAlign.getEditAlign());
		targetEvent = getTempoEventOnTick(baseTick, delta);

		// 指定範囲内にイベントがなければ、挿入のみを有効にします.
		if (targetEvent == null) {
			insertMenu.setEnabled(true);
			editMenu.setEnabled(false);
			deleteMenu.setEnabled(false);
		} else {
			insertMenu.setEnabled(false);
			editMenu.setEnabled(true);
			deleteMenu.setEnabled(true);
		}
	}

	private T getTempoEventOnTick(int baseTick, int delta) {
		for (T event : getEventList()) {
			int tick = event.getTickOffset();
			if ( (tick > baseTick - delta) && 
					(tick < baseTick + delta) ) {
				return event;
			}
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String actionCommand = event.getActionCommand();
		if (actionCommand.equals(insertCommand)) {
			insertAction();
		} else if (actionCommand.equals(editCommand)) {
			editAction();
		} else if (actionCommand.equals(deleteCommand)) {
			deleteAction();
		}
		mmlManager.updateActivePart(true);
	}

	protected final void setDefaultFocus(JTextField textField) {
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

	protected abstract List<T> getEventList();
	protected abstract void insertAction();
	protected abstract void editAction();
	protected abstract void deleteAction();
}

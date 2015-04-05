/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLEvent;

/**
 * Marker (for MMLEvent) Editor
 *   edit.insert_<suffix>
 *   edit.edit_<suffix>
 *   edit.delete_<suffix>
 *   edit.label_<suffix>
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

	protected T targetEvent;
	protected int targetTick;

	public AbstractMarkerEditor(String suffix, IMMLManager mmlManager, IEditAlign editAlign) {
		this.suffix = suffix;
		this.insertCommand = "insert_" + suffix;
		this.editCommand   = "edit_" + suffix;
		this.deleteCommand = "delete_" + suffix;
		this.mmlManager = mmlManager;
		this.editAlign = editAlign;

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

	private JMenuItem newMenuItem(String name) {
		JMenuItem menu = new JMenuItem(name);
		menu.addActionListener(this);
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

	protected abstract List<T> getEventList();
	protected abstract void insertAction();
	protected abstract void editAction();
	protected abstract void deleteAction();
}

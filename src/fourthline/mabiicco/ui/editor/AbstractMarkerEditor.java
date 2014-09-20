/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;

import fourthline.mabiicco.AppResource;
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
public abstract class AbstractMarkerEditor<T extends MMLEvent> implements ActionListener {

	private final JMenuItem insertMenu;
	private final JMenuItem editMenu;
	private final JMenuItem deleteMenu;

	protected final String suffix;
	protected final String insertCommand;
	protected final String editCommand;
	protected final String deleteCommand;

	public AbstractMarkerEditor(String suffix) {
		this.suffix = suffix;
		this.insertCommand = "insert_" + suffix;
		this.editCommand   = "edit_" + suffix;
		this.deleteCommand = "delete_" + suffix;

		insertMenu = newMenuItem(AppResource.appText("edit."+insertCommand));
		insertMenu.setActionCommand(insertCommand);
		editMenu = newMenuItem(AppResource.appText("edit."+editCommand));
		editMenu.setActionCommand(editCommand);
		deleteMenu = newMenuItem(AppResource.appText("edit."+deleteCommand));
		deleteMenu.setActionCommand(deleteCommand);
	}

	public List<JMenuItem> getMenuItems() {
		return Arrays.asList(insertMenu, editMenu, deleteMenu);
	}

	private JMenuItem newMenuItem(String name) {
		JMenuItem menu = new JMenuItem(name);
		menu.addActionListener(this);
		return menu;
	}

	protected T targetEvent;
	protected List<T> eventList;
	protected int targetTick;
	public void activateEditMenuItem(List<T> eventList, int baseTick, int delta) {
		this.eventList = eventList;
		this.targetTick = baseTick;
		targetEvent = getTempoEventOnTick(eventList, baseTick, delta);

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

	private T getTempoEventOnTick(List<T> eventList, int baseTick, int delta) {
		System.out.println(baseTick+" "+delta);
		for (T event : eventList) {
			int tick = event.getTickOffset();
			if ( (tick > baseTick - delta) && 
					(tick < baseTick + delta) ) {
				return event;
			}
		}
		return null;
	}
}

/*
 * Copyright (C) 2014-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;

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
public abstract class AbstractMarkerEditor<T extends MMLEvent> extends AbstractColumnEditor {

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

		insertMenu = newMenuItem(AppResource.appText("edit."+insertCommand), insertCommand);
		editMenu = newMenuItem(AppResource.appText("edit."+editCommand), editCommand);
		deleteMenu = newMenuItem(AppResource.appText("edit."+deleteCommand), deleteCommand);
	}

	@Override
	public List<JMenuItem> getMenuItems() {
		return Arrays.asList(insertMenu, editMenu, deleteMenu);
	}

	@Override
	protected void viewTargetMarker(JMenuItem menu, boolean b) {
		if (!b || !menu.isEnabled()) {
			viewTargetMarker.PaintOff();
		} else if (targetEvent != null) {
			viewTargetMarker.PaintOnTarget(targetEvent.getTickOffset());
		} else {
			viewTargetMarker.PaintOnTarget(targetTick);
		}
	}

	/**
	 * ターゲットTickを編集対象にあわせる
	 * 
	 * @param baseTick
	 * @return
	 */
	protected int targetTickAlign(int baseTick) {
		return baseTick - (baseTick % this.editAlign.getEditAlign());
	}

	@Override
	public void activateEditMenuItem(int baseTick, int delta) {
		this.targetTick = targetTickAlign(baseTick);
		targetEvent = getTempoEventOnTick(this.targetTick, delta);

		// 指定範囲内にイベントがなければ、挿入のみを有効にします.
		boolean existTarget = (targetEvent != null);
		insertMenu.setEnabled(!existTarget);
		editMenu.setEnabled(existTarget);
		deleteMenu.setEnabled(existTarget);
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
		boolean b = false;
		if (actionCommand.equals(insertCommand)) {
			b = insertAction();
		} else if (actionCommand.equals(editCommand)) {
			b = editAction();
		} else if (actionCommand.equals(deleteCommand)) {
			b = deleteAction();
		}
		if (b) {
			mmlManager.updateActivePart(true);
		}
	}

	protected abstract List<T> getEventList();
	protected abstract boolean insertAction();
	protected abstract boolean editAction();
	protected abstract boolean deleteAction();
}

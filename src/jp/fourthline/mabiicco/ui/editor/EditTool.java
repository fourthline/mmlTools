/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.SettingButtonGroupIconItem;

public enum EditTool implements SettingButtonGroupIconItem {
	NORMAL(EditMode.SELECT, KeyEvent.VK_A, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)),
	SPLIT(EditMode.SPLIT, KeyEvent.VK_S, "Split Cursor", false),
	GLUE(EditMode.GLUE, KeyEvent.VK_D, "Glue Cursor", true);
	private final EditMode mode;
	private final int keyCode;
	private final String viewName;
	private final ImageIcon icon;
	private final Cursor cursor;

	private EditTool(EditMode initMode, int keyCode, Cursor cursor) {
		this.mode = initMode;
		this.keyCode = keyCode;
		var resourceName = "edit_tool." + name().toLowerCase();
		this.viewName = AppResource.appText(resourceName);
		this.icon = AppResource.getImageIcon(AppResource.appText(resourceName + ".icon"));
		this.cursor = cursor;
	}

	private EditTool(EditMode initMode, int keyCode, String cursorName, boolean dir) {
		this.mode = initMode;
		this.keyCode = keyCode;
		var resourceName = "edit_tool." + name().toLowerCase();
		this.viewName = AppResource.appText(resourceName);
		this.icon = AppResource.getImageIcon(AppResource.appText(resourceName + ".icon"));

		var toolkit = Toolkit.getDefaultToolkit();
		Dimension nativeSize = toolkit.getBestCursorSize(this.icon.getIconWidth(), this.icon.getIconHeight());
		var point = !dir ? new Point(0, 0) : new Point(0, nativeSize.height-1);
		this.cursor = toolkit.createCustomCursor(this.icon.getImage(), point, cursorName);
	}
	
	public EditMode getEditMode() {
		return mode;
	}
	
	@Override
	public String getButtonName() {
		return viewName;
	}
	
	public int getKeyCode() {
		return keyCode;
	}
	
	@Override
	public ImageIcon getIcon() {
		return icon;
	}

	public Cursor getCursor() {
		return cursor;
	}
}

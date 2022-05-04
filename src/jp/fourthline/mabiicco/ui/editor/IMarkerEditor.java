/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.util.List;
import javax.swing.JMenuItem;

public interface IMarkerEditor {
	public List<JMenuItem> getMenuItems();
	public void activateEditMenuItem(int baseTick, int delta);
}

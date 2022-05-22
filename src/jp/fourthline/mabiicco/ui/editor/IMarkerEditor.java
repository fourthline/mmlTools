/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.util.List;
import javax.swing.JMenuItem;

public interface IMarkerEditor {
	List<JMenuItem> getMenuItems();
	void activateEditMenuItem(int baseTick, int delta);
}

/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Frame;
import javax.swing.JOptionPane;

import fourthline.mabiicco.AppResource;

public final class About {
	public void show(Frame parentFrame) {
		String title = AppResource.appText("menu.about");
		StringBuilder sb = new StringBuilder();
		sb.append("Version:  ").append(AppResource.getVersionText());
		sb.append('\n').append("Runtime: ").append(AppResource.getRuntimeVersion());
		JOptionPane.showMessageDialog(parentFrame, sb.toString(), title, JOptionPane.PLAIN_MESSAGE);
	}
}

/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Frame;
import javax.swing.JOptionPane;

import fourthline.mabiicco.AppResource;

public class About {
	public void show(Frame parentFrame) {
		String title = AppResource.getText("menu.about");
		String text = "Version: " + AppResource.getVersionText();
		JOptionPane.showMessageDialog(parentFrame, text, title, JOptionPane.PLAIN_MESSAGE);
	}
}

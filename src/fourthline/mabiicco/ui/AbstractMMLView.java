/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import javax.swing.JPanel;



public abstract class AbstractMMLView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -701943909797286599L;
	public static final int OCTNUM = 9;
	public static final int HEIGHT = 6;
	
	/**
	 * Panel上のy座標をnote番号に変換します.
	 * @param y
	 * @return
	 */
	public final int convertY2Note(int y) {
		int note = -1;
		if (y >= 0) {
			note = (9*12-(y/6)) -1;
		}

		return note;
	}
}

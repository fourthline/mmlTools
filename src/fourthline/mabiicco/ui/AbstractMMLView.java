/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui;

import javax.swing.JPanel;



public abstract class AbstractMMLView extends JPanel {
	private static final long serialVersionUID = -701943909797286599L;
	public static final int OCTNUM = 9;
	public static final int HEIGHT_C = 6;

	/**
	 * Panel上のy座標をnote番号に変換します.
	 * @param y
	 * @return
	 */
	public final int convertY2Note(int y) {
		int note = -1;
		if (y >= 0) {
			note = (9*12-(y/HEIGHT_C)) -1;
		}

		return note;
	}

	/**
	 * note番号をPanel上のy座標に変換します.
	 * @param note
	 * @return
	 */
	public final int convertNote2Y(int note) {
		int y = 9*12 - note - 1;
		y *= HEIGHT_C;

		return y;
	}
}

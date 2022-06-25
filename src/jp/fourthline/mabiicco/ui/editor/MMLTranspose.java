/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Frame;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;


public final class MMLTranspose extends AbstractNumberDialogAction {

	/**
	 * 移調を行うダイアログを作成する.
	 * @param parentFrame
	 * @param mmlManager
	 */
	public MMLTranspose(Frame parentFrame, IMMLManager mmlManager) {
		super(parentFrame,
				AppResource.appText("edit.transpose"),
				AppResource.appText("edit.transpose.text"),
				0, -12, 12, 1,
				t -> {
					if (t == 0) {
						return;
					}
					mmlManager.getMMLScore().transpose(t);
					mmlManager.updateActivePart(true);
				});
	}
}

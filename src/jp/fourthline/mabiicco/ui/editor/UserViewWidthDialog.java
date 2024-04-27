/*
 * Copyright (C) 2022-2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Frame;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mmlTools.MMLScore;


public final class UserViewWidthDialog extends AbstractNumberDialogAction {

	private final IMMLManager mmlManager;

	/**
	 * MMLScoreの幅指定ダイアログを作成する.
	 * @param parentFrame
	 * @param mmlManager
	 */
	public UserViewWidthDialog(Frame parentFrame, IMMLManager mmlManager) {
		super(parentFrame,
				AppResource.appText("view.setUserViewMeasure"),
				AppResource.appText("view.setUserViewMeasure.label"),
				mmlManager.getMMLScore().getUserViewMeasure(), 0, MMLScore.MAX_USER_VIEW_MEASURE, 1);
		this.mmlManager = mmlManager;
	}

	@Override
	public void apply(int v) {
		mmlManager.getMMLScore().setUserViewMeasure(v);
		parentFrame.repaint();
	}
}

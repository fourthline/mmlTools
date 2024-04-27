/*
 * Copyright (C) 2022-2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.UIUtils;


public final class MMLTranspose extends AbstractNumberDialogAction {

	/**
	 * 2回目以降も前回の設定値を引き継ぐようにしたいため, staticで作成.
	 */
	private static int selectedIndex = 0;
	private static final List<RangeMode> modeList;

	static {
		List<RangeMode> list = Arrays.asList(RangeMode.values());

		// デフォルトをALL_TRACKにするため, 逆順に.
		Collections.reverse(list);
		modeList = list;
	}

	private final List<JRadioButton> buttons = new ArrayList<>();
	private final IMMLManager mmlManager;

	/**
	 * 移調を行うダイアログを作成する.
	 * @param parentFrame
	 * @param mmlManager
	 */
	public MMLTranspose(Frame parentFrame, IMMLManager mmlManager) {
		super(parentFrame,
				AppResource.appText("edit.transpose"),
				AppResource.appText("edit.transpose.text"),
				0, -12, 12, 1);

		this.mmlManager = mmlManager;

		var panel = UIUtils.createTitledPanel("edit.transpose.range", null);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		ButtonGroup bg = new ButtonGroup();
		for (var m : modeList) {
			var button = new JRadioButton(AppResource.appText(m.getButtonName()));
			panel.add(button);
			bg.add(button);
			buttons.add(button);
			button.addActionListener(t -> {
				int index = buttons.indexOf(t.getSource());
				selectedIndex = index;
			});
		}

		buttons.get(selectedIndex).setSelected(true);

		cPanel.add(panel, BorderLayout.SOUTH);
	}

	@Override
	public void apply(int v) {
		if (v == 0) {
			return;
		}

		var mode = modeList.get(selectedIndex);
		mode.action(mmlManager, (trackIndex, partIndex) -> {
			var track = mmlManager.getMMLScore().getTrack(trackIndex);

			// 移調ができる楽器の種類かを確認. 通常の打楽器は不可, シロフォンは可能.
			if (MabiDLS.getInstance().getInstByProgram(track.getProgram()).getType().allowTranspose()) {
				var part = track.getMMLEventAtIndex(partIndex);
				part.transpose(v);
			}
		});

		mmlManager.updateActivePart(true);
	}
}

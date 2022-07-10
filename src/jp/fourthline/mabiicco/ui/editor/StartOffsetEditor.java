/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.IViewTargetMarker;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

public final class StartOffsetEditor extends AbstractColumnEditor {

	private final static String START_OFFSET = "startOffset";
	private final static String START_DELTA = "startDelta";
	private final static String START_SONG_DELTA = "startSongDelta";

	private final JMenuItem startOffsetMenu = newMenuItem(AppResource.appText("edit.label_"+START_OFFSET), START_OFFSET);
	private final JMenuItem startDeltaMenu = newMenuItem(AppResource.appText("edit.label_"+START_DELTA), START_DELTA);
	private final JMenuItem startSongDeltaMenu = newMenuItem(AppResource.appText("edit.label_"+START_SONG_DELTA), START_SONG_DELTA);

	private final Frame parentFrame;
	private final IMMLManager mmlManager;
	private final IEditAlign editAlign;
	private final IViewTargetMarker viewTargetMarker;

	private int targetTick;

	public StartOffsetEditor(Frame parentFrame, IMMLManager mmlManager, IEditAlign editAlign, IViewTargetMarker viewTargetMarker) {
		this.parentFrame = parentFrame;
		this.mmlManager = mmlManager;
		this.editAlign = editAlign;
		this.viewTargetMarker = viewTargetMarker;
	}

	@Override
	protected void viewTargetMarker(JMenuItem menu, boolean b) {
		if (!b || !menu.isEnabled()) {
			viewTargetMarker.PaintOff();
		} else {
			viewTargetMarker.PaintOnTarget(targetTick);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		System.out.println("Action: " + action + ", " + targetTick);
		MMLScore score = mmlManager.getMMLScore();
		MMLTrack activeTrack = mmlManager.getActiveTrack();
		try {
			switch (action) {
			case START_OFFSET:
				if (!score.setStartOffsetAll(targetTick)) {
					throw new IllegalArgumentException();
				}
				break;
			case START_DELTA:
				activeTrack.setStartDelta(targetTick - activeTrack.getCommonStartOffset());
				break;
			case START_SONG_DELTA:
				activeTrack.setStartSongDelta(targetTick - activeTrack.getCommonStartOffset());
				break;
			default:
				System.err.println("unknown action " + action);
				return;
			}
		} catch (IllegalArgumentException e) {
			// エラーメッセージの表示.
			JOptionPane.showMessageDialog(parentFrame, AppResource.appText("error.startOffset"), AppResource.getAppTitle(), JOptionPane.WARNING_MESSAGE);
		}

		mmlManager.updateActivePart(true);
	}

	@Override
	public List<JMenuItem> getMenuItems() {
		return List.of(startOffsetMenu ,startDeltaMenu, startSongDeltaMenu);
	}

	@Override
	public void activateEditMenuItem(int baseTick, int delta) {
		this.targetTick = baseTick - (baseTick % this.editAlign.getEditAlign());
	}

}

/*
 * Copyright (C) 2022 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.IViewTargetMarker;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

public final class StartOffsetEditor implements IMarkerEditor, ActionListener {

	private final static String START_OFFSET = "startOffset";
	private final static String START_DELTA = "startDelta";
	private final static String START_SONG_DELTA = "startSongDelta";

	private final JMenuItem startOffsetMenu = newMenuItem(START_OFFSET);
	private final JMenuItem startDeltaMenu = newMenuItem(START_DELTA);
	private final JMenuItem startSongDeltaMenu = newMenuItem(START_SONG_DELTA);

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


	private void viewTargetMarker(JMenuItem menu, boolean b) {
		if (!b || !menu.isEnabled()) {
			viewTargetMarker.PaintOff();
		} else {
			viewTargetMarker.PaintOnTarget(targetTick);
		}
	}

	private JMenuItem newMenuItem(String name) {
		JMenuItem menu = new JMenuItem( AppResource.appText("edit.label_"+name) );
		menu.setActionCommand(name);
		menu.addActionListener(this);
		menu.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				viewTargetMarker(menu, false);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				viewTargetMarker(menu, false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				viewTargetMarker(menu, true);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				viewTargetMarker(menu, true);
			}

			@Override
			public void mouseClicked(MouseEvent e) {}
		});
		return menu;
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

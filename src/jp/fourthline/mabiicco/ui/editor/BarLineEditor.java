/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.IViewTargetMarker;
import jp.fourthline.mmlTools.BarLineType;
import jp.fourthline.mmlTools.Measure;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.TimeSignature;


public final class BarLineEditor extends AbstractColumnEditor {
	private final Frame parentFrame;
	private final IMMLManager mmlManager;
	private final IViewTargetMarker viewTargetMarker;

	private final JMenu setTypeMenu;

	private int targetMeasure;

	private final Map<JRadioButtonMenuItem, BarLineType> menuMap = new HashMap<>();

	private static final String MENU_PREFIX = "edit.barLine_";

	public BarLineEditor(Frame parentFrame, IMMLManager mmlManager, IViewTargetMarker viewTargetMarker) {
		this.parentFrame = parentFrame;
		this.mmlManager = mmlManager;
		this.viewTargetMarker = viewTargetMarker;

		setTypeMenu = newMenu(AppResource.appText("edit.set_barLine_type"), JMenu::new);

		ButtonGroup group = new ButtonGroup();
		for (var t : BarLineType.values()) {
			newRadioMenu(group, t);
		}
	}

	private void newRadioMenu(ButtonGroup group, BarLineType type) {
		var text = MENU_PREFIX + type.name().toLowerCase();
		var menu = newMenu(AppResource.appText(text), JRadioButtonMenuItem::new);
		group.add(menu);
		setTypeMenu.add(menu);
		menu.addActionListener(this);
		menuMap.put(menu, type);
	}

	@Override
	public List<JMenuItem> getMenuItems() {
		return Arrays.asList(setTypeMenu);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MMLScore score = mmlManager.getMMLScore();
		Object source = e.getSource();
		var type = menuMap.get(source);
		if (type != null) {
			score.setBarLineType(targetMeasure, type);
			mmlManager.updateActivePart(true);
			parentFrame.repaint();
		}
	}

	@Override
	public void activateEditMenuItem(int baseTick, int delta) {
		targetMeasure = new Measure(mmlManager.getMMLScore(), baseTick).getMeasure();
		BarLineType type = mmlManager.getMMLScore().getBarLineTypeMap().getOrDefault(targetMeasure, BarLineType.NORMAL);
		menuMap.forEach((menu, t) -> menu.setSelected(t == type));
	}

	@Override
	protected void viewTargetMarker(AbstractButton menu, boolean b) {
		if (b) {
			int tick = TimeSignature.measureToTick(mmlManager.getMMLScore(), targetMeasure);
			viewTargetMarker.PaintOnTarget(tick);
		} else {
			viewTargetMarker.PaintOff();
		}
	}
}
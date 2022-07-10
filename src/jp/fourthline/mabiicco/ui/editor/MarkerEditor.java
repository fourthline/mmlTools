/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.IViewTargetMarker;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.Marker;

/**
 * Marker Editor
 *   edit.insert_marker
 *   edit.edit_marker
 *   edit.delete_marker
 *   edit.label_marker
 *   edit.new.marker
 * @see AbstractMarkerEditor
 */
public final class MarkerEditor extends AbstractMarkerEditor<Marker> {

	private final Frame parentFrame;

	public MarkerEditor(Frame parentFrame, IMMLManager mmlManager, IEditAlign editAlign, IViewTargetMarker viewTargetMarker) {
		super("marker", mmlManager, editAlign, viewTargetMarker);
		this.parentFrame = parentFrame;
	}

	private String showTextInputDialog(String title, String text) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(AppResource.appText("edit.label_"+suffix)));
		JTextField textField = new JTextField(text, 10);
		UIUtils.setDefaultFocus(textField);
		panel.add(textField);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);

		int status = JOptionPane.showConfirmDialog(this.parentFrame, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
			return textField.getText();
		}

		return null;
	}

	@Override
	protected List<Marker> getEventList() {
		return mmlManager.getMMLScore().getMarkerList();
	}

	@Override
	protected boolean insertAction() {
		String text = showTextInputDialog(AppResource.appText("edit."+insertCommand), AppResource.appText("edit.new.marker"));
		if ((text == null) || (text.length() == 0)) {
			return false;
		}

		// tempo align
		Marker marker = new Marker(text, targetTick);
		getEventList().add(marker);
		return true;
	}

	@Override
	protected boolean editAction() {
		String text = showTextInputDialog(AppResource.appText("edit."+editCommand), targetEvent.getName());
		if (text == null) {
			return false;
		}
		targetEvent.setName(text);
		return true;
	}

	@Override
	protected boolean deleteAction() {
		getEventList().remove(targetEvent);
		return true;
	}
}

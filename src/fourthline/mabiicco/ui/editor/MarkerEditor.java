/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.Marker;

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

	public MarkerEditor(IMMLManager mmlManager, IEditAlign editAlign) {
		super("marker", mmlManager, editAlign);
	}

	private String showTextInputDialog(String title, String text) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(AppResource.appText("edit.label_"+suffix)));
		JTextField textField = new JTextField(text, 10);
		panel.add(textField);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);

		int status = JOptionPane.showConfirmDialog(null, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
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
	protected void insertAction() {
		String text = showTextInputDialog(AppResource.appText("edit."+insertCommand), AppResource.appText("edit.new.marker"));
		if ((text == null) || (text.length() == 0)) {
			return;
		}

		// tempo align
		Marker marker = new Marker(text, targetTick);
		getEventList().add(marker);
	}

	@Override
	protected void editAction() {
		String text = showTextInputDialog(AppResource.appText("edit."+editCommand), targetEvent.getName());
		if (text == null) {
			return;
		}
		targetEvent.setName(text);
	}

	@Override
	protected void deleteAction() {
		getEventList().remove(targetEvent);
	}
}

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

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

	private final IEditAlign editAlign;
	private final IMMLManager mmlManager;

	public MarkerEditor(IMMLManager mmlManager, IEditAlign editAlign) {
		super("marker");
		this.editAlign = editAlign;
		this.mmlManager = mmlManager;
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
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals(insertCommand)) {
			String text = showTextInputDialog(AppResource.appText("edit."+insertCommand), AppResource.appText("edit.new.marker"));
			if ((text == null) || (text.length() == 0)) {
				return;
			}

			// tempo align
			int tick = targetTick - (targetTick % this.editAlign.getEditAlign());
			Marker marker = new Marker(text, tick);
			eventList.add(marker);
		} else if (actionCommand.equals(editCommand)) {
			String text = showTextInputDialog(AppResource.appText("edit."+insertCommand), targetEvent.getName());
			if (text == null) {
				return;
			}
			targetEvent.setName(text);
		} else if (actionCommand.equals(deleteCommand)) {
			eventList.remove(targetEvent);
		}

		mmlManager.updateActivePart();
	}
}

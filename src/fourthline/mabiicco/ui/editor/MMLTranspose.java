/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.IFileState;
import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.UndefinedTickException;

public final class MMLTranspose {
	private final IFileState fileState;

	public MMLTranspose(IFileState fileState) {
		this.fileState = fileState;
	}

	public void execute(Frame parentFrame, IMMLManager mmlManager) {
		int transpose = showTransposeDialog(parentFrame);
		if (transpose == 0) {
			return;
		}

		MabiDLS dls = MabiDLS.getInstance();
		for (MMLTrack track : mmlManager.getMMLScore().getTrackList()) {
			// ドラムパートは移調対象外
			if (dls.getInstByProgram(track.getProgram()).getType().equals(InstType.DRUMS)) {
				continue;
			}
			for (MMLEventList eventList : track.getMMLEventList()) {
				for (MMLNoteEvent note : eventList.getMMLNoteEventList()) {
					note.setNote( note.getNote() + transpose );
				}
			}
		}

		try {
			mmlManager.getMMLScore().generateAll();
		} catch (UndefinedTickException e) {
			fileState.revertState();
		}
		mmlManager.updateActivePart();
	}

	private int showTransposeDialog(Frame parentFrame) {
		String title = AppResource.appText("edit.transpose");
		JPanel panel = new JPanel();
		panel.add(new JLabel(AppResource.appText("edit.transpose.text")));
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0, -12, 12, 1));
		spinner.setFocusable(false);
		panel.add(spinner);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);

		int status = JOptionPane.showConfirmDialog(parentFrame, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
			return ((Integer) spinner.getValue()).intValue();
		}

		return 0;
	}
}

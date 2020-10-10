/*
 * Copyright (C) 2018 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.mml.TrackListTable;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLTrack;

/**
 * 複数トラックの音量変更する.
 */
public final class MultiTracksVelocityChangeEditor extends JPanel {

	private static final long serialVersionUID = 426213296832443395L;

	private TrackListTable table;
	private final JDialog dialog;
	private JButton applyButton;
	private JSpinner spinner;

	private final IMMLManager mmlManager;
	private final Frame parentFrame;

	public MultiTracksVelocityChangeEditor(Frame parentFrame, IMMLManager mmlManager) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("edit.tracks.velocity"), true);
		this.mmlManager = mmlManager;
		this.parentFrame = parentFrame;

		initializePanel();
	}

	public void showDialog() {
		dialog.getContentPane().add(this);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parentFrame);
		dialog.setVisible(true);
	}

	private void initializePanel() {
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.add(new JLabel(AppResource.appText("note.properties.incdecr")));
		spinner = NumberSpinner.createSpinner(0, -15, 15, 1);
		spinner.setFocusable(false);
		spinner.addChangeListener(t -> {
			updateButtonStatus();
		});
		panel.add(spinner);

		applyButton = new JButton(AppResource.appText("edit.apply"));
		applyButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(applyButton);
		applyButton.addActionListener((event) -> {
			apply();
			dialog.setVisible(false);
		});

		JButton closeButton = new JButton(AppResource.appText("mml.output.closeButton"));
		closeButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((event) -> {
			dialog.setVisible(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 422, 169);
		p.add(scrollPane, BorderLayout.CENTER);
	
		JPanel panelT = new JPanel();
		JButton allSelectButton = new JButton(AppResource.appText("menu.selectAll"));
		allSelectButton.addActionListener(t -> {
			table.setInitialCheck( table.getCheckList().length );
			updateButtonStatus();
			repaint();
		});
		panelT.add(allSelectButton);
		p.add(panelT, BorderLayout.NORTH);

		table = new TrackListTable(mmlManager.getMMLScore().getTrackList(), true);
		table.addPropertyChangeListener( t -> updateButtonStatus() );
		scrollPane.setViewportView(table);
		table.setDefaultEditor(Object.class, null);

		InputMap imap = dialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		dialog.getRootPane().getActionMap().put("close-it", new AbstractAction() {
			private static final long serialVersionUID = -4495368209645211523L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}});

		add(buttonPanel, BorderLayout.SOUTH);
		add(panel, BorderLayout.NORTH);
		add(p, BorderLayout.CENTER);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(440, 300);
	}

	private void updateButtonStatus() {
		boolean enable = true;
		if (table.getCheckCount() == 0) {
			enable = false;
		}
		if (spinner.getValue().equals(0)) {
			enable = false;
		}

		applyButton.setEnabled(enable);
	}

	public void apply() {
		int delta = ((Integer) spinner.getValue()).intValue();
		List<MMLTrack> trackList = mmlManager.getMMLScore().getTrackList();
		boolean checkList[] = table.getCheckList();
		for (int i = 0; i < trackList.size(); i++) {
			if (checkList[i]) {
				trackList.get(i).getMMLEventList().forEach(t -> {
					for (MMLNoteEvent n : t.getMMLNoteEventList()) {
						n.setVelocity( n.getVelocity() + delta );
					}
				});
			}
		}
		mmlManager.updateActivePart(true);
	}
}

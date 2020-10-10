/*
 * Copyright (C) 2019 たんらる
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.mml.TrackListTable;
import fourthline.mmlTools.MMLTrack;

/**
 * トラック表示設定.
 */
public final class MultiTracksViewEditor extends JPanel {
	private static final long serialVersionUID = 7227807579193973972L;

	private TrackListTable table;
	private final JDialog dialog;
	private JButton applyButton;

	private final IMMLManager mmlManager;
	private final Frame parentFrame;

	private boolean oldValue[];

	public MultiTracksViewEditor(Frame parentFrame, IMMLManager mmlManager) {
		if (parentFrame != null) {
			this.dialog = new JDialog(parentFrame, AppResource.appText("edit.tracks.view"), true, parentFrame.getGraphicsConfiguration());
		} else {
			this.dialog = new JDialog(parentFrame, AppResource.appText("edit.tracks.view"), true);
		}
		this.mmlManager = mmlManager;
		this.parentFrame = parentFrame;

		initializePanel();
	}

	public void showDialog() {
		dialog.getContentPane().add(this);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void initializePanel() {
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		applyButton = new JButton(AppResource.appText("edit.apply"));
		applyButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(applyButton);
		applyButton.addActionListener((event) -> {
			apply(table.getCheckList());
			mmlManager.updateActivePart(true);
			dialog.setVisible(false);
		});

		JButton cancelButton = new JButton(AppResource.appText("edit.cancel"));
		cancelButton.setMargin(new Insets(5, 10, 5, 10));
		buttonPanel.add(cancelButton);
		cancelButton.setFocusable(false);
		cancelButton.addActionListener((event) -> {
			apply(oldValue);
			dialog.setVisible(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 422, 169);
		p.add(scrollPane, BorderLayout.CENTER);

		JPanel panelT = new JPanel();
		JButton allSelectButton = new JButton(AppResource.appText("menu.selectAll"));
		allSelectButton.addActionListener(t -> {
			table.setInitialCheck( table.getCheckList().length );
			repaint();
		});
		panelT.add(allSelectButton);

		JButton allClearButton = new JButton(AppResource.appText("menu.selectClear"));
		allClearButton.addActionListener(t -> {
			table.setInitialCheck( table.getCheckList().length, false );
			repaint();
		});
		panelT.add(allClearButton);
		p.add(panelT, BorderLayout.NORTH);

		/*********************************************************************************************/
		table = new TrackListTable(mmlManager.getMMLScore().getTrackList(), true);
		table.addPropertyChangeListener( t-> apply(table.getCheckList()) );
		scrollPane.setViewportView(table);
		table.setDefaultEditor(Object.class, null);
		for (int i = 0; i < table.getCheckList().length; i++) {
			MMLTrack track = mmlManager.getMMLScore().getTrack(i);
			table.getCheckList()[i] = track.isVisible();
		}
		oldValue = table.getCheckList().clone();

		InputMap imap = dialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		dialog.getRootPane().getActionMap().put("close-it", new AbstractAction() {
			private static final long serialVersionUID = -4495368209645211523L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				apply(oldValue);
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

	public void apply(boolean list[]) {
		List<MMLTrack> trackList = mmlManager.getMMLScore().getTrackList();
		for (int i = 0; i < trackList.size(); i++) {
			trackList.get(i).setVisible(list[i]);
		}
		parentFrame.repaint();
	}
}

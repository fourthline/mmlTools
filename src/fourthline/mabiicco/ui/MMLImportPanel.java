/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;

import fourthline.mabiicco.AppResource;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

import javax.swing.JLabel;

public final class MMLImportPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -1504636951822574399L;
	private JTable table;
	private final JDialog dialog;
	private final JButton importButton = new JButton(AppResource.appText("mml.input.import"));

	private List<MMLTrack> trackList;
	private IMMLManager mmlManager;
	private int possibleImportTrackCount;

	/**
	 * @wbp.parser.constructor
	 */
	public MMLImportPanel(Frame parentFrame) {
		this.dialog = null;
		initializePanel(null);
	}

	public MMLImportPanel(Frame parentFrame, MMLScore score, IMMLManager mmlManager) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("mml.input.import"), true);
		this.trackList = score.getTrackList();
		this.mmlManager = mmlManager;
		possibleImportTrackCount = MMLScore.MAX_TRACK - mmlManager.getMMLScore().getTrackCount();
		if (score.getTrackCount() < possibleImportTrackCount) {
			possibleImportTrackCount = score.getTrackCount();
		}
		initializePanel(trackList);
	}

	private void initializePanel(List<MMLTrack> trackList) {
		this.trackList = trackList;
		setLayout(null);

		importButton.setBounds(142, 214, 90, 29);
		add(importButton);
		importButton.addActionListener((event) -> {
			importMMLTrack();
			dialog.setVisible(false);
		});

		JButton closeButton = new JButton(AppResource.appText("mml.output.closeButton"));
		closeButton.setBounds(258, 214, 90, 29);
		add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((event) -> {
			dialog.setVisible(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 372, 169);
		add(scrollPane);

		table = MMLOutputPanel.createJTableFromMMLTrack(trackList);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(table);

		JLabel lblNewLabel = new JLabel(AppResource.appText("mml.input.import.possibleImport")+": "+possibleImportTrackCount);
		lblNewLabel.setBounds(22, 189, 300, 13);
		add(lblNewLabel);

		table.setDefaultEditor(Object.class, null);
		table.setFocusable(false);
		if (possibleImportTrackCount > 0) {
			table.setRowSelectionInterval(0, possibleImportTrackCount-1);
		}
		table.addMouseListener(this);
		table.addMouseMotionListener(this);
		updateImportAllowed();
	}

	private void importMMLTrack() {
		MMLScore targetScore = mmlManager.getMMLScore();
		for (int rowIndex : table.getSelectedRows()) {
			targetScore.addTrack(trackList.get(rowIndex));
		}
		mmlManager.updateActivePart();
	}

	private void updateImportAllowed() {
		if (possibleImportTrackCount > 0) {
			if (table.getSelectedRows().length <= possibleImportTrackCount) {
				importButton.setEnabled(true);
				return;
			}
		}
		importButton.setEnabled(false);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(390, 240);
	}

	/**
	 * ダイアログを表示する.
	 */
	public void showDialog() {
		dialog.getContentPane().add(this);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		updateImportAllowed();
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		updateImportAllowed();
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		updateImportAllowed();
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		updateImportAllowed();
	}

	@Override
	public void mouseExited(MouseEvent event) {
		updateImportAllowed();
	}

	@Override
	public void mousePressed(MouseEvent event) {
		updateImportAllowed();
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		updateImportAllowed();
	}
}

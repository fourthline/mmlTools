/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.JScrollPane;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.editor.MMLEditor;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLTrack;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.Color;

/**
 * パート入れ替え
 */
public final class MMLPartChangePanel extends JPanel {
	private static final long serialVersionUID = 8558159209741558854L;
	private TrackListTable table;
	private final JDialog dialog;
	private final JButton applyButton = new JButton(AppResource.appText("part_change.apply"));

	private final IMMLManager mmlManager;
	private final MMLEditor editor;

	private JRadioButton radioSelectArea;
	private JRadioButton radioAllArea;
	private JRadioButton radioChange;
	private JRadioButton radioMove;
	private JRadioButton radioCopy;
	private JComboBox<String> partSelectComboBox;

	public MMLPartChangePanel() {
		this.dialog = null;
		this.mmlManager = null;
		this.editor = null;
		initializePanel(null);
	}

	public MMLPartChangePanel(Frame parentFrame, IMMLManager mmlManager, MMLEditor editor) {
		this.dialog = new JDialog(parentFrame, AppResource.appText("part_change"), true);
		this.mmlManager = mmlManager;
		this.editor = editor;
		initializePanel(mmlManager.getMMLScore().getTrackList());
	}

	private void initializePanel(List<MMLTrack> trackList) {
		setLayout(null);

		applyButton.setBounds(202, 335, 90, 29);
		add(applyButton);
		applyButton.addActionListener((event) -> {
			changePartAction();
			mmlManager.updateActivePart(true);
			dialog.setVisible(false);
		});

		JButton closeButton = new JButton(AppResource.appText("part_change.cancel"));
		closeButton.setBounds(317, 335, 90, 29);
		add(closeButton);
		closeButton.setFocusable(false);
		closeButton.addActionListener((event) -> {
			dialog.setVisible(false);
		});

		/* 入れ替え元 */
		JPanel applyPanel = new JPanel();
		applyPanel.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), AppResource.appText("part_change.src"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		applyPanel.setBounds(12, 10, 414, 63);
		applyPanel.setLayout(null);
		add(applyPanel);
		ButtonGroup applyGroup = new ButtonGroup();

		radioSelectArea = new JRadioButton(AppResource.appText("part_change.selectArea"));
		radioSelectArea.setBounds(8, 22, 113, 21);
		applyPanel.add(radioSelectArea);
		applyGroup.add(radioSelectArea);

		radioAllArea = new JRadioButton(AppResource.appText("part_change.all"));
		radioAllArea.setBounds(138, 22, 113, 21);
		applyPanel.add(radioAllArea);
		applyGroup.add(radioAllArea);

		if (editor != null) {
			if (editor.hasSelectedNote()) {
				radioSelectArea.setSelected(true);
			} else {
				radioSelectArea.setEnabled(false);
				radioAllArea.setSelected(true);
			}
		}

		/* 入れ替え先 */
		JPanel destSelectPanel = new JPanel();
		destSelectPanel.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), AppResource.appText("part_change.dest"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		destSelectPanel.setBounds(12, 83, 414, 169);
		destSelectPanel.setLayout(null);
		add(destSelectPanel);

		JLabel label1 = new JLabel(AppResource.appText("track"));
		label1.setBounds(12, 23, 172, 13);
		destSelectPanel.add(label1);

		JLabel label2 = new JLabel(AppResource.appText("part"));
		label2.setBounds(12, 139, 64, 13);
		destSelectPanel.add(label2);

		partSelectComboBox = new JComboBox<>();
		partSelectComboBox.setBounds(100, 136, 131, 19);
		destSelectPanel.add(partSelectComboBox);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 46, 372, 83);
		destSelectPanel.add(scrollPane);

		table = new TrackListTable(trackList);
		scrollPane.setViewportView(table);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				updatePartSelectBox();
			}
		});

		/* 実行内容 */
		JPanel executePanel = new JPanel();
		executePanel.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), AppResource.appText("part_change.method"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		executePanel.setBounds(12, 262, 414, 63);
		executePanel.setLayout(null);
		add(executePanel);
		ButtonGroup executeGroup = new ButtonGroup();

		radioChange = new JRadioButton(AppResource.appText("part_change.swap"));
		radioChange.setBounds(8, 22, 113, 21);
		radioChange.setSelected(true);
		executePanel.add(radioChange);
		executeGroup.add(radioChange);

		radioMove = new JRadioButton(AppResource.appText("part_change.move"));
		radioMove.setBounds(139, 22, 113, 21);
		executePanel.add(radioMove);
		executeGroup.add(radioMove);

		radioCopy = new JRadioButton(AppResource.appText("part_change.copy"));
		radioCopy.setBounds(256, 22, 113, 21);
		executePanel.add(radioCopy);
		executeGroup.add(radioCopy);

		InputMap imap = dialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		dialog.getRootPane().getActionMap().put("close-it", new AbstractAction() {
			private static final long serialVersionUID = -4495368209645211523L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}});

		updatePartSelectBox();
	}

	private void updatePartSelectBox() {
		if (mmlManager == null) {
			return;
		}
		MMLEventList activePart = mmlManager.getActiveMMLPart();
		int trackIndex = table.getSelectedRow();
		partSelectComboBox.removeAllItems();
		MMLTrack selectedTrack = mmlManager.getMMLScore().getTrack(trackIndex);
		for (int i = 0; i < MMLTrackView.MMLPART_NAME.length; i++) {
			if (selectedTrack.getMMLEventAtIndex(i) != activePart) {
				partSelectComboBox.addItem(MMLTrackView.MMLPART_NAME[i]);
			}
		}
	}

	private void changePartAction() {
		if (mmlManager == null) {
			return;
		}
		int trackIndex = table.getSelectedRow();
		MMLTrack selectedTrack = mmlManager.getMMLScore().getTrack(trackIndex);
		int partIndex = Arrays.binarySearch(MMLTrackView.MMLPART_NAME, partSelectComboBox.getSelectedItem());

		MMLEditor.ChangePartAction action = MMLEditor.ChangePartAction.SWAP;
		if (radioMove.isSelected()) {
			action = MMLEditor.ChangePartAction.MOVE;
		} else if (radioCopy.isSelected()) {
			action = MMLEditor.ChangePartAction.COPY;
		}

		MMLEventList fromPart = mmlManager.getActiveMMLPart();
		MMLEventList toPart = selectedTrack.getMMLEventAtIndex(partIndex);
		editor.changePart(fromPart, toPart, radioSelectArea.isSelected(), action);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(441, 380);
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
}

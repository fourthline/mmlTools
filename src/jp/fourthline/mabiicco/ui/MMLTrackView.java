/*
 * Copyright (C) 2013-2021 たんらる
 */

package jp.fourthline.mabiicco.ui;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JTextField;
import javax.swing.JComboBox;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.InstType;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mmlTools.MMLTrack;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;

public final class MMLTrackView extends JPanel implements ActionListener {

	private static final long serialVersionUID = 4955513242349170508L;
	public static final String[] MMLPART_NAME = {
			AppResource.appText("melody"),
			AppResource.appText("chord1"),
			AppResource.appText("chord2"),
			AppResource.appText("song")
	};
	private final JToggleButton[] partButton;
	private final JTextField[] mmlText;

	private final JComboBox<InstClass> comboBox;
	private final JComboBox<InstClass> songComboBox;

	private final JLabel trackComposeLabel;
	private final JToolBar toolBar;
	private JButton muteButton;
	private JButton soloButton;
	private JButton allButton;
	private final JLabel trackIndexLabel = new JLabel();

	private IMMLManager mmlManager;

	private final InstClass noUseSongEx = new InstClass(AppResource.appText("instrument.nouse_chorus")+",0", -1, -1, null);
	private final InstClass excludeSongPart = new InstClass(AppResource.appText("instrument.excludeSongPart")+",0", -2, -2, null);
	private int trackIndex;
	private boolean disableAction = false;

	private final JPanel mmlTextPanel = new JPanel();

	public void setVisibleMMLTextPanel(boolean b) {
		if (b) {
			add(mmlTextPanel, BorderLayout.CENTER);
		} else {
			remove(mmlTextPanel);
		}
	}

	/**
	 * Create the panel.
	 */
	private MMLTrackView() {
		setLayout(new BorderLayout());

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());

		JPanel northLPanel = new JPanel();
		northLPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JPanel northRPanel = new JPanel();

		northPanel.add(northLPanel, BorderLayout.WEST);
		northPanel.add(northRPanel, BorderLayout.EAST);
		add(northPanel, BorderLayout.NORTH);

		setVisibleMMLTextPanel(true);

		trackComposeLabel = new JLabel("");
		northRPanel.add(trackComposeLabel);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{20, 0, 0, 0, 0, 20};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		mmlTextPanel.setLayout(gridBagLayout);

		comboBox = new JComboBox<>( MabiDLS.getInstance().getAvailableInstByInstType(InstType.MAIN_INST_LIST) );
		songComboBox = new JComboBox<>( MabiDLS.getInstance().getAvailableInstByInstType(InstType.SUB_INST_LIST) );
		songComboBox.addItem(noUseSongEx);
		songComboBox.addItem(excludeSongPart);
		songComboBox.setSelectedItem(noUseSongEx);
		comboBox.setFocusable(false);
		songComboBox.setFocusable(false);

		northLPanel.add(comboBox);
		comboBox.addActionListener(this);
		comboBox.setMaximumRowCount(30);
		comboBox.setPreferredSize(new Dimension(200, 20));
		northLPanel.add(songComboBox);
		songComboBox.addActionListener(this);
		songComboBox.setMaximumRowCount(30);
		songComboBox.setPreferredSize(new Dimension(140, 20));

		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		northPanel.add(toolBar);

		// 各パートのボタンとテキストフィールドを作成します.
		ButtonGroup bGroup = new ButtonGroup();
		partButton = new JToggleButton[MMLPART_NAME.length];
		mmlText = new JTextField[MMLPART_NAME.length];
		for (int i = 0; i < MMLPART_NAME.length; i++) {
			int gridy = 2*i + 1;

			// パートタイトル
			partButton[i] = new JToggleButton( MMLPART_NAME[i] );
			partButton[i].setFocusable(false);
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(0, 0, 5, 5);
			gbc_label.gridx = 1;
			gbc_label.gridy = gridy;
			mmlTextPanel.add(partButton[i], gbc_label);
			bGroup.add(partButton[i]);

			// パートのテキストフィールド
			mmlText[i] = new JTextField();
			mmlText[i].setEditable(false);
			mmlText[i].setFocusable(false);
			mmlText[i].setFont(new Font("Monospaced", Font.PLAIN, 12));
			mmlText[i].setColumns(10);
			mmlText[i].addMouseListener(new ButtonCombAdapter(partButton[i]));

			GridBagConstraints gbc_mmlText = new GridBagConstraints();
			gbc_mmlText.insets = new Insets(0, 0, 5, 5);
			gbc_mmlText.fill = GridBagConstraints.HORIZONTAL;
			gbc_mmlText.gridx = 3;
			gbc_mmlText.gridy = gridy;
			mmlTextPanel.add(mmlText[i], gbc_mmlText);
		}

		// 一番上のパートが初期の選択パート.
		partButton[0].setSelected(true);
	}

	/**
	 * マウスクリックで関連するボタンを選択状態にする.
	 */
	private static class ButtonCombAdapter extends MouseAdapter {
		private final JToggleButton button;
		private ButtonCombAdapter(JToggleButton b) {
			button = b;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (button.isEnabled()) {
				button.setSelected(true);
				Arrays.asList(button.getActionListeners()).forEach(t -> t.actionPerformed(null));
			}
		}
	}

	/**
	 * インデックス指定で MMLTrackViewを生成.
	 * @param trackIndex TrackTabIconを各ボタンに設定するためのIndex値.
	 * @param actionListener
	 */
	private MMLTrackView(int trackIndex, ActionListener actionListener) {
		this();
		this.trackIndex = trackIndex;

		muteButton = new JButton("");
		muteButton.setFocusable(false);
		muteButton.setToolTipText(AppResource.appText("mmltrack.mute"));
		toolBar.add(muteButton);
		soloButton = new JButton("");
		soloButton.setFocusable(false);
		soloButton.setIcon(AppResource.getImageIcon(AppResource.appText("mmltrack.solo.icon")));
		soloButton.setToolTipText(AppResource.appText("mmltrack.solo"));
		toolBar.add(soloButton);
		allButton = new JButton("");
		allButton.setFocusable(false);
		allButton.setIcon(AppResource.getImageIcon(AppResource.appText("mmltrack.all.icon")));
		allButton.setToolTipText(AppResource.appText("mmltrack.all"));
		toolBar.add(allButton);
		toolBar.add(trackIndexLabel);

		for (int i = 0; i < partButton.length; i++) {
			partButton[i].addActionListener(actionListener);
		}
		muteButton.addActionListener(this);
		soloButton.addActionListener(this);
		allButton.addActionListener(this);
	}
	private static final HashMap<Integer, MMLTrackView> instanceList = new HashMap<>();
	public static MMLTrackView getInstance(int trackIndex, ActionListener actionListener, IMMLManager mmlManager) {
		MMLTrackView view;
		if (instanceList.containsKey(trackIndex)) {
			view = instanceList.get(trackIndex);
		} else {
			view = new MMLTrackView(trackIndex, actionListener);
			instanceList.put(trackIndex, view);
		}
		view.mmlManager = mmlManager;
		view.updateTrack();
		view.updateMuteButton();
		view.updatePartButtonStatus();
		return view;
	}

	public void updateMuteButton() {
		muteButton.setEnabled(true);
		soloButton.setEnabled(true);
		allButton.setEnabled(true);
		if (MabiDLS.getInstance().getMute(trackIndex)) {
			muteButton.setIcon(AppResource.getImageIcon(AppResource.appText("mmltrack.mute.on.icon")));
		} else {
			muteButton.setIcon(AppResource.getImageIcon(AppResource.appText("mmltrack.mute.off.icon")));
		}
	}

	/**
	 * 選択中のMMLパートのindex値を返します.
	 * @return 選択中のindex
	 */
	public int getSelectedMMLPartIndex() {
		int index = 0;

		for (int i = 0; i < MMLPART_NAME.length; i++) {
			if (partButton[i].isSelected()) {
				index = i;
				break;
			}
		}

		return index;
	}

	/**
	 * 指定されたIndexのMMLパートを選択します.
	 * @param index
	 */
	public void setSelectMMLPartOfIndex(int index) {
		for (JToggleButton button : partButton) {
			button.setSelected(false);
		}

		partButton[index].setSelected(true);
	}

	public void updateTrack() {
		MMLTrack mmlTrack = mmlManager.getMMLScore().getTrack(trackIndex);
		String[] mml = mmlTrack.getMabiMMLArray();
		for (int i = 0, len = mmlText.length; i < len; i++) {
			if (i < mml.length) {
				mmlText[i].setText(mml[i]);
			} else {
				mmlText[i].setText("");
			}
		}

		setInstProgram(mmlTrack);
		updateRankFormat(mmlTrack);
	}

	private void updateRankFormat(MMLTrack mmlTrack) {
		trackComposeLabel.setText(mmlTrack.mmlRankFormat());
		String str = "   "+(trackIndex+1)+"/"+mmlManager.getMMLScore().getTrackCount();
		trackIndexLabel.setText(str);
	}

	private InstClass convertInstBySongProgram(InstClass inst, int songProgram) {
		InstClass songInst = MabiDLS.getInstance().getInstByProgram(songProgram);
		if (songInst != null) {
			return songInst;
		}

		if ( (songProgram == excludeSongPart.getProgram()) && (inst.getType() != InstType.VOICE) ) {
			return excludeSongPart;
		}
		return noUseSongEx;
	}

	private void setInstProgram(MMLTrack track) {
		int program = track.getProgram();
		int songProgram = track.getSongProgram();

		disableAction = true;
		InstClass inst = MabiDLS.getInstance().getInstByProgram(program);
		if (inst != null) {
			comboBox.setSelectedItem(inst);
		} else {
			comboBox.setSelectedIndex(0);
			program = ((InstClass) comboBox.getSelectedItem()).getProgram();
			track.setProgram(program);
		}

		InstClass songInst = convertInstBySongProgram(inst, songProgram);
		songComboBox.setSelectedItem(songInst);
		track.setSongProgram(songInst.getProgram());
		disableAction = false;

		updateProgramChangeStatus();
	}

	/**
	 * 楽器選択
	 *  N: melody, chord1, chord2
	 *  D: melody
	 *  V: songEX, songComboBoxは選択不可
	 * songEX
	 *  V: songEX
	 */
	private void updateProgramChangeStatus() {
		InstClass inst = (InstClass) comboBox.getSelectedItem();
		MMLTrack track = mmlManager.getMMLScore().getTrack(trackIndex);

		songComboBox.setVisible(inst.getType() != InstType.VOICE);

		updatePartButtonStatus();

		InstClass songInst = ((InstClass) songComboBox.getSelectedItem());
		int program = inst.getProgram();
		int songProgram = convertInstBySongProgram(inst, songInst.getProgram()).getProgram();
		if ( (track.getProgram() == program) && (track.getSongProgram() == songProgram) ) {
			return;
		}

		mmlManager.updateActiveTrackProgram(trackIndex, program, songProgram);
		updateRankFormat(track);
	}

	private void updatePartButtonStatus() {
		InstClass inst = (InstClass) comboBox.getSelectedItem();
		InstClass songInst = ((InstClass) songComboBox.getSelectedItem());

		// 選択された楽器にあわせて、MMLパートの有効/無効化を行う.
		boolean[] instEnable = inst.getType().getEnablePart();
		boolean[] songExEnable = songInst.getType().getEnablePart();
		int iconIndex = 0;
		for (int i = 0; i < partButton.length; i++) {
			boolean b = (instEnable[i] || songExEnable[i]);
			partButton[i].setEnabled(b);
			mmlText[i].setEnabled(b);
			partButton[i].setIcon(
					(b)
					? PartButtonIcon.getInstance(iconIndex++, trackIndex) :
						PartButtonIcon.getDefautIcon() );
		}

		if (!partButton[getSelectedMMLPartIndex()].isEnabled()) {
			partButton[getSelectedMMLPartIndex()].setSelected(false);
			for (JToggleButton button : partButton) {
				if (button.isEnabled()) {
					button.setSelected(true);
				}
			}
		}
	}

	public void switchMMLPart(boolean toNext) {
		int partIndex = 0;
		for (int i = 0; i < partButton.length; i++) {
			if (partButton[i].isSelected()) {
				partIndex = i;
				break;
			}
		}

		if (toNext) {
			if ( (partIndex+1 < partButton.length) && (partButton[partIndex+1].isEnabled()) ) {
				partIndex++;
			}
		} else {
			if ( (partIndex-1 >= 0) && (partButton[partIndex-1].isEnabled()) ) {
				partIndex--;
			}
		}
		partButton[partIndex].setSelected(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (disableAction) {
			return;
		}
		Object source = e.getSource();
		if (source instanceof JComboBox<?>) {
			updateProgramChangeStatus();
		} else if (source.equals(muteButton)) {
			MabiDLS.getInstance().toggleMute(trackIndex);
			updateMuteButton();
		} else if (source.equals(soloButton)) {
			MabiDLS.getInstance().solo(trackIndex);
			updateMuteButton();
		} else if (source.equals(allButton)) {
			MabiDLS.getInstance().all();
			updateMuteButton();
		}
	}

	JComboBox<InstClass> getComboBox() {
		return comboBox;
	}

	JComboBox<InstClass> getSongComboBox() {
		return songComboBox;
	}

	String getRankText() {
		return trackComposeLabel.getText();
	}
}

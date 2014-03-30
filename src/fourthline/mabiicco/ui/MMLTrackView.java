/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTools;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

public class MMLTrackView extends JPanel implements ActionListener, DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4955513242349170508L;
	public static final String MMLPART_NAME[] = {
		"メロディー", "和音1", "和音2", "歌"
	};
	private JToggleButton partButton[];
	private JTextField mmlText[];

	private JComboBox<InstClass> comboBox;
	private JComboBox<InstClass> songComboBox;

	private JLabel trackComposeLabel;
	private JToolBar toolBar;
	private JButton muteButton;
	private JButton soloButton;
	private JButton allButton;

	private IMMLManager mmlManager;

	private final InstClass noUseSongEx = new InstClass("コーラスなし,0", -1, -1);
	private int trackIndex;

	/**
	 * Create the panel.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MMLTrackView() {
		setLayout(new BorderLayout());

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());

		JPanel northLPanel = new JPanel();
		northLPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JPanel centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);

		JPanel northRPanel = new JPanel();
		add(northRPanel, BorderLayout.SOUTH);

		northPanel.add(northLPanel, BorderLayout.WEST);
		northPanel.add(northRPanel, BorderLayout.EAST);
		add(northPanel, BorderLayout.NORTH);

		trackComposeLabel = new JLabel("");
		northRPanel.add(trackComposeLabel);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{20, 0, 0, 0, 0, 20};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		centerPanel.setLayout(gridBagLayout);

		InstClass insts[] = null;
		try {
			insts = MabiDLS.getInstance().getInsts();
		} catch (NullPointerException e) {}
		if (insts == null) {
			comboBox = new JComboBox();
			songComboBox = new JComboBox();
		} else {
			comboBox = new JComboBox( InstClass.filterInstArray(insts, EnumSet.of(InstType.NORMAL, InstType.DRUMS, InstType.VOICE)) );
			songComboBox = new JComboBox( InstClass.filterInstArray(insts, EnumSet.of(InstType.CHORUS)) );
			songComboBox.addItem(noUseSongEx);
		}
		northLPanel.add(comboBox);
		comboBox.addActionListener(this);
		comboBox.setMaximumRowCount(30);
		comboBox.setPreferredSize(new Dimension(140, 20));
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
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(0, 0, 5, 5);
			gbc_label.gridx = 1;
			gbc_label.gridy = gridy;
			centerPanel.add(partButton[i], gbc_label);
			bGroup.add(partButton[i]);

			// パートのテキストフィールド
			mmlText[i] = new JTextField();
			mmlText[i].setEditable(false);
			mmlText[i].setFont(new Font("Monospaced", Font.PLAIN, 12));
			mmlText[i].getDocument().addDocumentListener(this);
			mmlText[i].setColumns(10);

			GridBagConstraints gbc_mmlText = new GridBagConstraints();
			gbc_mmlText.insets = new Insets(0, 0, 5, 5);
			gbc_mmlText.fill = GridBagConstraints.HORIZONTAL;
			gbc_mmlText.gridx = 3;
			gbc_mmlText.gridy = gridy;
			centerPanel.add(mmlText[i], gbc_mmlText);
		}

		// 一番上のパートが初期の選択パート.
		partButton[0].setSelected(true);

		updateComposeRank();
	}

	/**
	 * 
	 * @param track
	 * @param trackIndex TrackTabIconを各ボタンに設定するためのIndex値.
	 * @param actionListener
	 * @param mmlManager
	 */
	public MMLTrackView(MMLTrack track, int trackIndex, ActionListener actionListener, IMMLManager mmlManager) {
		this();
		this.setMMLTrack(track);
		this.mmlManager = mmlManager;
		this.trackIndex = trackIndex;
		trackComposeLabel.setText(track.mmlRankFormat());

		for (int i = 0; i < MMLPART_NAME.length; i++) {
			partButton[i].addActionListener(actionListener);
		}

		muteButton = new JButton("");
		updateMuteButton();
		muteButton.setToolTipText("Mute");
		toolBar.add(muteButton);
		soloButton = new JButton("");
		soloButton.setIcon(new ImageIcon(this.getClass().getResource("/img/play_solo.png")));
		soloButton.setToolTipText("Solo");
		toolBar.add(soloButton);
		allButton = new JButton("");
		allButton.setIcon(new ImageIcon(this.getClass().getResource("/img/play_all.png")));
		allButton.setToolTipText("All");
		toolBar.add(allButton);
		muteButton.addActionListener(this);
		soloButton.addActionListener(this);
		allButton.addActionListener(this);

		updatePartButtonStatus();
	}

	public void updateMuteButton() {
		if (MabiDLS.getInstance().getChannel(trackIndex).getMute()) {
			muteButton.setIcon(new ImageIcon(this.getClass().getResource("/img/mute_on.png")));
		} else {
			muteButton.setIcon(new ImageIcon(this.getClass().getResource("/img/mute_off.png")));
		}
	}

	public String getMMLText() {
		MMLTools tools = new MMLTools(
				mmlText[0].getText(),
				mmlText[1].getText(),
				mmlText[2].getText(),
				mmlText[3].getText()
				);
		return tools.getMML();
	}

	/**
	 * 選択中のMMLパートのindex値を返します.
	 * @return
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

	@Override
	public void removeUpdate(DocumentEvent event) {
	}
	@Override
	public void insertUpdate(DocumentEvent event) {
	}

	@Override
	public void changedUpdate(DocumentEvent event) {
	}

	/**
	 * 保持しているMMLテキストの作曲ランク文字列を取得します.
	 * @return 作曲ランクのフォーマットされた文字列.
	 */
	private String getRankText() {
		MMLTools tools = new MMLTools(
				mmlText[0].getText(),
				mmlText[1].getText(),
				mmlText[2].getText(),
				mmlText[3].getText()
				);

		String rank = tools.mmlRankFormat();
		return rank;
	}

	private void updateComposeRank() {
		trackComposeLabel.setText( getRankText() );
	}

	private void setInstProgram(int program, int songProgram) {
		InstClass insts[] = MabiDLS.getInstance().getInsts();

		InstClass inst = InstClass.searchInstAtProgram(insts, program);
		if (inst != null) {
			comboBox.setSelectedItem(inst);
		} else {
			comboBox.setSelectedItem(insts[0]);
		}
		InstClass songInst = InstClass.searchInstAtProgram(insts, songProgram);
		if ( (songInst != null) && (songInst.getType() == InstType.CHORUS) ) {
			songComboBox.setSelectedItem(songInst);
		} else {
			songComboBox.setSelectedItem(noUseSongEx);
		}

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
		if (inst.getType() == InstType.VOICE) {
			songComboBox.setSelectedItem(noUseSongEx);
			songComboBox.setVisible(false);
		} else {
			songComboBox.setVisible(true);
		}

		InstClass songInst = ((InstClass) songComboBox.getSelectedItem());
		int program = inst.getProgram();
		int songProgram = songInst.getProgram();

		updatePartButtonStatus();

		if (mmlManager != null) {
			mmlManager.updateActiveTrackProgram(program, songProgram);
		}
	}

	private void updatePartButtonStatus() {
		InstClass inst = (InstClass) comboBox.getSelectedItem();
		InstClass songInst = ((InstClass) songComboBox.getSelectedItem());

		// 選択された楽器にあわせて、MMLパートの有効/無効化を行う.
		boolean instEnable[] = inst.getType().getEnablePart();
		boolean songExEnable[] = songInst.getType().getEnablePart();
		for (int i = 0; i < partButton.length; i++) {
			boolean b = (instEnable[i] || songExEnable[i]);
			partButton[i].setEnabled(b);
			mmlText[i].setEnabled(b);
			partButton[i].setIcon(
					(b) 
					? (PartButtonIconArray.getInstance(i, trackIndex)) : 
						(PartButtonIcon.getInstance()) );
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

	public void setMMLTrack(MMLTrack track) {
		mmlText[0].setText( track.getMelody() );
		mmlText[1].setText( track.getChord1() );
		mmlText[2].setText( track.getChord2() );
		mmlText[3].setText( track.getSongEx() );

		setInstProgram( track.getProgram(), track.getSongProgram() );
	}

	public void setActivePartMMLString(String mml) {
		int index = getSelectedMMLPartIndex();

		setPartMMLString(index, mml);
	}

	public void setPartMMLString(int index, String mml) {
		mmlText[index].setText(mml);
		updateComposeRank();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
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
}

/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTools;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MMLTrackView extends JPanel implements ActionListener, DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4955513242349170508L;
	private final String MMLPART_NAME[] = {
			"メロディー", "和音1", "和音2"
	};
	private JToggleButton partButton[];
	private JTextField mmlText[];

	@SuppressWarnings("rawtypes")
	private JComboBox comboBox;

	private JLabel trackComposeLabel;

	private int channel;

	private IMMLManager mmlManager;



	/**
	 * Create the panel.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MMLTrackView() {
		setLayout(new BorderLayout());

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(northPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) southPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(southPanel, BorderLayout.SOUTH);

		trackComposeLabel = new JLabel("");
		southPanel.add(trackComposeLabel);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{20, 0, 0, 0, 0, 20};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		centerPanel.setLayout(gridBagLayout);

		InstClass insts[] = null;
		try {
			insts = MabiDLS.getInstance().getInsts();
		} catch (NullPointerException e) {}
		if (insts == null) {
			comboBox = new JComboBox();
		} else {
			comboBox = new JComboBox(insts);
		}
		northPanel.add(comboBox);
		comboBox.addActionListener(this);
		comboBox.setMaximumRowCount(30);

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

	public MMLTrackView(MMLTrack track, int channel, ActionListener actionListener, IMMLManager mmlManager) {
		this();

		this.channel = channel;
		this.setMMLTrack(track);
		this.mmlManager = mmlManager;
		trackComposeLabel.setText(track.mmlRankFormat());

		for (int i = 0; i < MMLPART_NAME.length; i++) {
			partButton[i].addActionListener(actionListener);
		}

		MabiDLS.getInstance().changeProgram(track.getProgram(), channel);
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getChannel() {
		// TODO: 歌パートのときは、チャンネルを変える。
		return this.channel;
	}

	public String getMMLText() {
		MMLTools tools = new MMLTools(
				mmlText[0].getText(),
				mmlText[1].getText(),
				mmlText[2].getText()
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

	@Override
	public void removeUpdate(DocumentEvent event) {
	}
	@Override
	public void insertUpdate(DocumentEvent event) {
	}
	@Override
	public void changedUpdate(DocumentEvent event) {
	}

	private void updateComposeRank() {
		MMLTools tools = new MMLTools(
				mmlText[0].getText(),
				mmlText[1].getText(),
				mmlText[2].getText()
				);

		String rank = tools.mmlRankFormat();
		trackComposeLabel.setText(rank);
		System.out.println(rank);
	}

	public void setInstProgram(int program) {
		InstClass insts[] = MabiDLS.getInstance().getInsts();
		InstClass selectedInst = insts[0];
		for ( int i = 0; i < insts.length; i++ ) {
			if ( insts[i].getProgram()  == program ) {
				selectedInst = insts[i];
				break;
			}
		}

		comboBox.setSelectedItem(selectedInst);
	}

	public void setMMLTrack(MMLTrack track) {
		mmlText[0].setText( track.getMelody() );
		mmlText[1].setText( track.getChord1() );
		mmlText[2].setText( track.getChord2() );

		setInstProgram( track.getProgram() );
	}

	public void setActivePartMMLString(String mml) {
		int index = getSelectedMMLPartIndex();

		setPartMMLString(index, mml);
	}

	public void setPartMMLString(int index, String mml) {
		mmlText[index].setText(mml);
		updateComposeRank();
	}


	/**
	 * コンボボックスによる楽器の変更。
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == comboBox) {
			InstClass inst = (InstClass) comboBox.getSelectedItem();
			int program = inst.getProgram();

			MabiDLS.getInstance().changeProgram(program, this.channel);
			if (mmlManager != null) {
				mmlManager.updateActiveTrackProgram(program);
			}
		}
	}

}

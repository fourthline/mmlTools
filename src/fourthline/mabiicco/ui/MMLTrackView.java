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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MMLTrackView extends JPanel implements ActionListener, DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4955513242349170508L;
	private JTextField mmlText1;
	private JTextField mmlText2;
	private JTextField mmlText3;
	@SuppressWarnings("rawtypes")
	private JComboBox comboBox;

	private JLabel trackComposeLabel;

	private MMLTrack mmlTrack;
	private int channel;

	private JToggleButton partButton1;
	private JToggleButton partButton2;
	private JToggleButton partButton3;

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

		partButton1 = new JToggleButton("メロディー");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 1;
		centerPanel.add(partButton1, gbc_label);

		mmlText1 = new JTextField();
		mmlText1.setEditable(false);
		mmlText1.setFont(new Font("Monospaced", Font.PLAIN, 12));
		mmlText1.getDocument().addDocumentListener(this);

		GridBagConstraints gbc_mmlText1 = new GridBagConstraints();
		gbc_mmlText1.insets = new Insets(0, 0, 5, 5);
		gbc_mmlText1.fill = GridBagConstraints.HORIZONTAL;
		gbc_mmlText1.gridx = 3;
		gbc_mmlText1.gridy = 1;
		centerPanel.add(mmlText1, gbc_mmlText1);
		mmlText1.setColumns(10);

		partButton2 = new JToggleButton("和音1");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 3;
		centerPanel.add(partButton2, gbc_label_1);

		mmlText2 = new JTextField();
		mmlText2.setEditable(false);
		mmlText2.setFont(new Font("Monospaced", Font.PLAIN, 12));
		mmlText2.getDocument().addDocumentListener(this);

		GridBagConstraints gbc_mmlText2 = new GridBagConstraints();
		gbc_mmlText2.insets = new Insets(0, 0, 5, 5);
		gbc_mmlText2.fill = GridBagConstraints.HORIZONTAL;
		gbc_mmlText2.gridx = 3;
		gbc_mmlText2.gridy = 3;
		centerPanel.add(mmlText2, gbc_mmlText2);
		mmlText2.setColumns(10);

		partButton3 = new JToggleButton("和音2");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 1;
		gbc_label_2.gridy = 5;
		centerPanel.add(partButton3, gbc_label_2);

		mmlText3 = new JTextField();
		mmlText3.setEditable(false);
		mmlText3.setFont(new Font("Monospaced", Font.PLAIN, 12));
		mmlText3.getDocument().addDocumentListener(this);

		GridBagConstraints gbc_mmlText3 = new GridBagConstraints();
		gbc_mmlText3.insets = new Insets(0, 0, 5, 5);
		gbc_mmlText3.fill = GridBagConstraints.HORIZONTAL;
		gbc_mmlText3.gridx = 3;
		gbc_mmlText3.gridy = 5;
		centerPanel.add(mmlText3, gbc_mmlText3);
		mmlText3.setColumns(10);
		
		ButtonGroup bGroup = new ButtonGroup();
		bGroup.add(partButton1);
		bGroup.add(partButton2);
		bGroup.add(partButton3);
		partButton1.setSelected(true);

		updateComposeRank(null);
	}

	public MMLTrackView(MMLTrack track, int channel, ActionListener actionListener) {
		this();

		this.channel = channel;
		this.setMMLTrack(track);
		trackComposeLabel.setText(track.mmlRankFormat());

		partButton1.addActionListener(actionListener);
		partButton2.addActionListener(actionListener);
		partButton3.addActionListener(actionListener);
		
		MabiDLS.getInstance().changeProgram(track.getProgram(), channel);
	}
	
	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	public int getChannel() {
		// TODO: 歌パートのときは、チャンネルを変える。
		return this.channel;
	}
	
	/**
	 * 選択中のMMLパートのindex値を返します.
	 * @return
	 */
	public int getSelectedMMLPartIndex() {
		int index = 0;
		
		if (partButton1.isSelected()) {
			index = 0;
		} else if (partButton2.isSelected()) {
			index = 1;
		} else if (partButton3.isSelected()) {
			index = 2;
		}
		
		return index;
	}

	@Override
	public void removeUpdate(DocumentEvent event) {
		updateComposeRank(event);
	}
	@Override
	public void insertUpdate(DocumentEvent event) {
		updateComposeRank(event);
	}
	@Override
	public void changedUpdate(DocumentEvent event) {
		updateComposeRank(event);
	}

	private void updateComposeRank(DocumentEvent event) {
		if (mmlTrack == null) {
			return;
		}
		

		String rank = mmlTrack.mmlRankFormat();
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
		mmlText1.setText( track.getMelody() );
		mmlText2.setText( track.getChord1() );
		mmlText3.setText( track.getChord2() );

		setInstProgram( track.getProgram() );

		this.mmlTrack = track;
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
			if (mmlTrack != null) {
				mmlTrack.setProgram(program);
			}
		}
	}

}

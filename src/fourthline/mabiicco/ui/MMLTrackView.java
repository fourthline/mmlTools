/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;

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
import fourthline.mmlTools.parser.MMLTrack;

import java.awt.Font;

public class MMLTrackView extends JPanel implements IInstSource {
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
		
		JLabel label = new JLabel("メロディー");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 1;
		centerPanel.add(label, gbc_label);
		
		mmlText1 = new JTextField();
		mmlText1.setFont(new Font("Monospaced", Font.PLAIN, 12));
		mmlText1.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
		});
		
		GridBagConstraints gbc_mmlText1 = new GridBagConstraints();
		gbc_mmlText1.insets = new Insets(0, 0, 5, 5);
		gbc_mmlText1.fill = GridBagConstraints.HORIZONTAL;
		gbc_mmlText1.gridx = 3;
		gbc_mmlText1.gridy = 1;
		centerPanel.add(mmlText1, gbc_mmlText1);
		mmlText1.setColumns(10);
		
		JLabel label_1 = new JLabel("和音1");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 3;
		centerPanel.add(label_1, gbc_label_1);
		
		mmlText2 = new JTextField();
		mmlText2.setFont(new Font("Monospaced", Font.PLAIN, 12));
		mmlText2.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
		});
		GridBagConstraints gbc_mmlText2 = new GridBagConstraints();
		gbc_mmlText2.insets = new Insets(0, 0, 5, 5);
		gbc_mmlText2.fill = GridBagConstraints.HORIZONTAL;
		gbc_mmlText2.gridx = 3;
		gbc_mmlText2.gridy = 3;
		centerPanel.add(mmlText2, gbc_mmlText2);
		mmlText2.setColumns(10);
		
		JLabel label_2 = new JLabel("和音2");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 1;
		gbc_label_2.gridy = 5;
		centerPanel.add(label_2, gbc_label_2);
		
		mmlText3 = new JTextField();
		mmlText3.setFont(new Font("Monospaced", Font.PLAIN, 12));
		mmlText3.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateComposeRank();
			}
		});
		GridBagConstraints gbc_mmlText3 = new GridBagConstraints();
		gbc_mmlText3.insets = new Insets(0, 0, 5, 5);
		gbc_mmlText3.fill = GridBagConstraints.HORIZONTAL;
		gbc_mmlText3.gridx = 3;
		gbc_mmlText3.gridy = 5;
		centerPanel.add(mmlText3, gbc_mmlText3);
		mmlText3.setColumns(10);

		updateComposeRank();
	}
	
	public MMLTrackView(MMLTrack track) {
		 this();
		 
		 this.setMMLTrack(track);
		 trackComposeLabel.setText(track.mmlRankFormat());
	}
	
	private void updateComposeRank() {
		String melody = mmlText1.getText();
		String chord1 = mmlText2.getText();
		String chord2 = mmlText3.getText();
		mmlTrack = new MMLTrack(melody, chord1, chord2);
		
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
	
	public int getInstProgram() {
		InstClass inst = (InstClass) comboBox.getSelectedItem();
		
		return inst.getProgram();
	}
	
	public void setMML(String mml) {
		this.mmlTrack = new MMLTrack(mml);
		mmlText1.setText(this.mmlTrack.getMelody());
		mmlText2.setText(this.mmlTrack.getChord1());
		mmlText3.setText(this.mmlTrack.getChord2());
	}
	
	public void setMMLTrack(MMLTrack track) {
		mmlText1.setText( track.getMelody() );
		mmlText2.setText( track.getChord1() );
		mmlText3.setText( track.getChord2() );
		
		setInstProgram( track.getProgram() );
		
		this.mmlTrack = track;
	}
	
	public MMLTrack getMMLTrack() {
		if (mmlTrack == null) {
			this.mmlTrack = new MMLTrack(
					mmlText1.getText(),
					mmlText2.getText(),
					mmlText3.getText()
					);
		}
		
		this.mmlTrack.setProgram( getInstProgram() );
		
		return this.mmlTrack;
	}
	
	public String getName() {
		if (mmlTrack != null) {
			return mmlTrack.getName();
		}
		
		return "";
	}

}

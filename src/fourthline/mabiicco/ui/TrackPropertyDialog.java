/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSlider;

import fourthline.mmlTools.MMLTrack;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class TrackPropertyDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7599129671956571455L;
	private final JPanel contentPanel = new JPanel();
	private JTextField trackNameField;
	private JSlider panpotSlider;
	
	MMLTrack track;
	INotifyMMLTrackProperty notify;

	/**
	 * Create the dialog.
	 */
	public TrackPropertyDialog() {
		super();
		initialize();
	}
	
	private void initialize() {
		setModal(true);
		setResizable(false);
		setTitle("トラックプロパティ");
		setBounds(100, 100, 363, 285);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("トラック名");
		lblNewLabel.setBounds(38, 39, 50, 13);
		contentPanel.add(lblNewLabel);
		
		trackNameField = new JTextField();
		trackNameField.setBounds(100, 36, 200, 19);
		// TODO: 日本語を入力したとき、再生時にCPU負荷がかかる問題あり.
		trackNameField.setEditable(false);
		contentPanel.add(trackNameField);
		trackNameField.setColumns(10);
		
		panpotSlider = new JSlider();
		panpotSlider.setSnapToTicks(true);
		panpotSlider.setPaintTicks(true);
		panpotSlider.setValue(64);
		panpotSlider.setMinorTickSpacing(16);
		panpotSlider.setMajorTickSpacing(16);
		panpotSlider.setMaximum(128);
		panpotSlider.setBounds(100, 97, 200, 23);
		contentPanel.add(panpotSlider);
		
		JLabel label = new JLabel("パンポット");
		label.setBounds(38, 97, 50, 13);
		contentPanel.add(label);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveProperty();
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("キャンセル");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				InputMap imap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
				getRootPane().getActionMap().put("escape", new AbstractAction(){
					private static final long serialVersionUID = 8365149917383455221L;

					@Override
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				}); 
			}
		}
	}
	
	private void saveProperty() {
		track.setTrackName( trackNameField.getText() );
		track.setPanpot( panpotSlider.getValue() );
		notify.setTrackProperty(track);
	}

	public TrackPropertyDialog(Window owner, INotifyMMLTrackProperty notify) {
		super(owner);
		initialize();
		this.notify = notify;
	}
	
	
	public void showDialog(MMLTrack track) { 
		this.track = track;
		trackNameField.setText(track.getTrackName());
		panpotSlider.setValue(track.getPanpot());
		setLocationRelativeTo(null);
		setVisible(true);
	}
}

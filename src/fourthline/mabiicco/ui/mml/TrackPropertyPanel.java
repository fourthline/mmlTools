/*
 * Copyright (C) 2013-2015 たんらる
 */

package fourthline.mabiicco.ui.mml;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSlider;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLTrack;


public final class TrackPropertyPanel extends JPanel {
	private static final long serialVersionUID = 7599129671956571455L;
	private JTextField trackNameField;
	private JSlider panpotSlider;
	private final IMMLManager mmlManager;

	private final MMLTrack track;

	/**
	 * Create the dialog.
	 */
	public TrackPropertyPanel(MMLTrack track, IMMLManager mmlManager) {
		super();
		this.track = track;
		this.mmlManager = mmlManager;
		initialize();
	}

	private void initialize() {
		setBounds(100, 100, 363, 285);
		setLayout(null);

		JLabel lblNewLabel = new JLabel(AppResource.appText("track_property.trackname"));
		lblNewLabel.setBounds(30, 39, 70, 14);
		add(lblNewLabel);

		trackNameField = new JTextField();
		trackNameField.setBounds(100, 36, 200, 19);
		trackNameField.setEditable(true);
		add(trackNameField);
		trackNameField.setColumns(10);

		panpotSlider = new JSlider();
		panpotSlider.setSnapToTicks(true);
		panpotSlider.setPaintTicks(true);
		panpotSlider.setValue(64);
		panpotSlider.setMinorTickSpacing(16);
		panpotSlider.setMajorTickSpacing(16);
		panpotSlider.setMaximum(128);
		panpotSlider.setBounds(100, 97, 200, 23);
		add(panpotSlider);

		JLabel panpotL = new JLabel("L64");
		panpotL.setBounds(100, 120, 30, 23);
		add(panpotL);
		JLabel panpotC = new JLabel("0");
		panpotC.setBounds(197, 120, 30, 23);
		add(panpotC);
		JLabel panpotR = new JLabel("R64");
		panpotR.setBounds(282, 120, 30, 23);
		add(panpotR);

		JLabel label = new JLabel(AppResource.appText("track_property.panpot"));
		label.setBounds(30, 97, 70, 14);
		add(label);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 150);
	}

	private void applyProperty() {
		track.setTrackName( trackNameField.getText() );
		track.setPanpot( panpotSlider.getValue() );
		mmlManager.updateActivePart(false);
	}

	public void showDialog(Frame parentFrame) {
		trackNameField.setText(track.getTrackName());
		panpotSlider.setValue(track.getPanpot());

		int status = JOptionPane.showConfirmDialog(parentFrame, 
				this,
				AppResource.appText("track_property"), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (status == JOptionPane.OK_OPTION) {
			applyProperty();
		}
	}
}

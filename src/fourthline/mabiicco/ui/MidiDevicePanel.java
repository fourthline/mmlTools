/*
 * Copyright (C) 2016 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;
import java.awt.Frame;

import javax.sound.midi.MidiDevice;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.MidiIn;


public final class MidiDevicePanel extends JPanel {
	private static final long serialVersionUID = 5991296719565714557L;
	private JComboBox<String> midiInComboBox;

	private MidiDevice.Info selectedInfo;
	/**
	 * Create the dialog.
	 */
	public MidiDevicePanel() {
		super();
		initialize();
	}

	private MidiDevice.Info infos[];
	private void initialize() {
		setBounds(100, 100, 363, 285);
		setLayout(null);

		JLabel label = new JLabel(AppResource.appText("midi_device.midi_in"));
		label.setBounds(30, 50, 70, 13);
		add(label);

		midiInComboBox = new JComboBox<>();
		midiInComboBox.setBounds(100, 50, 160, 19);
		add(midiInComboBox);
		infos = MidiIn.getMidiInDeviceInfos();

		for(MidiDevice.Info info: infos){
			midiInComboBox.addItem(info.getName());
		}
		// デバイスを選択しない選択肢
		midiInComboBox.addItem("-");
		int selectIndex = midiInComboBox.getItemCount() - 1;

		int index = MabiIccoProperties.getInstance().getMidiInIndex();
		String name = MabiIccoProperties.getInstance().getMidiInName();
		if( index < infos.length ){
			if( name.equals(infos[index].getName())){
				selectIndex = index;
			}
		}
		midiInComboBox.setSelectedIndex(selectIndex);

	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 150);
	}

	private void applySetting() {
		int index = midiInComboBox.getSelectedIndex();
		String name = (String)midiInComboBox.getSelectedItem();
		if(index < infos.length){
			this.selectedInfo = infos[index];
		}else{
			this.selectedInfo = null;
		}
		MabiIccoProperties.getInstance().setMidiIn(index, name);
	}

	public void showDialog(Frame parentFrame) {
		int status = JOptionPane.showConfirmDialog(parentFrame,
				this,
				AppResource.appText("menu.midi_device"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (status == JOptionPane.OK_OPTION) {
			applySetting();
		}
	}

	public MidiDevice.Info getSelected(){
		return this.selectedInfo;
	}

}

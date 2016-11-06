/*
 * Copyright (C) 2016 たんらる
 */

package fourthline.mabiicco.midi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

public class MidiIn {

	public static void loadMidiInDevice(MidiDevice.Info device, IPlayMidiMessage player )throws MidiUnavailableException, IOException, InvalidMidiDataException {
		clearMidiInDevice();
		System.out.println("Load : " + device.getName());
		MidiDevice midiDev = MidiSystem.getMidiDevice(device);
		midiDev.getTransmitter().setReceiver(new MabiReceiver(player));
		midiDev.open();
	}

	public static void clearMidiInDevice()throws MidiUnavailableException, IOException, InvalidMidiDataException {
		for(MidiDevice.Info info:MidiIn.getMidiInDeviceInfos()){
			MidiDevice device = MidiSystem.getMidiDevice(info);
			if(device.isOpen()){
				System.out.println("Close : " + info.getName());
				device.close();
			}
		}
	}

	public static MidiDevice.Info[] getMidiInDeviceInfos() {
		MidiDevice.Info midiInfos[] = MidiSystem.getMidiDeviceInfo();
		List<MidiDevice.Info> midiIn = new ArrayList<>();

		for( MidiDevice.Info info : midiInfos){
//			printMidiDeviceInfo(info);
			try{
				MidiDevice device = MidiSystem.getMidiDevice(info);
				int numTrans = device.getMaxTransmitters();
				if( numTrans != 0
					&& !(device instanceof Synthesizer)
					&& !(device instanceof Sequencer)){
//					System.out.println("transmitters: " + numTrans);
					midiIn.add(info);
				}
			}catch(MidiUnavailableException e){
				e.printStackTrace();
				continue;
			}
//			System.out.println("\n");
		}
		MidiDevice.Info midiInInfo[] =midiIn.toArray(new MidiDevice.Info[midiIn.size()]);
		return midiInInfo;
	}

	public static void printMidiDeviceInfo(MidiDevice.Info info){
		System.out.println(info.getDescription());
		System.out.println(info.getName());
		System.out.println(info.getVendor());
		System.out.println(info.getVersion());
		System.out.println(info.getClass());
	}
}

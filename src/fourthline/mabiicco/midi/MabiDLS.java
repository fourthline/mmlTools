/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.midi;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import javax.sound.midi.*;

import fourthline.mmlTools.parser.MMLTempoEvent;

public final class MabiDLS {
	private static final MabiDLS instance = new MabiDLS();
	private Synthesizer synthesizer;
	private Sequencer sequencer;
	private Soundbank sb;
	private MidiChannel[] channel;
	private InstClass[] insts;
	private Properties instProperties;
	
	private static final String INST_PROPERTIESFILE = "instrument.properties";
	public static final String DEFALUT_DLS_PATH = "C:/Nexon/Mabinogi/mp3/MSXspirit.dls";
	
	public static MabiDLS getInstance() {
		return instance;
	}
	
	private MabiDLS() {
	}
	
	public void initialize(String dls_filename) throws MidiUnavailableException, InvalidMidiDataException, IOException {
		this.synthesizer = MidiSystem.getSynthesizer();
		this.synthesizer.open();

		this.sequencer = MidiSystem.getSequencer();
		this.sequencer.open();
		this.sequencer.addMetaEventListener(new MetaEventListener() {
			@Override
			public void meta(MetaMessage meta) {
				if (meta.getType() == MMLTempoEvent.META) {
					byte metaData[] = meta.getData();
					int tempo = metaData[0] & 0xff;
					sequencer.setTempoInBPM(tempo);
					System.out.println(" [midi-event] tempo: " + tempo);
				}
			}
		});

		// シーケンサとシンセサイザの接続
		Receiver receiver = this.synthesizer.getReceiver();
		Transmitter transmitter = this.sequencer.getTransmitters().get(0);
		transmitter.setReceiver(receiver);
		
		// 楽器名の読み込み
		try {
			instProperties = new Properties();
			instProperties.load(new InputStreamReader(MabiDLS.class.getClassLoader().getResourceAsStream(INST_PROPERTIESFILE), "UTF-8"));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		this.sb = loadDLS();
		this.synthesizer.loadAllInstruments(sb);
		this.channel = this.synthesizer.getChannels();
	}

	public Sequencer getSequencer() {
		return sequencer;
	}
	
	public Synthesizer getSynthesizer() {
		return synthesizer;
	}
	
	public MidiChannel getChannel(int ch) {
		return channel[ch];
	}
	
	private String instName(Instrument inst) {
		String name = instProperties.getProperty(""+inst.getPatch().getProgram());
		
		return name;
	}
	
	private Soundbank loadDLS() throws InvalidMidiDataException, IOException {
		File dlsFile = new File(DEFALUT_DLS_PATH);
		sb = MidiSystem.getSoundbank(dlsFile);
		
		Instrument inst[] = sb.getInstruments();
		ArrayList<InstClass> instArray = new ArrayList<InstClass>();
		for (int i = 0; i < inst.length; i++) {
			String name = instName(inst[i]);
			int bank = inst[i].getPatch().getBank();
			int program = inst[i].getPatch().getProgram();
			if (name != null) {
				instArray.add(new InstClass( instName(inst[i]),
						bank,
						program ));
			}
			System.out.printf("%d=%s\n", program, name);
		}
		
		insts = new InstClass[instArray.size()];
		insts = instArray.toArray(insts);

		return sb;
	}
	
	
	public InstClass[] getInsts() {
		return insts;
	}
	
	private int play_note = -1;
	/** 単音再生 */
	public void playNote(int note) {
		MidiChannel ch0 = this.getChannel(0);
		
		if (note < 0) {
			ch0.allNotesOff();
			play_note = -1;
		} else if (note != play_note) {
			ch0.noteOff(play_note);
			ch0.noteOn(note, 100);
			play_note = note;
		}
	}
	
	public void changeProgram(int program) {
		MidiChannel ch0 = this.getChannel(0);
		if (ch0.getProgram() != program) {
			ch0.programChange(program);
		}
	}

}




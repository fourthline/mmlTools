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
	private MidiChannel[] channel;
	private InstClass[] insts;
	private Properties instProperties;
	
	private static final String INST_PROPERTIESFILE = "instrument.properties";
	public static final String DEFALUT_DLS_PATH = "C:/Nexon/Mabinogi/mp3/MSXspirit.dls";
	
	private INotifyTrackEnd notifier = null;
	
	public static MabiDLS getInstance() {
		return instance;
	}
	
	/**
	 * initializeMIDI() -> initializeSound() 
	 */
	private MabiDLS() {
	}
	
	public void initializeMIDI() throws MidiUnavailableException, InvalidMidiDataException, IOException {
		this.synthesizer = MidiSystem.getSynthesizer();
		this.synthesizer.open();
		
		long latency = this.synthesizer.getLatency();
		int maxPolyphony = this.synthesizer.getMaxPolyphony();
		System.out.printf("Latency: %d\nMaxPolyphony: %d\n", latency, maxPolyphony);

		this.sequencer = MidiSystem.getSequencer();
		this.sequencer.open();
		this.sequencer.addMetaEventListener(new MetaEventListener() {
			@Override
			public void meta(MetaMessage meta) {
				int type = meta.getType();
				if (type == MMLTempoEvent.META) {
					// テンポイベントを処理します.
					byte metaData[] = meta.getData();
					int tempo = metaData[0] & 0xff;
					sequencer.setTempoInBPM(tempo);
					System.out.println(" [midi-event] tempo: " + tempo);
				} else if (type == 0x2f) {
					// トラック終端
					if (notifier != null) {
						notifier.trackEndNotify();
					}
				}
			}
		});
	}
	
	public void setTrackEndNotifier(INotifyTrackEnd n) {
		notifier = n;
	}
	
	public void initializeSound(File dlsFile) throws MidiUnavailableException, InvalidMidiDataException, IOException {
		// 楽器名の読み込み
		try {
			instProperties = new Properties();
			instProperties.load(new InputStreamReader(MabiDLS.class.getClassLoader().getResourceAsStream(INST_PROPERTIESFILE), "UTF-8"));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		// シーケンサとシンセサイザの初期化
		Soundbank sb = loadDLS(dlsFile);
		Receiver receiver = initializeSynthesizer(sb);
		Transmitter transmitter = this.sequencer.getTransmitters().get(0);
		transmitter.setReceiver(receiver);
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
	
	private Soundbank loadDLS(File dlsFile) throws InvalidMidiDataException, IOException {
		Soundbank sb = MidiSystem.getSoundbank(dlsFile);
		
		Instrument inst[] = sb.getInstruments();
		ArrayList<InstClass> instArray = new ArrayList<InstClass>();
		for (int i = 0; i < inst.length; i++) {
			String name = instName(inst[i]);
			String originalName = inst[i].getName();
			int bank = inst[i].getPatch().getBank();
			int program = inst[i].getPatch().getProgram();
			if (name == null) {
				name = "*" + originalName;
			}
			name = ""+program+": "+name;
			instArray.add(new InstClass( name,
					bank,
					program ));
			System.out.printf("%d=%s \"%s\"\n", program,  originalName, name);
		}
		
		insts = new InstClass[instArray.size()];
		insts = instArray.toArray(insts);

		return sb;
	}
	
	
	private Receiver initializeSynthesizer(Soundbank sb) throws InvalidMidiDataException, IOException, MidiUnavailableException {
		this.synthesizer.loadAllInstruments(sb);
		this.channel = this.synthesizer.getChannels();
		Receiver receiver = this.synthesizer.getReceiver();
		
		// リバーブ設定
		for (int i = 0; i < this.channel.length; i++) {
			/* ctrl 91 汎用エフェクト 1(リバーブ) */
			ShortMessage message = new ShortMessage(ShortMessage.CONTROL_CHANGE, 
					i,
					91,
					0);
			
			receiver.send(message, 0);
		}
		
		return receiver;
	}
	
	
	public InstClass[] getInsts() {
		return insts;
	}
	
	private int play_note = -1;
	/** 単音再生 */
	public void playNote(int note, int channel) {
		/* シーケンサによる再生中は鳴らさない */
		if (sequencer.isRunning()) {
			return;
		}
		MidiChannel midiChannel = this.getChannel(channel);
		
		if (note < 0) {
			midiChannel.allNotesOff();
			play_note = -1;
		} else if (note != play_note) {
			midiChannel.noteOff(play_note);
			midiChannel.noteOn(note, 100);
			play_note = note;
		}
	}
	
	public void changeProgram(int program, int channel) {
		MidiChannel midiChannel = this.getChannel(channel);
		if (midiChannel.getProgram() != program) {
			midiChannel.programChange(program);
		}
	}

}




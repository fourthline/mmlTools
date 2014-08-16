/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.midi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sound.midi.*;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.ResourceLoader;

public final class MabiDLS {
	private static MabiDLS instance = null;
	private Synthesizer synthesizer;
	private Sequencer sequencer;
	private MidiChannel channel[];
	private ArrayList<MMLNoteEvent[]> playNoteList;
	private static final int MAX_CHANNEL_PLAY_NOTE = 4;
	private InstClass insts[];
	private ResourceBundle instResource;

	private static final String RESOURCE_NAME = "instrument";
	public static final String DEFALUT_DLS_PATH = "C:/Nexon/Mabinogi/mp3/MSXspirit.dls";

	private INotifyTrackEnd notifier = null;

	public static MabiDLS getInstance() {
		if (instance == null) {
			instance = new MabiDLS();
		}
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
		this.sequencer.addMetaEventListener((meta) -> {
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
		});
	}

	public void setTrackEndNotifier(INotifyTrackEnd n) {
		notifier = n;
	}

	public void initializeSound(File dlsFile) throws MidiUnavailableException, InvalidMidiDataException, IOException {
		// 楽器名の読み込み
		instResource = ResourceBundle.getBundle(RESOURCE_NAME, new ResourceLoader());

		// シーケンサとシンセサイザの初期化
		loadDLS(dlsFile);
		Receiver receiver = initializeSynthesizer();
		Transmitter transmitter = this.sequencer.getTransmitters().get(0);
		transmitter.setReceiver(receiver);
	}

	public synchronized void loadRequiredInstruments(MMLScore score) {
		ArrayList<InstClass> requiredInsts = new ArrayList<>();
		for (MMLTrack track : score.getTrackList()) {
			InstClass inst1 = getInstByProgram( track.getProgram() );
			InstClass inst2 = getInstByProgram( track.getSongProgram() );
			if ( (inst1 != null) && (!requiredInsts.contains(inst1)) ) {
				requiredInsts.add(inst1);
			}
			if ( (inst2 != null) && (!requiredInsts.contains(inst2)) ) {
				requiredInsts.add(inst2);
			}
		}

		// load required Instruments
		for (InstClass inst : requiredInsts) {
			try {
				synthesizer.loadInstrument(inst.getInstrument());
			} catch (OutOfMemoryError e) {
				System.exit(1);
			}
		}
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
		try {
			String name = instResource.getString(""+inst.getPatch().getProgram());
			return name;
		} catch (MissingResourceException e) {
			return null;
		}
	}

	private void loadDLS(File dlsFile) throws InvalidMidiDataException, IOException {
		Soundbank sb = MidiSystem.getSoundbank(dlsFile);

		ArrayList<InstClass> instArray = new ArrayList<>();
		for (Instrument inst : sb.getInstruments()) {
			String name = instName(inst);
			String originalName = inst.getName();
			int bank = inst.getPatch().getBank();
			int program = inst.getPatch().getProgram();
			System.out.printf("%d=%s \"%s\"\n", program,  originalName, name);
			if (name != null) {
				name = ""+program+": "+name;
				instArray.add(new InstClass( name,
						bank,
						program,
						inst));
			}
		}

		insts = new InstClass[instArray.size()];
		insts = instArray.toArray(insts);
	}

	private Receiver initializeSynthesizer() throws InvalidMidiDataException, IOException, MidiUnavailableException {
		this.channel = this.synthesizer.getChannels();
		this.playNoteList = new ArrayList<>();
		for (int i = 0; i < this.channel.length; i++) {
			this.playNoteList.add(new MMLNoteEvent[MAX_CHANNEL_PLAY_NOTE]);
		}
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

	public InstClass getInstByProgram(int program) {
		for (InstClass inst : insts) {
			if (inst.getProgram() == program) {
				return inst;
			}
		}

		return null;
	}

	/** 単音再生 */
	public void playNote(int note, int program, int channel) {
		MMLNoteEvent playNote = this.playNoteList.get(channel)[0];
		if ( (playNote == null) || (playNote.getNote() != note) ) {
			playNote = new MMLNoteEvent(note, 0, 0, 11);
		}
		playNotes(new MMLNoteEvent[] { playNote }, program, channel);
	}

	/** 和音再生 */
	public void playNotes(MMLNoteEvent noteList[], int program, int channel) {
		/* シーケンサによる再生中は鳴らさない */
		if (sequencer.isRunning()) {
			return;
		}
		changeProgram(program, channel);
		MidiChannel midiChannel = this.getChannel(channel);
		MMLNoteEvent[] playNoteList = this.playNoteList.get(channel);

		for (int i = 0; i < playNoteList.length; i++) {
			MMLNoteEvent note = null;
			if ( (noteList != null) && (i < noteList.length) ) {
				note = noteList[i];
			}
			if ( (note == null) || (note != playNoteList[i]) ) {
				if (playNoteList[i] != null) {
					midiChannel.noteOff( convertNoteMML2Midi(playNoteList[i].getNote()) );
					playNoteList[i] = null;
				}
			}
			if (note != playNoteList[i]) {
				InstType instType = getInstByProgram(program).getType();
				int volumn = instType.convertVelocityMML2Midi(note.getVelocity());
				midiChannel.noteOn( convertNoteMML2Midi(note.getNote()), volumn);
				playNoteList[i] = note;
			}
		}
	}

	public void changeProgram(int program, int channel) {
		MidiChannel midiChannel = this.getChannel(channel);
		if (midiChannel.getProgram() != program) {
			midiChannel.programChange(0, program);
		}
	}
	/**
	 * 指定したチャンネルのパンポットを設定します.
	 * @param ch_num
	 * @param panpot
	 */
	public void setChannelPanpot(int ch_num, int panpot) {
		channel[ch_num].controlChange(10, panpot);
	}

	public void toggleMute(int ch) {
		channel[ch].setMute(!channel[ch].getMute());
	}

	public void setMute(int ch, boolean mute) {
		channel[ch].setMute(mute);
	}

	public void solo(int ch) {
		for (MidiChannel c : channel) {
			c.setMute(true);
		}

		channel[ch].setMute(false);
	}

	public void all() {
		for (MidiChannel c : channel) {
			c.setMute(false);
		}
	}

	public static void main(String args[]) {
		try {
			MabiDLS midi = new MabiDLS();
			midi.initializeMIDI();
			for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
				System.out.println(info);
			}

			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePanpot(MMLScore score) {
		int trackCount = 0;
		for (MMLTrack mmlTrack : score.getTrackList()) {
			int panpot = mmlTrack.getPanpot();
			this.setChannelPanpot(trackCount, panpot);
			trackCount++;
		}
	}

	/**
	 * MIDIシーケンスを作成します。
	 * @throws InvalidMidiDataException 
	 */
	public Sequence createSequence(MMLScore score) throws InvalidMidiDataException {
		Sequence sequence = new Sequence(Sequence.PPQ, 96);

		int trackCount = 0;
		for (MMLTrack mmlTrack : score.getTrackList()) {
			convertMidiTrack(sequence.createTrack(), mmlTrack, trackCount);
			trackCount++;
		}

		// グローバルテンポ
		Track track = sequence.getTracks()[0];
		List<MMLTempoEvent> globalTempoList = score.getTempoEventList();
		for (MMLTempoEvent tempoEvent :  globalTempoList) {
			byte tempo[] = tempoEvent.getMetaData();
			int tickOffset = tempoEvent.getTickOffset();

			MidiMessage message = new MetaMessage(MMLTempoEvent.META, 
					tempo, tempo.length);
			track.add(new MidiEvent(message, tickOffset));
		}

		// コーラスパートの作成
		createVoiceMidiTrack(sequence, score, 11, 100); // 男声コーラス
		createVoiceMidiTrack(sequence, score, 12, 110); // 女声コーラス

		return sequence;
	}

	private void createVoiceMidiTrack(Sequence sequence, MMLScore score, int channel, int program) throws InvalidMidiDataException {
		Track track = sequence.createTrack();
		ShortMessage pcMessage = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 
				channel,
				program,
				0);
		track.add(new MidiEvent(pcMessage, 0));

		for (MMLTrack mmlTrack : score.getTrackList()) {
			if (mmlTrack.getSongProgram() != program) {
				continue;
			}

			InstType instType = InstClass.searchInstAtProgram(insts, program).getType();
			convertMidiPart(track, mmlTrack.getMMLEventAtIndex(3), channel, instType);
		}
	}

	/**
	 * トラックに含まれるすべてのMMLEventListを1つのMIDIトラックに変換します.
	 * @param track
	 * @param channel
	 * @throws InvalidMidiDataException
	 */
	private void convertMidiTrack(Track track, MMLTrack mmlTrack, int channel) throws InvalidMidiDataException {
		int program = mmlTrack.getProgram();
		ShortMessage pcMessage = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 
				channel,
				program,
				0);
		track.add(new MidiEvent(pcMessage, 0));
		boolean enablePart[] = InstClass.getEnablePartByProgram(program);
		InstType instType = getInstByProgram(mmlTrack.getProgram()).getType();

		ArrayList<MMLEventList> registedPart = new ArrayList<>();
		for (int i = 0; i < enablePart.length; i++) {
			if (enablePart[i]) {
				MMLEventList eventList = mmlTrack.getMMLEventAtIndex(i);
				MMLEventList playList = eventList.clone().emulateMabiPlay(registedPart, mmlTrack.getGlobalTempoList());
				convertMidiPart(track, playList, channel, instType);
				registedPart.add(eventList);
			}
		}
	}

	private void convertMidiPart(Track track, MMLEventList eventList, int channel, InstType inst) {
		int volumn = MMLNoteEvent.INITIAL_VOLUMN;

		// Noteイベントの変換
		for ( MMLNoteEvent noteEvent : eventList.getMMLNoteEventList() ) {
			int note = noteEvent.getNote();
			int tick = noteEvent.getTick();
			int tickOffset = noteEvent.getTickOffset() + 1;
			int endTickOffset = tickOffset + tick - 1;

			// ボリュームの変更
			if (noteEvent.getVelocity() >= 0) {
				volumn = noteEvent.getVelocity();
			}

			try {
				// ON イベント作成
				MidiMessage message1 = new ShortMessage(ShortMessage.NOTE_ON, 
						channel,
						convertNoteMML2Midi(note), 
						inst.convertVelocityMML2Midi(volumn));
				track.add(new MidiEvent(message1, tickOffset));

				// Off イベント作成
				MidiMessage message2 = new ShortMessage(ShortMessage.NOTE_OFF,
						channel, 
						convertNoteMML2Midi(note),
						0);
				track.add(new MidiEvent(message2, endTickOffset));
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
	}

	private int convertNoteMML2Midi(int mml_note) {
		return (mml_note + 12);
	}
}

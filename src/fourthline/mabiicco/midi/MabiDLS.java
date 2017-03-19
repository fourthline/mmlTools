/*
 * Copyright (C) 2013-2017 たんらる
 */

package fourthline.mabiicco.midi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.*;
import javax.sound.sampled.LineUnavailableException;

import com.sun.media.sound.DLSInstrument;
import com.sun.media.sound.DLSRegion;
import com.sun.media.sound.SoftSynthesizer;

import fourthline.mabiicco.AppErrorHandler;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTickTable;

/**
 * MabinogiのDLSファイルを使ってMIDIを扱います.
 */
public final class MabiDLS {
	private static MabiDLS instance = null;
	private Synthesizer synthesizer;
	private Sequencer sequencer;
	private MidiChannel channel[];
	private ArrayList<MMLNoteEvent[]> playNoteList = new ArrayList<>();
	private static final int MAX_CHANNEL_PLAY_NOTE = 4;
	public static final int MAX_MIDI_PART = 16;
	private ArrayList<InstClass> insts = new ArrayList<>();
	private static final int DLS_BANK = (0x79 << 7);

	public static final String DEFALUT_DLS_PATH = "Nexon/Mabinogi/mp3/MSXspirit.dls";

	private ArrayList<Runnable> notifier = new ArrayList<>();
	private boolean muteState[] = new boolean[ MAX_MIDI_PART ];
	private WavoutDataLine wavout;

	public static MabiDLS getInstance() {
		if (instance == null) {
			instance = new MabiDLS();
		}
		return instance;
	}

	private MabiDLS() {}

	/**
	 * initialize
	 */
	public void initializeMIDI() throws MidiUnavailableException, InvalidMidiDataException, IOException, LineUnavailableException {
		this.synthesizer = MidiSystem.getSynthesizer();
		((SoftSynthesizer)this.synthesizer).open(wavout = new WavoutDataLine(), null);
		addTrackEndNotifier(() -> wavout.stopRec());

		long latency = this.synthesizer.getLatency();
		int maxPolyphony = this.synthesizer.getMaxPolyphony();
		System.out.printf("Latency: %d\nMaxPolyphony: %d\n", latency, maxPolyphony);

		this.sequencer = MidiSystem.getSequencer();
		this.sequencer.open();
		this.sequencer.addMetaEventListener(meta -> {
			int type = meta.getType();
			if (type == MMLTempoEvent.META) {
				// テンポイベントを処理します.
				byte metaData[] = meta.getData();
				sequencer.setTempoInMPQ(ByteBuffer.wrap(metaData).getInt());
			} else if (type == 0x2f) {
				// トラック終端
				if (loop) {
					sequenceStart();
				} else {
					notifier.forEach(t -> t.run());
				}
			}
		});

		// シーケンサとシンセサイザの初期化
		initializeSynthesizer();
		Transmitter transmitter = this.sequencer.getTransmitters().get(0);
		transmitter.setReceiver(this.synthesizer.getReceiver());
	}

	// ループ再生時にも使用するパラメータ.
	private boolean loop = false;
	private long startTick;
	private int startTempo;

	public void setLoop(boolean b) {
		this.loop = b;
	}

	public boolean isLoop() {
		return this.loop;
	}

	/**
	 * MMLScoreからMIDIシーケンスに変換して, 再生を開始する.
	 * @param mmlScore
	 * @param startTick
	 */
	public void createSequenceAndStart(MMLScore mmlScore, long startTick) {
		try {
			MabiDLS.getInstance().loadRequiredInstruments(mmlScore);
			Sequencer sequencer = MabiDLS.getInstance().getSequencer();
			Sequence sequence = createSequence(mmlScore);

			sequencer.setSequence(sequence);
			this.startTick = startTick;
			this.startTempo = mmlScore.getTempoOnTick(startTick);
			updatePanpot(mmlScore);
			sequenceStart();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public IWavoutState getWavout() {
		return wavout;
	}

	public void startWavout(MMLScore mmlScore, File outFile, Runnable endNotify) {
		createSequenceAndStart(mmlScore, 0);
		try {
			wavout.startRec(new FileOutputStream(outFile), endNotify);
		} catch (FileNotFoundException e) {
			wavout.stopRec();
			e.printStackTrace();
		}
	}

	public void stopWavout() {
		wavout.stopRec();
		sequencer.stop();
	}

	private void allNoteOff() {
		for (MidiChannel ch : this.channel) {
			ch.allNotesOff();
		}
	}

	private void sequenceStart() {
		allNoteOff();
		midiSetMuteState();
		sequencer.setTickPosition(startTick);
		sequencer.setTempoInBPM(startTempo);
		sequencer.start();
	}

	public void addTrackEndNotifier(Runnable n) {
		notifier.add(n);
	}

	public void loadingDLSFile(File file) throws InvalidMidiDataException, IOException {
		System.out.println("["+file.getName()+"]");
		if (file.getName().equals("")) {
			return;
		}
		if (!file.exists()) {
			// 各Rootディレクトリを探索します.
			for (Path path : FileSystems.getDefault().getRootDirectories()) {
				File aFile = new File(path.toString() + file.getPath());
				if (aFile.exists()) {
					file = aFile;
					break;
				}
			};
		}
		if (file.exists()) {
			List<InstClass> loadList = InstClass.loadDLS(file);
			for (InstClass inst : loadList) {
				if (!insts.contains(inst)) {
					insts.add(inst);
				}
			}
		}
	}

	public synchronized void loadRequiredInstruments(MMLScore score) {
		ArrayList<InstClass> requiredInsts = new ArrayList<>();
		ArrayList<MMLTrack> trackList = new ArrayList<>(score.getTrackList());
		for (MMLTrack track : trackList) {
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
		List<Instrument> loadedList = Arrays.asList(synthesizer.getLoadedInstruments());
		for (InstClass inst : requiredInsts) {
			try {
				Instrument instrument = inst.getInstrument();
				if (!loadedList.contains(instrument)) {
					synthesizer.loadInstrument(instrument);
				}
			} catch (OutOfMemoryError e) {
				AppErrorHandler.getInstance().exec();
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

	private void initializeSynthesizer() throws InvalidMidiDataException, IOException, MidiUnavailableException {
		this.channel = this.synthesizer.getChannels();
		for (int i = 0; i < this.channel.length; i++) {
			this.playNoteList.add(new MMLNoteEvent[MAX_CHANNEL_PLAY_NOTE]);
		}

		for (MidiChannel ch : this.channel) {
			ch.programChange(DLS_BANK, 0);
			/* ctrl 91 汎用エフェクト 1(リバーブ) */
			ch.controlChange(91, 0);
		}

		this.synthesizer.unloadAllInstruments(this.synthesizer.getDefaultSoundbank());
		all();
	}

	public InstClass[] getAvailableInstByInstType(List<InstType> e) {
		return insts.stream()
				.filter(inst -> e.contains(inst.getType()))
				.toArray(size -> new InstClass[size]);
	}

	public InstClass getInstByProgram(int program) {
		for (InstClass inst : insts) {
			if (inst.getProgram() == program) {
				return inst;
			}
		}

		return null;
	}

	/**
	 * 単音再生
	 */
	public void playNote(int note, int program, int channel, int velocity) {
		MMLNoteEvent playNote = this.playNoteList.get(channel)[0];
		if ( (playNote == null) || (playNote.getNote() != note) ) {
			playNote = new MMLNoteEvent(note, 0, 0, velocity);
		}
		playNotes(new MMLNoteEvent[] { playNote }, program, channel);
	}

	/** 和音再生 */
	public void playNotes(MMLNoteEvent noteList[], int program, int channel) {
		/* シーケンサによる再生中は鳴らさない */
		if (sequencer.isRunning()) {
			return;
		}
		midiMuteOff();
		changeProgram(program, channel);
		setChannelPanpot(channel, 64);
		MidiChannel midiChannel = this.channel[channel];
		MMLNoteEvent[] playNoteEvents = this.playNoteList.get(channel);

		for (int i = 0; i < playNoteEvents.length; i++) {
			MMLNoteEvent note = null;
			if ( (noteList != null) && (i < noteList.length) ) {
				note = noteList[i];
			}
			if ( (note == null) || (!note.equals(playNoteEvents[i])) ) {
				if (playNoteEvents[i] != null) {
					midiChannel.noteOff( convertNoteMML2Midi(playNoteEvents[i].getNote()) );
					playNoteEvents[i] = null;
				}
			}
			if ( (note != null) && (!note.equals(playNoteEvents[i]))) {
				InstClass instClass = getInstByProgram(program);
				int velocity = convertVelocityOnAtt(instClass, note.getNote(), note.getVelocity());
				int midiNote = convertNoteMML2Midi(note.getNote());
				if (midiNote >= 0) {
					midiChannel.noteOn(midiNote, velocity);
				}
				playNoteEvents[i] = note;
			}
		}
	}

	public void changeProgram(int program, int ch) {
		if (channel[ch].getProgram() != program) {
			channel[ch].programChange(DLS_BANK, program);
		}
	}

	/**
	 * 指定したチャンネルのパンポットを設定します.
	 * @param ch
	 * @param panpot
	 */
	public void setChannelPanpot(int ch, int panpot) {
		channel[ch].controlChange(10, panpot);
	}

	public void toggleMute(int ch) {
		muteState[ch] = !muteState[ch];
		midiSetMuteState();
	}

	public void setMute(int ch, boolean mute) {
		muteState[ch] = mute;
		midiSetMuteState();
	}

	public boolean getMute(int ch) {
		return muteState[ch];
	}

	public void solo(int ch) {
		for (int i = 0; i < muteState.length; i++) {
			muteState[i] = (i != ch);
		}
		midiSetMuteState();
	}

	public void all() {
		for (int i = 0; i < muteState.length; i++) {
			muteState[i] = false;
		}
		midiSetMuteState();
	}

	/** MIDIにMuteStateを反映する. */
	private void midiSetMuteState() {
		for (int i = 0; i < muteState.length; i++) {
			channel[i].setMute(muteState[i]);
		}
	}

	/** MIDIのMute解除する. */
	private void midiMuteOff() {
		for (MidiChannel c : channel) {
			c.setMute(false);
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
		Sequence sequence = new Sequence(Sequence.PPQ, MMLTickTable.TPQN);

		int trackCount = 0;
		for (MMLTrack mmlTrack : score.getTrackList()) {
			convertMidiTrack(sequence.createTrack(), mmlTrack, trackCount);
			trackCount++;
			if (trackCount >= MAX_MIDI_PART) {
				break;
			}
		}

		// グローバルテンポ
		Track track = sequence.getTracks()[0];
		List<MMLTempoEvent> globalTempoList = score.getTempoEventList();
		for (MMLTempoEvent tempoEvent : globalTempoList) {
			byte tempo[] = tempoEvent.getMetaData();
			int tickOffset = tempoEvent.getTickOffset();

			MidiMessage message = new MetaMessage(MMLTempoEvent.META, 
					tempo, tempo.length);
			track.add(new MidiEvent(message, tickOffset));
		}

		if (trackCount <= 13) {
			// コーラスパートの作成
			createVoiceMidiTrack(sequence, score, 13, 100); // 男声コーラス
			createVoiceMidiTrack(sequence, score, 14, 110); // 女声コーラス
		}

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

			InstClass instClass = getInstByProgram(program);
			convertMidiPart(track, mmlTrack.getMMLEventAtIndex(3).getMMLNoteEventList(), channel, instClass);
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
		InstClass instClass = getInstByProgram(mmlTrack.getProgram());

		MMLMidiTrack midiTrack = new MMLMidiTrack(mmlTrack.getGlobalTempoList());
		for (int i = 0; i < enablePart.length; i++) {
			if (enablePart[i]) {
				MMLEventList eventList = mmlTrack.getMMLEventAtIndex(i);
				midiTrack.add(eventList.getMMLNoteEventList());
			}
		}
		convertMidiPart(track, midiTrack.getNoteEventList(), channel, instClass);
	}

	private void convertMidiPart(Track track, List<MMLNoteEvent> eventList, int channel, InstClass inst) {
		int velocity = MMLNoteEvent.INIT_VOL;

		// Noteイベントの変換
		for ( MMLNoteEvent noteEvent : eventList ) {
			int note = noteEvent.getNote();
			int tick = noteEvent.getTick();
			int tickOffset = noteEvent.getTickOffset() + 1;
			int endTickOffset = tickOffset + tick - 1;

			// ボリュームの変更
			if (noteEvent.getVelocity() >= 0) {
				velocity = convertVelocityOnAtt(inst, note, noteEvent.getVelocity());
			}

			try {
				// ON イベント作成
				MidiMessage message1 = new ShortMessage(ShortMessage.NOTE_ON, 
						channel,
						convertNoteMML2Midi(note), 
						velocity);
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

	/**
	 * 音源のAttenuationをVelocityに反映する.
	 * @param inst  音源
	 * @param note  変換前のNote
	 * @param velocity  変換前のVelocity
	 * @return  変換後のVelocity
	 */
	private int convertVelocityOnAtt(InstClass inst, int note, int velocity) {
		velocity = inst.getType().convertVelocityMML2Midi(velocity);
		if (velocity == 0) {
			return 0;
		}

		note = convertNoteMML2Midi(note);
		Instrument instrument = inst.getInstrument();
		if (instrument instanceof DLSInstrument) {
			DLSInstrument dlsinst = (DLSInstrument) instrument;
			for (DLSRegion region : dlsinst.getRegions()) {
				if ( (note >= region.getKeyfrom())
						&& (note <= region.getKeyto()) ) {
					double attenuation = region.getSampleoptions().getAttenuation() / 655360.0;
					velocity = (int) Math.sqrt( Math.pow(10.0, attenuation/20) * (double)(velocity * velocity) );
					break;
				}
			}
		}

		return velocity;
	}

	public List<MidiDevice.Info> getMidiInDevice() {
		ArrayList<MidiDevice.Info> midiDeviceList = new ArrayList<>();
		for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
			try {
				MidiDevice device = MidiSystem.getMidiDevice(info);
				if ( (device.getMaxTransmitters() != 0) && (device.getMaxReceivers() == 0) ) {
					// -1は制限なし.
					midiDeviceList.add(device.getDeviceInfo());
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
		return midiDeviceList;
	}

	public static void main(String args[]) {
		try {
			InstClass.debug = true;
			MabiDLS midi = new MabiDLS();
			midi.initializeMIDI();
			for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
				System.out.println(info);
			}
			midi.loadingDLSFile(new File(DEFALUT_DLS_PATH));
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

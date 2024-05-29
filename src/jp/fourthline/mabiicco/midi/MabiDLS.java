/*
 * Copyright (C) 2013-2024 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.midi.*;
import javax.sound.sampled.LineUnavailableException;

import com.sun.media.sound.SoftSynthesizer;

import jp.fourthline.mabiicco.AppErrorHandler;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTempoEvent;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.Marker;
import jp.fourthline.mmlTools.core.MMLTickTable;

/**
 * MabinogiのDLSファイルを使ってMIDIを扱います.
 */
public final class MabiDLS {
	private static MabiDLS instance = null;
	private Synthesizer synthesizer;
	private Sequencer sequencer;
	private MidiChannel[] channel;
	private final ArrayList<MMLNoteEvent[]> playNoteList = new ArrayList<>();
	public static final int MAX_CHANNEL_PLAY_NOTE = 4;
	private static final int CHORUS_INDEX = 3;
	private static final int NUM_CHANNEL_ON_TRACK = 4;
	private static final int MAX_MIDI_PART = MMLScore.MAX_TRACK * NUM_CHANNEL_ON_TRACK;
	private final ArrayList<InstClass> insts = new ArrayList<>();
	private final Map<File, List<InstClass>> instsMap = new TreeMap<>();
	public static final int DLS_BANK = (0x79 << 7);
	public static final int DRUM_BANK = (0x78 << 7);

	public static final String[] DEFALUT_DLS_PATH = {
			"Nexon/Mabinogi/mp3/MSXspirit01.dls",
			"Nexon/Mabinogi/mp3/MSXspirit02.dls",
			"Nexon/Mabinogi/mp3/MSXspirit03.dls",
			"Nexon/Mabinogi/mp3/MSXspirit04.dls"
	};

	private final ArrayList<Runnable> notifier = new ArrayList<>();
	private final boolean[] muteState = new boolean[ MMLScore.MAX_TRACK ];
	private WavoutDataLine wavout;
	private boolean allLoaded = false;

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
		HashMap<String, Object> info = new HashMap<>();
		info.put("midi channels", MAX_MIDI_PART);
		info.put("large mode", "true");
		//		info.put("load default soundbank", "false");
		info.put("max polyphony", "256");
		((SoftSynthesizer)this.synthesizer).open(wavout = new WavoutDataLine(), info);
		addTrackEndNotifier(() -> wavout.stopRec());

		long latency = this.synthesizer.getLatency();
		int maxPolyphony = this.synthesizer.getMaxPolyphony();
		int midiChannels = this.synthesizer.getChannels().length;
		System.out.printf("Latency: %d\nMaxPolyphony: %d\nChannels: %d\n", latency, maxPolyphony, midiChannels);

		this.sequencer = MidiSystem.getSequencer(false);
		this.sequencer.open();
		this.sequencer.addMetaEventListener(meta -> {
			int type = meta.getType();
			if (type == MMLTempoEvent.META) {
				// テンポイベントを処理します.
				byte[] metaData = meta.getData();
				sequencer.setTempoInMPQ(ByteBuffer.wrap(metaData).getInt());
			} else if (type == 0x2f) {
				// トラック終端, wav出力の場合はループしない.
				if (loop && !wavout.isRec()) {
					sequenceStart();
				} else {
					notifier.forEach(t -> t.run());
				}
			}
		});

		// シーケンサとシンセサイザの初期化
		initializeSynthesizer();
		Transmitter transmitter = this.sequencer.getTransmitter();
		transmitter.setReceiver(new ExtendMessage.ExtendReceiver(this.synthesizer.getReceiver(), MAX_MIDI_PART));
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
		createSequenceAndStandby(mmlScore, startTick);
		sequenceStart();
	}

	/**
	 * MMLScoreからMIDIシーケンスに変換して, 再生を準備をする.
	 * @param mmlScore
	 * @param startTick
	 */
	private void createSequenceAndStandby(MMLScore mmlScore, long startTick) {
		try {
			MabiDLS.getInstance().loadRequiredInstruments(mmlScore);
			Sequencer sequencer = MabiDLS.getInstance().getSequencer();
			Sequence sequence = createSequenceOnSepChannel(mmlScore);
			sequencer.setSequence(sequence);
			this.startTick = startTick;
			this.startTempo = mmlScore.getTempoOnTick(startTick);
			updateMidiControl(mmlScore);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public IWavoutState getWavout() {
		return wavout;
	}

	public void startWavout(MMLScore mmlScore, File outFile, Runnable endNotify) throws IOException {
		createSequenceAndStandby(mmlScore, 0);
		wavout.startRec(outFile, endNotify);
		sequenceStart();
	}

	public void stopWavout() {
		wavout.stopRec();
		sequencer.stop();
	}

	public void allNoteOff() {
		for (MidiChannel ch : this.channel) {
			ch.allNotesOff();

			// sustain off
			ch.controlChange(64, 0);
			ch.resetAllControllers();
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

	public void loadingDefaultSound() {
		try {
			boolean nameConvert = MabiIccoProperties.getInstance().soundEnv.get().nameConvertForDefaultSoundBank();
			List<InstClass> loadList = InstClass.defaultSoundBank(nameConvert);
			for (InstClass inst : loadList) {
				if (!insts.contains(inst)) {
					insts.add(inst);
				}
			}
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}

		for (var inst : insts) {
			this.synthesizer.loadInstrument(inst.getInstrument());
		}
		allLoaded = true;
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
			}
		}
		if (file.exists()) {
			ArrayList<InstClass> addList = new ArrayList<>();
			if (!instsMap.containsKey(file)) {
				List<InstClass> loadList = InstClass.loadDLS(file);
				for (InstClass inst : loadList) {
					if (!insts.contains(inst)) {
						insts.add(inst);
						addList.add(inst);
					}
				}
				instsMap.put(file, addList);
			}
		}
	}

	public Map<File, List<InstClass>> getInstsMap() {
		return this.instsMap;
	}

	public synchronized void loadRequiredInstruments(MMLScore score) {
		if (allLoaded) {
			return;
		}

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

		loadRequiredInstruments(requiredInsts);
	}

	public synchronized void loadRequiredInstruments(List<InstClass> requiredInsts) {
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

			// sustain off
			ch.controlChange(64, 0);
		}

		// デフォルトSoundBankのドラム以外をアンロードする.
		var sb = this.synthesizer.getDefaultSoundbank();
		var list = new ArrayList<Patch>();
		for (var inst : sb.getInstruments()) {
			var p = inst.getPatch();
			if (p instanceof com.sun.media.sound.ModelPatch patch) {
				if (!patch.isPercussion()) {
					list.add(patch);
				}
			}
		}
		this.synthesizer.unloadInstruments(sb, list.toArray(Patch[]::new));

		all();
	}

	public List<InstClass> getAllInst() {
		return insts;
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
	 * トラックIndexとパートIndexからチャンネル番号を得る.
	 * @param trackIndex
	 * @param partIndex
	 * @return
	 */
	private int getChannel(int trackIndex, int partIndex) {
		return trackIndex * NUM_CHANNEL_ON_TRACK + partIndex;
	}

	/**
	 * 単音再生
	 */
	public void playNote(MMLScore score, int note, int velocity, int trackIndex, int partIndex) {
		int channel = getChannel(trackIndex, partIndex);
		MMLNoteEvent playNote = this.playNoteList.get(channel)[0];
		if ( (playNote == null) || (playNote.getNote() != note) ) {
			playNote = new MMLNoteEvent(note, 0, 0, velocity);
		}
		playNotes(score, new MMLNoteEvent[] { playNote }, trackIndex, partIndex);
	}

	public void playDrum(boolean isMidi, int note, int velocity, int program) {
		int channel = 9;
		int bank = 0;
		if (isMidi) {
			program = 0;
		} else {
			bank = DLS_BANK;
		}
		MidiChannel midiChannel = this.channel[channel];
		midiChannel.setMute(false);
		midiChannel.controlChange(10, 64);
		midiChannel.controlChange(7, 100);
		midiChannel.programChange(bank, program);
		if (note >= 0) {
			midiChannel.noteOn(isMidi ? note : convertNoteMML2Midi(note), velocity);
		} else {
			midiChannel.allNotesOff();
		}
	}

	/** 和音再生 */
	public void playNotes(MMLScore score, MMLNoteEvent[] noteList, int trackIndex, int partIndex) {
		int channel = getChannel(trackIndex, partIndex);

		/* シーケンサによる再生中は鳴らさない */
		if (sequencer.isRunning()) {
			return;
		}
		if (channel >= this.channel.length) {
			return;
		}
		updateMidiControl(score);

		// Mute設定は変更せず, 一時的にチャンネルのミュートを解除する
		this.channel[channel].setMute(false); // TODO: ミュート & エレキギター/チェロ/ヴァイオリンなどのチャネルだと、残音が残ってしまう.

		setTrackPanpot(trackIndex, 64);
		setTrackVolume(trackIndex, MMLTrack.INITIAL_VOLUME);
		MMLNoteEvent[] playNoteEvents = this.playNoteList.get(channel);
		MidiChannel midiChannel = this.channel[channel];
		int program = midiChannel.getProgram();

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

	/**
	 * 歌パートのプログラム番号を選択する
	 * @param program
	 * @param songProgram
	 * @return
	 */
	public int selectSongProgram(int program, int songProgram) {
		int ret = program;
		if ((getInstByProgram(program).getType() != InstType.VOICE) && (songProgram >= 0)) {
			ret = songProgram;
		}
		return ret;
	}

	private void changeProgram(int trackIndex, int program, int songProgram) {
		int bank = DLS_BANK;

		if ((program & InstClass.DRUM) != 0) {
			bank = DRUM_BANK;
			program -= InstClass.DRUM;
		}
		int chorusChannel = getChannel(trackIndex, CHORUS_INDEX);
		for (int i = getChannel(trackIndex, 0); i < chorusChannel; i++) {
			channel[i].programChange(bank, program);
		}

		channel[chorusChannel].programChange(DLS_BANK, selectSongProgram(program, songProgram));
	}

	/**
	 * 指定したトラックのパンポットを設定します.
	 * @param trackIndex
	 * @param panpot
	 */
	public void setTrackPanpot(int trackIndex, int panpot) {
		if ( (trackIndex < 0) || (trackIndex >= MMLScore.MAX_TRACK) ) {
			throw new IllegalArgumentException("trackIndex: " + trackIndex);
		}

		for (int i = getChannel(trackIndex, 0); i < getChannel(trackIndex+1, 0); i++) {
			channel[i].controlChange(10, panpot);
		}
	}

	/**
	 * 指定したトラックのメイン・ボリュームを設定します.
	 * @param trackIndex
	 * @param volume
	 */
	public void setTrackVolume(int trackIndex, int volume) {
		if ( (trackIndex < 0) || (trackIndex >= MMLScore.MAX_TRACK) ) {
			throw new IllegalArgumentException("trackIndex: " + trackIndex);
		}

		for (int i = getChannel(trackIndex, 0); i < getChannel(trackIndex+1, 0); i++) {
			channel[i].controlChange(7, volume);
		}
	}

	public void toggleMute(int trackIndex) {
		if ( (trackIndex < 0) || (trackIndex >= MMLScore.MAX_TRACK) ) {
			throw new IllegalArgumentException("trackIndex: " + trackIndex);
		}

		muteState[trackIndex] = !muteState[trackIndex];
		midiSetMuteState();
	}

	public void setMute(int trackIndex, boolean mute) {
		if ( (trackIndex < 0) || (trackIndex >= MMLScore.MAX_TRACK) ) {
			throw new IllegalArgumentException("trackIndex: " + trackIndex);
		}

		muteState[trackIndex] = mute;
		midiSetMuteState();
	}

	public boolean getMute(int trackIndex) {
		if ( (trackIndex < 0) || (trackIndex >= MMLScore.MAX_TRACK) ) {
			throw new IllegalArgumentException("trackIndex: " + trackIndex);
		}
		return muteState[trackIndex];
	}

	public void solo(int trackIndex) {
		if ( (trackIndex < 0) || (trackIndex >= MMLScore.MAX_TRACK) ) {
			throw new IllegalArgumentException("trackIndex: " + trackIndex);
		}

		for (int i = 0; i < muteState.length; i++) {
			muteState[i] = (i != trackIndex);
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
			for (int chIndex = getChannel(i, 0); chIndex < getChannel(i+1, 0); chIndex++) {
				channel[chIndex].setMute(muteState[i]);
			}
		}
	}

	public void updateMidiControl(MMLScore score) {
		int trackCount = 0;
		for (MMLTrack mmlTrack : score.getTrackList()) {
			this.setTrackPanpot(trackCount, mmlTrack.getPanpot());
			this.setTrackVolume(trackCount, mmlTrack.getVolume());
			this.changeProgram(trackCount, mmlTrack.getProgram(), mmlTrack.getSongProgram());
			trackCount++;
		}
	}

	/**
	 * MIDIシーケンスを作成します。
	 * チャンネル構成変更後したので使用できない。
	 * @throws InvalidMidiDataException 
	 */
	public Sequence createSequenceForPlay(MMLScore score) throws InvalidMidiDataException {
		return createSequence(score, 1, true, false, true);
	}

	/**
	 * MIDIシーケンスを作成します。
	 * @throws InvalidMidiDataException 
	 */
	public Sequence createSequenceForMidi(MMLScore score) throws InvalidMidiDataException {
		return createSequence(score, 0, false, true, false);
	}

	/**
	 * MIDIシーケンスを作成します。
	 * @param score
	 * @param startOffset
	 * @param attackDelayCorrect
	 * @param withMeta
	 * @return
	 * @throws InvalidMidiDataException
	 */
	private Sequence createSequence(MMLScore score, int startOffset, boolean attackDelayCorrect, boolean withMeta, boolean withMute) throws InvalidMidiDataException {
		Sequence sequence = new Sequence(Sequence.PPQ, MMLTickTable.TPQN);
		int totalTick = score.getTotalTickLength();
		Track track = sequence.createTrack();

		// マーカー
		if (withMeta) {
			for (Marker marker : score.getMarkerList()) {
				var data = marker.getMetaData();
				track.add(new MidiEvent(new MetaMessage(Marker.META, data, data.length), marker.getTickOffset()));
			}
		}

		// グローバルテンポ
		List<MMLTempoEvent> globalTempoList = score.getTempoEventList();
		for (MMLTempoEvent tempoEvent : globalTempoList) {
			byte[] tempo = tempoEvent.getMetaData();
			int tickOffset = tempoEvent.getTickOffset();
			if (tickOffset >= totalTick) {
				break;
			}
			track.add(new MidiEvent(new MetaMessage(MMLTempoEvent.META, tempo, tempo.length), tickOffset));
		}

		int trackCount = 0;
		for (MMLTrack mmlTrack : score.getTrackList()) {
			convertMidiTrack(sequence.createTrack(), mmlTrack, trackCount, mmlTrack.getProgram(), startOffset, attackDelayCorrect, withMeta, withMute);
			if (mmlTrack.getSongProgram() >= 0) {
				convertMidiTrack(sequence.createTrack(), mmlTrack, trackCount+MMLScore.MAX_TRACK, mmlTrack.getSongProgram(), startOffset, attackDelayCorrect, withMeta, withMute);
			}
			trackCount++;
			if (trackCount >= this.channel.length) {
				break;
			}
		}

		return sequence;
	}

	/**
	 * 全パートを単独チャンネルで鳴らすシーケンスを作成する
	 * @param score
	 * @return
	 * @throws InvalidMidiDataException
	 */
	public Sequence createSequenceOnSepChannel(MMLScore score) throws InvalidMidiDataException {
		Sequence sequence = new Sequence(Sequence.PPQ, MMLTickTable.TPQN);
		int totalTick = score.getTotalTickLength();
		Track track = sequence.createTrack();

		// グローバルテンポ
		List<MMLTempoEvent> globalTempoList = score.getTempoEventList();
		for (MMLTempoEvent tempoEvent : globalTempoList) {
			byte[] tempo = tempoEvent.getMetaData();
			int tickOffset = tempoEvent.getTickOffset();
			if (tickOffset >= totalTick) {
				break;
			}
			track.add(new MidiEvent(new MetaMessage(MMLTempoEvent.META, tempo, tempo.length), tickOffset));
		}

		int trackIndex = 0;
		for (MMLTrack mmlTrack : score.getTrackList()) {
			for (int i = 0; i < NUM_CHANNEL_ON_TRACK; i++) {
				int program = mmlTrack.getProgram();
				if ((i == CHORUS_INDEX) && (mmlTrack.getSongProgram() >= 0)) {
					program = mmlTrack.getSongProgram();
				}
				boolean[] enablePart = InstClass.getEnablePartByProgram(program);
				if (enablePart[i]) {
					var eventList = mmlTrack.getMMLEventAtIndex(i).getMMLNoteEventList();
					if (eventList.size() > 0) {
						int channel = getChannel(trackIndex, i);
						var instClass = getInstByProgram(program);
						MMLMidiTrack midiTrack = new MMLMidiTrack(instClass);
						midiTrack.setAttackDelayCorrect(mmlTrack.getAttackDelayCorrect(i));
						midiTrack.add(eventList);
						convertMidiPart(sequence.createTrack(), midiTrack.getNoteEventList(), channel, instClass, 1, true);
					}
				}
			}
			trackIndex++;
		}

		return sequence;
	}

	/**
	 * トラックに含まれるすべてのMMLEventListを1つのMIDIトラックに変換します.
	 * @param track
	 * @param channel
	 * @throws InvalidMidiDataException
	 */
	private void convertMidiTrack(Track track, MMLTrack mmlTrack, int channel, int targetProgram, int startOffset, boolean attackDelayCorrect, boolean withMeta, boolean withMute) throws InvalidMidiDataException {
		// トラック名
		if (withMeta) {
			var nameData = mmlTrack.getTrackName().getBytes();
			MetaMessage nameMessage = new MetaMessage(3, nameData, nameData.length);
			track.add(new MidiEvent(nameMessage, 0));
		}

		boolean[] enablePart = InstClass.getEnablePartByProgram(targetProgram);
		InstClass instClass = getInstByProgram(targetProgram);
		MMLMidiTrack midiTrack = new MMLMidiTrack(instClass);
		for (int i = 0; i < enablePart.length; i++) {
			if (enablePart[i]) {
				if (attackDelayCorrect) {
					midiTrack.setAttackDelayCorrect(mmlTrack.getAttackDelayCorrect(i));
				}
				MMLEventList eventList = mmlTrack.getMMLEventAtIndex(i);
				midiTrack.add(eventList.getMMLNoteEventList());
			}
		}
		convertMidiPart(track, midiTrack.getNoteEventList(), channel, instClass, startOffset, withMute);
	}

	private void convertMidiPart(Track track, List<MMLNoteEvent> eventList, int channel, InstClass inst, int startOffset, boolean withMute) throws InvalidMidiDataException {
		int velocity = MMLNoteEvent.INIT_VOL;
		int targetProgram = inst.getProgram();

		// Program Change
		ShortMessage pcMessage = new ExtendMessage(ShortMessage.PROGRAM_CHANGE, 
				channel,
				targetProgram & 0xff,
				0);
		track.add(new MidiEvent(pcMessage, 0));

		// Noteイベントの変換
		for ( MMLNoteEvent noteEvent : eventList ) {
			if (withMute && noteEvent.isMute()) {
				continue;
			}
			int note = noteEvent.getNote();
			int tick = noteEvent.getTick();
			int tickOffset = noteEvent.getTickOffset() + startOffset;
			int endTickOffset = tickOffset + tick - startOffset;

			// ボリュームの変更
			if (noteEvent.getVelocity() >= 0) {
				velocity = convertVelocityOnAtt(inst, note, noteEvent.getVelocity());
			}

			try {
				// ON イベント作成
				MidiMessage message1 = new ExtendMessage(ShortMessage.NOTE_ON, 
						channel,
						convertNoteMML2Midi(note), 
						velocity);
				track.add(new MidiEvent(message1, tickOffset));

				// Off イベント作成
				MidiMessage message2 = new ExtendMessage(ShortMessage.NOTE_OFF,
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

		double attenuation = inst.getAttention(note);
		velocity = (int) Math.sqrt( Math.pow(10.0, attenuation/20) * (double)(velocity * velocity) );

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

	public void setSoundDataLine(ISoundDataLine soundDataLine) {
		wavout.setSoundDataLine(soundDataLine);
	}

	public static void main(String[] args) {
		try {
			InstClass.debug = true;
			MabiDLS midi = new MabiDLS();
			midi.initializeMIDI();
			for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
				System.out.println(info);
			}
			for (String t : DEFALUT_DLS_PATH) {
				midi.loadingDLSFile(new File(t));
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

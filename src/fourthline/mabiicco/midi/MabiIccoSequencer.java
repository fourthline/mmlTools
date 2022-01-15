/*
 * Copyright (C) 2021 たんらる
 */

package fourthline.mabiicco.midi;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

/**
 * MabiIcco拡張用シーケンサー
 */
public final class MabiIccoSequencer implements Sequencer {
	private final Sequencer target;
	private final MabiDLS dls;
	public MabiIccoSequencer(Sequencer target, MabiDLS dls) {
		this.target = target;
		this.dls = dls;
	}

	@Override
	public Info getDeviceInfo() {
		return target.getDeviceInfo();
	}

	@Override
	public void open() throws MidiUnavailableException {
		target.open();
	}

	@Override
	public void close() {
		target.close();
	}

	@Override
	public boolean isOpen() {
		return target.isOpen();
	}

	@Override
	public int getMaxReceivers() {
		return target.getMaxReceivers();
	}

	@Override
	public int getMaxTransmitters() {
		return target.getMaxTransmitters();
	}

	@Override
	public Receiver getReceiver() throws MidiUnavailableException {
		return target.getReceiver();
	}

	@Override
	public List<Receiver> getReceivers() {
		return target.getReceivers();
	}

	@Override
	public Transmitter getTransmitter() throws MidiUnavailableException {
		return target.getTransmitter();
	}

	@Override
	public List<Transmitter> getTransmitters() {
		return target.getTransmitters();
	}

	@Override
	public void setSequence(Sequence sequence) throws InvalidMidiDataException {
		target.setSequence(sequence);
	}

	@Override
	public void setSequence(InputStream stream) throws IOException, InvalidMidiDataException {
		target.setSequence(stream);
	}

	@Override
	public Sequence getSequence() {
		return target.getSequence();
	}

	@Override
	public void start() {
		target.start();
	}

	@Override
	public void stop() {
		target.stop(); // TODO: 2秒止まらない場合がある?
		dls.allNoteOff();
	}

	@Override
	public boolean isRunning() {
		return target.isRunning();
	}

	@Override
	public void startRecording() {
		target.startRecording();
	}

	@Override
	public void stopRecording() {
		target.stopRecording();
	}

	@Override
	public boolean isRecording() {
		return target.isRecording();
	}

	@Override
	public void recordEnable(Track track, int channel) {
		target.recordEnable(track, channel);
	}

	@Override
	public void recordDisable(Track track) {
		target.recordDisable(track);
	}

	@Override
	public float getTempoInBPM() {
		return target.getTempoInBPM();
	}

	@Override
	public void setTempoInBPM(float bpm) {
		target.setTempoInBPM(bpm);
	}

	@Override
	public float getTempoInMPQ() {
		return target.getTempoInMPQ();
	}

	@Override
	public void setTempoInMPQ(float mpq) {
		target.setTempoInMPQ(mpq);
	}

	@Override
	public void setTempoFactor(float factor) {
		target.setTempoFactor(factor);
	}

	@Override
	public float getTempoFactor() {
		return target.getTempoFactor();
	}

	@Override
	public long getTickLength() {
		return target.getTickLength();
	}

	@Override
	public long getTickPosition() {
		return target.getTickPosition();
	}

	@Override
	public void setTickPosition(long tick) {
		dls.allNoteOff();
		target.setTickPosition(tick);
	}

	@Override
	public long getMicrosecondLength() {
		return target.getMicrosecondLength();
	}

	@Override
	public long getMicrosecondPosition() {
		return target.getMicrosecondPosition();
	}

	@Override
	public void setMicrosecondPosition(long microseconds) {
		target.setMicrosecondPosition(microseconds);
	}

	@Override
	public void setMasterSyncMode(SyncMode sync) {
		target.setMasterSyncMode(sync);
	}

	@Override
	public SyncMode getMasterSyncMode() {
		return target.getMasterSyncMode();
	}

	@Override
	public SyncMode[] getMasterSyncModes() {
		return target.getMasterSyncModes();
	}

	@Override
	public void setSlaveSyncMode(SyncMode sync) {
		target.setSlaveSyncMode(sync);
	}

	@Override
	public SyncMode getSlaveSyncMode() {
		return target.getSlaveSyncMode();
	}

	@Override
	public SyncMode[] getSlaveSyncModes() {
		return target.getMasterSyncModes();
	}

	@Override
	public void setTrackMute(int track, boolean mute) {
		target.setTrackMute(track, mute);
	}

	@Override
	public boolean getTrackMute(int track) {
		return target.getTrackMute(track);
	}

	@Override
	public void setTrackSolo(int track, boolean solo) {
		target.setTrackSolo(track, solo);
	}

	@Override
	public boolean getTrackSolo(int track) {
		return target.getTrackSolo(track);
	}

	@Override
	public boolean addMetaEventListener(MetaEventListener listener) {
		return target.addMetaEventListener(listener);
	}

	@Override
	public void removeMetaEventListener(MetaEventListener listener) {
		target.removeMetaEventListener(listener);
	}

	@Override
	public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
		return target.addControllerEventListener(listener, controllers);
	}

	@Override
	public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
		return target.removeControllerEventListener(listener, controllers);
	}

	@Override
	public void setLoopStartPoint(long tick) {
		target.setLoopStartPoint(tick);
	}

	@Override
	public long getLoopStartPoint() {
		return target.getLoopStartPoint();
	}

	@Override
	public void setLoopEndPoint(long tick) {
		target.setLoopEndPoint(tick);
	}

	@Override
	public long getLoopEndPoint() {
		return target.getLoopEndPoint();
	}

	@Override
	public void setLoopCount(int count) {
		target.setLoopCount(count);
	}

	@Override
	public int getLoopCount() {
		return target.getLoopCount();
	}
}

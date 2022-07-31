/*
 * Copyright (C) 2017-2022 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;

import jp.fourthline.mmlTools.core.NanoTime;

import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public final class WavoutDataLine implements SourceDataLine, IWavoutState {

	private final SourceDataLine parent;
	private final AudioFormat format = new AudioFormat(44100, 16, 2, true, false);

	private boolean rec = false;
	private OutputStream outputStream = null;
	private File tempFile = null;
	private OutputStream tempOutputStream = null;
	private Runnable endNotify;

	public WavoutDataLine() throws LineUnavailableException {
		this.parent = AudioSystem.getSourceDataLine(format);
	}

	private long time;
	private long curLen;
	
	@Override
	public long getTime() { return time; }

	@Override
	public long getLen() { return curLen; }

	public void startRec(File outFile, Runnable endNotify) throws IOException {
		try {
			tempFile = File.createTempFile("wavout_", ".tmp", outFile.getParentFile());
			System.out.println("startRec:" + tempFile);
			tempOutputStream = new BufferedOutputStream(new FileOutputStream(tempFile), 65536);
			this.rec = true;
			this.outputStream = new FileOutputStream(outFile);
			this.endNotify = endNotify;
			time = 0;
			curLen = 0;
		} catch (IOException e) {
			if (tempOutputStream != null) {
				tempOutputStream.close();
				tempOutputStream = null;
			}
			if (tempFile != null) {
				tempFile.delete();
				tempFile = null;
			}
			throw e;
		}
	}

	public void stopRec() {
		if (this.rec && (this.endNotify != null)) {
			this.rec = false;
			this.endNotify.run();
			this.endNotify = null;
		}
		this.rec = false;
	}

	@Override
	public void drain() {
		parent.drain();
	}

	@Override
	public void flush() {
		parent.flush();
	}

	@Override
	public void start() {
		parent.start();
	}

	@Override
	public void stop() {
		parent.stop();
	}

	@Override
	public boolean isRunning() {
		return parent.isRunning();
	}

	@Override
	public boolean isActive() {
		return parent.isActive();
	}

	@Override
	public AudioFormat getFormat() {
		return parent.getFormat();
	}

	@Override
	public int getBufferSize() {
		return parent.getBufferSize();
	}

	@Override
	public int available() {
		return available();
	}

	@Override
	public int getFramePosition() {
		return parent.getFramePosition();
	}

	@Override
	public long getLongFramePosition() {
		return parent.getLongFramePosition();
	}

	@Override
	public long getMicrosecondPosition() {
		return parent.getMicrosecondPosition();
	}

	@Override
	public float getLevel() {
		return parent.getLevel();
	}

	@Override
	public javax.sound.sampled.Line.Info getLineInfo() {
		return parent.getLineInfo();
	}

	@Override
	public void open() throws LineUnavailableException {
		parent.open();
	}

	@Override
	public void close() {
		parent.close();
	}

	@Override
	public boolean isOpen() {
		return parent.isOpen();
	}

	@Override
	public Control[] getControls() {
		return parent.getControls();
	}

	@Override
	public boolean isControlSupported(Type control) {
		return parent.isControlSupported(control);
	}

	@Override
	public Control getControl(Type control) {
		return parent.getControl(control);
	}

	@Override
	public void addLineListener(LineListener listener) {
		parent.addLineListener(listener);
	}

	@Override
	public void removeLineListener(LineListener listener) {
		parent.removeLineListener(listener);
	}

	@Override
	public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
		parent.open(format, bufferSize);
	}

	@Override
	public void open(AudioFormat format) throws LineUnavailableException {
		parent.open(format);
	}

	private void wavoutEndCheck(byte[] b, int off, int len) {
		if (!rec && (tempOutputStream != null)) {
			boolean stop = true;
			for (int i = 0; i < len; i++) {
				if (b[i] != 0) {
					stop = false;
					break;
				}
			}
			if (stop) {
				try {
					tempOutputStream.close();
					long size = tempFile.length();
					AudioInputStream in = new AudioInputStream(new FileInputStream(tempFile), format, size/format.getFrameSize());
					AudioSystem.write(in, AudioFileFormat.Type.WAVE, outputStream);
					in.close();
					outputStream.close();
					System.out.println("stopRec: "+size);
				} catch (IOException e) {
					e.printStackTrace();
				}
				tempFile.delete();
				tempOutputStream = null;
			}
		}
	}

	@Override
	public int write(byte[] b, int off, int len) {
		wavoutEndCheck(b, off, len);
		if (tempOutputStream != null) {
			NanoTime time = NanoTime.start();
			curLen += len;
			try {
				tempOutputStream.write(b, off, len);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.time += time.ms();
		}
		return parent.write(b, off, len);
	}
}

/*
 * Copyright (C) 2017-2025 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

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
	private ByteArrayOutputStream tempOutputStream = null;
	private Runnable endNotify;
	private ISoundDataLine soundDataLine = null;

	private final AtomicInteger lineStallCounter = new AtomicInteger(0);

	private void reconnect() {
		parent.flush();
		System.out.println(System.currentTimeMillis() + " flush");
	}

	private Thread dataLineObserverThread;
	private final Runnable dataLineObserver = () -> {
		try {
			System.out.println("start DataLineAutoFlush.");

			while (true) {
				Thread.sleep(100);
				if (lineStallCounter.incrementAndGet() > 3) {
					reconnect();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	};

	public WavoutDataLine() throws LineUnavailableException {
		this.parent = AudioSystem.getSourceDataLine(format);
	}

	private long time;
	private long curLen;

	@Override
	public long getTime() { return time; }

	@Override
	public long getLen() { return curLen; }

	public void startRec(File outFile, Runnable endNotify, int bufSizeHint) throws IOException {
		int bufSize = bufSizeHint + 1024*1024;
		System.out.println("startRec:" + bufSize);
		try {
			tempOutputStream = new ByteArrayOutputStream(bufSize);
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

	public boolean isRec() {
		return this.rec;
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
		return parent.available();
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

	private synchronized void createWavFile(byte[] data) {
		try {
			var time = NanoTime.start();
			long size = data.length;
			AudioInputStream in = new AudioInputStream(new ByteArrayInputStream(data), format, size/format.getFrameSize());
			AudioSystem.write(in, AudioFileFormat.Type.WAVE, outputStream);
			in.close();
			outputStream.close();
			System.out.println("stopRec: "+size + "  " + time.ms() + "ms");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				var data = tempOutputStream.toByteArray();
				new Thread(() -> this.createWavFile(data)).start();
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
			tempOutputStream.write(b, off, len);
			this.time += time.ms();
		}

		if (soundDataLine != null) {
			soundDataLine.write(b);
		}
		var ret = parent.write(b, off, len);
		lineStallCounter.set(0);
		return ret;
	}

	public void setSoundDataLine(ISoundDataLine soundDataLine) {
		this.soundDataLine = soundDataLine;
	}

	@Override
	public void startDataLine() {
		synchronized (this) {
			if ("true".equals(System.getProperties().get("mabiicco.dlaf"))) {
				if (dataLineObserverThread == null) {
					dataLineObserverThread = new Thread(dataLineObserver, "DataLine Observer");
					dataLineObserverThread.start();
				}
			}
		}
	}
}

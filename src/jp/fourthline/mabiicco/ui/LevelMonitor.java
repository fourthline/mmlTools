/*
 * Copyright (C) 2023-2024 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JComponent;

import jp.fourthline.mabiicco.midi.ISoundDataLine;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mabiicco.ui.color.ColorSet;

public final class LevelMonitor extends JComponent implements ISoundDataLine {
	private static final long serialVersionUID = -3910689963056146451L;
	private static final int PIR = 600;
	private static final int R_PIR = 20;
	private int counter = 0;
	private int reduceCounter = 0;

	private static final ColorSet borderColor = ColorSet.create(Color.LIGHT_GRAY, new Color(96, 96, 96));
	private static final ColorSet barColor = ColorSet.create(Color.LIGHT_GRAY, Color.GRAY);
	private static final ColorSet holtBarColor = ColorSet.create(new Color(255, 128, 0), new Color(200, 96, 0));

	private final DataChannel left = new DataChannel();
	private final DataChannel right = new DataChannel();

	private final boolean dlsChain;
	private int paintCode = -1; // アイドル時用負荷低減用.

	public static final class DataChannel {
		private int max;
		private int currentMax;

		private int value;
		private int holtValue;

		public void update(int v) {
			max = Math.max(max, Math.abs(v));
		}

		public void commit(boolean isRun) {
			if (isRun) {
				double value = ( (Math.log10((double) max / 32768.0) * 10) + 40); // -40dB..0dB
				setValue(value);
			} else {
				setValue(0);
			}
			this.currentMax = max;
			max = 0;
		}

		private void setValue(double value) {
			this.value = Math.max(0, (int)(value));
			this.holtValue = Math.max(this.value, this.holtValue);
		}

		public void reduce(boolean isRun) {
			this.holtValue -= isRun ? 2 : 10;
			this.holtValue = Math.max(0, Math.max(value, this.holtValue));
		}

		int getValue() {
			return value;
		}

		int getHoltValue() {
			return holtValue;
		}

		int getMax() {
			return currentMax;
		}

		private int getCode() {
			return encode(value, holtValue);
		}

		private int encode(int a, int b) {
			int v = (a & 0xff ) << 8;
			v |= (b & 0xff);
			return v;
		}

		public int paint(Graphics g, int x, int y, int barH) {
			int a1 = getValue();
			int a2 = getHoltValue();

			g.setColor(barColor.get());
			if (a1 > 0) {
				for (int i = 0; i <= a1; i+=3) {
					g.fillRect(i, y, 2, barH);
				}
			}
			g.setColor(holtBarColor.get());
			if (a2 > 0) {
				g.fillRect(a2+x, y, 2, barH);
			}
			return encode(a1, a2);
		}
	}

	public LevelMonitor() {
		this(true);
	}

	public LevelMonitor(boolean dlsChain) {
		setPreferredSize(new Dimension(46, 20));
		this.dlsChain = dlsChain;
		if (dlsChain) {
			MabiDLS.getInstance().setSoundDataLine(this);
		}
	}

	@Override
	public void write(byte data[]) {
		boolean isRun = dlsChain ? MabiDLS.getInstance().getSequencer().isRunning() : true;
		var b = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		int c = b.capacity() / 2;
		for (int i = 0; i < c; i++) {
			if (isRun) {
				left.update(b.get());
				right.update(b.get());
			}
			if (++counter >= PIR) {
				counter = 0;
				left.commit(isRun);
				right.commit(isRun);
				if (++reduceCounter >= R_PIR) {
					reduceCounter = 0;
					left.reduce(isRun);
					right.reduce(isRun);
				}
				int code = (left.getCode() << 16) + (right.getCode());
				if (code != paintCode) {
					// アイドル時の負荷低減, 描画済みのデータを同じであれば再描画しない.
					repaint();
				}
			}
		}
	}

	DataChannel getLeft() {
		return left;
	}

	DataChannel getRight() {
		return right;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(borderColor.get());
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);

		int barH = 6;
		int y = getHeight() / 2;
		int leftY = (y - barH) / 2;
		int RightY = y + leftY - 1;
		int x = 2;

		int code = left.paint(g, x, leftY, barH) << 16;
		code += right.paint(g, x, RightY, barH);
		paintCode = code;

		g.dispose();
	}
}

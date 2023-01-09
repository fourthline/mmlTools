/*
 * Copyright (C) 2022-2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.util.List;

import javax.sound.midi.Sequencer;
import javax.swing.JComboBox;

import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTempoConverter;
import jp.fourthline.mmlTools.MMLTempoEvent;


/**
 * 時間表示
 */
public final class TimeBox extends JComboBox<StringBuffer> implements Runnable {
	private static final long serialVersionUID = 6907274508888816165L;
	private static final int UPDATE_TIME = 100;
	private static final int RUNNING_UPDATE_TIME = 25;

	private final IMMLManager mmlManager;
	private final Sequencer sequencer;

	private final StringBuffer time1 = new StringBuffer("time MM:SS/MM:SS (t120)     ");
	private final StringBuffer time2 = new StringBuffer();

	public TimeBox(IMMLManager mmlManager) {
		super();
		this.mmlManager = mmlManager;
		addItem(time1);
		addItem(time2);
		new Thread(this).start();
		sequencer = MabiDLS.getInstance().getSequencer();
	}

	/**
	 * 表示を更新する
	 * @param position
	 */
	private void update(long position) {
		MMLScore score = mmlManager.getMMLScore();
		List<MMLTempoEvent> tempoList = score.getTempoEventList();
		long time = Math.round(MMLTempoConverter.getTimeOnTickOffset(tempoList, (int)position));
		int totalTick = score.getTotalTickLength();
		long totalTime = Math.round(MMLTempoConverter.getTimeOnTickOffset(tempoList, totalTick));
		int tempo = MMLTempoEvent.searchOnTick(tempoList, (int)position);

		String str1 = String.format("time %d:%02d.%d/%d:%02d.%d (t%d)", 
				(time/60/1000), (time/1000%60), (time/100%10),
				(totalTime/60/1000), (totalTime/1000%60), (totalTime/100%10),
				tempo);
		String str2 = score.getBarTextTick((int)position) + "/" + score.getBarTextTick(totalTick) + " (t" + tempo + ")";

		boolean repaint = false;
		if (!time1.toString().equals(str1)) {
			time1.replace(0, time1.length(), str1);
			repaint = true;
		}
		if (!time2.toString().equals(str2)) {
			time2.replace(0, time2.length(), str2);
			repaint = true;
		}
		if (repaint) {
			repaint();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				update(mmlManager.getSequencePosition());
				Thread.sleep(sequencer.isRunning() ? RUNNING_UPDATE_TIME : UPDATE_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.core.MMLTools;
import fourthline.mmlTools.optimizer.MMLStringOptimizer;

public class MMLTrack extends MMLTools implements Serializable {
	private static final long serialVersionUID = 2006880378975808647L;
	
	private List<MMLEventList> mmlParts;
	private List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();

	private int program = 0;
	private String trackName;
	private int panpot = 64;

	private static final int PART_COUNT = 3;

	public MMLTrack(String mml) {
		super(mml);

		mmlParse();
	}

	public MMLTrack(String mml1, String mml2, String mml3) {
		super(mml1, mml2, mml3);

		mmlParse();
	}

	private void mmlParse() {
		mmlParts = new ArrayList<MMLEventList>(PART_COUNT);
		String mml[] = {
				getMelody(),
				getChord1(),
				getChord2()
		};

		for (int i = 0; i < mml.length; i++) {
			mmlParts.add( new MMLEventList(mml[i], globalTempoList) );
		}
	}

	public void setGlobalTempoList(List<MMLTempoEvent> globalTempoList) {
		this.globalTempoList = globalTempoList;

		for (MMLEventList eventList : mmlParts) {
			eventList.setGlobalTempoList(globalTempoList);
		}
	}

	public List<MMLTempoEvent> getGlobalTempoList() {
		return this.globalTempoList;
	}

	public void setProgram(int program) {
		this.program = program;
	}

	public int getProgram() {
		return this.program;
	}

	public void setTrackName(String name) {
		this.trackName = name;
	}

	public String getTrackName() {
		return this.trackName;
	}

	public void setPanpot(int panpot) {
		if (panpot > 127) {
			panpot = 127;
		} else if (panpot < 0) {
			panpot = 0;
		}
		this.panpot = panpot;
	}

	public int getPanpot() {
		return this.panpot;
	}

	public MMLEventList getMMLEventList(int index) {
		return mmlParts.get(index);
	}

	public int getMMLEventListSize() {
		return mmlParts.size();
	}

	public long getMaxTickLength() {
		long max = 0;
		for (int i = 0; i < mmlParts.size(); i++) {
			long tick = mmlParts.get(i).getTickLength();
			if (max < tick) {
				max = tick;
			}
		}

		return max;
	}

	public String getMMLString() {
		String mml[] = getMMLStrings();
		MMLTools tools = new MMLTools(mml[0], mml[1], mml[2]);

		return tools.getMML();
	}

	public String[] getMMLStrings() {
		int count = mmlParts.size();
		String mml[] = new String[count];
		int totalTick = (int)this.getMaxTickLength();

		for (int i = 0; i < count; i++) {
			// メロディパートのMML更新（テンポ, tickLengthにあわせる.
			MMLEventList eventList = mmlParts.get(i);
			if (i == 0) {
				mml[i] = eventList.toMMLString(true, totalTick);
			} else {
				mml[i] = eventList.toMMLString();
			}

			mml[i] = new MMLStringOptimizer(mml[i]).toString();
		}

		double playTime = getPlayTime();
		double mmlTime = getMabinogiTime();
		int tick = (int)(totalTick - mmlParts.get(0).getTickLength());
		if (playTime > mmlTime) {
			// スキルが演奏の途中で止まるのを防ぎます.
			mml[0] += new MMLTicks("r", tick, false).toString();
		} else if (playTime < mmlTime) {
			// 演奏が終ってスキルが止まらないのを防ぎます.
			if (tick > 0) {
				mml[0] += new MMLTicks("r", tick, false).toString() + "v0c64";
			}
			mml[0] += MMLTempoEvent.getMaxTempoEvent(globalTempoList).toMMLString();
		}

		return mml;
	}

	/**
	 * MMLの演奏時間を取得する.
	 * @return 時間（秒）
	 */
	public double getPlayTime() {
		int totalTick = (int)getMaxTickLength();
		double playTime = MMLTempoEvent.getTimeOnTickOffset(globalTempoList, totalTick);

		return playTime;
	}	

	/**
	 * マビノギでの演奏スキル時間を取得する.
	 * <p>演奏時間  － 0.6秒 ＜ スキル時間 であれば、切れずに演奏される</p>
	 * @return 時間（秒）
	 */
	public double getMabinogiTime() {
		double partTime[] = new double[mmlParts.size()];

		int melodyTick = (int)mmlParts.get(0).getTickLength();
		partTime[0] = MMLTempoEvent.getTimeOnTickOffset(globalTempoList, melodyTick);

		ArrayList<MMLTempoEvent> globalTailTempo = new ArrayList<MMLTempoEvent>();
		MMLTempoEvent lastTempoEvent = new MMLTempoEvent(120, 0);
		if (globalTempoList.size() > 0) {
			lastTempoEvent.setTempo(globalTempoList.get(globalTempoList.size()-1).getTempo());
		}
		globalTailTempo.add(new MMLTempoEvent(lastTempoEvent.getTempo(), 0));

		for (int i = 1; i < partTime.length; i++) {
			int tick = (int)mmlParts.get(i).getTickLength();
			partTime[i] = MMLTempoEvent.getTimeOnTickOffset(globalTailTempo, tick);
		}

		double maxTime = 0.0;
		for (double time : partTime) {
			if (maxTime < time) {
				maxTime = time;
			}
		}

		return maxTime;
	}
}

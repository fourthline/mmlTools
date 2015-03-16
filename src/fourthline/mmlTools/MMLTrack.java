/*
 * Copyright (C) 2013-2015 たんらる
 */

package fourthline.mmlTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fourthline.mmlTools.core.MMLText;
import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.optimizer.MMLStringOptimizer;

public final class MMLTrack implements Serializable {
	private static final long serialVersionUID = 2006880378975808647L;

	private static final int PART_COUNT = 4;
	private List<MMLEventList> mmlParts = new ArrayList<>();
	private List<MMLTempoEvent> globalTempoList = new ArrayList<>();
	private boolean generated = false;

	private int program = 0;
	private String trackName;
	private int panpot = 64;

	// for MML input
	private MMLText originalMML = new MMLText();

	// for MML output
	private MMLText mabiMML = new MMLText();

	// コーラスオプション (楽器＋歌）
	private int songProgram = -1;  // コーラスを使用しない.

	public MMLTrack() {
		mmlParse();
		generated = true;
	}

	public MMLTrack setMML(String mml) {
		originalMML.setMMLText(mml);
		mabiMML.setMMLText(mml);

		mmlParse();
		return this;
	}

	public MMLTrack setMML(String mml1, String mml2, String mml3, String mml4) {
		originalMML.setMMLText(mml1, mml2, mml3, mml4);
		mabiMML.setMMLText(mml1, mml2, mml3, mml4);

		mmlParse();
		return this;
	}

	private void mmlParse() {
		mmlParts.clear();
		generated = false;

		for (int i = 0; i < PART_COUNT; i++) {
			String s = originalMML.getText(i);
			mmlParts.add( new MMLEventList(s, globalTempoList) );
		}
	}

	public boolean isEmpty() {
		return originalMML.isEmpty();
	}

	public String getOriginalMML() {
		return originalMML.getMML();
	}

	public String getMabiMML() {
		return mabiMML.getMML();
	}

	/**
	 * 出力用のMMLランク
	 * @return　フォーマット済みRank文字列
	 */
	public String mmlRankFormat() {
		return (generated ? "" : "*") + mabiMML.mmlRankFormat();
	}

	/**
	 * 出力用のMMLを取得する.
	 * @return　各パートのMML文字列
	 */
	public String[] getMabiMMLArray() {
		String mml[] = new String[ PART_COUNT ];
		for (int i = 0; i < mml.length; i++) {
			mml[i] = mabiMML.getText(i);
		}
		return mml;
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

	public void setSongProgram(int songProgram) {
		this.songProgram = songProgram;
	}

	public int getSongProgram() {
		return this.songProgram;
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

	public MMLEventList getMMLEventAtIndex(int index) {
		return mmlParts.get(index);
	}

	public int getMMLEventListSize() {
		return mmlParts.size();
	}

	public List<MMLEventList> getMMLEventList() {
		return mmlParts;
	}

	public long getMaxTickLength() {
		long max = 0;
		for (MMLEventList eventList : mmlParts) {
			long tick = eventList.getTickLength();
			if (max < tick) {
				max = tick;
			}
		}

		return max;
	}

	public MMLTrack generate() throws UndefinedTickException {
		String mml1 = getOriginalMML();
		originalMML.setMMLText(getMMLStrings(false, false));
		if (!this.equals(new MMLTrack().setMML(getOriginalMML()))) {
			System.err.println("Verify error.");
			System.err.println(mml1);
			System.err.println(getOriginalMML());
			throw new UndefinedTickException("Verify error.");
		}
		mabiMML.setMMLText(getMMLStrings(true, true));
		generated = true;
		return this;
	}

	private String[] getMMLStrings(boolean tailFix, boolean mabiTempo) throws UndefinedTickException {
		int count = mmlParts.size();
		String mml[] = new String[count];
		int totalTick = (int)this.getMaxTickLength();

		for (int i = 0; i < count; i++) {
			// メロディパートのMML更新（テンポ, tickLengthにあわせる.
			MMLEventList eventList = mmlParts.get(i);
			boolean isPrimaryTempoPart = (i == 0) || (i == 3);
			if ( isPrimaryTempoPart ) {
				mml[i] = eventList.toMMLString(true, totalTick, mabiTempo);
			} else {
				mml[i] = eventList.toMMLString();
			}
		}
		if (tailFix) { // 終端補正
			mml[0] = tailFix(mml[0]);
		}
		for (int i = 0; i < count; i++) {
			mml[i] = new MMLStringOptimizer(mml[i]).toString();
		}
		if ((mmlParts.get(3).getTickLength() == 0)) {
			mml[3] = "";
		}

		return mml;
	}

	private String tailFix(String s) throws UndefinedTickException {
		long totalTick = this.getMaxTickLength();
		double playTime = getPlayTime();
		double mmlTime = getMabinogiTime();
		int tick = (int)(totalTick - new MMLEventList(s).getTickLength());
		if (playTime > mmlTime) {
			// スキルが演奏の途中で止まるのを防ぎます.
			s += new MMLTicks("r", tick, false).toMMLText();
		} else if (playTime < mmlTime) {
			// 演奏が終ってスキルが止まらないのを防ぎます.
			if (tick > 0) {
				s += new MMLTicks("r", tick, false).toMMLText() + "v0c64";
			}
			s += MMLTempoEvent.getMaxTempoEvent(globalTempoList).toMMLString();
		}

		return s;
	}

	/**
	 * MMLの演奏時間を取得する.
	 * @return 時間（秒）
	 */
	public double getPlayTime() {
		int totalTick = (int)getMaxTickLength();
		long playTime = MMLTempoEvent.getTimeOnTickOffset(globalTempoList, totalTick);

		return playTime/1000.0;
	}	

	/**
	 * マビノギでの演奏スキル時間を取得する.
	 * <p>演奏時間  － 0.6秒 ＜ スキル時間 であれば、切れずに演奏される</p>
	 * TODO: 歌パートの扱いは調べる必要があります・・・.
	 * @return 時間（秒）
	 */
	public double getMabinogiTime() {
		long partTime[] = new long[mmlParts.size()];

		int melodyTick = (int)mmlParts.get(0).getTickLength();
		partTime[0] = MMLTempoEvent.getTimeOnTickOffset(globalTempoList, melodyTick);

		ArrayList<MMLTempoEvent> globalTailTempo = new ArrayList<>();
		MMLTempoEvent lastTempoEvent = new MMLTempoEvent(120, 0);
		if (globalTempoList.size() > 0) {
			lastTempoEvent.setTempo(globalTempoList.get(globalTempoList.size()-1).getTempo());
		}
		globalTailTempo.add(new MMLTempoEvent(lastTempoEvent.getTempo(), 0));

		for (int i = 1; i < partTime.length; i++) {
			int tick = (int)mmlParts.get(i).getTickLength();
			partTime[i] = MMLTempoEvent.getTimeOnTickOffset(globalTailTempo, tick);
		}

		long maxTime = 0;
		for (long time : partTime) {
			if (maxTime < time) {
				maxTime = time;
			}
		}

		return maxTime/1000.0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MMLTrack)) {
			return false;
		}
		MMLTrack mmlTrack = (MMLTrack) obj;
		if (this.mmlParts.size() != mmlTrack.mmlParts.size()) {
			return false;
		}

		if (Arrays.equals(this.mmlParts.toArray(), mmlTrack.mmlParts.toArray())) {
			return true;
		}

		return false;
	}
}

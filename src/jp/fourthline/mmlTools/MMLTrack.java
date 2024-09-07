/*
 * Copyright (C) 2013-2024 たんらる
 */

package jp.fourthline.mmlTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import jp.fourthline.mmlTools.core.MMLText;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.MMLException;
import jp.fourthline.mmlTools.optimizer.MMLStringOptimizer;

public final class MMLTrack implements Serializable, Cloneable {
	private static final long serialVersionUID = 2006880378975808647L;

	private static final int MAX_TRACK_NAME_LEN = 32;
	public static final int INITIAL_VOLUME = 100;

	public static final int NO_CHORUS = -1;
	public static final int EXCLUDE_SONG = -2;

	/** program番号から和音へのテンポ出力が可能かどうかの判定を行うためのFunction */
	private static IntFunction<Boolean> tempoAllowChordPartFunction = t -> true;
	public static void setTempoAllowChordPartFunction(IntFunction<Boolean> f) {
		tempoAllowChordPartFunction = f;
	}

	/** program番号から打楽器モーション用の休符処理をするかどうかの判定を行うためのFunction */
	public static IntFunction<Boolean> percussionMotionFix = t -> false;
	public static void setPercussionMotionFixFunction(IntFunction<Boolean> f) {
		percussionMotionFix = f;
	}

	private static final int PART_COUNT = 4;
	private final List<MMLEventList> mmlParts = new ArrayList<>();
	private List<MMLTempoEvent> globalTempoList = new ArrayList<>();
	private boolean generated = false;

	private int program = 0;
	private String trackName = "";
	private int panpot = 64;
	private int volume = INITIAL_VOLUME;
	private boolean visible = true;

	// start offset function
	private int commonStartOffset = 0;      // 共通の開始位置
	private int startDelta = 0;             // 楽器部の開始位置delta
	private int startSongDelta = 0;         // 歌部の開始位置delta
	private int attackDelayCorrect = 0;     // 楽器部のアタック遅延補正
	private int attackSongDelayCorrect = 0; // 歌部のアタック遅延補正

	// Nコマンド無効化オプション
	private boolean disableNopt = false;

	// for MML input
	private final MMLText originalMML = new MMLText();

	// for MML output
	private final MMLText mabiMML = new MMLText();

	// コーラスオプション (楽器＋歌）
	private int songProgram = NO_CHORUS;  // コーラスを使用しない.

	// 64bit mabi 合奏ズレ補正
	private boolean fix64Tempo = false;

	// テンポをメロディに限定するオプション (フリースタイルジャム用)
	private boolean optTempoOnlyMelody = false;

	// インポートしたデータ: ドラム変換を多重実行したときに変換対象にするデータ.
	private String importedData;

	public MMLTrack() {
		this(0, 0, 0);
	}

	public MMLTrack(int commonStartOffset, int startDelta, int startSongDelta) {
		if (commonStartOffset < 0) {
			throw new IllegalArgumentException();
		}
		if (commonStartOffset % 6 != 0) {
			throw new IllegalArgumentException();
		}
		this.commonStartOffset = commonStartOffset;
		this.startDelta = startDelta;
		this.startSongDelta = startSongDelta;
		mmlParse(false);
		generated = true;
	}

	public MMLTrack setMML(String mml) {
		return setMML(mml, false);
	}

	public MMLTrack setMabiMML(String mml) {
		return setMML(mml, true);
	}

	public MMLTrack setMML(String mml, boolean delayOption) {
		if (mml.indexOf('\n') >= 0) {
			// ゲーム内からコピーされたフォーマットを読む.
			String[] parts = mml.split("\n", 8);
			boolean invalidFormat = false;
			if (parts.length == 7) {
				for (int i = 2; i < parts.length-1; i++) {
					int startIndex = parts[i].indexOf(':');
					if (startIndex < 0) {
						invalidFormat = true;
						break;
					}
					parts[i] = parts[i].substring(startIndex+1).trim();
				}
				if (!invalidFormat) {
					return setMML(parts[2], parts[3], parts[4], parts[5]);
				}
			}
		}

		originalMML.setMMLText(mml);
		mabiMML.setMMLText(mml);

		mmlParse(delayOption);
		return this;
	}

	public MMLTrack setMML(String mml1, String mml2, String mml3, String mml4) {
		originalMML.setMMLText(mml1, mml2, mml3, mml4);
		mabiMML.setMMLText(mml1, mml2, mml3, mml4);

		mmlParse(false);
		return this;
	}

	public void setImportedData(String str) {
		importedData = str;
	}

	private void mmlParse(boolean delayOption) {
		mmlParts.clear();
		generated = false;

		for (int i = 0; i < PART_COUNT; i++) {
			String s = originalMML.getText(i);
			int startOffset = getStartOffset(i);
			if (delayOption) {
				startOffset -= getAttackDelayCorrect(i);
			}
			mmlParts.add( new MMLEventList(s, globalTempoList, startOffset) );
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

	public ComposeRank mmlRank() {
		mabiMML.setExcludeSongPart(isExcludeSongPart());
		return mabiMML.mmlRank();
	}

	/**
	 * 出力用のMMLランク
	 * @return　フォーマット済みRank文字列
	 */
	public String mmlRankFormat() {
		mabiMML.setExcludeSongPart(isExcludeSongPart());
		return (generated ? "" : "*") + mabiMML.mmlRankFormat();
	}

	/**
	 * 出力用のMMLを取得する.
	 * @return　各パートのMML文字列
	 */
	public String[] getMabiMMLArray() {
		String[] mml = new String[ PART_COUNT ];
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

	public MMLTrack setProgram(int program) {
		this.program = program;
		return this;
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
		if (name == null) {
			this.trackName = "";
		} else if (name.length() > MAX_TRACK_NAME_LEN) {
			this.trackName = name.substring(0, MAX_TRACK_NAME_LEN);
		} else {
			this.trackName = name;
		}
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

	public void setVolume(int volume) {
		if ( (volume < 0) || (volume > 127) ) {
			throw new IllegalArgumentException("illeagl volume: " + volume);
		}
		this.volume = volume;
	}

	public int getVolume() {
		return this.volume;
	}

	public boolean getFix64() {
		return fix64Tempo;
	}

	public void setFix64(boolean b) {
		fix64Tempo = b;
	}

	public MMLEventList getMMLEventAtIndex(int index) {
		return mmlParts.get(index);
	}

	public List<MMLEventList> getMMLEventList() {
		return mmlParts;
	}

	public String getImportedData() {
		return importedData;
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

	/**
	 * 64bit mabinogi 環境における合奏ズレを補正する
	 * 開始テンポと終了テンポが異なる場合に、MML最終位置に開始テンポを追加する. （和音パートへも許可）
	 * @param mml
	 * @return
	 */
	private String[] fix64Tempo(String mml[]) {
		// 楽器パート処理.
		int firstTempo = MMLTempoEvent.searchOnTick(globalTempoList, commonStartOffset + startDelta);
		int tick = MMLEventList.maxEndTick(mmlParts.subList(0, 3));
		int endTempo = MMLTempoEvent.searchOnTick(globalTempoList, tick - 1);
		if (firstTempo != endTempo) {
			for (int i = 0; i < 3; i++) {
				var note = mmlParts.get(i).getLastNote();
				if ((note != null) && (note.getEndTick() == tick)) {
					mml[i] += "t" + firstTempo;
					break;
				}
			}
		}

		// 歌パート処理.
		int firstSongTempo = MMLTempoEvent.searchOnTick(globalTempoList, commonStartOffset + startSongDelta);
		var songLastNote = mmlParts.get(3).getLastNote();
		if (songLastNote != null) {
			int endSongTempo = MMLTempoEvent.searchOnTick(globalTempoList, songLastNote.getEndTick() - 1);
			if (firstSongTempo != endSongTempo) {
				mml[3] += "t" + firstSongTempo;
			}
		}
		return mml;
	}

	/**
	 * テンポなしでMMLデータを作成する.
	 * @return
	 * @throws MMLExceptionList
	 */
	private String[] internalData() throws MMLExceptionList {
		int size = mmlParts.size();
		String[] mml = new String[size];
		var errList = new ArrayList<MMLExceptionList.Entry>();
		for (int i = 0; i < size; i++) {
			try {
				var eventList = mmlParts.get(i);
				var str = MMLBuilder.create(eventList, getStartOffset(i)).toMMLString(false, false);
				mml[i] = new MMLStringOptimizer(str).toString();
			} catch (MMLExceptionList e) {
				errList.addAll(e.getErr());
			}
		}
		if (!errList.isEmpty()) {
			throw new MMLExceptionList(errList);
		}
		return mml;
	}

	public MMLTrack generate() throws MMLExceptionList, MMLVerifyException {
		String mml1 = getOriginalMML();
		try {
			originalMML.setMMLText(getMMLStrings(false, false));
		} catch (MMLExceptionList e) {
			originalMML.setMMLText(internalData());
		}
		var t = new MMLTrack(commonStartOffset, startDelta, startSongDelta).setMML(getOriginalMML());
		t.setGlobalTempoList(globalTempoList);
		if (!this.equals(t)) {
			System.err.println("Verify error.");
			System.err.println(mml1);
			System.err.println(getOriginalMML());
			throw new MMLVerifyException(this);
		}
		/*
		 * tailFixはMusicQアップデートで不要になりました. 2017/01/07
		 */
		String[] mmlStrings = (optTempoOnlyMelody) ? getMMLStrings(false, true) : getMMLStringsMusicQ();

		// 64ビットクライアントにより、合奏ズレが発生している向けの補正機能. 2023/02/05
		if (fix64Tempo) {
			mmlStrings = fix64Tempo(mmlStrings);
		}
		mabiMML.setMMLText(mmlStrings);
		generated = true;
		return this;
	}

	private String[] getMMLStrings(boolean tailFix, boolean mabiTempo) throws MMLExceptionList {
		int count = mmlParts.size();
		String[] mml = new String[count];
		var errList = new ArrayList<MMLExceptionList.Entry>();

		for (int i = 0; i < count; i++) {
			boolean pix = mabiTempo && percussionMotionFix.apply(program) && (i == 0);
			int startOffset = mabiTempo ? getStartOffsetforMabiMML(i) : getStartOffset(i);
			// メロディパートのMML更新（テンポ, tickLengthにあわせる.
			MMLEventList eventList = mmlParts.get(i);
			boolean isPrimaryTempoPart = (i == 0) || (i == 3);
			try {
				if ( isPrimaryTempoPart ) {
					// part0 の場合, 1,2のパート情報を渡す
					List<MMLEventList> relationPart = (i == 0) ? mmlParts.subList(1, 3) : null;
					mml[i] = MMLBuilder.create(eventList, startOffset, pix).toMMLString(true, mabiTempo, relationPart);
				} else {
					mml[i] = MMLBuilder.create(eventList, startOffset, pix).toMMLString();
				}
			} catch (MMLExceptionList e) {
				errList.addAll(e.getErr());
			}
		}
		if (!errList.isEmpty()) {
			throw new MMLExceptionList(errList);
		}
		if (tailFix) { // 終端補正
			try {
				mml[0] = tailFix(mml[0], mml[1], mml[2]);
			} catch (MMLException e) {
				e.printStackTrace();
			}
		}
		// for mabi MML, メロディ～和音2 までがカラの時にはメロディパートもカラにする.
		if ( mabiTempo && mmlParts.get(0).getMMLNoteEventList().isEmpty() && mml[1].equals("") && mml[2].equals("") ) {
			mml[0] = "";
		}
		for (int i = 0; i < count; i++) {
			if (mabiTempo) {
				mml[i] = mabiMMLOptimizeFunc.apply(new MMLStringOptimizer(mml[i]).setDisableNopt(disableNopt));
			} else {
				// 内部データ向けは旧アルゴリズムを使用する.
				mml[i] = new MMLStringOptimizer(mml[i]).toString();
			}
		}
		if ((mmlParts.get(3).getTickLength() == 0)) {
			mml[3] = "";
		}

		return mml;
	}

	private List<List<MMLEventList>> makeRelationPart() {
		List<List<MMLEventList>> list = new ArrayList<>();
		List<MMLEventList> list1 = new ArrayList<>();
		list1.add(mmlParts.get(1));
		list1.add(mmlParts.get(2));

		List<MMLEventList> list2 = new ArrayList<>();
		list2.add(mmlParts.get(2));
		list2.add(mmlParts.get(0));

		List<MMLEventList> list3 = new ArrayList<>();
		list3.add(mmlParts.get(0));
		list3.add(mmlParts.get(1));

		list.add(list1);
		list.add(list2);
		list.add(list3);
		return list;
	}

	/**
	 * 最適化前のMML列を取得する.
	 * TODO: 別クラスでもいいかも？
	 * @param allowTempoChord   テンポを和音パートに出力許可するか
	 * @return
	 * @throws MMLExceptionList 
	 */
	public String[] getGenericMMLStrings(boolean allowTempoChord) throws MMLExceptionList {
		int count = mmlParts.size();
		String[] mml = new String[count];
		LinkedList<MMLTempoEvent> localTempoList = new LinkedList<>(globalTempoList);
		List<List<MMLEventList>> relationParts = makeRelationPart();
		var errList = new ArrayList<MMLExceptionList.Entry>();

		for (int i = 0; i < count; i++) {
			boolean pix = percussionMotionFix.apply(program) && (i == 0);
			// メロディパートのMML更新（テンポ, tickLengthにあわせる.
			MMLEventList eventList = mmlParts.get(i);
			if (i == 3) {
				localTempoList = new LinkedList<>(globalTempoList);
			}
			List<MMLEventList> relationPart = ((i < 3) && (allowTempoChord)) ? relationParts.get(i) : null;
			boolean checkDelta = (i < 2);
			try {
				mml[i] = MMLBuilder.create(eventList, getStartOffsetforMabiMML(i), MMLBuilder.INIT_OCT, pix).toMMLStringMusicQ(localTempoList, relationPart, checkDelta);
			} catch (MMLExceptionList e) {
				errList.addAll(e.getErr());
			}
		}
		if (!errList.isEmpty()) {
			throw new MMLExceptionList(errList);
		}

		// for mabi MML, メロディ～和音2 までがカラの時にはメロディパートもカラにする.
		if ( mmlParts.get(0).getMMLNoteEventList().isEmpty() && mml[1].equals("") && mml[2].equals("") ) {
			mml[0] = "";
		}
		return mml;
	}

	/**
	 * MusicQ以降用のMabinogi用MML生成.
	 * @return
	 * @throws MMLExceptionList 
	 */
	private String[] getMMLStringsMusicQ() throws MMLExceptionList {
		boolean allowed = tempoAllowChordPartFunction.apply(program);
		String[] mml = getGenericMMLStrings(allowed);
		for (int i = 0; i < mml.length; i++) {
			mml[i] = mabiMMLOptimizeFunc.apply(new MMLStringOptimizer(mml[i]).setDisableNopt(disableNopt));
		}
		if ((mmlParts.get(3).getTickLength() == 0)) {
			mml[3] = "";
		}

		return mml;
	}

	private static Function<MMLStringOptimizer, String> mabiMMLOptimizeFunc = t -> t.preciseOptimize();
	public static void setMabiMMLOptimizeFunc(Function<MMLStringOptimizer, String> f) {
		mabiMMLOptimizeFunc = (f != null) ? (f) : (t -> t.preciseOptimize());
	}

	private String tailFix(String melody, String chord1, String chord2) throws MMLException {
		String s = melody;
		MMLTrack partTrack = new MMLTrack().setMML(melody, chord1, chord2, "");
		long totalTick = partTrack.getMaxTickLength();
		double playTime = partTrack.getPlayTime();
		double mmlTime = partTrack.getMabinogiTime();
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
		double playTime = MMLTempoConverter.getTimeOnTickOffset(globalTempoList, totalTick);
		return playTime/1000.0;
	}	

	/**
	 * マビノギでの演奏スキル時間を取得する.
	 * <p>演奏時間  － 0.6秒 ＜ スキル時間 であれば、切れずに演奏される</p>
	 * @return 時間（秒）
	 */
	public double getMabinogiTime() {
		double[] partTime = new double[mmlParts.size()];

		int melodyTick = (int)mmlParts.get(0).getTickLength();
		partTime[0] = MMLTempoConverter.getTimeOnTickOffset(globalTempoList, melodyTick);

		ArrayList<MMLTempoEvent> globalTailTempo = new ArrayList<>();
		MMLTempoEvent lastTempoEvent = new MMLTempoEvent(120, 0);
		if (globalTempoList.size() > 0) {
			lastTempoEvent.setTempo(globalTempoList.get(globalTempoList.size()-1).getTempo());
		}
		globalTailTempo.add(new MMLTempoEvent(lastTempoEvent.getTempo(), 0));

		for (int i = 1; i < partTime.length; i++) {
			int tick = (int)mmlParts.get(i).getTickLength();
			partTime[i] = MMLTempoConverter.getTimeOnTickOffset(globalTailTempo, tick);
		}

		double maxTime = 0;
		for (double time : partTime) {
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

		return Arrays.equals(this.mmlParts.toArray(), mmlTrack.mmlParts.toArray());
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isExcludeSongPart() {
		return songProgram == EXCLUDE_SONG;
	}

	/**
	 * 各種オフセット変更にともなうシフト処理を行う.
	 * 対応するNoteもシフトする
	 * @param delta   オフセット変更量
	 * @param inst　　　　楽器部をシフトするかどうか
	 * @param song     歌部をシフトするかどうか
	 */
	public void updateStartOffsetNoteEvents(int delta, boolean inst, boolean song) {
		int size = mmlParts.size();
		for (int i = 0; i < size; i++) {
			if ( (inst && (i >= 0) && (i <= 2)) || (song && (i == 3)) ) {
				var part = mmlParts.get(i).getMMLNoteEventList();
				part.forEach(note -> note.setTickOffset(note.getTickOffset() + delta));
				// マイナスになったら消す
				part.removeIf(t -> t.getTickOffset() < 0);
			}
		}
	}

	public MMLTrack setStartOffset(int offset, List<MMLTempoEvent> globalTempoList) {
		// 全体のスタート位置変更の場合はノートも移動する
		int delta = offset - commonStartOffset;
		updateStartOffsetNoteEvents(delta, true, true);
		if (this.globalTempoList != globalTempoList) {
			this.globalTempoList.forEach(t -> t.setTickOffset(t.getTickOffset() + delta));
		}
		commonStartOffset = offset;
		return this;
	}

	private boolean checkStartOffset(int newStartOffset, List<MMLEventList> partList) {
		for (var part : partList) {
			var eventList = part.getMMLNoteEventList();
			if (eventList.size() > 0) {
				if (eventList.get(0).getTickOffset() < newStartOffset) {
					return false;
				}
			}
		}
		return true;
	}

	public MMLTrack setStartDelta(int delta) {
		int newStartOffset = commonStartOffset + delta;
		if ((newStartOffset >= 0) && (checkStartOffset(newStartOffset, mmlParts.subList(0, 3)))) {
			// ノートの移動を行わない
			startDelta = delta;
		} else {
			throw new IllegalArgumentException();
		}
		return this;
	}

	public MMLTrack setStartSongDelta(int delta) {
		int newStartOffset = commonStartOffset + delta;
		if ((newStartOffset >= 0) && (checkStartOffset(newStartOffset, mmlParts.subList(3, 4)))) {
			// ノートの移動を行わない
			startSongDelta = delta;
		} else {
			throw new IllegalArgumentException();
		}
		return this;
	}

	public int getStartOffset(int index) {
		return this.commonStartOffset + 
				switch (index) {
				case 0, 1, 2 -> this.startDelta;
				case 3 ->       this.startSongDelta;
				default -> throw new IllegalArgumentException();
				};
	}

	/**
	 * MabiMML用のstartOffsetを取得する
	 * @param index
	 * @return
	 */
	private int getStartOffsetforMabiMML(int index) {
		return  getStartOffset(index) - getAttackDelayCorrect(index);
	}

	public int getCommonStartOffset() {
		return this.commonStartOffset;
	}

	public int getStartDelta() {
		return this.startDelta;
	}

	public int getStartSongDelta() {
		return this.startSongDelta;
	}

	public void setAttackDelayCorrect(int value) {
		attackDelayCorrect = value;
	}

	public int getAttackDelayCorrect() {
		return attackDelayCorrect;
	}

	public void setAttackSongDelayCorrect(int value) {
		attackSongDelayCorrect = value;
	}

	public int getAttackSongDelayCorrect() {
		return attackSongDelayCorrect;
	}

	public int getAttackDelayCorrect(int index) {
		return switch (index) {
		case 0, 1, 2 -> this.attackDelayCorrect;
		case 3 ->       this.attackSongDelayCorrect;
		default -> throw new IllegalArgumentException();
		};
	}

	public boolean isFantasyTrack() {
		boolean b1 = !mmlParts.get(0).isEmpty() || !mmlParts.get(1).isEmpty() || !mmlParts.get(2).isEmpty();
		boolean b2 = !mmlParts.get(3).isEmpty();
		return b1 && b2;
	}

	public void setDisableNopt(boolean b) {
		disableNopt = b;
	}

	public boolean getDisableNopt() {
		return disableNopt;
	}

	public void setOptTempoOnlyMelody(boolean b) {
		optTempoOnlyMelody = b;
	}

	public boolean getOptTempoMelodyOnly() {
		return optTempoOnlyMelody;
	}

	@Override
	public MMLTrack clone() {
		MMLTrack o = new MMLTrack(commonStartOffset, startDelta, startSongDelta)
				.setMML(this.getOriginalMML());
		o.setGlobalTempoList(globalTempoList);
		o.setPanpot(panpot);
		o.setVolume(volume);
		o.setProgram(program);
		o.setSongProgram(songProgram);
		o.setTrackName(trackName);
		o.setAttackDelayCorrect(attackDelayCorrect);
		o.setAttackSongDelayCorrect(attackSongDelayCorrect);
		o.setDisableNopt(disableNopt);
		o.setImportedData(importedData);
		o.setOptTempoOnlyMelody(optTempoOnlyMelody);
		if (generated) {
			try {
				o.generate();
			} catch (MMLExceptionList | MMLVerifyException e) {
				e.printStackTrace();
			}
		}
		return o;
	}
}

/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mmlTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.UndefinedTickException;
import jp.fourthline.mmlTools.parser.MMSFile;


/**
 * Score
 */
public final class MMLScore implements Cloneable {
	private final LinkedList<MMLTrack> trackList = new LinkedList<>();
	private final List<MMLTempoEvent> globalTempoList = new ArrayList<>();
	private final List<Marker> markerList = new ArrayList<>();

	public static final int MAX_TRACK = 24;

	private String title = "";
	private String author = "";
	private int numTime = 4;
	private int baseTime = 4;

	private final Stack<UndefinedTickException> exceptionStack = new Stack<>();

	public static final int MAX_USER_VIEW_MEASURE = 200;
	private int userViewMeasure;

	/**
	 * 新たにトラックを追加します.
	 * @param track
	 * @return トラック数の上限を超えていて、追加できないときは -1. 追加できた場合は、追加したindex値を返します(0以上).
	 */
	public synchronized int addTrack(MMLTrack track) {
		if (trackList.size() >= MAX_TRACK) {
			return -1;
		}

		// 既存トラックがあれば、StartOffsetをあわせる
		if (trackList.size() > 0) {
			int initialStartOffset = trackList.getFirst().getCommonStartOffset();
			track.setStartOffset(initialStartOffset, globalTempoList);
		}

		// トラックリストの末尾に追加
		trackList.add(track);
		int trackIndex = trackList.size() - 1;

		// グローバルテンポリストの統合.
		MMLTempoEvent.mergeTempoList(track.getGlobalTempoList(), globalTempoList);
		track.setGlobalTempoList(globalTempoList);

		return trackIndex;
	}

	/**
	 * 指定したindexのトラックを削除します.
	 * @param index
	 */
	public synchronized void removeTrack(int index) {
		trackList.remove(index);
	}

	public synchronized void moveTrack(int fromIndex, int toIndex) {
		MMLTrack mmlTrack = getTrack(fromIndex);
		removeTrack(fromIndex);
		trackList.add(toIndex, mmlTrack);
	}

	/**
	 * 保持しているトラックの数を返します.
	 * @return
	 */
	public int getTrackCount() {
		return trackList.size();
	}

	/**
	 * 保持しているトラックリストを返します.
	 * @return MMLTrackの配列
	 */
	public synchronized List<MMLTrack> getTrackList() {
		return trackList;
	}

	/**
	 * 指定したindexのトラックを返します.
	 * @param index
	 * @return
	 */
	public MMLTrack getTrack(int index) {
		return trackList.get(index);
	}

	/**
	 * 指定されたindexにトラックをセットします.
	 * @param index
	 * @param track
	 */
	public void setTrack(int index, MMLTrack track) {
		trackList.set(index, track);
		int startOffset = trackList.getFirst().getCommonStartOffset();
		track.setStartOffset(startOffset, globalTempoList);

		// グローバルテンポリストの統合.
		MMLTempoEvent.mergeTempoList(track.getGlobalTempoList(), globalTempoList);
		track.setGlobalTempoList(globalTempoList);
	}

	public int getTempoOnTick(long tickOffset) {
		return MMLTempoEvent.searchOnTick(globalTempoList, tickOffset);
	}

	public List<MMLTempoEvent> getTempoEventList() {
		return globalTempoList;
	}

	public List<Marker> getMarkerList() {
		return markerList;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setBaseTime(String baseTime) {
		String[] s = baseTime.split("/");
		this.numTime = Integer.parseInt(s[0]);
		this.baseTime = Integer.parseInt(s[1]);
	}

	public String getBaseTime() {
		return numTime + "/" + baseTime;
	}

	public String getBaseOnly() {
		return String.valueOf(baseTime);
	}

	public void setBaseOnly(int base) {
		baseTime = base;
	}

	public int getTimeCountOnly() {
		return numTime;
	}

	public void setTimeCountOnly(int value) {
		numTime = value;
	}

	public int getMeasureTick() {
		return (getTimeCountOnly() * getBeatTick());
	}

	public int getBeatTick() {
		try {
			return MMLTicks.getTick(getBaseOnly());
		} catch (UndefinedTickException e) {
			throw new AssertionError();
		}
	}

	public void setUserViewMeasure(int measure) {
		if (measure > MAX_USER_VIEW_MEASURE) measure = MAX_USER_VIEW_MEASURE;
		this.userViewMeasure = measure;
	}

	public int getUserViewMeasure() {
		return this.userViewMeasure;
	}

	public void addTicks(int tickPosition, int tick) {
		for (MMLTrack track : getTrackList()) {
			for (MMLEventList eventList : track.getMMLEventList()) {
				MMLEvent.insertTick(eventList.getMMLNoteEventList(), tickPosition, tick);
			}
		}

		// テンポ
		MMLEvent.insertTick(globalTempoList, tickPosition, tick);

		// マーカー
		MMLEvent.insertTick(markerList, tickPosition, tick);
	}

	public void removeTicks(int tickPosition, int tick) {
		for (MMLTrack track : getTrackList()) {
			for (MMLEventList eventList : track.getMMLEventList()) {
				MMLEvent.removeTick(eventList.getMMLNoteEventList(), tickPosition, tick);
			}
		}

		// テンポ
		MMLEvent.removeTick(globalTempoList, tickPosition, tick);

		// マーカー
		MMLEvent.removeTick(markerList, tickPosition, tick);
	}

	/**
	 * ノートのみのTick長を取得する.
	 * @return
	 */
	public int getTotalTickLength() {
		long tick = 0;
		for (MMLTrack track : trackList) {
			long currentTick = track.getMaxTickLength();
			if (tick < currentTick) {
				tick = currentTick;
			}
		}

		return (int)tick;
	}

	private int maxTickMMLEvent(int tick, List<? extends MMLEvent> list) {
		int ret = tick;
		if (list != null) {
			for (MMLEvent e : list) {
				int v = e.getTickOffset();
				if (ret < v) {
					ret = v;
				}
			}
		}
		return ret;
	}

	/**
	 * テンポ, マーカを含むTick長を取得する.
	 * @return
	 */
	public int getTotalTickLengthWithAll() {
		return maxTickMMLEvent(maxTickMMLEvent(getTotalTickLength(), globalTempoList), markerList);
	}

	/**
	 * @return　(ms)
	 */
	public long getTotalTime() {
		int totalTick = getTotalTickLength();
		return Math.round(MMLTempoConverter.getTimeOnTickOffset(globalTempoList, totalTick));
	}

	public byte[] getObjectState() {
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		new MMLScoreSerializer(this).writeToOutputStream(ostream);
		return ostream.toByteArray();
	}

	public void putObjectState(byte[] objState) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(objState);
			new MMLScoreSerializer(this).parse(bis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<MMLNoteEvent[]> getNoteListOnTickOffset(long tick) {
		ArrayList<MMLNoteEvent[]> noteListArray = new ArrayList<>();
		for (MMLTrack track : this.getTrackList()) {
			int partIndex = 0;
			MMLNoteEvent[] noteList = new MMLNoteEvent[4];
			for (MMLEventList eventList : track.getMMLEventList()) {
				if (partIndex == 3) {
					continue;
				}
				noteList[partIndex] = eventList.searchOnTickOffset(tick);
				partIndex++;
			}
			noteListArray.add(noteList);
		}

		return noteListArray;
	}

	/**
	 * generateした結果が同じであれば, generateした状態のMMLScoreにする.
	 * @param force  trueの場合は旧データとの比較をしない
	 * @return
	 */
	public MMLScore toGeneratedScore(boolean force) {
		try {
			MMLScore score = new MMLScore();
			score.putObjectState( this.getObjectState() );
			score.generateAll();
			if ( force || Arrays.equals(this.getObjectState(), score.getObjectState()) ) {
				return score;
			}
		} catch (UndefinedTickException e) {}
		return this;
	}


	public MMLScore generateOne(int trackIndex) throws UndefinedTickException {
		trackList.get(trackIndex).generate();
		return this;
	}

	public MMLScore generateAll() throws UndefinedTickException {
		exceptionStack.clear();
		trackList.parallelStream().forEach(t -> {
			try {
				t.generate();
			} catch (UndefinedTickException e) {
				exceptionStack.push(e);
			}
		});
		if (!exceptionStack.isEmpty()) {
			throw exceptionStack.pop();
		}
		return this;
	}

	/**
	 * 移調する.
	 * @param transpose
	 */
	public void transpose(int transpose) {
		MabiDLS dls = MabiDLS.getInstance();
		for (MMLTrack track : trackList) {
			// 移調ができる楽器の種類かを確認. 通常の打楽器は不可, シロフォンは可能.
			if (dls.getInstByProgram(track.getProgram()).getType().allowTranspose()) {
				for (MMLEventList eventList : track.getMMLEventList()) {
					for (MMLNoteEvent note : eventList.getMMLNoteEventList()) {
						note.setNote( note.getNote() + transpose );
					}
				}
			}
		}
	}

	/**
	 * 開始位置の設定を行う
	 * 設定することによってOffsetがマイナスになる場合は反映しない
	 * @param startOffset
	 * @return
	 */
	public boolean setStartOffsetAll(int startOffset) {
		if (startOffset < 0) {
			throw new IllegalArgumentException();
		}
		if (startOffset % 6 != 0) {
			throw new IllegalArgumentException();
		}
		if (trackList.isEmpty()) {
			return false;
		}
		MMLTrack firstTrack = trackList.getFirst();
		int oldStartOffset = firstTrack.getCommonStartOffset();
		for (MMLTrack t : trackList) {
			if (t.getCommonStartOffset() != oldStartOffset) {
				throw new IllegalStateException();
			}
			if ( (startOffset + t.getStartDelta() < 0) || (startOffset + t.getStartSongDelta() < 0) ) {
				return false;
			}
		}

		int delta = startOffset - oldStartOffset;
		// すべてのテンポが移動可能かどうかをチェックする
		for (var t : globalTempoList) {
			if (t.getTickOffset() + delta < 0) {
				return false;
			}
		}
		// すべてのマーカが移動可能かどうかをチェックする
		for (var t : markerList) {
			if (t.getTickOffset() + delta < 0) {
				return false;
			}
		}
		// 反映
		trackList.forEach(t -> t.setStartOffset(startOffset, globalTempoList));
		globalTempoList.forEach(t -> t.setTickOffset(t.getTickOffset() + delta));
		markerList.forEach(t -> t.setTickOffset(t.getTickOffset() + delta));
		return true;
	}

	/**
	 * tickに対する小節表記を取得する
	 * @param tick
	 * @return
	 */
	public String getBarTextTick(int tick) {
		try {
			int sect = MMLTicks.getTick(getBaseOnly());
			int sectBar = sect * getTimeCountOnly();
			int bar = tick / sectBar;
			int barR = (tick % sectBar);
			int c = barR / sect;
			int r = barR % sect;
			return String.format("%d:%02d:%02d", bar, c, r);
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
		return "N/A";
	}

	@Override
	public MMLScore clone() {
		var obj = this.getObjectState();
		var score = new MMLScore();
		score.putObjectState(obj);
		return score;
	}

	public static void main(String[] args) {
		try {
			System.out.println(" --- parse sample.mms ---");
			MMSFile mms = new MMSFile();
			MMLScore score = mms.parse(new FileInputStream("sample.mms"));
			new MMLScoreSerializer(score).writeToOutputStream(System.out);

			System.out.println(" --- parse sample-version1.mmi ---");
			score = new MMLScore();
			new MMLScoreSerializer(score).parse(new FileInputStream("sample-version1.mmi"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

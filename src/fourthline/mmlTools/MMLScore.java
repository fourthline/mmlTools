/*
 * Copyright (C) 2013-2022 たんらる
 */

package fourthline.mmlTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.core.UndefinedTickException;
import fourthline.mmlTools.parser.IMMLFileParser;
import fourthline.mmlTools.parser.MMLParseException;
import fourthline.mmlTools.parser.MMSFile;
import fourthline.mmlTools.parser.SectionContents;
import fourthline.mmlTools.parser.TextParser;


/**
 * Score
 */
public final class MMLScore implements IMMLFileParser {
	private final LinkedList<MMLTrack> trackList = new LinkedList<>();
	private final List<MMLTempoEvent> globalTempoList = new ArrayList<>();
	private final List<Marker> markerList = new ArrayList<>();

	public static final int MAX_TRACK = 24;

	private String title = "";
	private String author = "";
	private int numTime = 4;
	private int baseTime = 4;

	private final Stack<UndefinedTickException> exceptionStack = new Stack<>();

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
			track.setStartOffset(initialStartOffset);
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
		String s[] = baseTime.split("/");
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
		long totalTime = MMLTempoEvent.getTimeOnTickOffset(globalTempoList, totalTick);
		return totalTime;
	}

	private String getTempoObj() {
		if (globalTempoList.size() == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (MMLTempoEvent t : globalTempoList) {
			sb.append(',');
			sb.append(t.toString());
		}
		return sb.substring(1);
	}

	private void putTempoObj(String s) {
		if (s.length() > 0) {
			String l[] = s.split(",");
			globalTempoList.clear();
			for (String str : l) {
				MMLTempoEvent e = MMLTempoEvent.fromString(str);
				if (e != null) {
					e.appendToListElement(globalTempoList);
				}
			}
		}
	}

	public byte[] getObjectState() {
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		writeToOutputStream(ostream);
		return ostream.toByteArray();
	}

	public void putObjectState(byte objState[]) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(objState);
			parse(bis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeToOutputStream(OutputStream outputStream) {
		try {
			PrintStream stream = new PrintStream(outputStream, false, "UTF-8");

			stream.println("[mml-score]");
			stream.println("version=1");
			stream.println("title="+getTitle());
			stream.println("author="+getAuthor());
			stream.println("time="+getBaseTime());
			stream.println("tempo="+getTempoObj());
			if (getStartOffsetAll() > 0) {
				stream.println("startOffset="+getStartOffsetAll());
			}

			for (MMLTrack track : trackList) {
				// インスタンス時に先に指定したいので、オフセットたちは先に出力する
				if (track.getStartDelta() != 0) {
					stream.println("startDelta="+track.getStartDelta());
				}
				if (track.getStartSongDelta() != 0) {
					stream.println("startSongDelta="+track.getStartSongDelta());
				}
				//　本体
				stream.println("mml-track="+track.getOriginalMML());
				stream.println("name="+track.getTrackName());
				stream.println("program="+track.getProgram());
				stream.println("songProgram="+track.getSongProgram());
				stream.println("panpot="+track.getPanpot());
				stream.println("visible="+track.isVisible());
				if (track.getAttackDelayCorrect() != 0) {
					stream.println("attackDelayCorrect="+track.getAttackDelayCorrect());
				}
				if (track.getAttackSongDelayCorrect() != 0) {
					stream.println("attackSongDelayCorrect="+track.getAttackSongDelayCorrect());
				}
			}

			if (!markerList.isEmpty()) {
				stream.println("[marker]");
				for (Marker marker : markerList) {
					stream.println(marker.toString());
				}
			}

			stream.close();
		} catch (UnsupportedEncodingException e) {}
	}

	public List<MMLNoteEvent[]> getNoteListOnTickOffset(long tick) {
		ArrayList<MMLNoteEvent[]> noteListArray = new ArrayList<>();
		for (MMLTrack track : this.getTrackList()) {
			int partIndex = 0;
			MMLNoteEvent noteList[] = new MMLNoteEvent[4];
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
	 * @return
	 */
	public MMLScore toGeneratedScore() {
		try {
			MMLScore score = new MMLScore();
			score.putObjectState( this.getObjectState() );
			score.generateAll();
			if ( Arrays.equals(this.getObjectState(), score.getObjectState()) ) {
				return score;
			}
		} catch (UndefinedTickException e) {}
		return this;
	}

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		this.globalTempoList.clear();
		this.trackList.clear();
		this.markerList.clear();

		List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream, "UTF-8");
		if (contentsList.isEmpty()) {
			throw(new MMLParseException());
		}
		for (SectionContents section : contentsList) {
			if (section.getName().equals("[mml-score]")) {
				parseMMLScore(section.getContents());
			} else if (section.getName().equals("[marker]")) {
				parseMarker(section.getContents());
			}
		}
		return this;
	}

	/**
	 * MML@ - ; 内の空白文字を削除する.
	 * @param text
	 * @return
	 */
	private String fixMMLspace(String text) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		int start = 0;

		while ( (start = text.indexOf("MML@", index)) >= 0) {
			int end = text.indexOf(';', start);
			sb.append(text.substring(index, start));
			sb.append( text.substring(start, end+1).replaceAll("[ \t\f\r\n]", "") );
			index = end + 1;
		}

		sb.append(text.substring(index));
		return sb.toString();
	}

	private class ParseCache {
		private int startOffset = 0;
		private int startDelta = 0;
		private int startSongDelta = 0;
		private void clear() {
			startOffset = 0;
			startDelta = 0;
			startSongDelta = 0;
		}
	}

	/**
	 * parse [mml-score] contents
	 * @param contents
	 */
	private void parseMMLScore(String contents) {
		var cache = new ParseCache();
		TextParser.text(fixMMLspace(contents))
		// スタートOffsetはMMLTrack生成時に指定するため事前にみる
		.pattern("startOffset=",    t -> cache.startOffset = Integer.parseInt(t))
		.pattern("startDelta=",     t -> cache.startDelta = Integer.parseInt(t))
		.pattern("startSongDelta=", t -> cache.startSongDelta = Integer.parseInt(t))
		// for MMLTrack
		.pattern("mml-track=",   t -> { 
			this.addTrack(new MMLTrack(cache.startOffset, cache.startDelta, cache.startSongDelta).setMML(t));
			cache.clear();
		})
		.pattern("name=",        t -> this.trackList.getLast().setTrackName(t) )
		.pattern("program=",     t -> this.trackList.getLast().setProgram(Integer.parseInt(t)) )
		.pattern("songProgram=", t -> this.trackList.getLast().setSongProgram(Integer.parseInt(t)) )
		.pattern("panpot=",      t -> this.trackList.getLast().setPanpot(Integer.parseInt(t)) )
		.pattern("visible=",     t -> this.trackList.getLast().setVisible(Boolean.parseBoolean(t)) )
		.pattern("attackDelayCorrect=", t -> this.trackList.getLast().setAttackDelayCorrect(Integer.parseInt(t)))
		.pattern("attackDelaySongCorrect=", t -> this.trackList.getLast().setAttackSongDelayCorrect(Integer.parseInt(t)))
		.pattern("title=",       this::setTitle )
		.pattern("author=",      this::setAuthor )
		.pattern("time=",        this::setBaseTime )
		.pattern("tempo=",       this::putTempoObj )
		.parse();
	}

	/**
	 * parse [marker] contents
	 * @param contents
	 */
	private void parseMarker(String contents) {
		markerList.clear();
		for (String s : contents.split("\n")) {
			// <tickOffset>=<name>
			int index = s.indexOf('=');
			if (index > 0) {
				String tickString = s.substring(0, index);
				String name = s.substring(index+1);
				markerList.add( new Marker(name, Integer.parseInt(tickString)) );
			}
		}
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
		trackList.forEach(t -> t.setStartOffset(startOffset));
		globalTempoList.forEach(t -> t.setTickOffset(t.getTickOffset() + delta));
		markerList.forEach(t -> t.setTickOffset(t.getTickOffset() + delta));
		return true;
	}

	private int getStartOffsetAll() {
		if (trackList.size() > 0) {
			return trackList.getFirst().getCommonStartOffset();
		}
		return 0;
	}

	/**
	 * tickに対する小節表記を取得する
	 * @param tick
	 * @return
	 */
	public String getBarTextTick(int tick) {
		StringBuilder sb = new StringBuilder();
		try {
			int sect = MMLTicks.getTick(getBaseOnly());
			int sectBar = sect * getTimeCountOnly();
			int bar = tick / sectBar;
			int barR = (tick % sectBar);
			int c = barR / sect;
			int r = barR % sect;
			sb.append(bar).append(':').append(c).append(':').append(r);			
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static void main(String args[]) {
		try {
			System.out.println(" --- parse sample.mms ---");
			MMSFile mms = new MMSFile();
			MMLScore score = mms.parse(new FileInputStream("sample.mms"));
			score.writeToOutputStream(System.out);

			System.out.println(" --- parse sample-version1.mmi ---");
			score = new MMLScore();
			score.parse(new FileInputStream("sample-version1.mmi"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

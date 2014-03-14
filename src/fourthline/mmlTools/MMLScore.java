/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import fourthline.mmlTools.parser.IMMLFileParser;
import fourthline.mmlTools.parser.MMLParseException;
import fourthline.mmlTools.parser.MMSFile;


/**
 *
 */
public class MMLScore implements IMMLFileParser {
	private List<MMLTrack> trackList = new ArrayList<MMLTrack>();
	private List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();

	private static final int MAX_TRACK = 8;

	private String title = "";
	private String author = "";
	private String baseTime = "4/4";

	/**
	 * 新たにトラックを追加します.
	 * @param track
	 * @return トラック数の上限を超えていて、追加できないときは -1. 追加できた場合は、追加したindex値を返します(0以上).
	 */
	public int addTrack(MMLTrack track) {
		if (trackList.size() >= MAX_TRACK) {
			return -1;
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
	public void removeTrack(int index) {
		trackList.remove(index);
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
	public MMLTrack[] getTrackList() {
		MMLTrack list[] = new MMLTrack[trackList.size()];
		return trackList.toArray(list);
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
		this.baseTime = baseTime;
	}

	public String getBaseTime() {
		return this.baseTime;
	}

	public String getBaseOnly() {
		String s[] = this.baseTime.split("/");
		return s[1];
	}

	public int getTimeCountOnly() {
		String s[] = this.baseTime.split("/");
		return Integer.parseInt(s[0]);
	}

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

	public void writeToOutputStream(OutputStream outputStreame) {
		try {
			PrintStream stream = new PrintStream(outputStreame, false, "UTF-8");

			stream.println("[mml-score]");
			stream.println("version=1");
			stream.println("title="+getTitle());
			stream.println("author="+getAuthor());
			stream.println("time="+getBaseTime());

			for (MMLTrack track : trackList) {
				stream.println("mml-track="+track.getMMLString(false, false));
				stream.println("name="+track.getTrackName());
				stream.println("program="+track.getProgram());
				stream.println("songProgram="+track.getSongProgram());
				stream.println("panpot="+track.getPanpot());
			}

			stream.close();
		} catch (UnsupportedEncodingException e) {}
	}

	@Override
	public MMLScore parse(InputStream istream) throws MMLParseException {
		BufferedReader reader = null;
		this.globalTempoList.clear();
		this.trackList.clear();

		try {
			InputStreamReader isReader = new InputStreamReader(istream, "UTF-8");
			reader = new BufferedReader(isReader);

			String s;
			s = reader.readLine();
			/* ヘッダチェック */
			if (!s.equals("[mml-score]")) {
				throw(new MMLParseException());
			}
			/* バージョン */
			if ( !(s = reader.readLine()).startsWith("version=") ) {
				throw(new MMLParseException());
			}

			// mml-track
			MMLTrack track = null;
			while ( (s = reader.readLine()) != null ) {
				if ( s.startsWith("mml-track=") ) {
					track = new MMLTrack(s.substring("mml-track=".length()));
					this.addTrack(track);
				} else if ( s.startsWith("name=") ) {
					track.setTrackName(s.substring("name=".length()));
				} else if ( s.startsWith("program=") ) {
					track.setProgram(Integer.parseInt(s.substring("program=".length())));
				} else if ( s.startsWith("songProgram=") ) {
					track.setSongProgram(Integer.parseInt(s.substring("songProgram=".length())));
				} else if ( s.startsWith("panpot=") ) {
					track.setPanpot(Integer.parseInt(s.substring("panpot=".length())));
				} else if ( s.startsWith("title=") ) {
					/* 曲名 */
					setTitle(s.substring("title=".length()));
				} else if ( s.startsWith("author=") ) {
					/* 作者 */
					setAuthor(s.substring("author=".length()));
				} else if ( s.startsWith("time=") ) {
					/* 拍子 */
					setBaseTime(s.substring("time=".length()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}
		return this;
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

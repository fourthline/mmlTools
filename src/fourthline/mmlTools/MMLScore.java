/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class MMLScore {
	private ArrayList<MMLTrack> trackList = new ArrayList<MMLTrack>();
	private List<MMLTempoEvent> globalTempoList = new ArrayList<MMLTempoEvent>();

	private static final int MAX_TRACK = 8;

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
		byte objState[] = null;

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream ostream = new ObjectOutputStream(bos);
			ostream.writeObject(this.trackList);
			ostream.close();
			objState = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return objState;
	}

	public void putObjectState(byte objState[]) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(objState);
			ObjectInputStream istream = new ObjectInputStream(bis);
			this.trackList = (ArrayList<MMLTrack>) istream.readObject();
			istream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

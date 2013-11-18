/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import fourthline.mmlTools.core.MMLTools;

public class MMLTrack extends MMLTools {
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

	/**
	 * トラックに含まれるすべてのMMLEventListを1つのMIDIトラックに変換します.
	 * @param track
	 * @param channel
	 * @throws InvalidMidiDataException
	 */
	public void convertMidiTrack(Track track, int channel) throws InvalidMidiDataException {
		ShortMessage pcMessage = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 
				channel,
				program,
				0);
		track.add(new MidiEvent(pcMessage, 0));

		for (int i = 0; i < mmlParts.size(); i++) {
			mmlParts.get(i).convertMidiTrack(track, channel);
		}
	}

}

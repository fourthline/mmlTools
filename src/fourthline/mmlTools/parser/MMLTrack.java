/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import fourthline.mmlTools.core.MMLTools;

public class MMLTrack extends MMLTools {
	private List<List<MMLEvent>> mmlParts;
	private int program = 0;
	private String name;
	private int panpot = 64;
	
	public MMLTrack(String mml) {
		super(mml);

		mmlParse();
	}
	
	public MMLTrack(String mml1, String mml2, String mml3) {
		super(mml1, mml2, mml3);
		
		mmlParse();
	}
	private void mmlParse() {
		mmlParts = new ArrayList<List<MMLEvent>>();
		
		MMLEventParser parser = new MMLEventParser("");
		mmlParts.add( parser.parseMML(this.getMelody()) );
		parser = new MMLEventParser("");
		mmlParts.add( parser.parseMML(this.getChord1()) );
		parser = new MMLEventParser("");
		mmlParts.add( parser.parseMML(this.getChord2()) );
	}


	public void setProgram(int program) {
		this.program = program;
	}

	public int getProgram() {
		return this.program;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
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

	private int convertVelocityMML2Midi(int mml_velocity) {
		return (mml_velocity * 8);
	}
	private int convertNoteMML2Midi(int mml_note) {
		return (mml_note + 12);
	}

	public void convertMidiTrack(Track track, int channel) throws InvalidMidiDataException {
		ShortMessage message = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 
				channel,
				program,
				0);
		track.add(new MidiEvent(message, 0));

		/* TODO: ctrl 10 パンポット */
		message = new ShortMessage(ShortMessage.CONTROL_CHANGE, 
				channel,
				10,
				panpot);
		track.add(new MidiEvent(message, 0));

		convertMidiTrack_part(track, channel, 0);
		convertMidiTrack_part(track, channel, 1);
		convertMidiTrack_part(track, channel, 2);
	}

	protected void convertMidiTrack_part(Track track, int channel, int index) throws InvalidMidiDataException {
		int totalTick = 0;
		int velocity = 8;

		List<MMLEvent> part = mmlParts.get(index);
		boolean isTie = false;

		for ( Iterator<MMLEvent> i = part.iterator(); i.hasNext(); ) {
			MMLEvent event = i.next();
			System.out.println(" <mml-midi> " + event.toString());

			if (event instanceof MMLNoteEvent) {
				int note = ((MMLNoteEvent) event).getNote();
				int tick = ((MMLNoteEvent) event).getTick();

				/* 前の音が tie のときは、onのイベントを作成しない */
				if (!isTie) {
					if (note >= 0) {
						MidiMessage message1 = new ShortMessage(ShortMessage.NOTE_ON, 
								channel,
								convertNoteMML2Midi(note), 
								convertVelocityMML2Midi(velocity));
						track.add(new MidiEvent(message1, totalTick));
					}
				}

				/* 今の音が tie のときは、末offのイベントを作成しない */
				if (((MMLNoteEvent) event).getTie() == true) {
					isTie = true;
				} else {
					isTie = false;
					if (note >= 0) {
						MidiMessage message2 = new ShortMessage(ShortMessage.NOTE_OFF,
								channel, 
								convertNoteMML2Midi(note),
								0);
						track.add(new MidiEvent(message2, totalTick+tick-1));
					}
				}

				totalTick += tick;
			} else if (event instanceof MMLVelocityEvent) {
				velocity = ((MMLVelocityEvent) event).getVelocity();
			} else if (event instanceof MMLTempoEvent) {
				byte tempo[] = ((MMLTempoEvent) event).getMetaData();
				MidiMessage message = new MetaMessage(MMLTempoEvent.META, 
						tempo, tempo.length);
				track.add(new MidiEvent(message, totalTick));
			}
		}
	}

}

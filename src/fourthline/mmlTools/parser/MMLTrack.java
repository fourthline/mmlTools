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

public class MMLTrack {
	private List<List<MMLEvent>> mmlParts;
	private int program = 0;

	public MMLTrack(String mml1, String mml2, String mml3) {
		mmlParts = new ArrayList<List<MMLEvent>>();

		MMLEventParser parser = new MMLEventParser("");
		mmlParts.add( parser.parseMML(mml1) );
		parser = new MMLEventParser("");
		mmlParts.add( parser.parseMML(mml2) );
		parser = new MMLEventParser("");
		mmlParts.add( parser.parseMML(mml3) );
	}


	public void setProgram(int program) {
		this.program = program;
	}

	public int getProgram() {
		return this.program;
	}


	private int convertVelocityMML2Midi(int mml_velocity) {
		return (mml_velocity * 8);
	}
	private int convertNoteMML2Midi(int mml_note) {
		return (mml_note + 12);
	}

	public void convertMidiTrack(Track track) throws InvalidMidiDataException {
		MidiMessage message1 = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 
				program, 0);
		track.add(new MidiEvent(message1, 0));

		convertMidiTrack_part(track, 0);
		convertMidiTrack_part(track, 1);
		convertMidiTrack_part(track, 2);
	}

	protected void convertMidiTrack_part(Track track, int index) throws InvalidMidiDataException {
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
								convertNoteMML2Midi(note),
								0);
						track.add(new MidiEvent(message2, totalTick+tick));
					}
				}

				totalTick += tick;
			} else if (event instanceof MMLVelocityEvent) {
				velocity = ((MMLVelocityEvent) event).getVelocity();
			} else if (event instanceof MMLTempoEvent) {
				byte tempo[] = ((MMLTempoEvent) event).getMetaData();
				MidiMessage message = new MetaMessage(0x51, 
						tempo, tempo.length);
				track.add(new MidiEvent(message, totalTick));
			}
		}
	}

}

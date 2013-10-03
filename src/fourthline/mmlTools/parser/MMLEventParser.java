package fourthline.mmlTools.parser;

import java.util.Enumeration;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTokenizer;
import fourthline.mmlTools.core.MelodyParser;
import fourthline.mmlTools.core.ParserWarn3ML;

public class MMLEventParser extends MelodyParser implements Enumeration<MMLEvent> {

	private MMLTokenizer tokenizer;
	
	private MMLEvent nextEvent = null;
	
	public MMLEventParser(String mml) {
		super(mml);
		
		tokenizer = new MMLTokenizer(mml);
	}

	@Override
	public boolean hasMoreElements() {
		if (nextEvent != null) {
			/* すでに次のイベントが判定済み */
			return true;
		}
		
		/* 次のイベントを判定する */
		while (true) {
			if (tokenizer.hasNext() == false) {
				/* 判定すべきトークンがない */
				nextEvent = null;
				return false;
			}

			String token = tokenizer.next();
			char firstC = token.charAt(0);
			try {
				int tick = this.noteGT(token);
				if (MMLTokenizer.isNote(firstC)) {
					nextEvent = new MMLNoteEvent(this.noteNumber, tick);
					return true;
				} 
			} catch (UndefinedTickException e) {
				e.printStackTrace();
			} catch (ParserWarn3ML e) {
				e.printStackTrace();
			}
			
			if ( (firstC == 'v') || (firstC == 'V') ) {
				nextEvent = new MMLVelocityEvent(Integer.parseInt( token.substring(1) ));
				return true;
			}
			if ( (firstC == 't') || (firstC == 'T') ) {
				nextEvent = new MMLTempoEvent(Integer.parseInt( token.substring(1) ));
				return true;
			}
		}
	}

	@Override
	public MMLEvent nextElement() {
		if (nextEvent == null) {
			hasMoreElements();
		}
		
		MMLEvent returnEvent = nextEvent;
		nextEvent = null;
		return returnEvent;
	}

	private int convertVelocityMML2Midi(int mml_velocity) {
		return (mml_velocity * 8);
	}
	
	public void convertMidiTrack(Track track) throws InvalidMidiDataException {
		int totalTick = 0;
		int velocity = 8;
		int tempo = 120;
		
		MidiMessage message = new MetaMessage(0x51, 
				new byte[] { (byte)tempo }, 1);
		track.add(new MidiEvent(message, totalTick));
		
		while (hasMoreElements()) {
			MMLEvent event = nextElement();
			System.out.println(event.toString());
			
			if (event instanceof MMLNoteEvent) {
				int note = ((MMLNoteEvent) event).getNote();
				int tick = ((MMLNoteEvent) event).getTick();

				MidiMessage message1 = new ShortMessage(ShortMessage.NOTE_ON, 
						note, 
						convertVelocityMML2Midi(velocity));
				track.add(new MidiEvent(message1, totalTick));
				
				/* TODO: '&' の判定どうするの？ */
				MidiMessage message2 = new ShortMessage(ShortMessage.NOTE_OFF, note, 0);
				track.add(new MidiEvent(message2, totalTick+tick));
				
				totalTick += tick;
			} else if (event instanceof MMLVelocityEvent) {
				velocity = ((MMLVelocityEvent) event).getVelocity();
			}
		}
	}
	
	public static void main(String[] args) {
		MMLEventParser parser = new MMLEventParser("c4v10d8t120e16r2");
		
		while (parser.hasMoreElements()) {
			MMLEvent event = parser.nextElement();
			
			System.out.println(event.toString());
		}
	}
}

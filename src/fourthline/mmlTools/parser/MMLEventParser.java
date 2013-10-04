package fourthline.mmlTools.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTokenizer;
import fourthline.mmlTools.core.MelodyParser;
import fourthline.mmlTools.core.ParserWarn3ML;

public class MMLEventParser extends MelodyParser {

	public MMLEventParser(String mml) {
		super(mml);
	}


	public List<MMLEvent> parseMML(String mml) {
		ArrayList<MMLEvent> list = new ArrayList<MMLEvent>();
		
		MMLTokenizer tokenizer = new MMLTokenizer(mml);
		MMLNoteEvent prevNoteEvent = null;
		
		boolean hasTie = false;
		
		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			char firstC = token.charAt(0);
			if ( firstC == '&' ) {
				/* flag ? */
				hasTie = true;
				continue;
			}
			if ( (firstC == 'v') || (firstC == 'V') ) {
				MMLEvent newEvent = new MMLVelocityEvent(Integer.parseInt( token.substring(1) ));
				list.add(newEvent);
				continue;
			}
			if ( (firstC == 't') || (firstC == 'T') ) {
				MMLEvent newEvent = new MMLTempoEvent(Integer.parseInt( token.substring(1) ));
				list.add(newEvent);
				continue;
			}
			try {
				int tick = this.noteGT(token);
				if (MMLTokenizer.isNote(firstC)) {
					/* tie でかつ、同じノートであれば、tieイベントでつなげる */
					if ( (hasTie) && (prevNoteEvent != null) && (prevNoteEvent.getNote() == this.noteNumber)) {
						prevNoteEvent.setTie(true);
					}
					MMLNoteEvent newEvent = new MMLNoteEvent(this.noteNumber, tick);
					list.add(newEvent);
					prevNoteEvent = newEvent;
					hasTie = false;
				} 
			} catch (UndefinedTickException e) {
				e.printStackTrace();
			} catch (ParserWarn3ML e) {
				e.printStackTrace();
			}
			
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		MMLEventParser parser = new MMLEventParser("c4v10d8t120e16r2");
		
		List<MMLEvent> list = parser.parseMML("a&a&b");
		for ( Iterator<MMLEvent> i = list.iterator(); i.hasNext(); ) {
			MMLEvent event = i.next();
			System.out.println(" * " + event.toString());
		}
	}
}

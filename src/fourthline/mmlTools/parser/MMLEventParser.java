/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

import java.util.Iterator;

import fourthline.mmlTools.MMLEvent;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTokenizer;
import fourthline.mmlTools.core.MelodyParser;
import fourthline.mmlTools.core.ParserWarn3ML;

public class MMLEventParser extends MelodyParser implements Iterator<MMLEvent> {

	private MMLTokenizer tokenizer;

	public MMLEventParser(String mml) {
		super(mml);
		tokenizer = new MMLTokenizer(mml);
	}		


	private MMLEvent nextItem = null;

	// MMLパース用
	private boolean hasTie = false;
	private int totalTick = 0;
	private MMLNoteEvent prevNoteEvent = null;
	int volumn = MMLNoteEvent.NO_VEL;

	/**
	 * @return すべてMMLパースが終っているときは、nullを返す.
	 */
	private MMLEvent parseNextEvent() {
		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			char firstC = token.charAt(0);
			if ( firstC == '&' ) {
				hasTie = true;
				continue;
			}
			if ( (firstC == 'v') || (firstC == 'V') ) {
				try {
					volumn = Integer.parseInt( token.substring(1) );
					continue;
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			if ( (firstC == 't') || (firstC == 'T') ) {
				try {
					int tempo = Integer.parseInt( token.substring(1) );
					nextItem = new MMLTempoEvent(tempo, totalTick);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				return nextItem;
			}
			try {
				int tick = this.noteGT(token);
				if (MMLTokenizer.isNote(firstC)) {
					/* tie でかつ、同じノートであれば、前のNoteEventにTickを加算する */
					if ( (hasTie) && (prevNoteEvent != null) && (prevNoteEvent.getNote() == this.noteNumber)) {
						prevNoteEvent.setTick( prevNoteEvent.getTick() + tick);
					} else {
						nextItem = prevNoteEvent;
						prevNoteEvent = new MMLNoteEvent(this.noteNumber, tick, totalTick);
						prevNoteEvent.setVelocity(volumn);
						volumn = MMLNoteEvent.NO_VEL;
					}

					hasTie = false;
					totalTick += tick;
					if (nextItem != null) {
						return nextItem;
					}
				} 
			} catch (UndefinedTickException e) {
				e.printStackTrace();
			} catch (ParserWarn3ML e) {
				e.printStackTrace();
			}

		}

		nextItem = prevNoteEvent;
		prevNoteEvent = null;

		return nextItem;
	}



	@Override
	public boolean hasNext() {
		if (nextItem == null) {
			parseNextEvent();
		}

		if (nextItem == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public MMLEvent next() {
		if (nextItem == null) {
			return parseNextEvent();
		}

		MMLEvent returnEvent = nextItem;
		nextItem = null;

		return returnEvent;
	}

	@Override
	public void remove() {
		tokenizer.remove();
	}
}

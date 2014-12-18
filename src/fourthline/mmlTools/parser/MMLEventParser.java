/*
 * Copyright (C) 2013-2014 たんらる
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
import fourthline.mmlTools.core.TuningBase;

public final class MMLEventParser extends MelodyParser implements Iterator<MMLEvent> {
	private final MMLTokenizer tokenizer;

	public MMLEventParser(String mml) {
		super(mml);
		tokenizer = new MMLTokenizer(mml);
	}

	public static int firstNoteNumber(String mml) {
		MMLEventParser parser = new MMLEventParser(mml);
		while (parser.hasNext()) {
			MMLEvent e = parser.next();
			if (e instanceof MMLNoteEvent) {
				return ((MMLNoteEvent)e).getNote();
			}
		}

		return -1;
	}

	private MMLEvent nextItem = null;

	// MMLパース用
	private boolean hasTie = false;
	private int totalTick = 0;
	private MMLNoteEvent prevNoteEvent = null;
	private int volumn = MMLNoteEvent.INIT_VOL;

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
					int nextVolumn = Integer.parseInt( token.substring(1) );
					if ( (nextVolumn >= 0) && (nextVolumn <= MMLNoteEvent.MAX_VOL) ) {
						volumn = nextVolumn;
					}
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
				} catch (IllegalArgumentException e) {
					continue;
				}

				return nextItem;
			}
			try {
				int tick = this.noteGT(token);
				if (MMLTokenizer.isNote(firstC)) {
					/* tie でかつ、同じノートであれば、前のNoteEventにTickを加算する */
					if ( (hasTie) && (prevNoteEvent != null) && (prevNoteEvent.getNote() == this.noteNumber)) {
						int prevTick = prevNoteEvent.getTick();
						if ( (prevTick == tick) && (TuningBase.getInstance(tick) != null) ) {
							prevNoteEvent.setTuningNote(TuningBase.getInstance(tick));
						}
						prevNoteEvent.setTick( prevTick + tick);
						prevNoteEvent.getIndexOfMMLString()[1] = tokenizer.getIndex()[1];
					} else if (this.noteNumber >= 0) {
						nextItem = prevNoteEvent;
						prevNoteEvent = new MMLNoteEvent(this.noteNumber, tick, totalTick, volumn);
						prevNoteEvent.setIndexOfMMLString(tokenizer.getIndex());
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

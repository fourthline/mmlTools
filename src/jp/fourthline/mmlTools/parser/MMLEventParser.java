/*
 * Copyright (C) 2013-2022 たんらる
 */

package jp.fourthline.mmlTools.parser;

import java.util.Iterator;

import jp.fourthline.mmlTools.MMLEvent;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLTempoEvent;
import jp.fourthline.mmlTools.core.MMLTokenizer;
import jp.fourthline.mmlTools.core.MelodyParser;
import jp.fourthline.mmlTools.core.ParserWarn3ML;
import jp.fourthline.mmlTools.core.TuningBase;
import jp.fourthline.mmlTools.core.UndefinedTickException;

public final class MMLEventParser implements Iterator<MMLEvent> {
	private final MMLTokenizer tokenizer;
	private final MelodyParser parser;

	public MMLEventParser(String mml) {
		this(mml, 0);
	}

	/**
	 * 開始オフセットを指定してパーサーを生成する
	 * @param mml
	 * @param startOffset
	 */
	public MMLEventParser(String mml, int startOffset) {
		this.totalTick = startOffset;
		tokenizer = new MMLTokenizer(mml);
		parser = new MelodyParser(mml);
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
				} catch (IllegalArgumentException e) {
					continue;
				}

				return nextItem;
			}
			try {
				int tick = parser.noteGT(token);
				if (MMLTokenizer.isNote(firstC)) {
					/* tie でかつ、同じノートであれば、前のNoteEventにTickを加算する */
					if ( (hasTie) && (prevNoteEvent != null) && (prevNoteEvent.getNote() == parser.getNoteNumber())) {
						int prevTick = prevNoteEvent.getTick();
						if ( (prevTick == tick) && (TuningBase.getInstance(tick) != null) ) {
							prevNoteEvent.setTuningNote(TuningBase.getInstance(tick));
						}
						prevNoteEvent.setTick( prevTick + tick);
						prevNoteEvent.getIndexOfMMLString()[1] = tokenizer.getIndex()[1];
					} else if (parser.getNoteNumber() >= -1) {
						nextItem = prevNoteEvent;
						prevNoteEvent = new MMLNoteEvent(parser.getNoteNumber(), tick, totalTick, volumn);
						prevNoteEvent.setIndexOfMMLString(tokenizer.getIndex());
					}

					hasTie = false;
					totalTick += tick;
					if (nextItem != null) {
						return nextItem;
					}
				} 
			} catch (UndefinedTickException | ParserWarn3ML e) {
				System.err.println(e.getMessage());
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

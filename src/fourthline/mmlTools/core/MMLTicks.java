/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools.core;


import java.util.HashMap;

import fourthline.mmlTools.UndefinedTickException;


/**
 * 音符の時間変換値
 * @author たんらる
 */
public final class MMLTicks {

	/**
	 * For MML text -> tick
	 */
	private static final HashMap<String, Integer> tickTable = new HashMap<>();

	/**
	 * For tick -> MML text
	 */
	private static final HashMap<Integer, String> tickInvTable = new HashMap<>();


	static {
		add("1.", 576);
		add("2.", 288);
		add("3.", 192);
		add("4.", 144);
		add("5.", 114);
		add("6.", 96);
		add("7.", 81);
		add("8.", 72);
		add("9.", 63);
		add("10.", 57);
		add("11.", 51);
		add("12.", 48);
		add("13.", 43);
		add("14.", 40);
		add("15.", 37);
		add("16.", 36);
		add("17.", 33);
		add("18.", 31);
		add("19.", 30);
		add("20.", 28);
		add("21.", 27);
		add("22.", 25);
		add("23.", 24);
		add("24.", 24);
		add("25.", 22);
		add("26.", 21);
		add("27.", 21);
		add("28.", 19);
		add("29.", 19);
		add("30.", 18);
		add("31.", 18);
		add("32.", 18);
		add("33.", 16);
		add("34.", 16);
		add("35.", 15);
		add("36.", 15);
		add("37.", 15);
		add("38.", 15);
		add("39.", 13);
		add("40.", 13);
		add("41.", 13);
		add("42.", 13);
		add("43.", 12);
		add("44.", 12);
		add("45.", 12);
		add("46.", 12);
		add("47.", 12);
		add("48.", 12);
		add("49.", 10);
		add("50.", 10);
		add("51.", 10);
		add("52.", 10);
		add("53.", 10);
		add("54.", 10);
		add("55.", 9);
		add("56.", 9);
		add("57.", 9);
		add("58.", 9);
		add("59.", 9);
		add("60.", 9);
		add("61.", 9);
		add("62.", 9);
		add("63.", 9);
		add("64.", 9);

		add("1", 384);
		add("2", 192);
		add("3", 128);
		add("4", 96);
		add("5", 76);
		add("6", 64);
		add("7", 54);
		add("8", 48);
		add("9", 42);
		add("10", 38);
		add("11", 34);
		add("12", 32);
		add("13", 29);
		add("14", 27);
		add("15", 25);
		add("16", 24);
		add("17", 22);
		add("18", 21);
		add("19", 20);
		add("20", 19);
		add("21", 18);
		add("22", 17);
		add("23", 16);
		add("24", 16);
		add("25", 15);
		add("26", 14);
		add("27", 14);
		add("28", 13);
		add("29", 13);
		add("30", 12);
		add("31", 12);
		add("32", 12);
		add("33", 11);
		add("34", 11);
		add("35", 10);
		add("36", 10);
		add("37", 10);
		add("38", 10);
		add("39", 9);
		add("40", 9);
		add("41", 9);
		add("42", 9);
		add("43", 8);
		add("44", 8);
		add("45", 8);
		add("46", 8);
		add("47", 8);
		add("48", 8);
		add("49", 7);
		add("50", 7);
		add("51", 7);
		add("52", 7);
		add("53", 7);
		add("54", 7);
		add("55", 6);
		add("56", 6);
		add("57", 6);
		add("58", 6);
		add("59", 6);
		add("60", 6);
		add("61", 6);
		add("62", 6);
		add("63", 6);
		add("64", 6);
	}

	static private void add(String s, Integer value) {
		tickTable.put(s, value);

		String invText = tickInvTable.get(value);
		if ( (invText == null) || (s.length() <= invText.length()) ) {
			tickInvTable.put(value, s);
		}
	}

	static public int getTick(String gt) throws UndefinedTickException {
		String str = gt;
		while (!tickTable.containsKey(str)) {
			int len = str.length();
			if (len > 1) {
				str = str.substring(0, len - 1);
			} else {
				throw new UndefinedTickException(gt);
			}
		}

		return tickTable.get(str);
	}

	static public int minimumTick() {
		Integer minimum = null;
		for (Integer i : tickTable.values()) {
			if ( (minimum == null) || (i < minimum) ) {
				minimum = i;
			}
		}

		return minimum;
	}


	private String noteName;
	int tick;
	boolean needTie;

	/**
	 * tick長をMML文字列に変換します.
	 * @param noteName
	 * @param tick
	 */
	public MMLTicks(String noteName, int tick) {
		this(noteName, tick, true);
	}

	/**
	 * tick長をMML文字列に変換します.
	 * @param noteName
	 * @param tick
	 * @param needTie noteNameを連結するときに tie が必要かどうかを指定します. 休符 or 音量ゼロのときは, falseを指定してください.
	 */
	public MMLTicks(String noteName, int tick, boolean needTie) {
		this.noteName = noteName;
		this.tick = tick;
		this.needTie = needTie;
	}

	private String mmlNotePart(String phoneticString) {
		StringBuilder sb = new StringBuilder();
		if (needTie) {
			sb.append('&');
		}
		sb.append(noteName).append(phoneticString);

		return sb.toString();
	}

	/**
	 * noteNameとtickをMMLの文字列に変換します.
	 * needTieがtrueのときは、'&'による連結を行います.
	 * @throws UndefinedTickException
	 */
	public String toMMLText() throws UndefinedTickException {
		int remTick = tick;
		StringBuilder sb = new StringBuilder();

		// "1."
		int mTick = getTick("1.");
		int tick1 = getTick("1");
		while (remTick > (tick1*2)) {
			sb.append( mmlNotePart("1.") );
			remTick -= mTick;
		}

		// 1~64の分割
		for (int base = 1; base <= 64; base *= 2) {
			int baseTick = getTick(""+base);
			if (tickInvTable.containsKey(remTick)) {
				sb.append( mmlNotePart(tickInvTable.get(remTick)) );
				remTick = 0;
				break;
			}
			while (remTick >= baseTick) {
				sb.append( mmlNotePart(""+base) );
				remTick -= baseTick;
			}
		}
		if (remTick > 0) {
			throw new UndefinedTickException(remTick + "/" + tick);
		}

		if (needTie) {
			return sb.substring(1);
		} else {
			return sb.toString();
		}
	}

	/**
	 * L64を使って変換します.　（調律用）
	 * @return
	 * @throws UndefinedTickException
	 */
	public String toMMLTextByL64() throws UndefinedTickException {
		int remTick = tick;
		StringBuilder sb = new StringBuilder();

		int base = 64;
		int baseTick = getTick(""+base);
		while (remTick >= baseTick) {
			sb.append( mmlNotePart(""+base) );
			remTick -= baseTick;
		}

		if (needTie) {
			return sb.substring(1);
		} else {
			return sb.toString();
		}
	}
}

/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.core;


import java.util.HashMap;

import fourthline.mmlTools.UndefinedTickException;


/**
 * 音符の時間変換値
 * @author たんらる
 */
public class MMLTicks {
//	private static String resourceName = "tick";
	private static HashMap<String, Integer> tickTable = new HashMap<String, Integer>();
	
	
	static {
		tickTable.put("1", 384);
		tickTable.put("2", 192);
		tickTable.put("3", 128);
		tickTable.put("4", 96);
		tickTable.put("5", 76);
		tickTable.put("6", 64);
		tickTable.put("7", 54);
		tickTable.put("8", 48);
		tickTable.put("9", 42);
		tickTable.put("10", 38);
		tickTable.put("11", 34);
		tickTable.put("12", 32);
		tickTable.put("13", 29);
		tickTable.put("14", 27);
		tickTable.put("15", 25);
		tickTable.put("16", 24);
		tickTable.put("17", 22);
		tickTable.put("18", 21);
		tickTable.put("19", 20);
		tickTable.put("20", 19);
		tickTable.put("21", 18);
		tickTable.put("22", 17);
		tickTable.put("23", 16);
		tickTable.put("24", 16);
		tickTable.put("25", 15);
		tickTable.put("26", 14);
		tickTable.put("27", 14);
		tickTable.put("28", 13);
		tickTable.put("29", 13);
		tickTable.put("30", 12);
		tickTable.put("31", 12);
		tickTable.put("32", 12);
		tickTable.put("33", 11);
		tickTable.put("34", 11);
		tickTable.put("35", 10);
		tickTable.put("36", 10);
		tickTable.put("37", 10);
		tickTable.put("38", 10);
		tickTable.put("39", 9);
		tickTable.put("40", 9);
		tickTable.put("41", 9);
		tickTable.put("42", 9);
		tickTable.put("43", 8);
		tickTable.put("44", 8);
		tickTable.put("45", 8);
		tickTable.put("46", 8);
		tickTable.put("47", 8);
		tickTable.put("48", 8);
		tickTable.put("49", 7);
		tickTable.put("50", 7);
		tickTable.put("51", 7);
		tickTable.put("52", 7);
		tickTable.put("53", 7);
		tickTable.put("54", 7);
		tickTable.put("55", 6);
		tickTable.put("56", 6);
		tickTable.put("57", 6);
		tickTable.put("58", 6);
		tickTable.put("59", 6);
		tickTable.put("60", 6);
		tickTable.put("61", 6);
		tickTable.put("62", 6);
		tickTable.put("63", 6);
		tickTable.put("64", 6);

		tickTable.put("1.", 576);
		tickTable.put("2.", 288);
		tickTable.put("3.", 192);
		tickTable.put("4.", 144);
		tickTable.put("5.", 114);
		tickTable.put("6.", 96);
		tickTable.put("7.", 81);
		tickTable.put("8.", 72);
		tickTable.put("9.", 63);
		tickTable.put("10.", 57);
		tickTable.put("11.", 51);
		tickTable.put("12.", 48);
		tickTable.put("13.", 43);
		tickTable.put("14.", 40);
		tickTable.put("15.", 37);
		tickTable.put("16.", 36);
		tickTable.put("17.", 33);
		tickTable.put("18.", 31);
		tickTable.put("19.", 30);
		tickTable.put("20.", 28);
		tickTable.put("21.", 27);
		tickTable.put("22.", 25);
		tickTable.put("23.", 24);
		tickTable.put("24.", 24);
		tickTable.put("25.", 22);
		tickTable.put("26.", 21);
		tickTable.put("27.", 21);
		tickTable.put("28.", 19);
		tickTable.put("29.", 19);
		tickTable.put("30.", 18);
		tickTable.put("31.", 18);
		tickTable.put("32.", 18);
		tickTable.put("33.", 16);
		tickTable.put("34.", 16);
		tickTable.put("35.", 15);
		tickTable.put("36.", 15);
		tickTable.put("37.", 15);
		tickTable.put("38.", 15);
		tickTable.put("39.", 13);
		tickTable.put("40.", 13);
		tickTable.put("41.", 13);
		tickTable.put("42.", 13);
		tickTable.put("43.", 12);
		tickTable.put("44.", 12);
		tickTable.put("45.", 12);
		tickTable.put("46.", 12);
		tickTable.put("47.", 12);
		tickTable.put("48.", 12);
		tickTable.put("49.", 10);
		tickTable.put("50.", 10);
		tickTable.put("51.", 10);
		tickTable.put("52.", 10);
		tickTable.put("53.", 10);
		tickTable.put("54.", 10);
		tickTable.put("55.", 9);
		tickTable.put("56.", 9);
		tickTable.put("57.", 9);
		tickTable.put("58.", 9);
		tickTable.put("59.", 9);
		tickTable.put("60.", 9);
		tickTable.put("61.", 9);
		tickTable.put("62.", 9);
		tickTable.put("63.", 9);
		tickTable.put("64.", 9);
	}
	
	
	static public int getTick(String gt) throws UndefinedTickException {
		try {
			int tick = tickTable.get(gt);
			return tick;
		} catch (NullPointerException e) {
			throw new UndefinedTickException(gt);
		}
	}
}

/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.midi;

import fourthline.mmlTools.parser.MMLEventParser;

public class InstClass {
	private String name;
	private int bank;
	private int program;
	private int lowerNote = 0;
	private int upperNote = 1024;
	private String type;

	public InstClass(String name, int bank, int program) {
		String str[] = name.split(",");
		this.name = str[0];

		if (str.length > 1) {
			this.type = str[1];
		}
		if (str.length > 2) {
			this.lowerNote = MMLEventParser.firstNoteNumber(str[2]);
		}
		if (str.length > 3) {
			this.upperNote = MMLEventParser.firstNoteNumber(str[3]);
		}

		this.bank = bank;
		this.program = program;
	}

	public String toString() {
		return this.name;
	}

	public int getBank() {
		return this.bank;
	}

	public int getProgram() {
		return this.program;
	}

	public int getLowerNote() {
		return this.lowerNote;
	}

	public int getUpperNote() {
		return this.upperNote;
	}
}

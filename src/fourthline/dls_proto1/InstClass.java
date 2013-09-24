/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.dls_proto1;

public class InstClass {
	private String name;
	private int bank;
	private int program;

	public InstClass(String name, int bank, int program) {
		this.name = name;
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
}

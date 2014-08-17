/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.midi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

import fourthline.mmlTools.core.ResourceLoader;
import fourthline.mmlTools.parser.MMLEventParser;

public final class InstClass {
	private final String name;
	private final int bank;
	private final int program;
	private final int lowerNote;
	private final int upperNote;
	private final InstType type;
	private final Instrument inst;

	private static final String RESOURCE_NAME = "instrument";
	private static final ResourceBundle instResource = ResourceBundle.getBundle(RESOURCE_NAME, new ResourceLoader());

	public InstClass(String name, int bank, int program, Instrument inst) {
		String str[] = name.split(",");
		this.name = str[0];
		this.inst = inst;

		if (str.length > 1) {
			this.type = InstType.getInstType(str[1]);
		} else {
			this.type = InstType.NORMAL;
		}
		if (str.length > 2) {
			this.lowerNote = MMLEventParser.firstNoteNumber(str[2]);
		} else {
			this.lowerNote = 0;
		}
		if (str.length > 3) {
			this.upperNote = MMLEventParser.firstNoteNumber(str[3]);
		} else {
			this.upperNote = 1024;
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

	public InstType getType() {
		return this.type;
	}

	public Instrument getInstrument() {
		return this.inst;
	}

	public static InstClass[] filterInstArray(InstClass[] array, EnumSet<InstType> e) {
		ArrayList<InstClass> resultArray = new ArrayList<>();
		for (InstClass inst : array) {
			if (e.contains(inst.type)) {
				resultArray.add(inst);
			}
		}
		return resultArray.toArray(new InstClass[resultArray.size()]);
	}

	public static InstClass searchInstAtProgram(InstClass insts[], int program) {
		for (InstClass inst : insts) {
			if (inst.getProgram() == program) {
				return inst;
			}
		}

		return null;
	}

	/**
	 * プログラム番号上で有効なパート情報を取得する.
	 * @param program
	 * @return
	 */
	public static boolean[] getEnablePartByProgram(int program) {
		InstClass insts[] = MabiDLS.getInstance().getInsts();
		InstClass inst = InstClass.searchInstAtProgram(insts, program);
		if (inst != null) {
			return inst.getType().getEnablePart();
		}
		return InstType.NONE.getEnablePart();
	}

	/**
	 * プログラム番号上で有効な最初のパート番号を取得する.
	 * @param program
	 * @return パート番号
	 */
	public static int getFirstPartNumberOnProgram(int program) {
		boolean b[] = getEnablePartByProgram(program);

		for (int i = 0; i < b.length; i++) {
			if (b[i]) {
				return i;
			}
		}

		throw new AssertionError("Invalid Inst Part Number.");
	}

	private static String instName(Instrument inst) {
		try {
			String name = instResource.getString(""+inst.getPatch().getProgram());
			return name;
		} catch (MissingResourceException e) {
			return null;
		}
	}

	public static InstClass[] loadDLS(File dlsFile) throws InvalidMidiDataException, IOException {
		Soundbank sb = MidiSystem.getSoundbank(dlsFile);

		ArrayList<InstClass> instArray = new ArrayList<>();
		for (Instrument inst : sb.getInstruments()) {
			String name = instName(inst);
			String originalName = inst.getName();
			int bank = inst.getPatch().getBank();
			int program = inst.getPatch().getProgram();
			System.out.printf("%d=%s \"%s\"\n", program,  originalName, name);
			if (name != null) {
				name = ""+program+": "+name;
				instArray.add(new InstClass( name,
						bank,
						program,
						inst));
			}
		}

		InstClass insts[] = new InstClass[instArray.size()];
		insts = instArray.toArray(insts);
		return insts;
	}
}

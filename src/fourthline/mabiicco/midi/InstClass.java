/*
 * Copyright (C) 2013-2017 たんらる
 */

package fourthline.mabiicco.midi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

import com.sun.media.sound.DLSInstrument;
import com.sun.media.sound.DLSRegion;

import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mmlTools.core.ResourceLoader;

/**
 * 楽器に関する情報を扱います. (instrument.propertiesに対応)
 */
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
	public static boolean debug = false;

	public InstClass(String name, int bank, int program, Instrument inst) {
		String str[] = name.split(",");
		this.name = str[0];
		this.inst = inst;

		if (str.length > 1) {
			this.type = InstType.getInstType(str[1]);
		} else {
			this.type = InstType.NORMAL;
		}
		KeyRegion region = regionFromTo(inst);
		this.lowerNote = region.from;
		this.upperNote = region.to;

		this.bank = bank;
		this.program = program;
	}

	private static final class KeyRegion {
		final int from;
		final int to;
		KeyRegion() {
			this.from = 0;
			this.to = 1024;
		}
		KeyRegion(int from, int to) {
			this.from = from;
			this.to = to;
		}
	}

	private KeyRegion regionFromTo(Instrument inst) {
		if (inst == null) {
			return new KeyRegion();
		}

		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		if (inst instanceof DLSInstrument) {
			DLSInstrument dlsinst = (DLSInstrument) inst;
			for (DLSRegion reg : dlsinst.getRegions()) {
				min = Math.min(min, reg.getKeyfrom());
				max = Math.max(max, reg.getKeyto());
			}
		}
		if ( (max == Integer.MIN_VALUE) || (min == Integer.MAX_VALUE) ) {
			return new KeyRegion();
		}
		min -= 12;
		max -= 12;
		return new KeyRegion(min, max);
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof InstClass) {
			InstClass obj = (InstClass) o;
			return (bank == obj.bank) && (program == obj.program);
		}
		return false;
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

	/**
	 * プログラム番号上で有効なパート情報を取得する.
	 * @param program
	 * @return
	 */
	public static boolean[] getEnablePartByProgram(int program) {
		InstClass inst = MabiDLS.getInstance().getInstByProgram(program);
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

	public static List<InstClass> loadDLS(File dlsFile) throws InvalidMidiDataException, IOException {
		Soundbank sb = null;
		try {
			sb = MidiSystem.getSoundbank(dlsFile);
		} catch (Exception e) {
			MabiIccoProperties.getInstance().setDlsFile(null);
			throw new IOException("loadDLS: "+dlsFile.getName());
		}

		ArrayList<InstClass> instArray = new ArrayList<>();
		for (Instrument inst : sb.getInstruments()) {
			String name = instName(inst);
			String originalName = inst.getName();
			int bank = inst.getPatch().getBank();
			int program = inst.getPatch().getProgram();
			System.out.printf("%d,%d=%s \"%s\"\n", bank, program, originalName, name);
			if (name != null) {
				name = ""+program+": "+name;
				instArray.add(new InstClass( name,
						bank,
						program,
						inst));
			}

			if ( (debug) && (inst instanceof DLSInstrument) ) {
				DLSInstrument dlsinst = (DLSInstrument) inst;
				for (DLSRegion reg : dlsinst.getRegions()) {
					double attenuation = reg.getSample().getSampleoptions().getAttenuation()/655360.0;
					System.out.print(" >> "+reg.getSample().getName()+" ");
					System.out.print(attenuation+" ");
					System.out.print(Math.pow(10.0, attenuation/20.0)+" ");
					System.out.print(reg.getKeyfrom()+" ");
					System.out.print(reg.getKeyto()+" ");
					System.out.print(reg.getVelfrom()+" ");
					System.out.println(reg.getVelto()+" ");
				}
			}
		}

		return instArray;
	}
}

/*
 * Copyright (C) 2013-2025 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;

import com.sun.media.sound.DLSInstrument;
import com.sun.media.sound.DLSRegion;

import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mmlTools.core.ResourceLoader;
import jp.fourthline.mmlTools.parser.MMLEventParser;

/**
 * 楽器に関する情報を扱います. (instrument.propertiesに対応)
 */
public final class InstClass {
	private final String name;
	private final int program;
	private final int lowerNote;
	private final int upperNote;
	private final InstType type;
	private final Instrument inst;
	private final Options options;

	private static ResourceBundle instResource = null;
	static {
		try {
			String instName = MabiIccoProperties.getInstance().soundEnv.get().getInstrumentName();
			if (instName != null) {
				instResource = ResourceBundle.getBundle(instName, new ResourceLoader());
			}
		} catch (Exception e) {}
	}

	public static boolean debug = false;

	public static int DRUM = 0x100;
	private static int logicalProgramNum(Instrument inst) {
		if (inst == null) {
			throw new IllegalArgumentException("inst is null");
		}
		int program = inst.getPatch().getProgram();
		if (inst.toString().startsWith("Drumkit: ")) {
			program += DRUM;
		}

		int bank = inst.getPatch().getBank();
		return logicalProgramNum(bank, program);
	}

	public static int logicalProgramNum(int bank, int program) {
		bank &= 0x7f;
		return (bank << 9) + program;
	}

	public static int toMidiProram(int program) {
		return program & 0x7f;
	}

	public static int toMidiBank(int program) {
		return (program >> 9) & 0x7f;
	}

	public InstClass(String name, int bank, int program, Instrument inst) {
		String[] str = (name != null) ? name.split(",") : new String[] { bank+","+program };
		this.inst = inst;

		if (str.length > 1) {
			this.type = InstType.getInstType(str[1]);
		} else {
			this.type = InstType.NORMAL;
		}
		KeyRegion region = regionFromTo(inst);
		if (str.length > 2) {
			region.from = Math.max(region.from, MMLEventParser.firstNoteNumber(str[2]));
		}
		if (str.length > 3) {
			region.to = Math.min(region.to, MMLEventParser.firstNoteNumber(str[3]));
		}
		this.lowerNote = region.from;
		this.upperNote = region.to;

		this.program = (inst != null) ? logicalProgramNum(inst) : program;
		this.name = (name != null) && (program >= 0) ? toProgramText(this.program) + ": " + str[0] : str[0];

		this.options = new Options(inst);
	}

	private static final class KeyRegion {
		int from;
		int to;
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
		if (inst instanceof DLSInstrument dlsinst) {
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
			return (program == obj.program);
		}
		return false;
	}

	public int getProgram() {
		return this.program;
	}

	public boolean checkPitchRange(int note) {
		return (note >= lowerNote) && (note <= upperNote);
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

	public String getMidiName() {
		return "Instrument: " + inst.getName() + " bank #" + toMidiBank(program) + " preset #" + toMidiProram(program);
	}

	private final static class ExcludeRegion {
		private static final ExcludeRegion[] values = {
				new ExcludeRegion("Pipe_c5", 60 ,71, 60),
				new ExcludeRegion("Pipe_c5", 48 ,59, 48),
				new ExcludeRegion("Pipe_c5", 72 ,83, 60),
		};
		private static boolean isExlcude(DLSRegion region) {
			for (ExcludeRegion v : values) {
				if (v.name.equals(region.getSample().getName())
						&& (v.from == region.getKeyfrom())
						&& (v.to == region.getKeyto())
						&& (v.unitynote == region.getSampleoptions().getUnitynote())) {
					return true;
				}
			}
			return false;
		}
		private final String name;
		private final int from;
		private final int to;
		private final int unitynote;
		private ExcludeRegion(String name, int from, int to, int unitynote) {
			this.name = name;
			this.from = from;
			this.to = to;
			this.unitynote = unitynote;
		}
	}

	/**
	 * DLSの情報に基づくOptions
	 */
	public static final class Options {
		public final static int OPTION_NUM = 256;
		private final double[] attentionList;
		private final boolean[] overlapList;
		private final boolean[] validList;

		private Options(Instrument instrument) {
			if (instrument instanceof DLSInstrument dlsinst) {
				attentionList = new double[ OPTION_NUM ];
				overlapList = new boolean[ OPTION_NUM ];
				validList = new boolean[ OPTION_NUM ];
				for (int i = 0; i < OPTION_NUM; i++) {
					attentionList[i] = 0.0;
					overlapList[i] = false;
					validList[i] = false;
				}

				int min = OPTION_NUM;
				int max = 0;
				for (DLSRegion region : dlsinst.getRegions()) {
					min = Math.min(min, region.getKeyfrom());
					max = Math.max(max, region.getKeyto());
					double v = region.getSampleoptions().getAttenuation() / 655360.0;
					boolean overlap = region.getOptions() == 1;
					for (int i = region.getKeyfrom(); i <= region.getKeyto(); i++ ) {
						if (ExcludeRegion.isExlcude(region)) {
							// 特定Regionを無効化する.
							System.out.println("　　region skip > "+region.getSample().getName()+"["+region.getKeyfrom()+"-"+region.getKeyto()+"]");
							region.setKeyfrom(0);
							region.setKeyto(0);
							break;
						} else {
							validList[i] = true;
							attentionList[i] = v;
							overlapList[i] = overlap;
						}
					}
				}
			} else {
				attentionList = null;
				overlapList = null;
				validList = null;
			}
		}
	}

	private int convertNoteMML2Midi(int mml_note) {
		return (mml_note + 12);
	}

	public double getAttention(int note) {
		if (options.attentionList == null) {
			return 0;
		}
		try {
			note = convertNoteMML2Midi(note);
			return options.attentionList[note];
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
	}

	public boolean isOverlap(int note) {
		if (options.overlapList == null) {
			return false;
		}
		try {
			note = convertNoteMML2Midi(note);
			return options.overlapList[note];
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isValid(int note) {
		if (options.overlapList == null) {
			return false;
		}
		try {
			note = convertNoteMML2Midi(note);
			return options.validList[note];
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
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
		boolean[] b = getEnablePartByProgram(program);

		for (int i = 0; i < b.length; i++) {
			if (b[i]) {
				return i;
			}
		}

		throw new AssertionError("Invalid Inst Part Number.");
	}

	private static String toProgramText(int program) {
		StringBuilder sb = new StringBuilder();
		sb.append(program & 0x1ff);    // ドラムフラグがあるので toMidiProgram()を使わない
		int bank = toMidiBank(program);
		if (bank != 0) {
			sb.append('.').append(bank);
		}
		return sb.toString();
	}

	private static String instName(Instrument inst, ResourceBundle resource) {
		if (resource == null) {
			return inst.getName().trim();
		}
		try {
			int program = logicalProgramNum(inst);
			String s = resource.getString(toProgramText(program));
			if (s.equals("-")) {
				return inst.getName().trim();
			}
			return s;
		} catch (MissingResourceException e) {
			return null;
		}
	}

	public String toStringOptionsInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append(program);
		for (boolean b : options.validList) {
			sb.append(',').append(b);
		}
		for (double v : options.attentionList) {
			sb.append(',').append(v);
		}
		for (boolean b : options.overlapList) {
			sb.append(',').append(b);
		}

		return sb.toString();
	}

	public void dlsInfoWriteToOutputStream(OutputStream outputStream) {
		PrintStream out = new PrintStream(outputStream);
		String name = instName(inst, instResource);
		String originalName = inst.getName();
		int bank = inst.getPatch().getBank();
		int program = inst.getPatch().getProgram();
		out.printf("%d,%d=%s \"%s\"\n", bank&0x7f, program, originalName, name);
		if (name != null) {
			name = ""+program+": "+name;
		}

		if ( (debug) && (inst instanceof DLSInstrument) ) {
			DLSInstrument dlsinst = (DLSInstrument) inst;
			for (DLSRegion reg : dlsinst.getRegions()) {
				double attenuation = reg.getSample().getSampleoptions().getAttenuation()/655360.0;
				out.print(" >> "+reg.getSample().getName()+" ");
				out.print(attenuation+" ");
				out.print(Math.pow(10.0, attenuation/20.0)+" ");
				out.print(reg.getKeyfrom()+" ");
				out.print(reg.getKeyto()+" ");
				out.print(reg.getVelfrom()+" ");
				out.print(reg.getVelto()+" ");
				out.print(reg.getChannel()+" ");
				out.print(reg.getExclusiveClass()+" ");
				out.print(reg.getFusoptions()+" ");
				out.print(reg.getPhasegroup()+" ");
				out.print(reg.getOptions()+"* "); // overlap
				out.print(reg.getSampleoptions().getUnitynote()+" ");
				out.print(reg.getSampleoptions().getAttenuation()+" ");
				out.print(reg.getSampleoptions().getOptions()+" ");
				out.print(reg.getSampleoptions().getFinetune()+" ");
				out.println();
			}
		}
	}

	public static List<InstClass> defaultSoundBank(boolean nameConvert) throws MidiUnavailableException {
		Soundbank sb = MidiSystem.getSynthesizer().getDefaultSoundbank();
		return loadSoundBank(sb, nameConvert);
	}

	public static List<InstClass> loadDLS(File dlsFile) throws InvalidMidiDataException, IOException {
		try (var stream = new BufferedInputStream(new FileInputStream(dlsFile))) {
			Soundbank sb = MidiSystem.getSoundbank(stream);
			return loadSoundBank(sb, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("loadDLS: "+dlsFile);
		}
	}

	private static Instrument instFix(Instrument inst) {
		if (inst instanceof DLSInstrument dlsinst) {
			if (inst.getSoundbank().getName().equals("Fury Sound Pack - Mabinogi Mobile Instrument Set")) {
				dlsinst.setPatch(new Patch(2, inst.getPatch().getProgram()));
			}
			dlsinst.setPatch(new Patch(inst.getPatch().getBank()+MabiDLS.DLS_BANK, inst.getPatch().getProgram()));
		}
		return inst;
	}

	private static List<InstClass> loadSoundBank(Soundbank sb, boolean nameConvert) {
		ArrayList<InstClass> instArray = new ArrayList<>();
		for (Instrument inst : sb.getInstruments()) {
			if (inst.getPatch().getBank() != 0) continue;
			if (debug) System.out.print(inst.toString() + "\t");
			inst = InstClass.instFix(inst);
			String originalName = inst.getName();
			String name = nameConvert ? instName(inst, instResource) : originalName.trim();
			int bank = inst.getPatch().getBank() & 0x7f;
			int program = inst.getPatch().getProgram();
			int lProgram = logicalProgramNum(inst);
			if (debug) System.out.printf("%d,%d(%d)=%s \"%s\"\n", bank, program, lProgram ,originalName, name);
			if ( (name != null) || (debug) ) {
				InstClass instc = new InstClass(name,
						bank,
						program,
						inst);
				instArray.add(instc);
				//				if (debug) instc.dlsInfoWriteToOutputStream(System.out);
			}
		}
		return instArray;
	}
}

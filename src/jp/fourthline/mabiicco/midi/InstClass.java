/*
 * Copyright (C) 2013-2021 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.io.File;
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
	private final int bank;
	private final int program;
	private final int lowerNote;
	private final int upperNote;
	private final InstType type;
	private final Instrument inst;
	private final Options options;

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
		if (str.length > 2) {
			region.from = Math.max(region.from, MMLEventParser.firstNoteNumber(str[2]));
		}
		if (str.length > 3) {
			region.to = Math.min(region.to, MMLEventParser.firstNoteNumber(str[3]));
		}
		this.lowerNote = region.from;
		this.upperNote = region.to;

		this.bank = bank;
		this.program = program;

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

	public boolean checkPitchRange(int note) {
		if ( (note < lowerNote) || (note > upperNote) ) {
			return false;
		} else {
			return true;
		}
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

	private final static class ExcludeRegion {
		private static final ExcludeRegion values[] = {
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
	public final class Options {
		public final static int OPTION_NUM = 256;
		private final double attentionList[];
		private final boolean overlapList[];
		private final boolean validList[];

		private Options(Instrument instrument) {
			if (instrument instanceof DLSInstrument) {
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
				DLSInstrument dlsinst = (DLSInstrument) instrument;
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
		String name = instName(inst);
		String originalName = inst.getName();
		int bank = inst.getPatch().getBank();
		int program = inst.getPatch().getProgram();
		out.printf("%d,%d=%s \"%s\"\n", bank, program, originalName, name);
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

	public static List<InstClass> defaultSoundBank() throws MidiUnavailableException {
		Soundbank sb = MidiSystem.getSynthesizer().getDefaultSoundbank();
		return loadSoundBank(sb, false);
	}

	public static List<InstClass> loadDLS(File dlsFile) throws InvalidMidiDataException, IOException {
		Soundbank sb = null;
		try {
			sb = MidiSystem.getSoundbank(dlsFile);
			return loadSoundBank(sb, true);
		} catch (Exception e) {
			MabiIccoProperties.getInstance().setDlsFile(null);
			throw new IOException("loadDLS: "+dlsFile.getName());
		}
	}

	private static List<InstClass> loadSoundBank(Soundbank sb, boolean nameConvert) {
		ArrayList<InstClass> instArray = new ArrayList<>();
		for (Instrument inst : sb.getInstruments()) {
			String originalName = inst.getName();
			String name = nameConvert ? instName(inst) : originalName.trim();
			int bank = inst.getPatch().getBank();
			int program = inst.getPatch().getProgram();
			System.out.printf("%d,%d=%s \"%s\"\n", bank, program, originalName, name);
			if (bank != 0) continue;
			if ( (name != null) || (debug == true) ) {
				name = ""+program+": "+name;
				InstClass instc = new InstClass( name,
						bank,
						program,
						inst);
				instArray.add(instc);
				instc.dlsInfoWriteToOutputStream(System.out);
			}
		}
		return instArray;
	}
}

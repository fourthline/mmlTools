/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.dls_proto1;

import java.io.File;
import javax.sound.midi.*;

public final class MabiDLS {
	private static final MabiDLS instance = new MabiDLS();
	private Synthesizer synthesizer;
	private Soundbank sb;
	private MidiChannel[] channel;
	private InstClass[] insts;
	
	public static MabiDLS getInstance() {
		return instance;
	}
	
	private MabiDLS() {
		try {
			synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			sb = loadDLS();
			synthesizer.loadAllInstruments(sb);
			channel = synthesizer.getChannels();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public MidiChannel getChannel(int ch) {
		return channel[ch];
	}
	
	private Soundbank loadDLS() throws Exception {
		File dlsFile = new File("C:/Nexon/Mabinogi/mp3/MSXspirit.dls");
		sb = MidiSystem.getSoundbank(dlsFile);
		
		Instrument inst[] = sb.getInstruments();
		insts = new InstClass[inst.length];
		System.out.println("inst.length = " + inst.length);
		for (int i = 0; i < inst.length; i++) {
			System.out.printf("%s: bank = %d, program %d\n", 
			inst[i].getName(),
			inst[i].getPatch().getBank(),
			inst[i].getPatch().getProgram() );
			
			insts[i] = new InstClass(inst[i].getName(),
					inst[i].getPatch().getBank(),
					inst[i].getPatch().getProgram() );
		}

		return sb;
	}
	
	public InstClass[] getInsts() {
		return insts;
	}
}




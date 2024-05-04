/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.ResourceLoader;
import jp.fourthline.mmlTools.parser.MMLEventParser;

public final class DrumConverter {

	private static final Map<Integer, Integer> drumMap = new HashMap<>();

	private static final int X = MMLEventParser.firstNoteNumber("O3D");

	static {
		ResourceBundle instPatch = ResourceBundle.getBundle("midDrum_mabiDrum", new ResourceLoader());
		instPatch.keySet().stream().sorted().forEach(key -> {
			String newInst = instPatch.getString(key).replaceAll("#.*", "");
			String s[] = newInst.split("\t");
			int midDrum = Integer.parseInt(key);
			int mabiDrum = MMLEventParser.firstNoteNumber("O" + s[2] + s[3].replace("b", "-"));
			drumMap.put(midDrum, mabiDrum);
		});
	}

	public static void midDrum2MabiDrum(IMMLManager mmlManager, MMLTrack track) {
		var type = MabiDLS.getInstance().getInstByProgram(track.getProgram()).getType();
		if (type == InstType.DRUMS) {
			for (var eventList : track.getMMLEventList()) {
				for (var noteEvent : eventList.getMMLNoteEventList()) {
					var note = drumMap.get(noteEvent.getNote());
					if (note != null) {
						noteEvent.setNote(note);
					} else {
						noteEvent.setNote(X);
					}
				}
			}
			mmlManager.updateActivePart(true);
		}
	}
}

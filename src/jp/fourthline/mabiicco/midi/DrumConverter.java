/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.ResourceLoader;
import jp.fourthline.mmlTools.parser.MMLEventParser;

/**
 * ドラム変換 (General MIDI -> Mabi)
 */
public final class DrumConverter {
	private static final Map<KeyMap, KeyMap> drumMap = new HashMap<>();
	private static final Map<Integer, KeyMap> mabiMap = new TreeMap<>();
	private static final Map<Integer, KeyMap> midMap = new TreeMap<>();

	private static final String X_NOTE = "O3D";
	private static final String X_NAME = "Snare ghost";

	public record KeyMap(String key, String name) implements Comparable<KeyMap> {
		public int getKey() {
			if (key.startsWith("O")) {
				return MMLEventParser.firstNoteNumber(key);
			} else {
				return Integer.parseInt(key);
			}
		}

		@Override
		public String toString() {
			return key + ": " + name;
		}

		public String validName(InstClass inst) {
			boolean valid = inst.isValid(getKey());
			return key + ": " + (valid ? name : "-");
		}

		@Override
		public int hashCode() {
			return getKey();
		}

		@Override
		public int compareTo(KeyMap o) {
			return getKey() - o.getKey();
		}
	}

	static {
		ResourceBundle instPatch = ResourceBundle.getBundle("midDrum_mabiDrum", new ResourceLoader());
		instPatch.keySet().stream().sorted().forEach(key -> {
			String lineStr = instPatch.getString(key);
			String s[] = lineStr.split("\t");
			String mabiDrumNote = "O" + s[2] + s[3].replace("b", "-").replace("#", "+");
			var midEntry = new KeyMap(key, s[0]);
			var mabiEntry = new KeyMap(mabiDrumNote, s[4]);
			if (mabiMap.containsKey(mabiEntry.getKey())) {
				mabiEntry = mabiMap.get(mabiEntry.getKey());
			} else {
				mabiMap.put(mabiEntry.getKey(), mabiEntry);
			}
			if (!midMap.containsKey(midEntry.getKey())) {
				drumMap.put(midEntry, mabiEntry);
				midMap.put(midEntry.getKey(), midEntry);
			}
		});

		var n = new KeyMap(X_NOTE, X_NAME);
		drumMap.put(null, n);
		mabiMap.entrySet().forEach(t -> System.out.println(t));
	}

	/**
	 * DrumConvertの対象となるドラムタイムの楽器であるかどうかを判定する.
	 * @param track
	 * @return
	 */
	public static boolean isDrumTrack(MMLTrack track) {
		if (track == null) {
			return false;
		}
		var type = MabiDLS.getInstance().getInstByProgram(track.getProgram()).getType();
		return (type == InstType.DRUMS);
	}

	public static void midDrum2MabiDrum(IMMLManager mmlManager) {
		var track = mmlManager.getActiveTrack();
		if (isDrumTrack(track)) {
			for (var eventList : track.getMMLEventList()) {
				for (var noteEvent : eventList.getMMLNoteEventList()) {
					var key = midMap.get(noteEvent.getNote());
					var item = drumMap.get(key);
					var note = item.getKey();
					noteEvent.setNote(note);
				}
			}
			mmlManager.updateActivePart(true);
		}
	}

	public static void showConvertMap(Frame parentFrame) {
		Vector<String> column = new Vector<>();
		column.add("General MIDI");
		column.add("Mabi");

		final var inst = MabiDLS.getInstance().getAvailableInstByInstType(List.of(InstType.DRUMS))[0];

		Vector<Vector<Object>> list = new Vector<>();
		midMap.forEach((key, value) -> {
			var mid = value;
			var mabi = drumMap.get(value);
			list.add(new Vector<Object>(List.of(mid, mabi.validName(inst))));
		});

		JTable table = UIUtils.createTable(list, column);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		String title = AppResource.appText("menu.drum_converting_map");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);

		JOptionPane.showMessageDialog(parentFrame, panel, title, JOptionPane.PLAIN_MESSAGE);
	}
}

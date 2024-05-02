/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.ResourceLoader;
import jp.fourthline.mmlTools.parser.MMLEventParser;

public final class DrumConverter {

	private static final Map<Integer, Entry> drumMap = new HashMap<>();

	private static final String X_NOTE = "O3D";
	private static final int X = MMLEventParser.firstNoteNumber(X_NOTE);
	private static final String X_NAME = "Snare ghost";

	private record Entry(String midStr, String mabiStr, String mabiDrumNote, int mabiDrum) {}

	static {
		ResourceBundle instPatch = ResourceBundle.getBundle("midDrum_mabiDrum", new ResourceLoader());
		instPatch.keySet().stream().sorted().forEach(key -> {
			String lineStr = instPatch.getString(key);
			String s[] = lineStr.split("\t");
			int midDrum = Integer.parseInt(key);
			String mabiDrumNote = "O" + s[2] + s[3].replace("b", "-").replace("#", "+");
			int mabiDrum = MMLEventParser.firstNoteNumber(mabiDrumNote);
			drumMap.put(midDrum, new Entry(s[0], s[4], mabiDrumNote, mabiDrum));
		});
	}

	/**
	 * DrumConvertの対象となるドラムタイムの楽器であるかどうかを判定する.
	 * @param track
	 * @return
	 */
	public static boolean isDrumTrack(MMLTrack track) {
		var type = MabiDLS.getInstance().getInstByProgram(track.getProgram()).getType();
		return (type == InstType.DRUMS);
	}

	public static void midDrum2MabiDrum(IMMLManager mmlManager) {
		var track = mmlManager.getActiveTrack();
		if (isDrumTrack(track)) {
			for (var eventList : track.getMMLEventList()) {
				for (var noteEvent : eventList.getMMLNoteEventList()) {
					var item = drumMap.get(noteEvent.getNote());
					var note = (item == null) ? X : item.mabiDrum;
					noteEvent.setNote(note);
				}
			}
			mmlManager.updateActivePart(true);
		}
	}

	public static void showConvertMap(Frame parentFrame) {
		Vector<String> column = new Vector<>();
		column.add("GM key");
		column.add("GM name");
		column.add("Mabi key");
		column.add("Mabi name");

		final var inst = MabiDLS.getInstance().getAvailableInstByInstType(List.of(InstType.DRUMS))[0];

		Vector<Vector<Object>> list = new Vector<>();
		drumMap.keySet().stream().sorted().forEach(key -> {
			var item = drumMap.get(key);
			var v = new Vector<Object>();
			v.add(key);
			v.add(item.midStr);
			v.add(item.mabiDrumNote);
			v.add(inst.isValid(item.mabiDrum) ? item.mabiStr : "-");
			list.add(v);
		});
		list.add(new Vector<Object>(List.of("-", "-", X_NOTE, X_NAME)));

		JTable table = UIUtils.createTable(list, column);
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(2).setMaxWidth(100);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		String title = AppResource.appText("menu.drum_converting_map");
		JOptionPane.showMessageDialog(parentFrame, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
	}
}

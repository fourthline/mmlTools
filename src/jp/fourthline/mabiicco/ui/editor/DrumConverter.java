/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static jp.fourthline.mabiicco.AppResource.appText;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.IEditStateObserver;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.Utils;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.InstType;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.SettingButtonGroupItem.SettingButtonItem;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLScoreSerializer;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.ResourceLoader;
import jp.fourthline.mmlTools.parser.MMLEventParser;

/**
 * ドラム変換 (General MIDI -> Mabi)
 */
public final class DrumConverter {
	private final Map<KeyMap, KeyMap> defaultMap = new HashMap<>();
	private final Map<KeyMap, KeyMap> drumMap = new HashMap<>();   // mid -> mabi
	private final Map<Integer, KeyMap> mabiMap = new TreeMap<>();  // note -> mabi
	private final Map<Integer, KeyMap> midMap = new TreeMap<>();   // note -> mid

	private static final String X_NOTE = "O3D";
	private static final String X_NAME = "Snare ghost";

	public static final RangeMode[] modes = RangeMode.values();

	private static class KeyMap implements Comparable<KeyMap> {
		private final int key;
		private final String keyName;
		private final Optional<String> name;

		private KeyMap(String key, String name, InstClass inst) {
			this.keyName = key;
			if (key.startsWith("O")) {
				this.key = MMLEventParser.firstNoteNumber(key);
			} else {
				this.key= Integer.parseInt(key);
			}

			if ((inst == null) || (inst.isValid(this.key))) {
				this.name = Optional.of(name);
			} else {
				this.name = Optional.empty();
			}
		}

		public String getName() {
			if (name.isPresent()) {
				return name.get();
			}
			return "-";
		}
		@Override
		public int hashCode() {
			return key;
		}

		@Override
		public int compareTo(KeyMap o) {
			return key - o.key;
		}

		@Override
		public String toString() {
			var str = keyName;
			if (name.isPresent()) {
				str += ": " + name.get();
			}
			return str;
		}
	}

	private static DrumConverter instance = null;
	public static DrumConverter getInstance() {
		if (instance == null) {
			instance = new DrumConverter();
		}
		return instance;
	}

	private final InstClass inst = MabiDLS.getInstance().getAvailableInstByInstType(List.of(InstType.DRUMS))[0];
	private JDialog dialog = null;

	private DrumConverter() {
		ResourceBundle instPatch = ResourceBundle.getBundle("midDrum_mabiDrum", new ResourceLoader());
		instPatch.keySet().stream().sorted().forEach(key -> {
			String lineStr = instPatch.getString(key);
			String s[] = lineStr.split("\t");
			String mabiDrumNote = "O" + s[2] + s[3].replace("b", "-").replace("#", "+");
			var midEntry = new KeyMap(key, s[0], null);
			var mabiEntry = new KeyMap(mabiDrumNote, s[4], inst);
			if (mabiMap.containsKey(mabiEntry.key)) {
				mabiEntry = mabiMap.get(mabiEntry.key);
			} else {
				mabiMap.put(mabiEntry.key, mabiEntry);
			}
			if (!midMap.containsKey(midEntry.key)) {
				drumMap.put(midEntry, mabiEntry);
				midMap.put(midEntry.key, midEntry);
			}
		});

		var n = new KeyMap(X_NOTE, X_NAME, null);
		if (!mabiMap.containsKey(n.key)) {
			mabiMap.put(n.key, n);
		}
		drumMap.put(null, n);
		defaultMap.putAll(drumMap);

		// カスタム設定のロード.
		loadData();

		// 不足しているMabiMapをここで作ってしまえばよいのでは.
		var key = List.of("C", "C+", "D", "D+", "E", "F", "F+", "G", "G+", "A", "A+", "B");
		for (int i = 2; i < 7; i++) {
			for (var t : key) {
				var m = new KeyMap("O"+i+t, "<UNKNOWN>", inst);
				if (!mabiMap.containsKey(m.key)) {
					if (m.name.isPresent()) {
						// 音データがあるけど名前がわからない.
						throw new IllegalStateException("unknown: " + key);
					}
					mabiMap.put(m.key, m);
				}
			}
		}
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
		if (type == InstType.DRUMS) {
			return track.getImportedData() != null;
		}
		return false;
	}

	public static boolean containsDrumTrack(MMLScore score) {
		boolean b = false;
		for (var track : score.getTrackList()) {
			if (isDrumTrack(track)) {
				b = true;
			}
		}
		return b;
	}

	/**
	 * ドラム変換を行う.
	 * @param mmlManager
	 * @param trackIndex
	 * @param partIndex
	 * @return  変更した場合はtrueを返す
	 */
	private boolean convert(IMMLManager mmlManager, int trackIndex, int partIndex) {
		boolean update = false;
		var track = mmlManager.getMMLScore().getTrack(trackIndex);
		if (!isDrumTrack(track)) {
			return false;
		}

		var importedData = MMLScoreSerializer.parseImportedData(track.getImportedData());
		if (importedData == null) {
			return false;
		}

		var eventList = track.getMMLEventAtIndex(partIndex);
		if (partIndex < importedData.size()) {
			var importedList = importedData.get(partIndex);
			for (var data : importedList.getMMLNoteEventList()) {
				int tickOffset = data.getTickOffset();
				var noteEvent = eventList.searchOnTickOffset(tickOffset);
				if ((noteEvent.getTickOffset() == tickOffset) && (noteEvent.getTick() == data.getTick())) {
					// 同じTickOffsetに同じTickのノートがあれば, インポートしたデータを基準に変換する.
					var key = midMap.get(data.getNote());
					var item = drumMap.get(key);
					if (noteEvent.getNote() != item.key) {
						noteEvent.setNote(item.key);
						update = true;
					}
				}
			}
		}
		return update;
	}

	public void midDrum2MabiDrum(IMMLManager mmlManager, RangeMode mode) {
		AtomicBoolean update = new AtomicBoolean(false);
		mode.action(mmlManager, (trackIndex, partIndex) -> {
			if (convert(mmlManager, trackIndex, partIndex)) {
				update.set(true);
			}
		});
		if (update.get()) {
			mmlManager.updateActivePart(true);
		}
	}

	private void saveData() {
		var p = new Properties();
		defaultMap.forEach((mid, mabi) -> {
			var v = drumMap.get(mid);
			if (!mabi.equals(v)) {
				int key = Integer.MIN_VALUE;
				if (mid != null) {
					key = mid.key;
				}
				p.setProperty(Integer.toString(key), Integer.toString(v.key));
			}
		});
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		try {
			p.store(bstream, "");
		} catch (IOException e) {
			e.printStackTrace();
		}

		MabiIccoProperties.getInstance().drumConvertCustomMap.set(Utils.compress(bstream.toString()));
	}

	private void loadData() {
		String str = MabiIccoProperties.getInstance().drumConvertCustomMap.get();
		if (str.length() == 0) return;
		byte[] data = Utils.decompress(str);
		if (data == null) return;
		ByteArrayInputStream istream = new ByteArrayInputStream(data);
		var p = new Properties();
		try {
			p.load(istream);
			p.forEach((a, b) -> {
				if (a instanceof String mid) {
					if (b instanceof String mabi) {
						int midKey = Integer.parseInt(mid);
						int mabiKey = Integer.parseInt(mabi);
						var m1 = midKey == Integer.MIN_VALUE ? null : midMap.get(midKey);
						var m2 = mabiMap.get(mabiKey);
						drumMap.put(m1, m2);
					}
				}
			});
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
			saveData();
		}
	}

	public static class Editor extends JPanel implements ActionListener, IEditStateObserver {
		private static final long serialVersionUID = -7937826516875634485L;
		private final JComboBox<KeyMap> combo = new JComboBox<>();
		private final JLabel midLabel = new JLabel();
		private final JLabel mabiLabel = new JLabel();

		private String mabiName(KeyMap mid, KeyMap mabi) {
			boolean isDefault = c.defaultMap.get(mid).key == mabi.key;
			String str = mabi.getName();
			if (!isDefault) {
				str += "  (*)";
			}
			return str;
		}

		private final DrumConverter c = DrumConverter.getInstance();
		private final JTable table;
		private final Vector<Vector<Object>> list = new Vector<>();
		private final JComboBox<SettingButtonItem> modeCombo;
		private final JButton execButton = new JButton(appText("drum_convert.convert"));
		private final JButton closeButton = new JButton(appText("drum_convert.close"));

		private final JDialog dialog;
		private final IMMLManager mmlManager;

		private KeyMap getKeyMap(int row) {
			var item = list.get(row);
			var key = item.get(0);
			KeyMap mid = null;
			if (!"-".equals(key)) {
				mid = c.midMap.get(key);
			}
			return mid;
		}

		private Editor(JDialog dialog, IMMLManager mmlManager) {
			super(new BorderLayout());
			this.dialog = dialog;
			this.mmlManager = mmlManager;
			var icon = AppResource.getImageIcon(AppResource.appText("drum_convert.sound_icon"));

			c.midMap.forEach((key, value) -> {
				var mid = value;
				var mabi = c.drumMap.get(value);
				list.add(new Vector<>(List.of(mid.key, mid.getName(), mabi.keyName, mabiName(mid, mabi))));
			});
			var noMapItem = c.drumMap.get(null);
			list.add(new Vector<>(List.of("-", "-", noMapItem.keyName, mabiName(null, noMapItem))));

			table = UIUtils.createTable(list, new Vector<>(List.of("GM key", "GM name", "Mabi key", "Mabi name")));
			table.getColumnModel().getColumn(0).setMinWidth(100);
			table.getColumnModel().getColumn(0).setMaxWidth(100);
			table.getColumnModel().getColumn(2).setMinWidth(100);
			table.getColumnModel().getColumn(2).setMaxWidth(100);
			table.setRowSelectionAllowed(true);
			table.getSelectionModel().addListSelectionListener(t -> updateCombo());

			// combo
			combo.setEnabled(false);
			JPanel comboPanel = UIUtils.createTitledPanel(AppResource.appText("drum_convert.map_change"), new BorderLayout());
			JPanel comboButtonPanel = new JPanel();
			JButton c1 = new JButton(AppResource.appText("edit.default"));
			c1.addActionListener(t -> selectedDefaultMabiKey(true));
			JButton c2 = new JButton(AppResource.appText("edit.apply"));
			c2.addActionListener(t -> selectedMabiKey(true));
			comboButtonPanel.add(c1);
			comboButtonPanel.add(c2);
			JPanel cp1 = new JPanel(new BorderLayout());
			cp1.add(new JLabel("MIDI: "), BorderLayout.WEST);
			cp1.add(midLabel, BorderLayout.CENTER);
			var b1 = new JButton(icon);
			b1.setFocusable(false);
			b1.addMouseListener(new DMouseListener(null, () -> selectedMidKey()));
			cp1.add(b1, BorderLayout.EAST);
			JPanel cp2 = new JPanel(new BorderLayout());
			cp2.add(new JLabel(AppResource.appText("drum_convert.current")), BorderLayout.WEST);
			cp2.add(mabiLabel, BorderLayout.CENTER);
			var b2 = new JButton(icon);
			b2.setFocusable(false);
			b2.addMouseListener(new DMouseListener(c.inst, () -> selectedDefaultMabiKey(false)));
			cp2.add(b2, BorderLayout.EAST);
			JPanel cp3 = new JPanel(new BorderLayout());
			cp3.add(new JLabel(AppResource.appText("drum_convert.change")), BorderLayout.WEST);
			cp3.add(combo, BorderLayout.CENTER);
			var b3 = new JButton(icon);
			b3.setFocusable(false);
			b3.addMouseListener(new DMouseListener(c.inst, () -> selectedMabiKey(false)));
			cp3.add(b3, BorderLayout.EAST);
			JPanel cp4 = new JPanel(new BorderLayout());
			cp4.add(cp1, BorderLayout.NORTH);
			cp4.add(cp2, BorderLayout.CENTER);
			cp4.add(cp3, BorderLayout.SOUTH);

			comboPanel.add(comboButtonPanel, BorderLayout.SOUTH);
			comboPanel.add(cp4, BorderLayout.CENTER);

			// 変換Map
			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(600, 400));
			JPanel convertMapPanel = UIUtils.createTitledPanel(appText("drum_convert.map"), new BorderLayout());
			convertMapPanel.add(scrollPane, BorderLayout.CENTER);
			JLabel gmDesc = new JLabel(" * GM: General MIDI");
			convertMapPanel.add(gmDesc, BorderLayout.SOUTH);

			// 実行パネル
			JPanel execPanel = new JPanel(new BorderLayout());
			JPanel execPanel2 = new JPanel();
			JPanel execPanel3 = new JPanel();
			var modeList = new Vector<SettingButtonItem>();
			for (var l : SettingButtonItem.create(modes)) {
				modeList.add(l);
			}
			Collections.reverse(modeList);
			modeCombo = new JComboBox<>(modeList);
			modeCombo.addActionListener(this);
			execButton.addActionListener(this);
			updateExecButtonStatus();
			closeButton.addActionListener(this);
			dialog.getRootPane().setDefaultButton(execButton);
			execPanel2.add(modeCombo);
			execPanel2.add(execButton);
			execPanel3.add(closeButton);
			execPanel.add(execPanel2, BorderLayout.CENTER);
			execPanel.add(execPanel3, BorderLayout.SOUTH);

			add(convertMapPanel, BorderLayout.NORTH);
			add(comboPanel, BorderLayout.CENTER);
			add(execPanel, BorderLayout.SOUTH);
		}

		private static final class DMouseListener extends MouseAdapter {
			private final InstClass inst;
			private final Supplier<Integer> f;

			private DMouseListener(InstClass inst, Supplier<Integer> f) {
				this.inst = inst;
				this.f = f;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				playNote(f.get());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				playNote(-1);
			}

			private void playNote(int note) {
				boolean isMidi = (inst == null);
				int program = 0;
				var dls = MabiDLS.getInstance();
				if (!isMidi) {
					dls.loadRequiredInstruments(List.of(inst));
					program = inst.getProgram();
				}
				dls.playDrum(isMidi, note, 100, program);
			}
		}

		private void updateCombo() {
			int row = table.getSelectedRow();
			var mid = getKeyMap(row);
			var mabi = c.drumMap.get(mid);
			midLabel.setText(mid == null ? "-" : mid.toString());
			mabiLabel.setText(mabi.toString());
			combo.removeAllItems();
			c.mabiMap.entrySet().forEach(m -> combo.addItem(m.getValue()));
			combo.setSelectedItem(c.drumMap.get(mid));
			combo.setEnabled(true);
		}

		private void setKeyMap(int row, KeyMap mid, KeyMap mabi) {
			c.drumMap.put(mid, mabi);
			table.getModel().setValueAt(mabi.keyName, row, 2);
			table.getModel().setValueAt(mabiName(mid, mabi), row, 3);
		}

		private int selectedMidKey() {
			int row = table.getSelectedRow();
			if (row < 0) return -1;
			var mid = getKeyMap(row);
			return (mid != null) ? mid.key : -1;
		}

		private int selectedDefaultMabiKey(boolean apply) {
			int row = table.getSelectedRow();
			var mid = getKeyMap(row);
			var mabi = c.defaultMap.get(mid);
			if (apply) {
				setKeyMap(row, mid, mabi);
				c.saveData();
			}
			return mabi.key;
		}

		private int selectedMabiKey(boolean apply) {
			int ret = -1;
			if (combo.getSelectedItem() instanceof KeyMap mabi) {
				int row = table.getSelectedRow();
				var mid = getKeyMap(row);
				if (apply) {
					setKeyMap(row, mid, mabi);
					c.saveData();
				}
				ret = mabi.key;
			}
			return ret;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			var source = e.getSource();
			if (source == closeButton) {
				dialog.setVisible(false);
			} else if (source == execButton) {
				apply();
			} else if (source == modeCombo) {
				updateExecButtonStatus();
			}
		}

		private RangeMode getCurrentRangeMode() {
			if (modeCombo.getSelectedItem() instanceof SettingButtonItem item) {
				if (item.getItem() instanceof RangeMode mode) {
					return mode;
				}
			}
			return null;
		}

		private void apply() {
			var mode = getCurrentRangeMode();
			if (mode != null) {
				c.midDrum2MabiDrum(mmlManager, mode);
			}
		}

		private void updateExecButtonStatus() {
			var mode = getCurrentRangeMode();
			if (mode != null) {
				AtomicBoolean enableExec = new AtomicBoolean(false);
				mode.action(mmlManager, (trackIndex, partIndex) -> {
					var track = mmlManager.getMMLScore().getTrack(trackIndex);
					if (isDrumTrack(track)) {
						enableExec.set(true);
					}
				});
				execButton.setEnabled(enableExec.get());
			}
		}

		@Override
		public void notifyUpdateEditState() {
			updateExecButtonStatus();
		}
	}

	public void showConvertMap(Frame parentFrame, IMMLManager mmlManager) {
		if (dialog == null) {
			dialog = new JDialog(parentFrame, appText("edit.drum_convert"));
			var editor = new Editor(dialog, mmlManager);
			dialog.add(editor);
			dialog.pack();
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					dialog.setVisible(false);
				}
			});
			dialog.setLocationRelativeTo(parentFrame);
			ActionDispatcher.getInstance().addEditObserber(editor);
		}
		dialog.setVisible(true);
	}
}

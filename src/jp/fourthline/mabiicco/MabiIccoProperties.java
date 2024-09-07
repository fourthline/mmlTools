/*
 * Copyright (C) 2013-2024 たんらる
 */

package jp.fourthline.mabiicco;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jp.fourthline.mabiicco.midi.MMLMidiTrack.OverlapMode;
import jp.fourthline.mabiicco.midi.SoundEnv;
import jp.fourthline.mabiicco.ui.PianoRollScaler.MouseScrollWidth;
import jp.fourthline.mabiicco.ui.PianoRollView;
import jp.fourthline.mabiicco.ui.TimeBox;
import jp.fourthline.mabiicco.ui.color.ColorSet;
import jp.fourthline.mabiicco.ui.color.ScaleColor;
import jp.fourthline.mabiicco.ui.editor.VelocityEditor.VelocityWidth;
import jp.fourthline.mmlTools.MMLBuilder;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.core.MMLText;
import jp.fourthline.mmlTools.core.ResourceLoader;

public final class MabiIccoProperties {
	private final PreloadedProperties properties = new PreloadedProperties();

	private static final MabiIccoProperties instance = new MabiIccoProperties();
	private final static String CONFIG_FILE = ".mabiicco.config";


	/** 最近開いたファイル */
	private static final String RECENT_FILE = "app.recent_file";

	/** DLSファイル */
	public final Property<String> dls_file = new StringProperty("app.dls_file", AppResource.appText("default.dls_file"));

	/** ウィンドウ最大化 */
	public final Property<Boolean> windowMaximize = new BooleanProperty("window.maximize", false);

	/** ウィンドウ位置 x座標 */
	private static final String WINDOW_X = "window.x";

	/** ウィンドウ位置 y座標 */
	private static final String WINDOW_Y = "window.y";

	/** ウィンドウ位置 幅 */
	private static final String WINDOW_WIDTH = "window.width";

	/** ウィンドウ位置 高さ */
	private static final String WINDOW_HEIGHT = "window.height";

	/** ピアノロール表示の高さスケール */
	public final EnumProperty<PianoRollView.NoteHeight> pianoRollNoteHeight = new EnumProperty<>("view.pianoRoll.heightScale", PianoRollView.NoteHeight.values(), PianoRollView.NoteHeight.H8);

	/** timeBox Index */
	public final EnumProperty<TimeBox.Type> timebox = new EnumProperty<>("view.timeBox", TimeBox.Type.values(), TimeBox.Type.MEASURE);

	/** 音源設定 Index */
	public final EnumProperty<SoundEnv> soundEnv = new EnumProperty<SoundEnv>("function.sound_env", SoundEnv.values(), SoundEnv.MABINOGI);

	/** クリック再生機能の有効/無効 */
	public final Property<Boolean> enableClickPlay = new BooleanProperty("function.enable_click_play", true);

	/** テンポ表示の有効/無効 */
	public final Property<Boolean> enableViewTempo = new BooleanProperty("function.enable_view_tempo", true);

	/** マーカー表示の有効/無効 */
	public final Property<Boolean> enableViewMarker = new BooleanProperty("function.enable_view_marker", true);

	/** 編集モード. 非編集時はアクティブパートなし, MMLのTextPanel非表示. */
	public final Property<Boolean> enableEdit = new BooleanProperty("function.enable_edit", true);

	/** 音源境界 */
	public final Property<Boolean> viewRange = new BooleanProperty("view.pianoRoll.range", true);

	/** 音源属性 */
	public final Property<Boolean> instAttr = new BooleanProperty("view.instrument.attribute", true);

	/** すべての音量を表示 */
	public final Property<Boolean> showAllVelocity = new BooleanProperty("view.pianoRoll.show_all_velocity", false);

	/** 音量線 */
	public final Property<Boolean> viewVelocityLine = new BooleanProperty("view.velocity.line", true);

	/** ノートクリックによるアクティブパート切り替え */
	public final Property<Boolean> activePartSwitch = new BooleanProperty("function.active_part_switch", false);

	/** 精密なMML最適化 */
//	public final Property<Boolean> enableMMLPreciseOptimize = new BooleanProperty("function.mml_precise_optimize", true, (t) -> MMLStringOptimizer.setEnablePreciseOptimize(t.booleanValue()));

	/** MML最適化レベル */
	public final EnumProperty<MMLOptimizeLevel> mmlOptimizeLevel = new EnumProperty<>("function.mml_optimize_level", MMLOptimizeLevel.values(), MMLOptimizeLevel.LV2, t -> t.change());

	/** Midi Device */
	public final Property<String> midiInputDevice = new StringProperty("midi.input_device");

	/** Scale color */
	public final EnumProperty<ScaleColor> scaleColor = new EnumProperty<ScaleColor>("scale.color", ScaleColor.values(), ScaleColor.C_MAJOR);

	/** Midi キーボード 和音入力 */
	public final Property<Boolean> midiChordInput = new BooleanProperty("midi.chord_input", false);

	/** ファイル履歴 */
	public static final int MAX_FILE_HISTORY = 8;
	private static final String FILE_HISTORY = "file.history";

	/** 和音にテンポ出力を許可するかどうか */
	// 2024/08/05 廃止.
//	public final Property<Boolean> mmlTempoAllowChordPart = new BooleanProperty("function.mml_tempo_allow_chord_part", true, t -> MMLTrack.setTempoAllowChordPart(t.booleanValue()));

	/** MML空補正 */
	public final Property<String> mmlEmptyCorrection = new StringProperty("function.mml_empty_correction", AppResource.appText("mml.emptyCorrection.default"), t -> MMLText.setMelodyEmptyStr(t));

	/** VZero Tempo */
	public final Property<Boolean> mmlVZeroTempo = new BooleanProperty("function.mml_vzero_tempo", false, t -> MMLBuilder.setMMLVZeroTempo(t.booleanValue()));

	/** fix64 Tempo */
	public final Property<Boolean> mmlFix64Tempo = new BooleanProperty("function.mml_fix64_tempo", false, t -> MMLScore.setMMLFix64(t.booleanValue()));

	/** LAF */
	public final EnumProperty<Laf> laf = new EnumProperty<>("ui.laf", Laf.values(), Laf.LIGHT, t -> ColorSet.update(t.isLight()));

	/** UIスケールを100%に固定する */
	public final Property<Boolean> uiscaleDisable = new BooleanProperty("ui.scale_disable", false);

	/** Overlap mode */
	/* 2023/04/19 のアップデートにより、重複音が問題なくできるようになったので固定値へ変更 */
	//	public final IndexProperty<OverlapMode> overlapMode = new IndexProperty<>("function.overlap_mode", OverlapMode.values(), OverlapMode.INST);
	public final Property<OverlapMode> overlapMode = new FixedProperty<>(OverlapMode.ALL);

	/** 内蔵音源を使用する */
	public final Property<Boolean> useDefaultSoundBank = new BooleanProperty("function.use_default_soundbank", false);

	/** テンポ削除時にもTick変換のダイアログ表示をする */
	public final Property<Boolean> enableTempoDeleteWithConvert = new BooleanProperty("function.tempoDeleteWithConvert", false);

	/** マウスホイールによる横スクロール量 */
	public final EnumProperty<MouseScrollWidth> mouseScrollWidth = new EnumProperty<>("function.mouse_scroll_width", MouseScrollWidth.values(), MouseScrollWidth.AUTO);

	/** ファイルOpen時にMML再生成する (旧データとの比較をしない) */
	public final Property<Boolean> reGenerateWithOpen = new BooleanProperty("function.reGenerateWithOpen", true);

	/** Velocity Editor */
	public final Property<Boolean> velocityEditor = new BooleanProperty("function.velocityEditor", false);

	/** Velocity bar width */
	public final EnumProperty<VelocityWidth> velocityWidth = new EnumProperty<VelocityWidth>("function.velocityEditor.velocity_width", VelocityWidth.values(), VelocityWidth.W4);

	/** DrumConverter: custom map */
	public final Property<String> drumConvertCustomMap = new StringProperty("function.drum_convert_custom_map");

	public static MabiIccoProperties getInstance() {
		return instance;
	}

	private MabiIccoProperties() {
		initFileHistory();
	}

	public String getRecentFile() {
		return properties.getProperty(RECENT_FILE, "");
	}

	public void setRecentFile(String path) {
		properties.setProperty(RECENT_FILE, path);
		properties.save();
	}

	public List<File> getDlsFile() {
		String str = dls_file.get();
		String[] filenames = str.split(",");
		ArrayList<File> fileArray = new ArrayList<>();
		for (String filename : filenames) {
			fileArray.add(new File(filename));
		}
		return fileArray;
	}

	public void setDlsFile(File[] fileArray) {
		StringBuilder sb = new StringBuilder();
		if (fileArray != null) {
			for (File file : fileArray) {
				sb.append(file.getPath()).append(',');
			}
			sb.deleteCharAt(sb.length()-1);
		}
		dls_file.set(sb.toString());
	}

	public Rectangle getWindowRect() {
		String x = properties.getProperty(WINDOW_X, null);
		String y = properties.getProperty(WINDOW_Y, null);
		String width = properties.getProperty(WINDOW_WIDTH, null);
		String height = properties.getProperty(WINDOW_HEIGHT, null);

		if ( (x == null) || (y == null) || (width == null) || (height == null) ) {
			return null;
		}
		return new Rectangle(
				Integer.parseInt(x),
				Integer.parseInt(y),
				Integer.parseInt(width),
				Integer.parseInt(height)
				);
	}

	public void setWindowRect(Rectangle rect) {
		if (!windowMaximize.get()) {
			if ( (rect.getX() >= 0.0) && (rect.getY() >= 0.0) ) {
				properties.setProperty(WINDOW_X, Integer.toString((int)rect.getX()));
				properties.setProperty(WINDOW_Y, Integer.toString((int)rect.getY()));
				properties.setProperty(WINDOW_WIDTH, Integer.toString((int)rect.getWidth()));
				properties.setProperty(WINDOW_HEIGHT, Integer.toString((int)rect.getHeight()));
				properties.save();
			}
		}
	}

	private final LinkedList<File> fileHistory = new LinkedList<>();
	private void initFileHistory() {
		for (int i = 0; i < MAX_FILE_HISTORY; i++) {
			String s = properties.getProperty(FILE_HISTORY+i);
			if (s != null) {
				fileHistory.addLast( new File(s) );
			}
		}
	}

	public File[] getFileHistory() {
		File[] list = new File[ fileHistory.size() ];
		return fileHistory.toArray( list );
	}

	public void setFileHistory(File file) {
		fileHistory.remove(file);
		fileHistory.addFirst(file);
		while (fileHistory.size() > MAX_FILE_HISTORY) {
			fileHistory.removeLast();
		}
		for (int i = 0; i < fileHistory.size(); i++) {
			properties.setProperty(FILE_HISTORY+i, fileHistory.get(i).getAbsolutePath());
		}
		properties.save();
	}

	public interface Property<T> extends Supplier<T> {
		void set(T value);
	}

	public static class FixedProperty<T> implements Property<T> {
		private final T value;
		private FixedProperty(T v) {
			value = v;
		}

		@Override
		public void set(T value) {
			throw new AssertionError();
		}

		@Override
		public T get() {
			return value;
		}
	}

	private abstract class AbstractProperty<T> implements Property<T> {
		protected final String name;
		protected final T defaultValue;
		protected Consumer<T> optDo;

		private AbstractProperty(String name, T defaultValue, Consumer<T> optDo) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.optDo = optDo;
			if (optDo != null) {
				optDo.accept(get());
			}
		}

		@Override
		public void set(T str) {
			properties.setProperty(name, stringValue(str));
			properties.save();
			if (optDo != null) {
				optDo.accept(str);
			}
		}

		@Override
		public T get() {
			String str = properties.getProperty(name, stringValue(defaultValue));
			T value = parseValue(str);
			if (value == null) {
				value = defaultValue;
			}
			return value;
		}

		abstract protected T parseValue(String str);
		abstract protected String stringValue(T v);
	}

	private final class StringProperty extends AbstractProperty<String> {
		private StringProperty(String name) {
			super(name, "", null);
		}

		private StringProperty(String name, String defaultValue) {
			super(name, defaultValue, null);
		}

		private StringProperty(String name, String defaultValue, Consumer<String> optDo) {
			super(name, defaultValue, optDo);
		}

		@Override
		protected String parseValue(String str) {
			return str;
		}

		@Override
		protected String stringValue(String v) {
			return v;
		}
	}

	private final class BooleanProperty extends AbstractProperty<Boolean> {
		private BooleanProperty(String name, boolean defaultValue) {
			super(name, defaultValue, null);
		}

		private BooleanProperty(String name, boolean defaultValue, Consumer<Boolean> optDo) {
			super(name, defaultValue, optDo);
		}

		@Override
		protected Boolean parseValue(String str) {
			return Boolean.parseBoolean(str);
		}

		@Override
		protected String stringValue(Boolean v) {
			return Boolean.toString(v);
		}
	}

	public final class EnumProperty<T extends Enum<T>> extends AbstractProperty<T> {
		private final T[] values;

		private EnumProperty(String name, T[] values, T defaultValue) {
			this(name, values, defaultValue, null);
		}

		private EnumProperty(String name, T[] values, T defaultValue, Consumer<T> optDo) {
			super(name, defaultValue, null); // values代入前なので optDo処理は行わせない.
			this.values = values;
			this.optDo = optDo;
			if (optDo != null) {
				optDo.accept(get());
			}
		}

		@Override
		protected T parseValue(String str) {
			for (T t : values) {
				if (stringValue(t).equals(str)) {
					return t;
				}
			}
			try {
				int index = Integer.parseInt(str);
				if ((index >= 0) && (index < values.length)) {
					var t = values[index];
					set(t);
					return t;
				}
			} catch (NumberFormatException e2) {}
			return defaultValue;
		}

		@Override
		protected String stringValue(T v) {
			return v.name();
		}

		public T[] getValues() {
			return values;
		}

		public T getDefault() {
			return defaultValue;
		}
	}

	private static final class PreloadedProperties extends Properties {
		private static final long serialVersionUID = 7450043736414817020L;
		private final boolean test_mode;
		private final String path;
		private PreloadedProperties() {
			test_mode = System.getProperty("mabiicco.test_mode") != null;
			path = ResourceLoader.getAppConfigPath(CONFIG_FILE);
			if (!test_mode) {
				try {
					FileInputStream in = new FileInputStream(path);
					super.load(in);
					in.close();
				} catch (InvalidPropertiesFormatException e) {
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void save() {
			if (!test_mode) {
				try {
					FileOutputStream out = new FileOutputStream(path);
					store(out, "");
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

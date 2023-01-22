/*
 * Copyright (C) 2013-2023 たんらる
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

import jp.fourthline.mabiicco.midi.SoundEnv;
import jp.fourthline.mabiicco.ui.PianoRollView;
import jp.fourthline.mmlTools.MMLBuilder;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLText;
import jp.fourthline.mmlTools.core.ResourceLoader;
import jp.fourthline.mmlTools.optimizer.MMLStringOptimizer;

public final class MabiIccoProperties {
	private final Properties properties = new Properties();

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
	private static final String HEIGHT_SCALE = "view.pianoRoll.heightScale";

	/** timeBox Index */
	private static final String TIMEBOX = "view.timeBox";

	/** 音源設定 Index */
	private static final String SOUND_ENV = "function.sound_env";

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
	public final Property<Boolean> enableMMLPreciseOptimize = new BooleanProperty("function.mml_precise_optimize", true, (t) -> MMLStringOptimizer.setEnablePreciseOptimize(t.booleanValue()));

	/** Midi Device */
	public final Property<String> midiInputDevice = new StringProperty("midi.input_device");

	/** Midi キーボード 和音入力 */
	public final Property<Boolean> midiChordInput = new BooleanProperty("midi.chord_input", false);

	/** ファイル履歴 */
	public static final int MAX_FILE_HISTORY = 8;
	private static final String FILE_HISTORY = "file.history";

	/** 和音にテンポ出力を許可するかどうか */
	public final Property<Boolean> mmlTempoAllowChordPart = new BooleanProperty("function.mml_tempo_allow_chord_part", true, t -> MMLTrack.setTempoAllowChordPart(t.booleanValue()));

	/** MML空補正 */
	public final Property<String> mmlEmptyCorrection = new StringProperty("function.mml_empty_correction", AppResource.appText("mml.emptyCorrection.default"), t -> MMLText.setMelodyEmptyStr(t));

	/** VZero Tempo */
	public final Property<Boolean> mmlVZeroTempo = new BooleanProperty("function.mml_vzero_tempo", true, t -> MMLBuilder.setMMLVZeroTempo(t.booleanValue()));

	/** システムのL&F */
	public final Property<Boolean> useSystemLaF = new BooleanProperty("ui.use_system_laf", false);

	/** UIスケールを100%に固定する */
	public final Property<Boolean> uiscaleDisable = new BooleanProperty("ui.scale_disable", false);

	/** 内蔵音源を使用する */
	public final Property<Boolean> useDefaultSoundBank = new BooleanProperty("function.use_default_soundbank", false);

	/** テンポ削除時にもTick変換のダイアログ表示をする */
	public final Property<Boolean> enableTempoDeleteWithConvert = new BooleanProperty("function.tempoDeleteWithConvert", false);

	/** ファイルOpen時にMML再生成する (旧データとの比較をしない) */
	public final Property<Boolean> reGenerateWithOpen = new BooleanProperty("function.reGenerateWithOpen", true);

	public static MabiIccoProperties getInstance() {
		return instance;
	}

	private final boolean test_mode;

	private MabiIccoProperties() {
		test_mode = System.getProperty("mabiicco.test_mode") != null;
		if (!test_mode) {
			try {
				FileInputStream in = new FileInputStream(ResourceLoader.getAppConfigPath(CONFIG_FILE));
				properties.load(in);
				in.close();
				initFileHistory();
			} catch (InvalidPropertiesFormatException e) {
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}

	private void save() {
		if (!test_mode) {
			try {
				FileOutputStream out = new FileOutputStream(ResourceLoader.getAppConfigPath(CONFIG_FILE));
				properties.store(out, "");
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getRecentFile() {
		return properties.getProperty(RECENT_FILE, "");
	}

	public void setRecentFile(String path) {
		properties.setProperty(RECENT_FILE, path);
		save();
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
			properties.setProperty(WINDOW_X, Integer.toString((int)rect.getX()));
			properties.setProperty(WINDOW_Y, Integer.toString((int)rect.getY()));
			properties.setProperty(WINDOW_WIDTH, Integer.toString((int)rect.getWidth()));
			properties.setProperty(WINDOW_HEIGHT, Integer.toString((int)rect.getHeight()));
			save();
		}
	}

	public int getPianoRollViewHeightScaleProperty() {
		String s = properties.getProperty(HEIGHT_SCALE, "1");
		int index = Integer.parseInt(s);
		if ( (index < 0) || (index >= PianoRollView.NoteHeight.values().length) ) {
			index = 1;
		}

		return index;
	}

	public void setPianoRollViewHeightScaleProperty(int index) {
		properties.setProperty(HEIGHT_SCALE, ""+index);
		save();
	}

	public int getTimeBoxIndex() {
		String s = properties.getProperty(TIMEBOX, "0");
		int index = Integer.parseInt(s);
		if ( (index < 0) || (index > 1) ) {
			index = 0;
		}
		return index;
	}

	public void setTimeBoxIndex(int index) {
		properties.setProperty(TIMEBOX, ""+index);
		save();
	}

	public int getSoundEnvIndex() {
		String s = properties.getProperty(SOUND_ENV, "0");
		int index = Integer.parseInt(s);
		if ( (index < 0) || (index >= SoundEnv.values().length) ) {
			index = 0;
		}
		return index;
	}

	public void setSoundEnvIndex(int index) {
		properties.setProperty(SOUND_ENV, Integer.toString(index));
		save();
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
		save();
	}

	public interface Property<T> {
		void set(T value);
		T get();
	}

	private final class StringProperty implements Property<String> {
		private final String name;
		private final String defaultValue;
		private final Consumer<String> optDo;

		private StringProperty(String name) {
			this(name, "");
		}

		private StringProperty(String name, String defaultValue) {
			this(name, defaultValue, null);
		}

		private StringProperty(String name, String defaultValue, Consumer<String> optDo) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.optDo = optDo;
			if (optDo != null) {
				optDo.accept(defaultValue);
			}
		}

		@Override
		public void set(String str) {
			properties.setProperty(name, str);
			save();
			if (optDo != null) {
				optDo.accept(str);
			}
		}

		@Override
		public String get() {
			String str = properties.getProperty(name, defaultValue);
			if (optDo != null) {
				// 設定ファイルからの反映に必要
				optDo.accept(str);
			}
			return str;
		}
	}

	private final class BooleanProperty implements Property<Boolean> {
		private final String name;
		private final String defaultValue;
		private final Consumer<Boolean> optDo;

		private BooleanProperty(String name, boolean defaultValue) {
			this(name, defaultValue, null);
		}

		private BooleanProperty(String name, boolean defaultValue, Consumer<Boolean> optDo) {
			this.name = name;
			this.defaultValue = Boolean.toString(defaultValue);
			this.optDo = optDo;
			if (optDo != null) {
				optDo.accept(defaultValue);
			}
		}

		@Override
		public void set(Boolean b) {
			properties.setProperty(name, Boolean.toString(b));
			save();
			if (optDo != null) {
				optDo.accept(b);
			}
		}

		@Override
		public Boolean get() {
			String str = properties.getProperty(name, defaultValue);
			Boolean value = Boolean.parseBoolean(str);
			if (optDo != null) {
				// 設定ファイルからの反映に必要
				optDo.accept(value);
			}
			return value;
		}
	}
}

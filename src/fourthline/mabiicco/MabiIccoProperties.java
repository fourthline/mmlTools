/*
 * Copyright (C) 2013-2022 たんらる
 */

package fourthline.mabiicco;

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

import fourthline.mabiicco.ui.PianoRollView;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLText;
import fourthline.mmlTools.core.ResourceLoader;
import fourthline.mmlTools.optimizer.MMLStringOptimizer;

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

	/** クリック再生機能の有効/無効 */
	public final Property<Boolean> enableClickPlay = new BooleanProperty("function.enable_click_play", true);

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

	/** システムのL&F */
	public final Property<Boolean> useSystemLaF = new BooleanProperty("ui.use_system_laf", false);

	/** UIスケールを100%に固定する */
	public final Property<Boolean> uiscale10 = new BooleanProperty("ui.scale10", false);

	public static MabiIccoProperties getInstance() {
		return instance;
	}

	private MabiIccoProperties() {
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

	private void save() {
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

	public String getRecentFile() {
		String str = properties.getProperty(RECENT_FILE, "");
		return str;
	}

	public void setRecentFile(String path) {
		properties.setProperty(RECENT_FILE, path);
		save();
	}

	public List<File> getDlsFile() {
		String str = dls_file.get();
		String filenames[] = str.split(",");
		ArrayList<File> fileArray = new ArrayList<>();
		for (String filename : filenames) {
			fileArray.add(new File(filename));
		}
		return fileArray;
	}

	public void setDlsFile(File fileArray[]) {
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
		String x = properties.getProperty(WINDOW_X, "-1");
		String y = properties.getProperty(WINDOW_Y, "-1");
		String width = properties.getProperty(WINDOW_WIDTH, "-1");
		String height = properties.getProperty(WINDOW_HEIGHT, "-1");

		Rectangle rect = new Rectangle(
				Integer.parseInt(x), 
				Integer.parseInt(y),
				Integer.parseInt(width),
				Integer.parseInt(height)
				);

		return rect;
	}

	public void setWindowRect(Rectangle rect) {
		properties.setProperty(WINDOW_X, Integer.toString((int)rect.getX()));
		properties.setProperty(WINDOW_Y, Integer.toString((int)rect.getY()));
		properties.setProperty(WINDOW_WIDTH, Integer.toString((int)rect.getWidth()));
		properties.setProperty(WINDOW_HEIGHT, Integer.toString((int)rect.getHeight()));
		save();
	}

	public int getPianoRollViewHeightScaleProperty() {
		String s = properties.getProperty(HEIGHT_SCALE, "1");
		int index = Integer.parseInt(s);
		if ( (index < 0) || (index >= PianoRollView.NOTE_HEIGHT_TABLE.length) ) {
			index = 1;
		}

		return index;
	}

	public void setPianoRollViewHeightScaleProperty(int index) {
		properties.setProperty(HEIGHT_SCALE, ""+index);
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
		File list[] = new File[ fileHistory.size() ];
		return fileHistory.toArray( list );
	}

	public void setFileHistory(File file) {
		if (fileHistory.contains(file)) {
			fileHistory.remove(file);
		}
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
		public void set(T value);
		public T get();
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

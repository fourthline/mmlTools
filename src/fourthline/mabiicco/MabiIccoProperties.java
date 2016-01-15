/*
 * Copyright (C) 2013-2015 たんらる
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

import fourthline.mabiicco.ui.PianoRollView;

public final class MabiIccoProperties {
	private final Properties properties = new Properties();
	private final String configFile = "config.properties";

	private static final MabiIccoProperties instance = new MabiIccoProperties();

	/** 最近開いたファイル */
	private static final String RECENT_FILE = "app.recent_file";

	/** DLSファイル */
	private static final String DLS_FILE = "app.dls_file";

	/** ウィンドウ最大化 */
	private static final String WINDOW_MAXIMIZE = "window.maximize";

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
	private static final String ENABLE_CLICK_PLAY = "function.enable_click_play";

	/** マーカー表示の有効/無効 */
	private static final String ENABLE_VIEW_MARKER = "function.enable_view_marker";

	/** 編集モード. 非編集時はアクティブパートなし, MMLのTextPanel非表示. */

	private static final String ENABLE_EDIT = "function.enable_edit";

	/** 音源境界 */
	private static final String VIEW_RANGE = "view.pianoRoll.range";

	/** 音量線 */
	private static final String VIEW_VELOCITY_LINE = "view.velocity.line";

	/** ノートクリックによるアクティブパート切り替え */
	private static final String ACTIVE_PART_SWITCH = "function.active_part_switch";

	/** ファイル履歴 */
	public static final int MAX_FILE_HISTORY = 8;
	private static final String FILE_HISTORY = "file.history";

	public static MabiIccoProperties getInstance() {
		return instance;
	}

	private MabiIccoProperties() {
		try {
			properties.load(new FileInputStream(configFile));
			initFileHistory();
		} catch (InvalidPropertiesFormatException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private void save() {
		try {
			properties.store(new FileOutputStream(configFile), "");
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
		String str = properties.getProperty(DLS_FILE, AppResource.appText("default.dls_file"));
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
		properties.setProperty(DLS_FILE, sb.toString());
		save();
	}

	public boolean getWindowMaximize() {
		String str = properties.getProperty(WINDOW_MAXIMIZE, "false");
		return Boolean.parseBoolean(str);
	}

	public void setWindowMaximize(boolean b) {
		properties.setProperty(WINDOW_MAXIMIZE, Boolean.toString(b));
		save();
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

	public boolean getEnableClickPlay() {
		String str = properties.getProperty(ENABLE_CLICK_PLAY, "true");
		return Boolean.parseBoolean(str);
	}

	public void setEnableClickPlay(boolean b) {
		properties.setProperty(ENABLE_CLICK_PLAY, Boolean.toString(b));
		save();
	}

	public boolean getEnableViewMarker() {
		String str = properties.getProperty(ENABLE_VIEW_MARKER, "true");
		return Boolean.parseBoolean(str);
	}

	public void setEnableViewMarker(boolean b) {
		properties.setProperty(ENABLE_VIEW_MARKER, Boolean.toString(b));
		save();
	}

	public boolean getEnableEdit() {
		String str = properties.getProperty(ENABLE_EDIT, "true");
		return Boolean.parseBoolean(str);
	}

	public void setEnableEdit(boolean b) {
		properties.setProperty(ENABLE_EDIT, Boolean.toString(b));
		save();
	}

	public boolean getViewRage() {
		String str = properties.getProperty(VIEW_RANGE, "true");
		return Boolean.parseBoolean(str);
	}

	public void setViewRage(boolean b) {
		properties.setProperty(VIEW_RANGE, Boolean.toString(b));
		save();
	}

	public boolean getVelocityLine() {
		String str = properties.getProperty(VIEW_VELOCITY_LINE, "false");
		return Boolean.parseBoolean(str);
	}

	public void setVelocityLine(boolean b) {
		properties.setProperty(VIEW_VELOCITY_LINE, Boolean.toString(b));
		save();
	}

	public boolean getActivePartSwitch() {
		String str = properties.getProperty(ACTIVE_PART_SWITCH, "false");
		return Boolean.parseBoolean(str);
	}

	public void setActivePartSwitch(boolean b) {
		properties.setProperty(ACTIVE_PART_SWITCH, Boolean.toString(b));
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
}

/*
 * Copyright (C) 2013-2024 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static jp.fourthline.mabiicco.AppResource.appText;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.IEditStateObserver;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.ui.PianoRollView.PaintMode;
import jp.fourthline.mabiicco.ui.editor.DrumConverter;
import jp.fourthline.mabiicco.ui.editor.NoteAlign;
import jp.fourthline.mabiicco.ui.editor.RangeMode;

import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.JToolBar;
import javax.swing.JComboBox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.io.File;


/**
 * メイン画面.
 * Menu, ツールバー, 主表示部 {@link MMLSeqView} を生成する.
 */
public final class MainFrame extends JFrame implements ComponentListener, ActionListener {
	private static final long serialVersionUID = -7484797594534384422L;

	private final JPanel contentPane;
	private final JTextField statusField;
	private final MMLSeqView mmlSeqView;
	private final JComboBox<NoteAlign> noteTypeSelect = new JComboBox<>(NoteAlign.values());
	private final JComboBox<PaintMode> paintModeSelect = new JComboBox<>(PaintMode.values());
	private final TimeBox timeBox;

	private final ActionListener listener;

	private final MabiIccoProperties appProperties = MabiIccoProperties.getInstance();

	/** シーケンス再生中に無効化する機能のリスト */
	private final ArrayList<PlayStateComponent<?>> noplayFunctions = new ArrayList<>();

	/** ショートカットキー情報 */
	private final Map<String, List<KeyStroke>> shortcutMap = new LinkedHashMap<>();

	/** 状態が変化するメニューたち */
	private PlayStateComponent<JMenuItem> reloadMenuItem = null;
	private PlayStateComponent<JMenuItem> undoMenu = null;
	private PlayStateComponent<JMenuItem> redoMenu = null;
	private PlayStateComponent<JMenuItem> saveMenuItem = null;
	private PlayStateComponent<JMenuItem> cutMenu = null;
	private PlayStateComponent<JMenuItem> copyMenu = null;
	private PlayStateComponent<JMenuItem> pasteMenu = null;
	private PlayStateComponent<JMenuItem> deleteMenu = null;
	private PlayStateComponent<JMenuItem> removeRestsBetweenNotesMenu = null;
	private PlayStateComponent<JMenuItem> octUpMenu = null;
	private PlayStateComponent<JMenuItem> octDownMenu = null;
	private PlayStateComponent<JMenuItem> velocityUpMenu = null;
	private PlayStateComponent<JMenuItem> velocityDownMenu = null;
	private PlayStateComponent<JMenuItem> xExportMenu = null;
	private List<PlayStateComponent<JMenuItem>> drumConvertMenu = null;

	private JButton loopButton = null;

	private final MenuWithIndex[] fileHistory = new MenuWithIndex[ MabiIccoProperties.MAX_FILE_HISTORY ];

	/**
	 * Create the frame.
	 * @param listener 関連付けるActionListener
	 */
	public MainFrame(ActionListener listener, IEditStateObserver editStateObserver) {
		this.listener = listener;

		setTitleAndFileName(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loadWindowPeoperties();
		addComponentListener(this);
		setIconImage(AppResource.getImageIcon("/img/MabiIcco_Icon(Large).png").getImage());

		setJMenuBar(createMenuBar());

		contentPane = new JPanel();
		initKeyAction();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		contentPane.add(northPanel, BorderLayout.NORTH);

		mmlSeqView = new MMLSeqView(this);
		mmlSeqView.setNoteAlignChanger(this::changeNoteTypeSelect);
		contentPane.add(mmlSeqView.getPanel(), BorderLayout.CENTER);
		contentPane.setFocusable(false);

		timeBox = new TimeBox(mmlSeqView);
		JToolBar toolBar = createToolBar();
		northPanel.add(toolBar);

		JPanel southPanel = new JPanel();
		contentPane.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BorderLayout(0, 0));

		statusField = new JTextField();
		statusField.setEditable(false);
		statusField.setFocusable(false);
		southPanel.add(statusField, BorderLayout.SOUTH);
		statusField.setColumns(10);

		setCanReloadFile(false);
		setCanUndo(false);
		setCanRedo(false);
		setCanSaveFile(false);
		setSelectedEdit(false);
		setPasteEnable(false);
		setRemoveRestsBetweenNotesEnable(false);
		setXExport(false);

		// 閉じるボタンへのアクション設定
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quitEvent();
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// ウィンドウがアクティベートされたときにペースト可能などを更新する.
				editStateObserver.notifyUpdateEditState();
			}
		});
	}

	private void quitEvent() {
		ActionEvent event = new ActionEvent(this, 0, ActionDispatcher.QUIT);
		this.listener.actionPerformed(event);
	}

	private PlayStateComponent<JMenuItem> createMenuItem(JMenu menu, String name, String actionCommand) {
		return createMenuItem(menu, name, actionCommand, false, null);
	}

	private PlayStateComponent<JMenuItem> createMenuItem(JMenu menu, String name, String actionCommand, boolean noplayFunction) {
		return createMenuItem(menu, name, actionCommand, noplayFunction, null);
	}

	private PlayStateComponent<JMenuItem> createMenuItem(JMenu menu, String name, String actionCommand, KeyStroke keyStroke) {
		return createMenuItem(menu, name, actionCommand, false, keyStroke);
	}

	private PlayStateComponent<JMenuItem> createMenuItem(JMenu menu, String name, String actionCommand, boolean noplayFunction, KeyStroke keyStroke) {
		JMenuItem menuItem = new JMenuItem(appText(name));
		String iconName = appText(name+".icon");
		if (!iconName.equals(name+".icon")) {
			Icon icon = AppResource.getImageIcon(iconName);
			if (icon != null) {
				menuItem.setIcon(icon);
			}
		}
		return createMenuItem(menu, menuItem, actionCommand, noplayFunction, keyStroke);
	}

	private PlayStateComponent<JMenuItem> createMenuItem(JMenu menu, JMenuItem menuItem, String actionCommand, boolean noplayFunction, KeyStroke keyStroke) {
		PlayStateComponent<JMenuItem> c = new PlayStateComponent<>(menuItem);
		menuItem.addActionListener(listener);
		menuItem.setActionCommand(actionCommand);

		if (noplayFunction) {
			noplayFunctions.add(c);
		}
		if (keyStroke != null) {
			menuItem.setAccelerator(keyStroke);
			addShortcutMap(menuItem.getText(), keyStroke);
		}

		menu.add(menuItem);
		return c;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		/************************* File Menu *************************/
		JMenu fileMenu = new JMenu(appText("menu.file"));
		menuBar.add(fileMenu);

		createMenuItem(fileMenu, "menu.newFile", ActionDispatcher.NEW_FILE, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		createMenuItem(fileMenu, "menu.openFile", ActionDispatcher.FILE_OPEN, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		reloadMenuItem = createMenuItem(fileMenu, "menu.reloadFile", ActionDispatcher.RELOAD_FILE, true);
		saveMenuItem = createMenuItem(fileMenu, "menu.saveFile", ActionDispatcher.SAVE_FILE,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		createMenuItem(fileMenu, "menu.saveAsFile", ActionDispatcher.SAVEAS_FILE,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

		fileMenu.add(new JSeparator());

		createMenuItem(fileMenu, "mml.input.import", ActionDispatcher.FILE_IMPORT, true);
		createMenuItem(fileMenu, "menu.midiExport", ActionDispatcher.MIDI_EXPORT);
		createMenuItem(fileMenu, "wavout", ActionDispatcher.WAVOUT, true);
		createMenuItem(fileMenu, "mml.export", ActionDispatcher.OTHER_MML_EXPORT);
		createMenuItem(fileMenu, "menu.scoreProperty", ActionDispatcher.SCORE_PROPERTY);

		fileMenu.add(new JSeparator());

		for (int i = 0; i < fileHistory.length; i++) {
			fileHistory[i] = new MenuWithIndex();
			createMenuItem(fileMenu, fileHistory[i], ActionDispatcher.FILE_OPEN_WITH_HISTORY, true, null);
		}
		updateFileHistoryMenu();

		fileMenu.add(new JSeparator());

		createMenuItem(fileMenu, "menu.quit", ActionDispatcher.QUIT);

		/************************* Edit Menu *************************/
		JMenu editMenu = new JMenu(appText("menu.edit"));
		menuBar.add(editMenu);

		undoMenu = createMenuItem(editMenu, "menu.undo", ActionDispatcher.UNDO,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
		redoMenu = createMenuItem(editMenu, "menu.redo", ActionDispatcher.REDO,
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));

		editMenu.add(new JSeparator());	

		cutMenu = createMenuItem(editMenu, "menu.cut", ActionDispatcher.CUT,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
		copyMenu = createMenuItem(editMenu, "menu.copy", ActionDispatcher.COPY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		pasteMenu = createMenuItem(editMenu, "menu.paste", ActionDispatcher.PASTE,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		deleteMenu = createMenuItem(editMenu, "menu.delete", ActionDispatcher.DELETE,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

		editMenu.add(new JSeparator());

		createMenuItem(editMenu, "menu.selectAll", ActionDispatcher.SELECT_ALL,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));

		editMenu.add(new JSeparator());

		createMenuItem(editMenu, "menu.changePart", ActionDispatcher.PART_CHANGE, true);
		createMenuItem(editMenu, "menu.addMeasure", ActionDispatcher.ADD_MEASURE, true);
		createMenuItem(editMenu, "menu.removeMeasure", ActionDispatcher.REMOVE_MEASURE, true);
		createMenuItem(editMenu, "menu.addBeat", ActionDispatcher.ADD_BEAT, true);
		createMenuItem(editMenu, "menu.removeBeat", ActionDispatcher.REMOVE_BEAT, true);
		createMenuItem(editMenu, "edit.transpose", ActionDispatcher.TRANSPOSE, true);
		createMenuItem(editMenu, "edit.tracks.velocity", ActionDispatcher.TRACKS_EDIT, true);
		removeRestsBetweenNotesMenu = createMenuItem(editMenu, "edit.remove_rests_between_notes", ActionDispatcher.REMOVE_RESTS_BETWEEN_NOTES, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK));
		octUpMenu = createMenuItem(editMenu, "edit.oct_up", ActionDispatcher.OCTAVE_UP, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK));
		octDownMenu = createMenuItem(editMenu, "edit.oct_down", ActionDispatcher.OCTAVE_DOWN, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK));
		velocityUpMenu = createMenuItem(editMenu, "edit.velocity_up", ActionDispatcher.VELOCITY_UP, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK));
		velocityDownMenu = createMenuItem(editMenu, "edit.velocity_down", ActionDispatcher.VELOCITY_DOWN, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK));
		if (MabiIccoProperties.getInstance().soundEnv.get().useDLS()) {
			var drumConvertGroupMenu = new JMenu(appText("edit.drum_convert"));
			var drumConvertMenuList = UIUtils.createGroupActionMenu(drumConvertGroupMenu, DrumConverter.modes, (s) -> s + " " + appText("drum_convert.menu_apply"), ActionDispatcher.MIDI_MABI_DRUM_CONVERT);
			drumConvertMenu = new ArrayList<>();
			drumConvertMenuList.forEach(t -> drumConvertMenu.add(new PlayStateComponent<>(t)));
			drumConvertGroupMenu.addSeparator();
			createMenuItem(drumConvertGroupMenu, "menu.drum_converting_map", ActionDispatcher.MIDI_MABI_DRUM_CONVERT_SHOW_MAP, true);
			editMenu.add(drumConvertGroupMenu);
		}

		editMenu.add(new JSeparator());

		createMenuItem(editMenu, "view.setUserViewMeasure", ActionDispatcher.SET_USER_VIEW_MEASURE, true, 
				KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		createMenuItem(editMenu, "edit.allClearTempo", ActionDispatcher.ALL_CLEAR_TEMPO, true);
		createMenuItem(editMenu, "mml.generate", ActionDispatcher.MML_GENERATE, true);
		createMenuItem(editMenu, "edit.keyboard.input", ActionDispatcher.KEYBOARD_INPUT, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
		createMenuItem(editMenu, "mml.text_edit", ActionDispatcher.MML_TEXT_EDIT, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK));

		/************************* Track Menu *************************/
		JMenu trackMenu = new JMenu(appText("menu.track"));
		menuBar.add(trackMenu);

		createMenuItem(trackMenu, "menu.addTrack", ActionDispatcher.ADD_TRACK, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
		createMenuItem(trackMenu, "menu.removeTrack", ActionDispatcher.REMOVE_TRACK, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		createMenuItem(trackMenu, "menu.duplicateTrack", ActionDispatcher.DUPLICATE_TRACK, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));

		trackMenu.add(new JSeparator());

		createMenuItem(trackMenu, "menu.trackProperty", ActionDispatcher.TRACK_PROPERTY, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK));
		createMenuItem(trackMenu, "edit.tracks.view", ActionDispatcher.TRACKS_VIEW, false);

		trackMenu.add(new JSeparator());

		createMenuItem(trackMenu, "menu.mml_import", ActionDispatcher.MML_IMPORT, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		createMenuItem(trackMenu, "menu.mml_export", ActionDispatcher.MML_EXPORT,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

		trackMenu.add(new JSeparator());

		createMenuItem(trackMenu, "menu.mml_x_import", ActionDispatcher.MML_X_IMPORT, true);
		xExportMenu = createMenuItem(trackMenu, "menu.mml_x_export", ActionDispatcher.MML_X_EXPORT, true);

		/************************* Play Menu *************************/
		JMenu playMenu = new JMenu(appText("menu.operate"));
		menuBar.add(playMenu);

		createMenuItem(playMenu, "menu.head", ActionDispatcher.SET_START_POSITION,
				KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		createMenuItem(playMenu, "menu.play", ActionDispatcher.PLAY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		createMenuItem(playMenu, "menu.stop", ActionDispatcher.STOP,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		createMenuItem(playMenu, "menu.pause", ActionDispatcher.PAUSE);

		playMenu.add(new JSeparator());	

		createMenuItem(playMenu, "menu.prev", ActionDispatcher.PREV_TIME,
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		createMenuItem(playMenu, "menu.next", ActionDispatcher.NEXT_TIME,
				KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

		/************************* Setting Menu *************************/
		JMenu settingMenu = new JMenu(appText("menu.setting"));
		menuBar.add(settingMenu);
		// 表示に関わる設定
		UIUtils.createGroupMenu(settingMenu, "menu.noteHeight", appProperties.pianoRollNoteHeight);
		UIUtils.createGroupMenu(settingMenu, "menu.scale_color", appProperties.scaleColor);
		createCheckMenu(settingMenu, "view.tempo", appProperties.enableViewTempo);
		createCheckMenu(settingMenu, "view.marker", appProperties.enableViewMarker);
		createCheckMenu(settingMenu, "view.range", appProperties.viewRange);
		createCheckMenu(settingMenu, "view.instAttr", appProperties.instAttr);
		createCheckMenu(settingMenu, "view.showAllVelocity", appProperties.showAllVelocity);
		createCheckMenu(settingMenu, "view.velocity", appProperties.viewVelocityLine);
		UIUtils.createGroupMenu(settingMenu, "ui.laf", appProperties.laf);
		createCheckMenu(settingMenu, "ui.scale_disable", appProperties.uiscaleDisable);
		settingMenu.add(new JSeparator());
		// 機能に関わる設定
		createCheckMenu(settingMenu, "edit.enable", appProperties.enableEdit);
		createCheckMenu(settingMenu, "edit.active_part_switch", appProperties.activePartSwitch);
		createCheckMenu(settingMenu, "clickPlayMenu", appProperties.enableClickPlay);
		createCheckMenu(settingMenu, "edit.tempoDeleteWithConvert", appProperties.enableTempoDeleteWithConvert);
		UIUtils.createGroupMenu(settingMenu, "ui.mouse_scroll_width", appProperties.mouseScrollWidth);
		createCheckMenu(settingMenu, "velocity_editor", appProperties.velocityEditor);
		settingMenu.add(new JSeparator());
		// MML生成に関わる設定
		//		createCheckMenu(settingMenu, "mml.precise_optimize", appProperties.enableMMLPreciseOptimize, ActionDispatcher.MML_GENERATE);
		UIUtils.createGroupMenu(settingMenu, "mml.optimize_level", appProperties.mmlOptimizeLevel);
		createCheckMenu(settingMenu, "mml.tempo_allow_chord_part", appProperties.mmlTempoAllowChordPart, ActionDispatcher.MML_GENERATE);
		createCheckMenu(settingMenu, "mml.vzero_tempo", appProperties.mmlVZeroTempo, ActionDispatcher.MML_GENERATE);
		createCheckMenu(settingMenu, "mml.fix64_tempo", appProperties.mmlFix64Tempo, ActionDispatcher.MML_GENERATE);
		createMenuItem(settingMenu, "mml.emptyCorrection", ActionDispatcher.INPUT_EMPTY_CORRECTION, true);
		createCheckMenu(settingMenu, "mml.regenerate_with_open", appProperties.reGenerateWithOpen);
		settingMenu.add(new JSeparator());
		// DLSに関わる設定
		//		createGroupMenu(settingMenu, "menu.overlap_mode", appProperties.overlapMode);  // 2023/04/19 のアップデートにより、重複音が問題なくできるようになったので固定値へ変更
		UIUtils.createGroupMenu(settingMenu, "menu.sound_env", appProperties.soundEnv);
		createCheckMenu(settingMenu, "menu.useDefaultSoundbank", appProperties.useDefaultSoundBank, ActionDispatcher.USE_DEFAULT_SOUNDBANK, true);
		createMenuItem(settingMenu, "menu.select_dls", ActionDispatcher.SELECT_DLS, true);

		/************************* Help Menu *************************/
		JMenu helpMenu = new JMenu(appText("menu.help"));
		menuBar.add(helpMenu);

		createMenuItem(helpMenu, "menu.about", ActionDispatcher.ABOUT);
		createMenuItem(helpMenu, "menu.shortcutInfo", ActionDispatcher.SHORTCUT_INFO);
		createMenuItem(helpMenu, "menu.mmlErrList", ActionDispatcher.MML_ERR_LIST);
		if (!appProperties.useDefaultSoundBank.get()) {
			createMenuItem(helpMenu, "menu.instList", ActionDispatcher.INST_LIST);
		}
		createMenuItem(helpMenu, "menu.polyphonyMonitor", ActionDispatcher.POLYPHONY_MONITOR);

		return menuBar;
	}

	private static class MenuWithIndex extends JMenuItem implements IntSupplier {
		private static final long serialVersionUID = -7944526274796801310L;
		private int index;
		private MenuWithIndex() {
			super();
		}
		public void set(int index) {
			this.index = index;
		}
		@Override
		public int getAsInt() {
			return this.index;
		}
	}

	/**
	 * 有効/無効の設定ボタンを作成します.
	 * @param settingMenu
	 */
	private void createCheckMenu(JMenu settingMenu, String itemName, MabiIccoProperties.Property<Boolean> property) {
		createCheckMenu(settingMenu, itemName, property, null, false);
	}

	private void createCheckMenu(JMenu settingMenu, String itemName, MabiIccoProperties.Property<Boolean> property, String actionCommand) {
		createCheckMenu(settingMenu, itemName, property, actionCommand, false);
	}

	private void createCheckMenu(JMenu settingMenu, String itemName, MabiIccoProperties.Property<Boolean> property, String actionCommand, boolean noplayFunction) {
		createCheckMenu(settingMenu, itemName, property, actionCommand, noplayFunction, null);
	}

	private void createCheckMenu(JMenu settingMenu, String itemName, MabiIccoProperties.Property<Boolean> property, String actionCommand, boolean noplayFunction, KeyStroke keyStroke) {
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(appText(itemName));
		String detail = itemName+".detail";
		String toolTipText = appText(detail); 
		if (toolTipText != detail) {
			menuItem.setToolTipText(toolTipText);
		}
		settingMenu.add(menuItem);

		menuItem.setSelected( property.get() );
		menuItem.setActionCommand(actionCommand);
		menuItem.addActionListener((e) -> {
			boolean b = property.get();
			property.set(!b);
			menuItem.setSelected(!b);
			if (mmlSeqView != null) {
				mmlSeqView.repaint();
			}
			if (actionCommand != null) {
				this.listener.actionPerformed(e);
			}
		});
		if (noplayFunction) {
			noplayFunctions.add(new PlayStateComponent<>(menuItem));
		}
		if (keyStroke != null) {
			menuItem.setAccelerator(keyStroke);
			addShortcutMap(menuItem.getText(), keyStroke);
		}
	}

	private PlayStateComponent<JButton> createToolButton(JComponent target, String title, String command, boolean noplayFunction) {
		JButton button = new JButton("");
		var c = new PlayStateComponent<>(button);
		Icon icon = null;
		String iconName = appText(title+".icon");
		if (!iconName.equals(title+".icon")) {
			icon = AppResource.getImageIcon(appText(iconName));
		}
		if (icon == null) {
			button.setText(appText(title));
		} else {
			button.setToolTipText(appText(title));
			button.setIcon(icon);
		}
		button.setFocusable(false);
		button.addActionListener(listener);
		button.setActionCommand(command);

		if (noplayFunction) {
			noplayFunctions.add(c);
		}

		target.add(button);
		return c;
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = UIUtils.createToolBar();

		createToolButton(toolBar, "menu.newFile", ActionDispatcher.NEW_FILE, true);
		createToolButton(toolBar, "menu.openFile", ActionDispatcher.FILE_OPEN, true);

		toolBar.add(newToolBarSeparator());

		createToolButton(toolBar, "menu.head", ActionDispatcher.SET_START_POSITION, false);
		createToolButton(toolBar, "menu.prev", ActionDispatcher.PREV_TIME, false);
		createToolButton(toolBar, "menu.play", ActionDispatcher.PLAY, false);
		createToolButton(toolBar, "menu.next", ActionDispatcher.NEXT_TIME, false);
		createToolButton(toolBar, "menu.pause", ActionDispatcher.PAUSE, false);
		createToolButton(toolBar, "menu.stop", ActionDispatcher.STOP, false);
		loopButton = createToolButton(toolBar, "menu.loop", ActionDispatcher.TOGGLE_LOOP, false).get();

		toolBar.add(newToolBarSeparator());

		createToolButton(toolBar, "menu.addMeasure", ActionDispatcher.ADD_MEASURE, true);
		createToolButton(toolBar, "menu.removeMeasure", ActionDispatcher.REMOVE_MEASURE, true);
		createToolButton(toolBar, "menu.addBeat", ActionDispatcher.ADD_BEAT, true);
		createToolButton(toolBar, "menu.removeBeat", ActionDispatcher.REMOVE_BEAT, true);

		toolBar.add(newToolBarSeparator());

		createToolButton(toolBar, "menu.inputMML", ActionDispatcher.INPUT_FROM_CLIPBOARD, true);
		createToolButton(toolBar, "menu.outputMML", ActionDispatcher.OUTPUT_TO_CLIPBOARD, false);

		toolBar.add(newToolBarSeparator());

		// ビューの拡大/縮小ツールボタン
		createToolButton(toolBar, "view.scale.up", ActionDispatcher.VIEW_SCALE_UP, false);
		createToolButton(toolBar, "view.scale.down", ActionDispatcher.VIEW_SCALE_DOWN, false);

		// 編集ノートタイプ
		noteTypeSelect.setFocusable(false);
		noteTypeSelect.addActionListener(this); // MainFrameでAction処理します.
		noteTypeSelect.setSelectedItem(NoteAlign.DEFAULT_ALIGN);
		setEditAlign();
		toolBar.add(noteTypeSelect);

		// Paint Mode
		paintModeSelect.setFocusable(false);
		paintModeSelect.addActionListener(this);
		paintModeSelect.setSelectedItem( mmlSeqView.getPaintMode() );
		toolBar.add(paintModeSelect);

		toolBar.add(newToolBarSeparator());
		timeBox.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		timeBox.setFocusable(false);
		timeBox.setType(appProperties.timebox.get());
		timeBox.addActionListener((t) -> appProperties.timebox.set(timeBox.getType()));
		timeBox.setPreferredSize(new Dimension(240, 20));
		toolBar.add(timeBox);

		toolBar.add(newToolBarSeparator());

		// レベルモニター
		var soundMonitor = new LevelMonitor();
		toolBar.add(soundMonitor);

		// 発音数モニター
		var ployMonitor = PolyphonyMonitor.getInstance().getTextField();
		toolBar.add(ployMonitor);

		return toolBar;
	}

	private JSeparator newToolBarSeparator() {
		JSeparator separator = new JToolBar.Separator();
		separator.setForeground(Color.DARK_GRAY);
		return separator;
	}

	public void setTitleAndFileName(String filename) {
		String fileTitle = "";
		if (filename != null) {
			fileTitle = filename;
		}
		setTitle(AppResource.getAppTitle() + " [" + fileTitle + "]");
	}

	private void loadWindowPeoperties() {
		Rectangle rect = appProperties.getWindowRect();
		if (rect == null) {
			setSize(1024, 768);
			setLocationRelativeTo(null);
		} else {
			setBounds(rect);
		}

		if (appProperties.windowMaximize.get()) {
			this.setExtendedState(MAXIMIZED_BOTH);
		}

	}

	private void updateWindowProperties() {
		int extendedState = this.getExtendedState();
		if ( (extendedState & MAXIMIZED_BOTH) == MAXIMIZED_BOTH ) {
			appProperties.windowMaximize.set(true);
		} else {
			appProperties.windowMaximize.set(false);
			appProperties.setWindowRect(this.getBounds());
		}
	}

	public MMLSeqView getMMLSeqView() {
		return mmlSeqView;
	}

	// JFrameのイベント
	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {
		updateWindowProperties();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		updateWindowProperties();
	}

	@Override
	public void componentShown(ComponentEvent e) {}

	/**
	 * 再生中に各機能を無効化する。
	 */
	public void disableNoplayItems() {
		for (PlayStateComponent<?> component : noplayFunctions) {
			component.setNoplay(false);
		}
	}

	/**
	 * 再生中に無効化されている機能を有効にする。
	 */
	public void enableNoplayItems() {
		for (PlayStateComponent<?> component : noplayFunctions) {
			component.setNoplay(true);
		}
		EventQueue.invokeLater(() -> {
			mmlSeqView.resetViewPosition();
			mmlSeqView.repaint();
		});
	}

	private void setEditAlign() {
		NoteAlign noteAlign = (NoteAlign) noteTypeSelect.getSelectedItem();
		int alignTick = noteAlign.getAlign();
		mmlSeqView.setEditAlign(alignTick);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source.equals(noteTypeSelect)) {
			setEditAlign();
		} else if (source.equals(paintModeSelect)) {
			PaintMode mode = (PaintMode) paintModeSelect.getSelectedItem();
			mmlSeqView.setPaintMode(mode);
			mmlSeqView.repaint();
		}
	}

	/**
	 * 再読み込みUIの有効化
	 * @param b trueで再読み込みUIが有効
	 */
	public void setCanReloadFile(boolean b) {
		reloadMenuItem.setEnabled(b);
	}

	/**
	 * Undo-UIの有効化
	 * @param b trueでUndo-UIが有効
	 */
	public void setCanUndo(boolean b) {
		undoMenu.setEnabled(b);
	}

	/**
	 * Redo-UIの有効化
	 * @param b trueでRedo-UIが有効
	 */
	public void setCanRedo(boolean b) {
		redoMenu.setEnabled(b);
	}

	/**
	 * 上書き保存UIの有効化
	 * @param b trueで上書き保存UIが有効
	 */
	public void setCanSaveFile(boolean b) {
		saveMenuItem.setEnabled(b);
	}

	public void setSelectedEdit(boolean b) {
		cutMenu.setEnabled(b);
		copyMenu.setEnabled(b);
		deleteMenu.setEnabled(b);
		octUpMenu.setEnabled(b);
		octDownMenu.setEnabled(b);
		velocityUpMenu.setEnabled(b);
		velocityDownMenu.setEnabled(b);
	}

	public void setPasteEnable(boolean b) {
		pasteMenu.setEnabled(b);
	}

	public void setRemoveRestsBetweenNotesEnable(boolean b) {
		removeRestsBetweenNotesMenu.setEnabled(b);
	}

	public void setXExport(boolean b) {
		xExportMenu.setEnabled(b);
	}

	public void setDrumConvert(boolean b, boolean all) {
		if (drumConvertMenu != null) {
			for (int i = 0; i < DrumConverter.modes.length; i++) {
				drumConvertMenu.get(i).setEnabled((DrumConverter.modes[i] == RangeMode.ALL_TRACK) ? all : b);
			}
		}
	}

	public void updateLoop(boolean b) {
		loopButton.setSelected(b);
	}

	public void updateFileHistoryMenu() {
		File[] fileList = appProperties.getFileHistory();
		for (int i = 0; i < fileHistory.length; i++) {
			if ( (i < fileList.length) && (fileList[i] != null) ) {
				fileHistory[i].setText( (i+1) + " " + fileList[i].getName() );
				fileHistory[i].setToolTipText(fileList[i].getAbsolutePath());
				fileHistory[i].set(i);
				fileHistory[i].setVisible(true);
			} else {
				fileHistory[i].setVisible(false);
			}
		}
	}

	public void setStatusText(String text) {
		statusField.setText(text);
	}

	/**
	 * 編集中ノート変更
	 * @param index
	 */
	private void changeNoteTypeSelect(int index) {
		if ( (index >= 0) && (index < noteTypeSelect.getItemCount()) )
			noteTypeSelect.setSelectedIndex(index);
	}

	private void addShortcutMap(String text, KeyStroke keyStroke) {
		shortcutMap.values().forEach(t -> {
			t.forEach(k -> {
				if (k.equals(keyStroke)) {
					throw new AssertionError("addShortcutMap ERROR: " + text + " / " + k);
				}
			});
		});
		if (shortcutMap.containsKey(text)) {
			shortcutMap.get(text).add(keyStroke);
		} else {
			var list = new ArrayList<KeyStroke>();
			list.add(keyStroke);
			shortcutMap.put(text, list);
		}
	}

	public Map<String, List<KeyStroke>> getShortcutMap() {
		return shortcutMap;
	}

	private void initKeyAction() {
		createKeyAction("select_paintMode.activePart",
				KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0),
				() -> paintModeSelect.setSelectedItem(PaintMode.ACTIVE_PART),
				appText("shortcut.select_paintMode.activePart"));
		createKeyAction("select_paintMode.activeTrack",
				KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0),
				() -> paintModeSelect.setSelectedItem(PaintMode.ACTIVE_TRACK),
				appText("shortcut.select_paintMode.activeTrack"));
		createKeyAction("select_paintMode.allTrack",
				KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),
				() -> paintModeSelect.setSelectedItem(PaintMode.ALL_TRACK),
				appText("shortcut.select_paintMode.allTrack"));

		createKeyAction(ActionDispatcher.SWITCH_TRACK_NEXT,
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.SWITCH_TRACK_NEXT)),
				appText("shortcut.track_next"));
		createKeyAction(ActionDispatcher.SWITCH_TRACK_PREV,
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.SWITCH_TRACK_PREV)),
				appText("shortcut.track_prev"));

		createKeyAction(ActionDispatcher.SWITCH_MMLPART_NEXT,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.SWITCH_MMLPART_NEXT)),
				appText("shortcut.part_next"));
		createKeyAction(ActionDispatcher.SWITCH_MMLPART_PREV,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.SWITCH_MMLPART_PREV)),
				appText("shortcut.part_prev"));

		createKeyAction(ActionDispatcher.PLAY,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.PLAY)),
				appText("shortcut.play"));

		// 編集ノート長選択
		for (var noteAlign : NoteAlign.values()) {
			if (noteAlign.getKeyCodeList() != null) {
				AtomicInteger i = new AtomicInteger();
				noteAlign.getKeyCodeList().forEach(keyCode -> 
				createKeyAction(noteAlign.name()+"."+(i.incrementAndGet()),
						KeyStroke.getKeyStroke(keyCode, 0),
						() -> changeNoteTypeSelect(Arrays.asList(NoteAlign.values()).indexOf(noteAlign)),
						noteAlign.toString()));
			}
		}
	}

	private void createKeyAction(String name, KeyStroke stroke, Runnable func, String text) {
		new KeyAction(name, stroke, contentPane, func);
		addShortcutMap(text, stroke);
	}

	private static final class KeyAction extends AbstractAction {
		private static final long serialVersionUID = -5439131294063926971L;
		private Runnable function;
		private KeyAction() {}
		private KeyAction(String name, KeyStroke stroke, JComponent rootPane, Runnable func) {
			this.function = func;
			rootPane.getInputMap().put(stroke, name);
			rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, name);
			rootPane.getActionMap().put(name, this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.function.run();
		}
	}

	/**
	 * 再生時に無効化することのできるコンポーネント
	 */
	static final class PlayStateComponent<T extends JComponent> {
		private boolean noplay = true;
		private boolean enable = true;
		private final T component;
		PlayStateComponent(T component) {
			this.component = component;
		}
		void setNoplay(boolean noplay) {
			this.noplay = noplay;
			component.setEnabled(this.enable && this.noplay);
		}
		void setEnabled(boolean enable) {
			this.enable = enable;
			component.setEnabled(this.enable && this.noplay);
		}
		T get() {
			return component;
		}
	}
}

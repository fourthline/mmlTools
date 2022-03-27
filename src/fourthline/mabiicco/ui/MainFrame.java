/*
 * Copyright (C) 2013-2022 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;

import fourthline.mabiicco.ActionDispatcher;
import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.IEditStateObserver;

import static fourthline.mabiicco.AppResource.appText;
import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.ui.PianoRollView.PaintMode;
import fourthline.mabiicco.ui.color.ScaleColor;
import fourthline.mabiicco.ui.editor.NoteAlign;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.JToolBar;
import javax.swing.JComboBox;

import java.awt.Color;
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
	private final JComboBox<NoteAlign> noteTypeSelect = new JComboBox<>(NoteAlign.createAlignList());
	private final JComboBox<PaintMode> paintModeSelect = new JComboBox<>(PaintMode.values());
	private final JLabel timeView = new JLabel("time MM:SS/MM:SS (120)");

	private final ActionListener listener;

	/** シーケンス再生中に無効化する機能のリスト */
	private final ArrayList<JComponent> noplayFunctions = new ArrayList<>();

	/** 状態が変化するメニューたち */
	private JMenuItem reloadMenuItem = null;
	private JMenuItem undoMenu = null;
	private JMenuItem redoMenu = null;
	private JMenuItem saveMenuItem = null;
	private JMenuItem cutMenu = null;
	private JMenuItem copyMenu = null;
	private JMenuItem pasteMenu = null;
	private JMenuItem deleteMenu = null;
	private JMenuItem removeRestsBetweenNotesMenu = null;

	private JButton loopButton = null;

	private MenuWithIndex fileHistory[] = new MenuWithIndex[ MabiIccoProperties.MAX_FILE_HISTORY ];

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
		mmlSeqView.setTimeView(timeView);
		mmlSeqView.setNoteAlignChanger(t -> {
			if ( (t >= 0) && (t < noteTypeSelect.getItemCount()) )
				noteTypeSelect.setSelectedIndex(t);
		});
		contentPane.add(mmlSeqView.getPanel(), BorderLayout.CENTER);
		contentPane.setFocusable(false);

		JToolBar toolBar = createToolBar();
		toolBar.setFloatable(false);
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

	private JMenuItem createMenuItem(JMenu menu, String name, String actionCommand) {
		return createMenuItem(menu, name, actionCommand, false, null);
	}

	private JMenuItem createMenuItem(JMenu menu, String name, String actionCommand, boolean noplayFunction) {
		return createMenuItem(menu, name, actionCommand, noplayFunction, null);
	}

	private JMenuItem createMenuItem(JMenu menu, String name, String actionCommand, KeyStroke keyStroke) {
		return createMenuItem(menu, name, actionCommand, false, keyStroke);
	}

	private JMenuItem createMenuItem(JMenu menu, String name, String actionCommand, boolean noplayFunction, KeyStroke keyStroke) {
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

	private JMenuItem createMenuItem(JMenu menu, JMenuItem menuItem, String actionCommand, boolean noplayFunction, KeyStroke keyStroke) {
		menuItem.addActionListener(listener);
		menuItem.setActionCommand(actionCommand);

		if (noplayFunction) {
			noplayFunctions.add(menuItem);
		}
		if (keyStroke != null) {
			menuItem.setAccelerator(keyStroke);
		}

		menu.add(menuItem);
		return menuItem;
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
		removeRestsBetweenNotesMenu = createMenuItem(editMenu, "edit.remove_rests_between_notes", ActionDispatcher.REMOVE_RESTS_BETWEEN_NOTES);

		editMenu.add(new JSeparator());

		createMenuItem(editMenu, "edit.allClearTempo", ActionDispatcher.ALL_CLEAR_TEMPO, true);
		createMenuItem(editMenu, "mml.generate", ActionDispatcher.MML_GENERATE, true);
		createMenuItem(editMenu, "edit.keyboard.input", ActionDispatcher.KEYBOARD_INPUT, true,
				KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));

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
		MabiIccoProperties properties = MabiIccoProperties.getInstance();
		JMenu settingMenu = new JMenu(appText("menu.setting"));
		menuBar.add(settingMenu);
		// 表示に関わる設定
		createNoteHeightMenu(settingMenu);
		createScaleColorMenu(settingMenu);
		createCheckMenu(settingMenu, "view.marker", properties.enableViewMarker);
		createCheckMenu(settingMenu, "view.range", properties.viewRange);
		createCheckMenu(settingMenu, "view.instAttr", properties.instAttr);
		createCheckMenu(settingMenu, "view.showAllVelocity", properties.showAllVelocity);
		createCheckMenu(settingMenu, "view.velocity", properties.viewVelocityLine);
		createCheckMenu(settingMenu, "ui.use_system_laf", properties.useSystemLaF, ActionDispatcher.CHANGE_UI);
		settingMenu.add(new JSeparator());
		// 機能に関わる設定
		createCheckMenu(settingMenu, "edit.enable", properties.enableEdit);
		createCheckMenu(settingMenu, "edit.active_part_switch", properties.activePartSwitch);
		createCheckMenu(settingMenu, "clickPlayMenu", properties.enableClickPlay);
		settingMenu.add(new JSeparator());
		// MML生成に関わる設定
		createCheckMenu(settingMenu, "mml.precise_optimize", properties.enableMMLPreciseOptimize);
		createCheckMenu(settingMenu, "mml.tempo_allow_chord_part", properties.mmlTempoAllowChordPart);
		createMenuItem(settingMenu, "mml.emptyCorrection", ActionDispatcher.INPUT_EMPTY_CORRECTION, true);
		settingMenu.add(new JSeparator());
		// DLSに関わる設定
		createMenuItem(settingMenu, "menu.clear_dls", ActionDispatcher.CLEAR_DLS, true);

		/************************* Help Menu *************************/
		JMenu helpMenu = new JMenu(appText("menu.help"));
		menuBar.add(helpMenu);

		createMenuItem(helpMenu, "menu.about", ActionDispatcher.ABOUT);

		return menuBar;
	}

	private class CheckBoxMenuWithIndex extends JCheckBoxMenuItem implements IntSupplier {
		private static final long serialVersionUID = -2688552233736202101L;
		private final int index;
		private CheckBoxMenuWithIndex(String text, int index) {
			super(text);
			this.index = index;
		}
		@Override
		public int getAsInt() {
			return this.index;
		}
	}

	private class CheckBoxMenuWith<T> extends JCheckBoxMenuItem implements Supplier<T> {
		private static final long serialVersionUID = -7786833458520626015L;
		private final T obj;
		private CheckBoxMenuWith(String text, T obj) {
			super(text);
			this.obj = obj;
		}
		@Override
		public T get() {
			return obj;
		}
	}

	private class MenuWithIndex extends JMenuItem implements IntSupplier {
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

	private void createNoteHeightMenu(JMenu settingMenu) {
		JMenu noteHeightMenu = new JMenu(appText("menu.noteHeight"));
		settingMenu.add(noteHeightMenu);

		ButtonGroup group = new ButtonGroup();
		int index = 0;
		for (int value : PianoRollView.NOTE_HEIGHT_TABLE) {
			CheckBoxMenuWithIndex menu = new CheckBoxMenuWithIndex(value+"px", index++);
			menu.setActionCommand(ActionDispatcher.CHANGE_NOTE_HEIGHT_INT);
			menu.addActionListener(listener);
			noteHeightMenu.add(menu);
			group.add(menu);
		}

		MabiIccoProperties properties = MabiIccoProperties.getInstance();
		index = properties.getPianoRollViewHeightScaleProperty();
		Collections.list(group.getElements()).get(index).setSelected(true);
	}

	/**
	 * 音階表示設定メニュー
	 */
	private void createScaleColorMenu(JMenu settingMenu) {
		JMenu plotScaleMenu = new JMenu(appText("menu.scale_color"));
		settingMenu.add(plotScaleMenu);

		ButtonGroup group = new ButtonGroup();
		for (ScaleColor p : ScaleColor.values()) {
			CheckBoxMenuWith<ScaleColor> menu = new CheckBoxMenuWith<>(appText(p.getName()), p);
			menu.setActionCommand(ActionDispatcher.CHANGE_SCALE_COLOR);
			menu.addActionListener(listener);
			plotScaleMenu.add(menu);
			group.add(menu);
		}

		Collections.list(group.getElements()).get(0).setSelected(true);
	}

	/**
	 * 有効/無効の設定ボタンを作成します.
	 * @param settingMenu
	 */
	private void createCheckMenu(JMenu settingMenu, String itemName, MabiIccoProperties.Property<Boolean> property) {
		createCheckMenu(settingMenu, itemName, property, null);
	}

	private void createCheckMenu(JMenu settingMenu, String itemName, MabiIccoProperties.Property<Boolean> property, String actionCommand) {
		JCheckBoxMenuItem clickPlayMenu = new JCheckBoxMenuItem(appText(itemName));
		settingMenu.add(clickPlayMenu);

		clickPlayMenu.setSelected( property.get() );
		clickPlayMenu.setActionCommand(actionCommand);
		clickPlayMenu.addActionListener((e) -> {
			boolean b = property.get();
			property.set(!b);
			clickPlayMenu.setSelected(!b);
			if (mmlSeqView != null) {
				mmlSeqView.repaint();
			}
			if (actionCommand != null) {
				this.listener.actionPerformed(e);
			}
		});
	}

	private JButton createToolButton(JComponent target, String title, String command, boolean noplayFunction) {
		JButton button = new JButton("");
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
			noplayFunctions.add(button);
		}

		target.add(button);
		return button;
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();

		createToolButton(toolBar, "menu.newFile", ActionDispatcher.NEW_FILE, true);
		createToolButton(toolBar, "menu.openFile", ActionDispatcher.FILE_OPEN, true);

		toolBar.add(newToolBarSeparator());

		createToolButton(toolBar, "menu.head", ActionDispatcher.SET_START_POSITION, false);
		createToolButton(toolBar, "menu.prev", ActionDispatcher.PREV_TIME, false);
		createToolButton(toolBar, "menu.play", ActionDispatcher.PLAY, false);
		createToolButton(toolBar, "menu.next", ActionDispatcher.NEXT_TIME, false);
		createToolButton(toolBar, "menu.pause", ActionDispatcher.PAUSE, false);
		createToolButton(toolBar, "menu.stop", ActionDispatcher.STOP, false);
		loopButton = createToolButton(toolBar, "menu.loop", ActionDispatcher.TOGGLE_LOOP, false);

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
		noteTypeSelect.setSelectedIndex(NoteAlign.DEFAULT_ALIGN_INDEX);
		setEditAlign();
		toolBar.add(noteTypeSelect);

		// Paint Mode
		paintModeSelect.setFocusable(false);
		paintModeSelect.addActionListener(this);
		paintModeSelect.setSelectedItem( mmlSeqView.getPaintMode() );
		toolBar.add(paintModeSelect);

		toolBar.add(newToolBarSeparator());
		timeView.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		toolBar.add(timeView);

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
		MabiIccoProperties properties = MabiIccoProperties.getInstance();

		Rectangle rect = properties.getWindowRect();
		if (rect.getX() < 0.0) {
			setSize(850, 650);
			setLocationRelativeTo(null);
		} else {
			setBounds(rect);
		}

		if (properties.windowMaximize.get()) {
			this.setExtendedState(MAXIMIZED_BOTH);
		}

	}

	private void updateWindowProperties() {
		int extendedState = this.getExtendedState();
		MabiIccoProperties properties = MabiIccoProperties.getInstance();

		if ( extendedState == MAXIMIZED_BOTH ) {
			properties.windowMaximize.set(true);
		} else {
			properties.windowMaximize.set(false);
			properties.setWindowRect(this.getBounds());
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
		for (JComponent component : noplayFunctions) {
			component.setEnabled(false);
		}
	}

	/**
	 * 再生中に無効化されている機能を有効にする。
	 */
	public void enableNoplayItems() {
		for (JComponent component : noplayFunctions) {
			component.setEnabled(true);
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
	}

	public void setPasteEnable(boolean b) {
		pasteMenu.setEnabled(b);
	}

	public void setRemoveRestsBetweenNotesEnable(boolean b) {
		removeRestsBetweenNotesMenu.setEnabled(b);
	}

	public void updateLoop(boolean b) {
		loopButton.setSelected(b);
	}

	public void updateFileHistoryMenu() {
		File fileList[] = MabiIccoProperties.getInstance().getFileHistory();
		for (int i = 0; i < fileHistory.length; i++) {
			if ( (i < fileList.length) && (fileList[i] != null) ) {
				fileHistory[i].setText( (i+1) + " " + fileList[i].getName() );
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

	private void initKeyAction() {
		createKeyAction("select_paintMode.activePart",
				KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0),
				() -> paintModeSelect.setSelectedItem(PaintMode.ACTIVE_PART));
		createKeyAction("select_paintMode.activeTrack",
				KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0),
				() -> paintModeSelect.setSelectedItem(PaintMode.ACTIVE_TRACK));
		createKeyAction("select_paintMode.allTrack",
				KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),
				() -> paintModeSelect.setSelectedItem(PaintMode.ALL_TRACK));

		createKeyAction(ActionDispatcher.SWITCH_TRACK_NEXT,
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.SWITCH_TRACK_NEXT)));
		createKeyAction(ActionDispatcher.SWITCH_TRACK_PREV,
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.SWITCH_TRACK_PREV)));

		createKeyAction(ActionDispatcher.SWITCH_MMLPART_NEXT,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.SWITCH_MMLPART_NEXT)));
		createKeyAction(ActionDispatcher.SWITCH_MMLPART_PREV,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.SWITCH_MMLPART_PREV)));

		createKeyAction(ActionDispatcher.PLAY,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
				() -> this.listener.actionPerformed(new ActionEvent(this, 0, ActionDispatcher.PLAY)));
	}

	private void createKeyAction(String name, KeyStroke stroke, Runnable func) {
		new KeyAction(name, stroke, contentPane, func);
	}

	private final class KeyAction extends AbstractAction {
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
}

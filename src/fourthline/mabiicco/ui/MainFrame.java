/*
 * Copyright (C) 2013-2015 たんらる
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
import static fourthline.mabiicco.AppResource.appText;
import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.INotifyTrackEnd;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.PianoRollView.PaintMode;
import fourthline.mabiicco.ui.editor.MMLEditor;
import fourthline.mabiicco.ui.editor.NoteAlign;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.ButtonGroup;
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



public final class MainFrame extends JFrame implements ComponentListener, INotifyTrackEnd, ActionListener {
	private static final long serialVersionUID = -7484797594534384422L;

	private final JPanel contentPane;
	private final JTextField statusField;
	private final MMLSeqView mmlSeqView;
	private final JComboBox<NoteAlign> noteTypeSelect = new JComboBox<>(MMLEditor.createAlignList());
	private final JComboBox<PaintMode> paintModeSelect = new JComboBox<>(PaintMode.values());
	private final JLabel timeView = new JLabel("time MM:SS/MM:SS (120)");

	private final ActionListener listener;

	/** シーケンス再生中に無効化する機能のリスト */
	private final ArrayList<JComponent> noplayFunctions = new ArrayList<>();

	/** 状態が変化するメニューたち */
	private JMenuItem reloadMenuItem;
	private JMenuItem undoMenu;
	private JMenuItem redoMenu;
	private JMenuItem saveMenuItem;
	private JMenuItem cutMenu;
	private JMenuItem copyMenu;
	private JMenuItem pasteMenu;
	private JMenuItem deleteMenu;

	/**
	 * Create the frame.
	 */
	public MainFrame(ActionListener listener) {
		this.listener = listener;

		setTitleAndFileName(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loadWindowPeoperties();
		addComponentListener(this);

		setJMenuBar(createMenuBar());

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		contentPane.add(northPanel, BorderLayout.NORTH);

		mmlSeqView = new MMLSeqView();
		mmlSeqView.setTimeView(timeView);
		contentPane.add(mmlSeqView.getPanel(), BorderLayout.CENTER);

		JToolBar toolBar = createToolBar();
		toolBar.setFloatable(false);
		northPanel.add(toolBar);

		JPanel southPanel = new JPanel();
		contentPane.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BorderLayout(0, 0));

		statusField = new JTextField();
		statusField.setEditable(false);
		southPanel.add(statusField, BorderLayout.SOUTH);
		statusField.setColumns(10);

		MabiDLS.getInstance().addTrackEndNotifier(this);

		setCanReloadFile(false);
		setCanUndo(false);
		setCanRedo(false);
		setCanSaveFile(false);
		setSelectedEdit(false);
		setPasteEnable(false);

		// 閉じるボタンへのアクション設定
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quitEvent();
			}
		});
	}

	private void quitEvent() {
		ActionEvent event = new ActionEvent(this, 0, ActionDispatcher.QUIT);
		this.listener.actionPerformed(event);
	}

	private JMenuItem createMenuItem(String name, String actionCommand) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.addActionListener(listener);
		menuItem.setActionCommand(actionCommand);

		return menuItem;
	}

	private JMenuItem createMenuItem(String name, String actionCommand, String iconName) {
		JMenuItem menuItem = createMenuItem(name, actionCommand);
		try {
			menuItem.setIcon(AppResource.getImageIcon(iconName));
		} catch (NullPointerException e) {}

		return menuItem;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		/************************* File Menu *************************/
		JMenu fileMenu = new JMenu(appText("menu.file"));
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		JMenuItem newFileMenuItem = createMenuItem(appText("menu.newFile"), ActionDispatcher.NEW_FILE, appText("menu.newFile.icon"));
		noplayFunctions.add(newFileMenuItem);
		newFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		fileMenu.add(newFileMenuItem);

		JMenuItem fileOpenMenuItem = createMenuItem(appText("menu.openFile"), ActionDispatcher.FILE_OPEN, appText("menu.openFile.icon"));
		noplayFunctions.add(fileOpenMenuItem);
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileMenu.add(fileOpenMenuItem);

		reloadMenuItem = createMenuItem(appText("menu.reloadFile"), ActionDispatcher.RELOAD_FILE);
		noplayFunctions.add(reloadMenuItem);
		fileMenu.add(reloadMenuItem);

		saveMenuItem = createMenuItem(appText("menu.saveFile"), ActionDispatcher.SAVE_FILE);
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		fileMenu.add(saveMenuItem);

		JMenuItem saveAsMenuItem = createMenuItem(appText("menu.saveAsFile"), ActionDispatcher.SAVEAS_FILE);
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_DOWN_MASK));
		fileMenu.add(saveAsMenuItem);

		fileMenu.add(new JSeparator());

		JMenuItem fileImportItem = createMenuItem(appText("mml.input.import"), ActionDispatcher.FILE_IMPORT);
		noplayFunctions.add(fileImportItem);
		fileMenu.add(fileImportItem);

		JMenuItem midiExportItem = createMenuItem(appText("menu.midiExport"), ActionDispatcher.MIDI_EXPORT);
		fileMenu.add(midiExportItem);

		JMenuItem scorePropertyMenu = createMenuItem(appText("menu.scoreProperty"), ActionDispatcher.SCORE_PROPERTY);
		fileMenu.add(scorePropertyMenu);

		fileMenu.add(new JSeparator());

		JMenuItem exitMenuItem = createMenuItem(appText("menu.quit"), ActionDispatcher.QUIT);
		fileMenu.add(exitMenuItem);

		/************************* Edit Menu *************************/
		JMenu editMenu = new JMenu(appText("menu.edit"));
		fileMenu.setMnemonic('E');
		menuBar.add(editMenu);

		undoMenu = createMenuItem(appText("menu.undo"), ActionDispatcher.UNDO);
		undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		editMenu.add(undoMenu);

		redoMenu = createMenuItem(appText("menu.redo"), ActionDispatcher.REDO);
		redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		editMenu.add(redoMenu);

		editMenu.add(new JSeparator());	

		cutMenu = createMenuItem(appText("menu.cut"), ActionDispatcher.CUT);
		cutMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		editMenu.add(cutMenu);

		copyMenu = createMenuItem(appText("menu.copy"), ActionDispatcher.COPY);
		copyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		editMenu.add(copyMenu);

		pasteMenu = createMenuItem(appText("menu.paste"), ActionDispatcher.PASTE);
		pasteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		editMenu.add(pasteMenu);

		deleteMenu = createMenuItem(appText("menu.delete"), ActionDispatcher.DELETE, appText("menu.delete.icon"));
		deleteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		editMenu.add(deleteMenu);

		editMenu.add(new JSeparator());

		JMenuItem selectAllMenu = createMenuItem(appText("menu.selectAll"), ActionDispatcher.SELECT_ALL);
		selectAllMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		editMenu.add(selectAllMenu);

		editMenu.add(new JSeparator());

		JMenuItem partChangeMenu = createMenuItem(appText("menu.changePart"), ActionDispatcher.PART_CHANGE);
		noplayFunctions.add(partChangeMenu);
		editMenu.add(partChangeMenu);

		JMenuItem addMeasureMenu = createMenuItem(appText("menu.addMeasure"), ActionDispatcher.ADD_MEASURE);
		noplayFunctions.add(addMeasureMenu);
		editMenu.add(addMeasureMenu);

		JMenuItem removeMeasureMenu = createMenuItem(appText("menu.removeMeasure"), ActionDispatcher.REMOVE_MEASURE);
		noplayFunctions.add(removeMeasureMenu);
		editMenu.add(removeMeasureMenu);

		JMenuItem addBeatMenu = createMenuItem(appText("menu.addBeat"), ActionDispatcher.ADD_BEAT);
		noplayFunctions.add(addBeatMenu);
		editMenu.add(addBeatMenu);

		JMenuItem removeBeatMenu = createMenuItem(appText("menu.removeBeat"), ActionDispatcher.REMOVE_BEAT);
		noplayFunctions.add(removeBeatMenu);
		editMenu.add(removeBeatMenu);

		JMenuItem transposeMenu = createMenuItem(appText("edit.transpose"), ActionDispatcher.TRANSPOSE);
		noplayFunctions.add(transposeMenu);
		editMenu.add(transposeMenu);

		/************************* Track Menu *************************/
		JMenu trackMenu = new JMenu(appText("menu.track"));
		menuBar.add(trackMenu);

		JMenuItem addTrackMenu = createMenuItem(appText("menu.addTrack"), ActionDispatcher.ADD_TRACK);
		noplayFunctions.add(addTrackMenu);
		addTrackMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		trackMenu.add(addTrackMenu);

		JMenuItem removeTrackMenu = createMenuItem(appText("menu.removeTrack"), ActionDispatcher.REMOVE_TRACK);
		noplayFunctions.add(removeTrackMenu);
		trackMenu.add(removeTrackMenu);

		trackMenu.add(new JSeparator());

		JMenuItem trackPropertyMenu = createMenuItem(appText("menu.trackProperty"), ActionDispatcher.TRACK_PROPERTY);
		noplayFunctions.add(trackPropertyMenu);
		trackPropertyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));
		trackMenu.add(trackPropertyMenu);

		trackMenu.add(new JSeparator());

		JMenuItem mmlImportMenu = createMenuItem(appText("menu.mml_import"), ActionDispatcher.MML_IMPORT);
		noplayFunctions.add(mmlImportMenu);
		mmlImportMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		trackMenu.add(mmlImportMenu);

		JMenuItem mmlExportMenu = createMenuItem(appText("menu.mml_export"), ActionDispatcher.MML_EXPORT);
		noplayFunctions.add(mmlExportMenu);
		mmlExportMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		trackMenu.add(mmlExportMenu);

		/************************* Play Menu *************************/
		JMenu playMenu = new JMenu(appText("menu.operate"));
		menuBar.add(playMenu);

		JMenuItem headPlayPositionMenuItem = createMenuItem(appText("menu.head"), ActionDispatcher.SET_START_POSITION, appText("menu.head.icon"));
		headPlayPositionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		playMenu.add(headPlayPositionMenuItem);

		JMenuItem playMenuItem = createMenuItem(appText("menu.play"), ActionDispatcher.PLAY, appText("menu.play.icon"));
		playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		playMenu.add(playMenuItem);

		JMenuItem stopMenuItem = createMenuItem(appText("menu.stop"), ActionDispatcher.STOP, appText("menu.stop.icon"));
		stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		playMenu.add(stopMenuItem);

		JMenuItem pauseMenuItem = createMenuItem(appText("menu.pause"), ActionDispatcher.PAUSE, appText("menu.pause.icon"));
		playMenu.add(pauseMenuItem);

		playMenu.add(new JSeparator());	

		JMenuItem prevMenuItem = createMenuItem(appText("menu.prev"), ActionDispatcher.PREV_TIME, appText("menu.prev.icon"));
		prevMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		playMenu.add(prevMenuItem);

		JMenuItem nextMenuItem = createMenuItem(appText("menu.next"), ActionDispatcher.NEXT_TIME, appText("menu.next.icon"));
		nextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		playMenu.add(nextMenuItem);

		/************************* Setting Menu *************************/
		MabiIccoProperties properties = MabiIccoProperties.getInstance();
		JMenu settingMenu = new JMenu(appText("menu.setting"));
		menuBar.add(settingMenu);
		createNoteHeightMenu(settingMenu);
		createCheckMenu(settingMenu, "clickPlayMenu", properties::getEnableClickPlay, properties::setEnableClickPlay);
		createCheckMenu(settingMenu, "view.marker", properties::getEnableViewMarker, properties::setEnableViewMarker);
		createCheckMenu(settingMenu, "view.range", properties::getViewRage, properties::setViewRage);
		createCheckMenu(settingMenu, "edit.enable", properties::getEnableEdit, properties::setEnableEdit);

		JMenuItem clearDLSMenu = createMenuItem(appText("menu.clear_dls"), ActionDispatcher.CLEAR_DLS);
		settingMenu.add(clearDLSMenu);

		/************************* Help Menu *************************/
		JMenu helpMenu = new JMenu(appText("menu.help"));
		menuBar.add(helpMenu);

		JMenuItem aboutMenuItem = createMenuItem(appText("menu.about"), ActionDispatcher.ABOUT);
		helpMenu.add(aboutMenuItem);

		return menuBar;
	}

	private void createNoteHeightMenu(JMenu settingMenu) {
		JMenu noteHeightMenu = new JMenu(appText("menu.noteHeight"));
		settingMenu.add(noteHeightMenu);

		ButtonGroup group = new ButtonGroup();
		int index = 0;
		for (int value : PianoRollView.NOTE_HEIGHT_TABLE) {
			JCheckBoxMenuItem menu = new JCheckBoxMenuItem(value+"px");
			menu.setActionCommand(ActionDispatcher.CHANGE_NOTE_HEIGHT_INT+(index++));
			menu.addActionListener(listener);
			noteHeightMenu.add(menu);
			group.add(menu);
		}

		MabiIccoProperties properties = MabiIccoProperties.getInstance();
		index = properties.getPianoRollViewHeightScaleProperty();
		Collections.list(group.getElements()).get(index).setSelected(true);
	}

	/**
	 * 有効/無効の設定ボタンを作成します.
	 * @param settingMenu
	 */
	private void createCheckMenu(JMenu settingMenu, String itemName, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		JCheckBoxMenuItem clickPlayMenu = new JCheckBoxMenuItem(appText(itemName));
		settingMenu.add(clickPlayMenu);

		clickPlayMenu.setSelected( getter.get() );

		clickPlayMenu.addActionListener((e) -> {
			boolean b = getter.get();
			setter.accept(!b);
			clickPlayMenu.setSelected(!b);
			if (mmlSeqView != null) {
				mmlSeqView.repaint();
			}
		});
	}

	private JButton createToolButton(String title, String iconName, String command) {
		JButton button = new JButton("");
		if (iconName == null) {
			button.setText(appText(title));
		} else {
			button.setToolTipText(appText(title));
			button.setIcon(AppResource.getImageIcon(appText(iconName)));
		}
		button.setFocusable(false);
		button.addActionListener(listener);
		button.setActionCommand(command);

		return button;
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();

		JButton newFileButton = createToolButton("menu.newFile", "menu.newFile.icon", ActionDispatcher.NEW_FILE);
		noplayFunctions.add(newFileButton);
		toolBar.add(newFileButton);

		JButton openFileButton = createToolButton("menu.openFile", "menu.openFile.icon", ActionDispatcher.FILE_OPEN);
		noplayFunctions.add(openFileButton);
		toolBar.add(openFileButton);

		toolBar.add(newToolBarSeparator());

		JButton startPositionButton = createToolButton("menu.head", "menu.head.icon", ActionDispatcher.SET_START_POSITION);
		toolBar.add(startPositionButton);

		JButton prevPositionButton = createToolButton("menu.prev", "menu.prev.icon", ActionDispatcher.PREV_TIME);
		toolBar.add(prevPositionButton);

		JButton playButton = createToolButton("menu.play", "menu.play.icon", ActionDispatcher.PLAY);
		toolBar.add(playButton);

		JButton nextPositionButton = createToolButton("menu.next", "menu.next.icon", ActionDispatcher.NEXT_TIME);
		toolBar.add(nextPositionButton);

		JButton pauseButton = createToolButton("menu.pause", "menu.pause.icon", ActionDispatcher.PAUSE);
		toolBar.add(pauseButton);

		JButton stopButton = createToolButton("menu.stop", "menu.stop.icon", ActionDispatcher.STOP);
		toolBar.add(stopButton);

		toolBar.add(newToolBarSeparator());

		JButton inputClipButton = createToolButton("menu.inputMML", "menu.inputMML.icon", ActionDispatcher.INPUT_FROM_CLIPBOARD);
		noplayFunctions.add(inputClipButton);
		toolBar.add(inputClipButton);

		JButton outputClipButton = createToolButton("menu.outputMML", "menu.outputMML.icon", ActionDispatcher.OUTPUT_TO_CLIPBOARD);
		toolBar.add(outputClipButton);

		toolBar.add(newToolBarSeparator());

		// ビューの拡大/縮小ツールボタン
		JButton expandButton = createToolButton("view.scale.up", null, ActionDispatcher.VIEW_SCALE_UP);
		toolBar.add(expandButton);

		JButton reduceButton = createToolButton("view.scale.down", null, ActionDispatcher.VIEW_SCALE_DOWN);
		toolBar.add(reduceButton);

		// 編集ノートタイプ
		noteTypeSelect.setFocusable(false);
		noteTypeSelect.addActionListener(this); // MainFrameでAction処理します.
		noteTypeSelect.setSelectedIndex(MMLEditor.DEFAULT_ALIGN_INDEX);
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

		if (properties.getWindowMaximize()) {
			this.setExtendedState(MAXIMIZED_BOTH);
		}

	}

	private void updateWindowProperties() {
		int extendedState = this.getExtendedState();
		MabiIccoProperties properties = MabiIccoProperties.getInstance();

		if ( extendedState == MAXIMIZED_BOTH ) {
			properties.setWindowMaximize(true);
		} else {
			properties.setWindowMaximize(false);
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

	@Override
	public void trackEndNotify() {
		enableNoplayItems();
	}

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
	public void actionPerformed(ActionEvent e) 
	{
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
	 * @param b
	 */
	public void setCanReloadFile(boolean b) {
		reloadMenuItem.setEnabled(b);
	}

	/**
	 * Undo-UIの有効化
	 * @param b
	 */
	public void setCanUndo(boolean b) {
		undoMenu.setEnabled(b);
	}

	/**
	 * Redo-UIの有効化
	 * @param b
	 */
	public void setCanRedo(boolean b) {
		redoMenu.setEnabled(b);
	}

	/**
	 * 上書き保存UIの有効化
	 * @param b
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
}

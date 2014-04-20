/*
 * Copyright (C) 2013-2014 たんらる
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
import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.INotifyTrackEnd;
import fourthline.mabiicco.midi.MabiDLS;
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
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import java.awt.Color;

import javax.swing.JComboBox;


public class MainFrame extends JFrame implements ComponentListener, INotifyTrackEnd, ActionListener {
	private static final long serialVersionUID = -7484797594534384422L;

	private final JPanel contentPane;
	private final JTextField statusField;
	private final MMLSeqView mmlSeqView;
	@SuppressWarnings("rawtypes")
	private final JComboBox noteTypeSelect;
	private final JLabel timeView;

	private final ActionListener listener;

	private final String DEFAULT_TITLE = " * MabiIcco *";

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

	private final ResourceBundle bundle = ResourceBundle.getBundle("mainFrame");
	private String text(String key) {
		try {
			return bundle.getString(key);
		} catch (java.util.MissingResourceException e) {
			return "(null)";
		}
	}

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

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		northPanel.add(toolBar);

		JButton newFileButton = new JButton("");
		newFileButton.setToolTipText(text("menu.newFile"));
		newFileButton.setIcon(new ImageIcon(MainFrame.class.getResource(text("menu.newFile.icon"))));
		noplayFunctions.add(newFileButton);
		newFileButton.setFocusable(false);
		newFileButton.addActionListener(listener);
		newFileButton.setActionCommand(ActionDispatcher.NEW_FILE);
		toolBar.add(newFileButton);

		JButton openFileButton = new JButton("");
		openFileButton.setToolTipText(text("menu.openFile"));
		openFileButton.setIcon(new ImageIcon(MainFrame.class.getResource(text("menu.openFile.icon"))));
		noplayFunctions.add(openFileButton);
		openFileButton.setFocusable(false);
		openFileButton.addActionListener(listener);
		openFileButton.setActionCommand(ActionDispatcher.FILE_OPEN);
		toolBar.add(openFileButton);

		toolBar.add(newToolBarSeparator());

		JButton startPositionButton = new JButton("");
		toolBar.add(startPositionButton);
		startPositionButton.setToolTipText(text("menu.head"));
		startPositionButton.setIcon(new ImageIcon(MainFrame.class.getResource(text("menu.head.icon"))));
		startPositionButton.setFocusable(false);
		startPositionButton.addActionListener(listener);
		startPositionButton.setActionCommand(ActionDispatcher.SET_START_POSITION);

		JButton prevPositionButton = new JButton("");
		toolBar.add(prevPositionButton);
		prevPositionButton.setToolTipText(text("menu.prev"));
		prevPositionButton.setIcon(new ImageIcon(MainFrame.class.getResource(text("menu.prev.icon"))));
		prevPositionButton.setFocusable(false);
		prevPositionButton.addActionListener(listener);
		prevPositionButton.setActionCommand(ActionDispatcher.PREV_TIME);

		JButton playButton = new JButton("");
		toolBar.add(playButton);
		playButton.setToolTipText(text("menu.play"));
		playButton.setIcon(new ImageIcon(MainFrame.class.getResource(text("menu.play.icon"))));
		playButton.setFocusable(false);
		playButton.addActionListener(listener);
		playButton.setActionCommand(ActionDispatcher.PLAY);

		JButton nextPositionButton = new JButton("");
		toolBar.add(nextPositionButton);
		nextPositionButton.setToolTipText(text("menu.next"));
		nextPositionButton.setIcon(new ImageIcon(MainFrame.class.getResource(text("menu.next.icon"))));
		nextPositionButton.setFocusable(false);
		nextPositionButton.addActionListener(listener);
		nextPositionButton.setActionCommand(ActionDispatcher.NEXT_TIME);

		JButton pauseButton = new JButton("");
		pauseButton.setToolTipText(text("menu.pause"));
		pauseButton.setIcon(new ImageIcon(MainFrame.class.getResource(text("menu.pause.icon"))));
		pauseButton.setFocusable(false);
		pauseButton.addActionListener(listener);
		pauseButton.setActionCommand(ActionDispatcher.PAUSE);
		toolBar.add(pauseButton);

		JButton stopButton = new JButton("");
		toolBar.add(stopButton);
		stopButton.setToolTipText(text("menu.stop"));
		stopButton.setIcon(new ImageIcon(MainFrame.class.getResource(text("menu.stop.icon"))));
		stopButton.setFocusable(false);
		stopButton.addActionListener(listener);
		stopButton.setActionCommand(ActionDispatcher.STOP);

		toolBar.add(newToolBarSeparator());

		JButton inputClipButton = new JButton(text("menu.inputMML"));
		noplayFunctions.add(inputClipButton);
		inputClipButton.setFocusable(false);
		inputClipButton.addActionListener(listener);
		inputClipButton.setActionCommand(ActionDispatcher.INPUT_FROM_CLIPBOARD);
		toolBar.add(inputClipButton);

		JButton outputClipButton = new JButton(text("menu.outputMML"));
		outputClipButton.setFocusable(false);
		outputClipButton.addActionListener(listener);
		outputClipButton.setActionCommand(ActionDispatcher.OUTPUT_TO_CLIPBOARD);
		toolBar.add(outputClipButton);

		toolBar.add(newToolBarSeparator());

		// ビューの拡大/縮小ツールボタン
		JButton expandButton = new JButton(text("view.scale.up"));
		expandButton.setFocusable(false);
		expandButton.addActionListener(listener);
		expandButton.setActionCommand(ActionDispatcher.VIEW_SCALE_UP);
		toolBar.add(expandButton);

		JButton reduceButton = new JButton(text("view.scale.down"));
		reduceButton.setFocusable(false);
		reduceButton.addActionListener(listener);
		reduceButton.setActionCommand(ActionDispatcher.VIEW_SCALE_DOWN);
		toolBar.add(reduceButton);

		noteTypeSelect = new JComboBox<>(MMLEditor.createAlignList());
		noteTypeSelect.setFocusable(false);
		toolBar.add(noteTypeSelect);

		toolBar.add(newToolBarSeparator());
		timeView = new JLabel("time MM:SS/MM:SS (120)");
		timeView.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		toolBar.add(timeView);

		mmlSeqView = new MMLSeqView();
		mmlSeqView.setTimeView(timeView);
		contentPane.add(mmlSeqView, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		contentPane.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BorderLayout(0, 0));

		statusField = new JTextField();
		statusField.setEditable(false);
		southPanel.add(statusField, BorderLayout.SOUTH);
		statusField.setColumns(10);

		MabiDLS.getInstance().setTrackEndNotifier(this);

		noteTypeSelect.addActionListener(this); // MainFrameでAction処理します.
		noteTypeSelect.setSelectedIndex(MMLEditor.DEFAULT_ALIGN_INDEX);
		setEditAlign();

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
			menuItem.setIcon(new ImageIcon(MainFrame.class.getResource(iconName)));
		} catch (NullPointerException e) {}

		return menuItem;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		// FIXME:
		/************************* File Menu *************************/
		JMenu fileMenu = new JMenu(text("menu.file"));
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		JMenuItem newFileMenuItem = createMenuItem(text("menu.newFile"), ActionDispatcher.NEW_FILE, text("menu.newFile.icon"));
		noplayFunctions.add(newFileMenuItem);
		newFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		fileMenu.add(newFileMenuItem);

		JMenuItem fileOpenMenuItem = createMenuItem(text("menu.openFile"), ActionDispatcher.FILE_OPEN, text("menu.openFile.icon"));
		noplayFunctions.add(fileOpenMenuItem);
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileMenu.add(fileOpenMenuItem);

		reloadMenuItem = createMenuItem(text("menu.reloadFile"), ActionDispatcher.RELOAD_FILE);
		noplayFunctions.add(reloadMenuItem);
		fileMenu.add(reloadMenuItem);

		saveMenuItem = createMenuItem(text("menu.saveFile"), ActionDispatcher.SAVE_FILE);
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		fileMenu.add(saveMenuItem);

		JMenuItem saveAsMenuItem = createMenuItem(text("menu.saveAsFile"), ActionDispatcher.SAVEAS_FILE);
		noplayFunctions.add(saveAsMenuItem);
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_DOWN_MASK));
		fileMenu.add(saveAsMenuItem);

		fileMenu.add(new JSeparator());

		JMenuItem scorePropertyMenu = createMenuItem(text("menu.scoreProperty"), ActionDispatcher.SCORE_PROPERTY);
		noplayFunctions.add(scorePropertyMenu);
		fileMenu.add(scorePropertyMenu);

		fileMenu.add(new JSeparator());

		JMenuItem exitMenuItem = createMenuItem(text("menu.quit"), ActionDispatcher.QUIT);
		fileMenu.add(exitMenuItem);

		/************************* Edit Menu *************************/
		JMenu editMenu = new JMenu(text("menu.edit"));
		fileMenu.setMnemonic('E');
		menuBar.add(editMenu);

		undoMenu = createMenuItem(text("menu.undo"), ActionDispatcher.UNDO);
		undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		editMenu.add(undoMenu);

		redoMenu = createMenuItem(text("menu.redo"), ActionDispatcher.REDO);
		redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		editMenu.add(redoMenu);

		editMenu.add(new JSeparator());	

		cutMenu = createMenuItem(text("menu.cut"), ActionDispatcher.CUT);
		cutMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		editMenu.add(cutMenu);

		copyMenu = createMenuItem(text("menu.copy"), ActionDispatcher.COPY);
		copyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		editMenu.add(copyMenu);

		pasteMenu = createMenuItem(text("menu.paste"), ActionDispatcher.PASTE);
		pasteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		editMenu.add(pasteMenu);

		deleteMenu = createMenuItem(text("menu.delete"), ActionDispatcher.DELETE);
		deleteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		editMenu.add(deleteMenu);

		editMenu.add(new JSeparator());

		JMenuItem partChangeMenu = createMenuItem(text("menu.changePart"), ActionDispatcher.PART_CHANGE);
		noplayFunctions.add(partChangeMenu);
		editMenu.add(partChangeMenu);

		JMenuItem addMeasureMenu = createMenuItem(text("menu.addMeasure"), ActionDispatcher.ADD_MEASURE);
		noplayFunctions.add(addMeasureMenu);
		editMenu.add(addMeasureMenu);

		JMenuItem removeMeasureMenu = createMenuItem(text("menu.removeMeasure"), ActionDispatcher.REMOVE_MEASURE);
		noplayFunctions.add(removeMeasureMenu);
		editMenu.add(removeMeasureMenu);

		/************************* Track Menu *************************/
		JMenu trackMenu = new JMenu(text("menu.track"));
		menuBar.add(trackMenu);

		JMenuItem addTrackMenu = createMenuItem(text("menu.addTrack"), ActionDispatcher.ADD_TRACK);
		noplayFunctions.add(addTrackMenu);
		addTrackMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		trackMenu.add(addTrackMenu);

		JMenuItem removeTrackMenu = createMenuItem(text("menu.removeTrack"), ActionDispatcher.REMOVE_TRACK);
		noplayFunctions.add(removeTrackMenu);
		trackMenu.add(removeTrackMenu);

		trackMenu.add(new JSeparator());

		JMenuItem trackPropertyMenu = createMenuItem(text("menu.trackProperty"), ActionDispatcher.TRACK_PROPERTY);
		noplayFunctions.add(trackPropertyMenu);
		trackPropertyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));
		trackMenu.add(trackPropertyMenu);

		/************************* Play Menu *************************/
		JMenu playMenu = new JMenu(text("menu.operate"));
		menuBar.add(playMenu);

		JMenuItem headPlayPositionMenuItem = createMenuItem(text("menu.head"), ActionDispatcher.SET_START_POSITION, text("menu.head.icon"));
		headPlayPositionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		playMenu.add(headPlayPositionMenuItem);

		JMenuItem playMenuItem = createMenuItem(text("menu.play"), ActionDispatcher.PLAY, text("menu.play.icon"));
		playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		playMenu.add(playMenuItem);

		JMenuItem stopMenuItem = createMenuItem(text("menu.stop"), ActionDispatcher.STOP, text("menu.stop.icon"));
		stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		playMenu.add(stopMenuItem);

		JMenuItem pauseMenuItem = createMenuItem(text("menu.pause"), ActionDispatcher.PAUSE, text("menu.pause.icon"));
		playMenu.add(pauseMenuItem);

		playMenu.add(new JSeparator());	

		JMenuItem prevMenuItem = createMenuItem(text("menu.prev"), ActionDispatcher.PREV_TIME, text("menu.prev.icon"));
		prevMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		playMenu.add(prevMenuItem);

		JMenuItem nextMenuItem = createMenuItem(text("menu.next"), ActionDispatcher.NEXT_TIME, text("menu.next.icon"));
		nextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		playMenu.add(nextMenuItem);

		/************************* Setting Menu *************************/
		JMenu settingMenu = new JMenu(text("menu.setting"));
		menuBar.add(settingMenu);
		createNoteHeightMenu(settingMenu);

		return menuBar;
	}

	private void createNoteHeightMenu(JMenu settingMenu) {
		JMenu noteHeightMenu = new JMenu(text("menu.noteHeight"));
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
		setTitle(DEFAULT_TITLE + " [" + fileTitle + "]");
	}

	private void loadWindowPeoperties() {
		MabiIccoProperties properties = MabiIccoProperties.getInstance();

		Rectangle rect = properties.getWindowRect();
		if (rect.getX() < 0.0) {
			setSize(640, 480);
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
		MabiDLS.getInstance().clearAllChannelPanpot();
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
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				mmlSeqView.resetViewPosition();
				mmlSeqView.repaint();
			}
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

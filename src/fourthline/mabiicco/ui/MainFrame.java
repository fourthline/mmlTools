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
import java.util.ArrayList;
import java.util.Iterator;

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

	/**
	 * 
	 */
	private static final long serialVersionUID = -7484797594534384422L;


	private JPanel contentPane;
	private JTextField statusField;
	private MMLSeqView mmlSeqView;
	@SuppressWarnings("rawtypes")
	private JComboBox noteTypeSelect;
	private JLabel timeView;

	private ActionListener listener;

	private final String DEFAULT_TITLE = " * MabiIcco *";

	/** シーケンス再生中に無効化する機能のリスト */
	ArrayList<JComponent> noplayFunctions = new ArrayList<JComponent>();

	/**
	 * Create the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		newFileButton.setToolTipText("新規作成");
		newFileButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/file.png")));
		noplayFunctions.add(newFileButton);
		newFileButton.setFocusable(false);
		newFileButton.addActionListener(listener);
		newFileButton.setActionCommand(ActionDispatcher.NEW_FILE);
		toolBar.add(newFileButton);

		JButton openFileButton = new JButton("");
		openFileButton.setToolTipText("開く");
		openFileButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/open.png")));
		noplayFunctions.add(openFileButton);
		openFileButton.setFocusable(false);
		openFileButton.addActionListener(listener);
		openFileButton.setActionCommand(ActionDispatcher.FILE_OPEN);
		toolBar.add(openFileButton);

		toolBar.add(newToolBarSeparator());

		JButton startPositionButton = new JButton("");
		toolBar.add(startPositionButton);
		startPositionButton.setToolTipText("先頭へ戻す");
		startPositionButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/head.png")));
		startPositionButton.setFocusable(false);
		startPositionButton.addActionListener(listener);
		startPositionButton.setActionCommand(ActionDispatcher.SET_START_POSITION);

		JButton playButton = new JButton("");
		toolBar.add(playButton);
		playButton.setToolTipText("再生");
		playButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/playButton.png")));
		playButton.setFocusable(false);
		playButton.addActionListener(listener);
		playButton.setActionCommand(ActionDispatcher.PLAY);

		JButton pauseButton = new JButton("");
		pauseButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/pause.png")));
		pauseButton.setToolTipText("一時停止");
		pauseButton.setFocusable(false);
		pauseButton.addActionListener(listener);
		pauseButton.setActionCommand(ActionDispatcher.PAUSE);
		toolBar.add(pauseButton);

		JButton stopButton = new JButton("");
		toolBar.add(stopButton);
		stopButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/stop.png")));
		stopButton.setToolTipText("停止");
		stopButton.setFocusable(false);
		stopButton.addActionListener(listener);
		stopButton.setActionCommand(ActionDispatcher.STOP);

		toolBar.add(newToolBarSeparator());

		JButton inputClipButton = new JButton("クリップボードから入力");
		noplayFunctions.add(inputClipButton);
		inputClipButton.setFocusable(false);
		inputClipButton.addActionListener(listener);
		inputClipButton.setActionCommand(ActionDispatcher.INPUT_FROM_CLIPBOARD);
		toolBar.add(inputClipButton);

		JButton outputClipButton = new JButton("クリップボードへ出力");
		outputClipButton.setFocusable(false);
		outputClipButton.addActionListener(listener);
		outputClipButton.setActionCommand(ActionDispatcher.OUTPUT_TO_CLIPBOARD);
		toolBar.add(outputClipButton);

		toolBar.add(newToolBarSeparator());

		// ビューの拡大/縮小ツールボタン
		JButton expandButton = new JButton("拡大");
		expandButton.setFocusable(false);
		expandButton.addActionListener(listener);
		expandButton.setActionCommand("viewExpand");
		toolBar.add(expandButton);

		JButton reduceButton = new JButton("縮小");
		reduceButton.setFocusable(false);
		reduceButton.addActionListener(listener);
		reduceButton.setActionCommand("viewReduce");
		toolBar.add(reduceButton);

		noteTypeSelect = new JComboBox(MMLEditor.createAlignList());
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
	}

	private JMenuItem createMenuItem(String name, String actionCommand) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.addActionListener(listener);
		menuItem.setActionCommand(actionCommand);

		return menuItem;
	}

	private JMenuItem createMenuItem(String name, String actionCommand, String iconName) {
		JMenuItem menuItem = createMenuItem(name, actionCommand);
		menuItem.setIcon(new ImageIcon(MainFrame.class.getResource(iconName)));

		return menuItem;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		// FIXME:
		/************************* File Menu *************************/
		JMenu fileMenu = new JMenu("ファイル");
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		JMenuItem newFileMenuItem = createMenuItem("新規作成", ActionDispatcher.NEW_FILE, "/img/file.png");
		noplayFunctions.add(newFileMenuItem);
		newFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		fileMenu.add(newFileMenuItem);

		JMenuItem fileOpenMenuItem = createMenuItem("開く", ActionDispatcher.FILE_OPEN, "/img/open.png");
		noplayFunctions.add(fileOpenMenuItem);
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileMenu.add(fileOpenMenuItem);

		JMenuItem reloadMenuItem = createMenuItem("再読み込み", ActionDispatcher.RELOAD_FILE);
		noplayFunctions.add(reloadMenuItem);
		fileMenu.add(reloadMenuItem);

		JMenuItem saveMenuItem = createMenuItem("上書き保存", ActionDispatcher.SAVE_FILE);
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveMenuItem.setEnabled(false);
		fileMenu.add(saveMenuItem);

		JMenuItem saveAsMenuItem = createMenuItem("名前を付けて保存", ActionDispatcher.SAVEAS_FILE);
		noplayFunctions.add(saveAsMenuItem);
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_DOWN_MASK));
		fileMenu.add(saveAsMenuItem);

		fileMenu.add(new JSeparator());

		JMenuItem exitMenuItem = createMenuItem("終了", ActionDispatcher.QUIT);
		fileMenu.add(exitMenuItem);

		/************************* Edit Menu *************************/
		JMenu editMenu = new JMenu("編集");
		fileMenu.setMnemonic('E');
		menuBar.add(editMenu);

		JMenuItem undoMenu = createMenuItem("Undo", ActionDispatcher.UNDO);
		undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		editMenu.add(undoMenu);

		JMenuItem redoMenu = createMenuItem("Redo", ActionDispatcher.REDO);
		redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		editMenu.add(redoMenu);

		/************************* Track Menu *************************/
		JMenu trackMenu = new JMenu("トラック");
		menuBar.add(trackMenu);

		JMenuItem addTrackMenu = createMenuItem("トラック追加", ActionDispatcher.ADD_TRACK);
		noplayFunctions.add(addTrackMenu);
		addTrackMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		trackMenu.add(addTrackMenu);

		JMenuItem removeTrackMenu = createMenuItem("トラック削除", ActionDispatcher.REMOVE_TRACK);
		noplayFunctions.add(removeTrackMenu);
		trackMenu.add(removeTrackMenu);

		trackMenu.add(new JSeparator());

		JMenuItem trackPropertyMenu = createMenuItem("トラックプロパティ", ActionDispatcher.TRACK_PROPERTY);
		noplayFunctions.add(trackPropertyMenu);
		trackPropertyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));
		trackMenu.add(trackPropertyMenu);

		/************************* Play Menu *************************/
		JMenu playMenu = new JMenu("操作");
		menuBar.add(playMenu);

		JMenuItem headPlayPositionMenuItem = createMenuItem("先頭へ戻す", ActionDispatcher.SET_START_POSITION, "/img/head.png");
		headPlayPositionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		playMenu.add(headPlayPositionMenuItem);

		JMenuItem playMenuItem = createMenuItem("再生", ActionDispatcher.PLAY, "/img/playButton.png");
		playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		playMenu.add(playMenuItem);

		JMenuItem stopMenuItem = createMenuItem("停止", ActionDispatcher.STOP, "/img/stop.png");
		stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		playMenu.add(stopMenuItem);

		JMenuItem pauseMenuItem = createMenuItem("一時停止", ActionDispatcher.PAUSE, "/img/pause.png");
		playMenu.add(pauseMenuItem);

		return menuBar;
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
		for (Iterator<JComponent> i = noplayFunctions.iterator(); i.hasNext(); ) {
			JComponent component = i.next();
			component.setEnabled(false);
		}
	}

	/**
	 * 再生中に無効化されている機能を有効にする。
	 */
	public void enableNoplayItems() {
		for (Iterator<JComponent> i = noplayFunctions.iterator(); i.hasNext(); ) {
			JComponent component = i.next();
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
}

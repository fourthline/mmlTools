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

		reloadMenuItem = createMenuItem("再読み込み", ActionDispatcher.RELOAD_FILE);
		noplayFunctions.add(reloadMenuItem);
		fileMenu.add(reloadMenuItem);

		saveMenuItem = createMenuItem("上書き保存", ActionDispatcher.SAVE_FILE);
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		fileMenu.add(saveMenuItem);

		JMenuItem saveAsMenuItem = createMenuItem("名前を付けて保存", ActionDispatcher.SAVEAS_FILE);
		noplayFunctions.add(saveAsMenuItem);
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_DOWN_MASK));
		fileMenu.add(saveAsMenuItem);

		fileMenu.add(new JSeparator());

		JMenuItem scorePropertyMenu = createMenuItem("プロパティ", ActionDispatcher.SCORE_PROPERTY);
		noplayFunctions.add(scorePropertyMenu);
		fileMenu.add(scorePropertyMenu);

		fileMenu.add(new JSeparator());

		JMenuItem exitMenuItem = createMenuItem("終了", ActionDispatcher.QUIT);
		fileMenu.add(exitMenuItem);

		/************************* Edit Menu *************************/
		JMenu editMenu = new JMenu("編集");
		fileMenu.setMnemonic('E');
		menuBar.add(editMenu);

		undoMenu = createMenuItem("元に戻す", ActionDispatcher.UNDO);
		undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		editMenu.add(undoMenu);

		redoMenu = createMenuItem("やり直す", ActionDispatcher.REDO);
		redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		editMenu.add(redoMenu);

		editMenu.add(new JSeparator());	

		cutMenu = createMenuItem("切り取り", ActionDispatcher.CUT);
		cutMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		editMenu.add(cutMenu);

		copyMenu = createMenuItem("コピー", ActionDispatcher.COPY);
		copyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		editMenu.add(copyMenu);

		pasteMenu = createMenuItem("貼り付け", ActionDispatcher.PASTE);
		pasteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		editMenu.add(pasteMenu);

		deleteMenu = createMenuItem("削除", ActionDispatcher.DELETE);
		deleteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		editMenu.add(deleteMenu);

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

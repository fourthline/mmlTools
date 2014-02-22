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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.INotifyTrackEnd;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.editor.MMLEditor;
import fourthline.mabiicco.ui.editor.NoteAlign;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.parser.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

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

	// action commands
	private static final String VIEW_EXPAND = "viewExpand";
	private static final String VIEW_REDUCE = "viewReduce";
	private static final String PLAY = "play";
	private static final String STOP = "stop";
	private static final String PAUSE = "pause";
	private static final String FILE_OPEN = "fileOpen";
	private static final String NEW_FILE = "newFile";
	private static final String RELOAD_FILE = "reloadFile";
	private static final String QUIT = "quit";
	private static final String ADD_TRACK = "addTrack";
	private static final String REMOVE_TRACK = "removeTrack";
	private static final String TRACK_PROPERTY = "trackProperty";
	private static final String SET_START_POSITION = "setStartPosition";
	private static final String INPUT_FROM_CLIPBOARD = "inputFromClipboard";
	private static final String OUTPUT_TO_CLIPBOARD = "outputToClipboard";
	private static final String UNDO = "undo";
	private static final String REDO = "redo";

	private JPanel contentPane;
	private JTextField statusField;
	private MMLSeqView mmlSeqView;
	@SuppressWarnings("rawtypes")
	private JComboBox noteTypeSelect;
	private JLabel timeView;

	private final String DEFAULT_TITLE = " * MabiIcco *";

	private File openedFile = null;

	/** シーケンス再生中に無効化する機能のリスト */
	ArrayList<JComponent> noplayFunctions = new ArrayList<JComponent>();

	/**
	 * Create the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MainFrame() {
		setTitleAndFile(null);
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
		newFileButton.addActionListener(this);
		newFileButton.setActionCommand(NEW_FILE);
		toolBar.add(newFileButton);

		JButton openFileButton = new JButton("");
		openFileButton.setToolTipText("開く");
		openFileButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/open.png")));
		noplayFunctions.add(openFileButton);
		openFileButton.setFocusable(false);
		openFileButton.addActionListener(this);
		openFileButton.setActionCommand(FILE_OPEN);
		toolBar.add(openFileButton);

		toolBar.add(newToolBarSeparator());

		JButton startPositionButton = new JButton("");
		toolBar.add(startPositionButton);
		startPositionButton.setToolTipText("先頭へ戻す");
		startPositionButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/head.png")));
		startPositionButton.setFocusable(false);
		startPositionButton.addActionListener(this);
		startPositionButton.setActionCommand(SET_START_POSITION);

		JButton playButton = new JButton("");
		toolBar.add(playButton);
		playButton.setToolTipText("再生");
		playButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/playButton.png")));
		playButton.setFocusable(false);
		playButton.addActionListener(this);
		playButton.setActionCommand(PLAY);

		JButton pauseButton = new JButton("");
		pauseButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/pause.png")));
		pauseButton.setToolTipText("一時停止");
		pauseButton.setFocusable(false);
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand(PAUSE);
		toolBar.add(pauseButton);

		JButton stopButton = new JButton("");
		toolBar.add(stopButton);
		stopButton.setIcon(new ImageIcon(MainFrame.class.getResource("/img/stop.png")));
		stopButton.setToolTipText("停止");
		stopButton.setFocusable(false);
		stopButton.addActionListener(this);
		stopButton.setActionCommand(STOP);

		toolBar.add(newToolBarSeparator());

		JButton inputClipButton = new JButton("クリップボードから入力");
		noplayFunctions.add(inputClipButton);
		inputClipButton.setFocusable(false);
		inputClipButton.addActionListener(this);
		inputClipButton.setActionCommand(INPUT_FROM_CLIPBOARD);
		toolBar.add(inputClipButton);

		JButton outputClipButton = new JButton("クリップボードへ出力");
		noplayFunctions.add(outputClipButton);
		outputClipButton.setFocusable(false);
		outputClipButton.addActionListener(this);
		outputClipButton.setActionCommand(OUTPUT_TO_CLIPBOARD);
		toolBar.add(outputClipButton);

		toolBar.add(newToolBarSeparator());

		// ビューの拡大/縮小ツールボタン
		JButton expandButton = new JButton("拡大");
		expandButton.setFocusable(false);
		expandButton.addActionListener(this);
		expandButton.setActionCommand("viewExpand");
		toolBar.add(expandButton);

		JButton reduceButton = new JButton("縮小");
		reduceButton.setFocusable(false);
		reduceButton.addActionListener(this);
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

		noteTypeSelect.addActionListener(this);
		noteTypeSelect.setSelectedIndex(MMLEditor.DEFAULT_ALIGN_INDEX);
		setEditAlign();
	}

	private JMenuItem createMenuItem(String name, String actionCommand) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.addActionListener(this);
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

		JMenuItem newFileMenuItem = createMenuItem("新規作成", NEW_FILE, "/img/file.png");
		noplayFunctions.add(newFileMenuItem);
		newFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		fileMenu.add(newFileMenuItem);

		JMenuItem fileOpenMenuItem = createMenuItem("開く", FILE_OPEN, "/img/open.png");
		noplayFunctions.add(fileOpenMenuItem);
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileMenu.add(fileOpenMenuItem);

		JMenuItem reloadMenuItem = createMenuItem("再読み込み", RELOAD_FILE);
		noplayFunctions.add(reloadMenuItem);
		fileMenu.add(reloadMenuItem);

		fileMenu.add(new JSeparator());

		JMenuItem exitMenuItem = createMenuItem("終了", QUIT);
		fileMenu.add(exitMenuItem);

		/************************* Edit Menu *************************/
		JMenu editMenu = new JMenu("編集");
		fileMenu.setMnemonic('E');
		menuBar.add(editMenu);

		JMenuItem undoMenu = createMenuItem("Undo", UNDO);
		undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		editMenu.add(undoMenu);

		JMenuItem redoMenu = createMenuItem("Redo", REDO);
		redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		editMenu.add(redoMenu);

		/************************* Track Menu *************************/
		JMenu trackMenu = new JMenu("トラック");
		menuBar.add(trackMenu);

		JMenuItem addTrackMenu = createMenuItem("トラック追加", ADD_TRACK);
		noplayFunctions.add(addTrackMenu);
		addTrackMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		trackMenu.add(addTrackMenu);

		JMenuItem removeTrackMenu = createMenuItem("トラック削除", REMOVE_TRACK);
		noplayFunctions.add(removeTrackMenu);
		trackMenu.add(removeTrackMenu);

		trackMenu.add(new JSeparator());

		JMenuItem trackPropertyMenu = createMenuItem("トラックプロパティ", TRACK_PROPERTY);
		noplayFunctions.add(trackPropertyMenu);
		trackPropertyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));
		trackMenu.add(trackPropertyMenu);

		/************************* Play Menu *************************/
		JMenu playMenu = new JMenu("操作");
		menuBar.add(playMenu);

		JMenuItem headPlayPositionMenuItem = createMenuItem("先頭へ戻す", SET_START_POSITION, "/img/head.png");
		headPlayPositionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		playMenu.add(headPlayPositionMenuItem);

		JMenuItem playMenuItem = createMenuItem("再生", PLAY, "/img/playButton.png");
		playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		playMenu.add(playMenuItem);

		JMenuItem stopMenuItem = createMenuItem("停止", STOP, "/img/stop.png");
		stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		playMenu.add(stopMenuItem);

		JMenuItem pauseMenuItem = createMenuItem("一時停止", PAUSE, "/img/pause.png");
		playMenu.add(pauseMenuItem);

		return menuBar;
	}

	private JSeparator newToolBarSeparator() {
		JSeparator separator = new JToolBar.Separator();
		separator.setForeground(Color.DARK_GRAY);
		return separator;
	}

	private void setTitleAndFile(File file) {
		String fileTitle = "";

		if (file != null) {
			fileTitle = file.getName() + " (read only)";
		}
		setTitle(DEFAULT_TITLE + " [" + fileTitle + "]");
	}

	private void openMMLFile(File file) {
		try {
			IMMLFileParser fileParser = new MMSFile();
			MMLScore score = fileParser.parse(file);
			mmlSeqView.setMMLScore(score);

			setTitleAndFile(file);
			MabiIccoProperties.getInstance().setRecentFile(file.getPath());
		} catch (MMLParseException e) {
			JOptionPane.showMessageDialog(this, "読み込みに失敗しました", "ファイル形式が不正です", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void newMMLFileAction() {
		setTitleAndFile(null);
		openedFile = null;
		mmlSeqView.initializeMMLTrack();
	}

	private void reloadMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			MabiDLS.getInstance().getSequencer().stop();
		}

		if (openedFile != null) {
			openMMLFile(openedFile);
		}
	}

	private void openMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			MabiDLS.getInstance().getSequencer().stop();
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String recentPath = MabiIccoProperties.getInstance().getRecentFile();
				JFileChooser fileChooser = new JFileChooser(new File(recentPath));
				FileFilter mmsFilter = new FileNameExtensionFilter("まきまびしーく形式 (*.mms)", "mms");
				fileChooser.addChoosableFileFilter(mmsFilter);
				fileChooser.setFileFilter(mmsFilter);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int status = fileChooser.showOpenDialog(null);
				if (status == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					openMMLFile(file);
					openedFile = file;
				}
			}
		});
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
	private void disableNoplayItems() {
		for (Iterator<JComponent> i = noplayFunctions.iterator(); i.hasNext(); ) {
			JComponent component = i.next();
			component.setEnabled(false);
		}
	}

	/**
	 * 再生中に無効化されている機能を有効にする。
	 */
	private void enableNoplayItems() {
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
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String command = e.getActionCommand();

		if (source.equals(noteTypeSelect)) {
			setEditAlign();
		} else if (command.equals(VIEW_EXPAND)) {
			mmlSeqView.expandPianoViewWide();
			mmlSeqView.repaint();
		} else if (command.equals(VIEW_REDUCE)) {
			mmlSeqView.reducePianoViewWide();
			mmlSeqView.repaint();
		} else if (command.equals(STOP)) {
			MabiDLS.getInstance().getSequencer().stop();
			MabiDLS.getInstance().clearAllChannelPanpot();
			enableNoplayItems();
		} else if (command.equals(PAUSE)) {
			MabiDLS.getInstance().getSequencer().stop();
			MabiDLS.getInstance().clearAllChannelPanpot();
			mmlSeqView.pauseTickPosition();
			enableNoplayItems();
		} else if (command.equals(FILE_OPEN)) {
			openMMLFileAction();
		} else if (command.equals(NEW_FILE)) {
			newMMLFileAction();
		} else if (command.equals(RELOAD_FILE)) {
			reloadMMLFileAction();
		} else if (command.equals(QUIT)) {
			System.exit(0);
		} else if (command.equals(ADD_TRACK)) {
			mmlSeqView.addMMLTrack(null);
		} else if (command.equals(REMOVE_TRACK)) {
			mmlSeqView.removeMMLTrack();
		} else if (command.equals(TRACK_PROPERTY)) {
			mmlSeqView.editTrackPropertyAction();
		} else if (command.equals(SET_START_POSITION)) {
			mmlSeqView.setStartPosition();
		} else if (command.equals(PLAY)) {
			mmlSeqView.startSequence();
			disableNoplayItems();
		} else if (command.equals(INPUT_FROM_CLIPBOARD)) {
			mmlSeqView.inputClipBoardAction();
		} else if (command.equals(OUTPUT_TO_CLIPBOARD)) {
			mmlSeqView.outputClipBoardAction();
		} else if (command.equals(UNDO)) {
			mmlSeqView.undo();
		} else if (command.equals(REDO)) {
			mmlSeqView.redo();
		}
	}

}

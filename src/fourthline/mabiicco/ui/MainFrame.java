/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;

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
import fourthline.mmlTools.parser.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;


public class MainFrame extends JFrame implements ComponentListener, INotifyTrackEnd {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7484797594534384422L;
	private JPanel contentPane;
	private JTextField statusField;
	private MMLSeqView mmlSeqView;

	private final String DEFAULT_TITLE = " * MabiIcco *";

	private File openedFile = null;
	
	/** シーケンス再生中に無効化する機能のリスト */
	ArrayList<JComponent> noplayFunctions = new ArrayList<JComponent>();

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle(DEFAULT_TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loadWindowPeoperties();
		addComponentListener(this);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("ファイル");
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		JMenuItem fileOpenMenuItem = new JMenuItem("開く");
		noplayFunctions.add(fileOpenMenuItem);
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileOpenMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openMMLFileAction();
			}
		});

		JMenuItem menuItem = new JMenuItem("新規作成");
		noplayFunctions.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newMMLFileAction();
			}
		});
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		fileMenu.add(menuItem);
		fileMenu.add(fileOpenMenuItem);

		JMenuItem reloadMenuItem = new JMenuItem("再読み込み");
		noplayFunctions.add(reloadMenuItem);
		reloadMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				reloadMMLFileAction();
			}
		});
		fileMenu.add(reloadMenuItem);

		JSeparator separator = new JSeparator();
		fileMenu.add(separator);

		JMenuItem exitMenuItem = new JMenuItem("終了");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);

		JMenu trackMenu = new JMenu("トラック");
		menuBar.add(trackMenu);

		JMenuItem addTrackMenu = new JMenuItem("トラック追加");
		noplayFunctions.add(addTrackMenu);
		addTrackMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		addTrackMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mmlSeqView.addMMLTrack("New Track");
			}
		});
		trackMenu.add(addTrackMenu);

		JMenuItem removeTrackMenu = new JMenuItem("トラック削除");
		noplayFunctions.add(removeTrackMenu);
		removeTrackMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mmlSeqView.removeMMLTrack();
			}
		});
		trackMenu.add(removeTrackMenu);

		JSeparator separator_1 = new JSeparator();
		trackMenu.add(separator_1);

		JMenuItem menuItem_1 = new JMenuItem("トラックプロパティ");
		noplayFunctions.add(menuItem_1);
		menuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				trackPropertyAction();
			}
		});
		menuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));
		trackMenu.add(menuItem_1);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel northPanel = new JPanel();
		contentPane.add(northPanel, BorderLayout.NORTH);

		JButton playButton = new JButton("再生");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mmlSeqView.startSequence();
				disableNoplayItems();
			}
		});
		
		JButton startPositionButton = new JButton("先頭へ戻す");
		startPositionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mmlSeqView.setStartPosition();
			}
		});
		northPanel.add(startPositionButton);
		northPanel.add(playButton);

		JButton stopButton = new JButton("停止");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MabiDLS.getInstance().getSequencer().stop();
				enableNoplayItems();
			}
		});
		northPanel.add(stopButton);


		JButton inputClipButton = new JButton("クリップボードから入力");
		noplayFunctions.add(inputClipButton);
		inputClipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String clipMML = getClipboardString();
				// 現在のトラックにMMLを設定する。
				mmlSeqView.setMMLselectedTrack(clipMML);
			}
		});
		northPanel.add(inputClipButton);

		mmlSeqView = new MMLSeqView();
		contentPane.add(mmlSeqView, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		contentPane.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BorderLayout(0, 0));

		statusField = new JTextField();
		statusField.setEditable(false);
		southPanel.add(statusField, BorderLayout.SOUTH);
		statusField.setColumns(10);
		
		MabiDLS.getInstance().setTrackEndNotifier(this);
	}

	private void setTitleAndFile(File file) {
		String fileTitle = "";

		if (file != null) {
			fileTitle = file.getName();
		}
		setTitle(DEFAULT_TITLE + " [" + fileTitle + "]");
	}

	private void openMMLFile(File file) {
		setTitleAndFile(file);
		MabiIccoProperties.getInstance().setRecentFile(file.getPath());
		IMMLFileParser fileParser = new MMSFile();
		try {
			MMLTrack track[] = fileParser.parse(file);

			mmlSeqView.setMMLTracks(track);
		} catch (MMLParseException e) {
			JOptionPane.showMessageDialog(this, "読み込みに失敗しました", "ファイル形式が不正です", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void newMMLFileAction() {
		setTitleAndFile(null);
		openedFile = null;
		MMLTrack track[] = new MMLTrack[5];

		for (int i = 0; i < track.length; i++) {
			String name = "Track"+(i+1);
			track[i] = new MMLTrack(name);
		}

		mmlSeqView.setMMLTracks(track);
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

		String recentPath = MabiIccoProperties.getInstance().getRecentFile();
		JFileChooser fileChooser = new JFileChooser(new File(recentPath));
		FileFilter mmsFilter = new FileNameExtensionFilter("まきまびしーく形式 (*.mms)", "mms");
		fileChooser.addChoosableFileFilter(mmsFilter);
		fileChooser.setFileFilter(mmsFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int status = fileChooser.showOpenDialog(this);
		if (status == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			openMMLFile(file);
			openedFile = file;
		}
	}

	private void trackPropertyAction() {
		TrackPropertyDialog dialog = new TrackPropertyDialog(
				this,
				mmlSeqView, 
				mmlSeqView.getSelectedTrack() );
		dialog.setVisible(true);
	}

	public static String getClipboardString() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();

		try {
			return (String) clip.getData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
	

}

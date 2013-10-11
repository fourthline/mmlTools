/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JButton;

import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.parser.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;


public class MainFrame extends JFrame implements INotifyMMLTrackProperty {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7484797594534384422L;
	private JPanel contentPane;
	private JTextField statusField;
	private JTabbedPane tabbedPane;
	private KeyboardView keyboardView;
	private PianoRollView pianoRollView;
	
	private final String DEFAULT_TITLE = " * MabiIcco *";
	
	private File openedFile = null;

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle(DEFAULT_TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 613, 450);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("ファイル");
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);
		
		JMenuItem fileOpenMenuItem = new JMenuItem("開く");
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileOpenMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openMMLFileAction();
			}
		});
		
		JMenuItem menuItem = new JMenuItem("新規作成");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newMMLFileAction();
			}
		});
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		fileMenu.add(menuItem);
		fileMenu.add(fileOpenMenuItem);
		
		JMenuItem reloadMenuItem = new JMenuItem("再読み込み");
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
		addTrackMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		addTrackMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPane.add("New Track", new MMLTrackView());
			}
		});
		trackMenu.add(addTrackMenu);
		
		JMenuItem removeTrackMenu = new JMenuItem("トラック削除");
		removeTrackMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPane.remove( tabbedPane.getSelectedComponent() );
			}
		});
		trackMenu.add(removeTrackMenu);
		
		JSeparator separator_1 = new JSeparator();
		trackMenu.add(separator_1);
		
		JMenuItem menuItem_1 = new JMenuItem("トラックプロパティ");
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
				try {
					Sequencer sequencer = MabiDLS.getInstance().getSequencer();
					Sequence sequence = new Sequence(Sequence.PPQ, 96);

					/* TODO: ここでいいのかなぁ */
					int count = tabbedPane.getTabCount();
					for (int i = 0; i < count; i++) {
						MMLTrack mmlTrack = ((MMLTrackView)(tabbedPane.getComponentAt(i))).getMMLTrack();
						mmlTrack.convertMidiTrack(sequence.createTrack(), i);
					}
					
					sequencer.setSequence(sequence);
					sequencer.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		northPanel.add(playButton);

		JButton stopButton = new JButton("停止");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MabiDLS.getInstance().getSequencer().stop();
			}
		});
		northPanel.add(stopButton);
		
		JButton inputClipButton = new JButton("クリップボードから入力");
		inputClipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String clipMML = getClipboardString();
				MMLTrackView selectedTrack = (MMLTrackView)(tabbedPane.getSelectedComponent());
				selectedTrack.setMML(clipMML);
			}
		});
		northPanel.add(inputClipButton);

		JPanel centerPanel = new JPanel();
		contentPane.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout(0, 0));

		// TODO: ピアノロールビュー
		pianoRollView = new PianoRollView();
		keyboardView = new KeyboardView();

		JScrollPane subScrollPane = new JScrollPane(pianoRollView);
		subScrollPane.setPreferredSize(new Dimension(150, 0));
		subScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		subScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		subScrollPane.setRowHeaderView(keyboardView);
		centerPanel.add(subScrollPane, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		contentPane.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BorderLayout(0, 0));
		southPanel.setPreferredSize(new Dimension(50, 50));

		statusField = new JTextField();
		statusField.setEditable(false);
		southPanel.add(statusField, BorderLayout.SOUTH);
		statusField.setColumns(10);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MMLTrackView selectedView = (MMLTrackView)(tabbedPane.getSelectedComponent());
				System.out.println("tabbedPane change: " + tabbedPane.getSelectedIndex());

				pianoRollView.setInstSource(selectedView);
				keyboardView.setInstSource(selectedView);
			}
		});
		southPanel.add(tabbedPane, BorderLayout.CENTER);
		southPanel.setPreferredSize(new Dimension(0, 200));
		
		newMMLFileAction();
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

			tabbedPane.removeAll();
			for (int i = 0; i < track.length; i++) {
				String name = track[i].getName();
				if (name == null) {
					name = "Track"+(i+1);
				}
				tabbedPane.add(new MMLTrackView(track[i]));
			}
		} catch (MMLParseException e) {
			JOptionPane.showMessageDialog(this, "読み込みに失敗しました", "ファイル形式が不正です", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void newMMLFileAction() {
		setTitleAndFile(null);
		openedFile = null;
		tabbedPane.removeAll();

		for (int i = 0; i < 5; i++) {
			String name = "Track"+(i+1);
			MMLTrackView trackView = new MMLTrackView();
			MMLTrack track = new MMLTrack(name);
			trackView.setMMLTrack(track);
			tabbedPane.add(name, trackView);
		}
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
				this,
				(MMLTrackView)(tabbedPane.getSelectedComponent()) );
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

	@Override
	public void setTrackProperty(MMLTrack track) {
		int index = tabbedPane.getSelectedIndex();
		
		tabbedPane.setTitleAt(index, track.getName());
	}
}

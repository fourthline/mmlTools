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


public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7484797594534384422L;
	private JPanel contentPane;
	private JTextField statusField;
	private JTabbedPane tabbedPane;
	private KeyboardView keyboardView;
	
	private final String DEFAULT_TITLE = " * MabiIcco *";


//	private MMLTrackView track1;
//	private MMLTrackView selectedTrack;

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
		menuBar.add(fileMenu);
		
		JMenuItem fileOpenMenuItem = new JMenuItem("開く");
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileOpenMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openMMLFileAction();
			}
		});
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
//					MMLTrack mmlTrack = track1.getMMLTrack();
//					MMLTrack mmlTrack = ((MMLTrackView)(tabbedPane.getComponentAt(0))).getMMLTrack();
//					mmlTrack.convertMidiTrack(sequence.createTrack());

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

		keyboardView = new KeyboardView();

		JScrollPane subScrollPane = new JScrollPane(keyboardView);
		subScrollPane.setPreferredSize(new Dimension(150, 0));
		subScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		subScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

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
				
				keyboardView.setInstSource(selectedView);
			}
		});
		tabbedPane.add("Track1", new MMLTrackView());
		tabbedPane.add("Track2", new MMLTrackView());
		tabbedPane.add("Track3", new MMLTrackView());
		tabbedPane.add("Track4", new MMLTrackView());
		tabbedPane.add("Track5", new MMLTrackView());
		southPanel.add(tabbedPane, BorderLayout.CENTER);
		southPanel.setPreferredSize(new Dimension(0, 200));
	}

	private void setTitleAndFile(File file) {
		setTitle(DEFAULT_TITLE + " [" + file.getName() + "]");
	}
	
	private void openMMLFile(File file) {
		setTitleAndFile(file);
		MabiIccoProperties.getInstance().setRecentFile(file.getPath());
		IMMLFileParser fileParser = new MMSFile();
		MMLTrack track[] = fileParser.parse(file);
		
		tabbedPane.removeAll();
		for (int i = 0; i < track.length; i++) {
			String name = track[i].getName();
			if (name == null) {
				name = "Track"+(i+1);
			}
			tabbedPane.add(name, new MMLTrackView(track[i]));
		}
	}
	
	private void reloadMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			MabiDLS.getInstance().getSequencer().stop();
		}
		
		File file = new File(MabiIccoProperties.getInstance().getRecentFile());
		openMMLFile(file);
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
		}
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
}

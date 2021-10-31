/*
 * Copyright (C) 2014-2021 たんらる
 */

package fourthline.mabiicco;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import sun.swing.FilePane;
import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.MainFrame;
import fourthline.mmlTools.MMLTrack;


/**
 * MabiIccoアプリケーションクラス (Main).
 * 
 * MMLの処理は MMLTools を使用し, DLSを読み込んで音を鳴らす部分はMIDIを使用します.
 * <pre>
 * 1. Midi初期化.
 * 2. DLSファイルの音源情報を読み込む. (Waveは読み込まない)
 * 3. DLSファイルがない場合は, ファイル選択のダイアログを表示する.
 * </pre>
 */
public final class MabiIcco {
	private final String args[];
	private final JDialog splash = new JDialog();
	private final SplashPanel splashPanel = new SplashPanel();

	private final class SplashPanel extends JPanel {
		private static final long serialVersionUID = 2210455372955295858L;
		private static final int WIDTH = 300;
		private static final int HEIGHT = 220; 
		private final ImageIcon img;
		private final JProgressBar progress = new JProgressBar();
		private final JLabel version = new JLabel();
		private final JTextArea textArea = new JTextArea();
		private SplashPanel() {
			super();
			setLayout(null);
			img = AppResource.getImageIcon("/img/title.png");
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			progress.setMaximum(100);
			add(progress);
			progress.setBounds(4, 206, 292, 12);
			add(version);
			version.setText("Version: "+AppResource.getVersionText());
			version.setBounds(160, 60, 120, 14);
			add(textArea);
			textArea.setEditable(false);
			textArea.setBounds(20, 100, 180, 100);
			setOpaque(false);
			setBorder(new LineBorder(Color.GRAY, 1, false));
		}

		@Override
		public void paint(Graphics g) {
			g.drawImage(img.getImage(), 0, 0, this);
			super.paint(g);
		}

		private void updateProgress(String s, int v) {
			textArea.setText(textArea.getText()+s);
			progress.setValue(v);
		}
	}

	public MabiIcco(String args[]) {
		this.args = args;
		splash.getContentPane().add(splashPanel);
		splash.setUndecorated(true);
		splash.pack();
		splash.setLocationRelativeTo(null);
		splash.setVisible(true);
	}

	private void updateProgress(String str, int progress) {
		splashPanel.updateProgress(str, progress);
	}

	public void start() {
		updateProgress(AppResource.appText("init.midi"), 10);
		initialize();
	}

	private void initialize() {
		try {
			// initialize
			MabiDLS.getInstance().initializeMIDI();
			updateProgress("OK\n", 20);

			MMLTrack.setTempoAllowChardPartFunction(t -> {
				InstType type = MabiDLS.getInstance().getInstByProgram(t).getType();
				return type.allowTempoChordPart();
			});

			updateProgress(AppResource.appText("init.dls"), 20);
			if ( !loadDLSFiles(20, 70) ) {
				JOptionPane.showMessageDialog(null, AppResource.appText("error.needDls"), "ERROR", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			updateProgress("OK\n", 90);
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		ActionDispatcher dispatcher = ActionDispatcher.getInstance();
		MainFrame mainFrame = new MainFrame(dispatcher);
		mainFrame.setTransferHandler(new FileTransferHandler(dispatcher));
		updateProgress("", 100);
		dispatcher.setMainFrame(mainFrame).initialize();
		if (dispatcher.recoveryCheck()) {
		} else if (args.length > 0) {
			dispatcher.checkAndOpenMMLFile(new File(args[0]));
		}
		try {
			this.splash.dispose();
		} catch (IllegalStateException e) {}
		mainFrame.setVisible(true);
	}

	/**
	 * DLSのロードを行います. 初回に失敗した場合は、DLSファイル選択ダイアログを表示します.
	 * @return 1つ以上のInstrumentをロードできれば true.
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 */
	private boolean loadDLSFiles(double initialProgress, double endProgress) throws InvalidMidiDataException, IOException {
		MabiIccoProperties appProperties = MabiIccoProperties.getInstance();
		if (tryloadDLSFiles(initialProgress, endProgress)) {
			return true;
		}
		JOptionPane.showMessageDialog(null, AppResource.appText("msg.dls_title.detail"), AppResource.appText("msg.dls_title"), JOptionPane.INFORMATION_MESSAGE);
		JFileChooser fileChooser = createFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		FileFilter dlsFilter = new FileNameExtensionFilter(AppResource.appText("file.dls"), "dls");
		fileChooser.addChoosableFileFilter(dlsFilter);
		fileChooser.setFileFilter(dlsFilter);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int status = fileChooser.showOpenDialog(null);
		if (status == JFileChooser.APPROVE_OPTION) {
			appProperties.setDlsFile( fileChooser.getSelectedFiles() );
		} else {
			return false;
		}

		return tryloadDLSFiles(initialProgress, endProgress);
	}

	/**
	 * DLSファイルのロードを試みます.
	 * @return 1つ以上のInstrumentをロードできれば true.
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 */
	private boolean tryloadDLSFiles(double initialProgress, double endProgress) throws InvalidMidiDataException, IOException {
		MabiDLS dls = MabiDLS.getInstance();
		MabiIccoProperties appProperties = MabiIccoProperties.getInstance();
		List<File> dlsFiles = appProperties.getDlsFile();
		double progressStep = (endProgress - initialProgress) / dlsFiles.size();
		double progress = initialProgress;
		for (File file : dlsFiles) {
			dls.loadingDLSFile(file);
			progress += progressStep;
			updateProgress("", (int)progress);
		}

		if (dls.getAvailableInstByInstType(InstType.MAIN_INST_LIST).length > 0) {
			return true;
		}
		return false;
	}

	private static void setUIFont(javax.swing.plaf.FontUIResource resource) {
		for (Object key : Collections.list(UIManager.getDefaults().keys())) {
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, resource);
			}
		}
	}

	public static JFileChooser createFileChooser() {
		JFileChooser chooser = new JFileChooser();
		Action detailsAction = chooser.getActionMap().get(FilePane.ACTION_VIEW_DETAILS);
		if (detailsAction != null) {
			detailsAction.actionPerformed(null);
		}

		return chooser;
	}

	public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		// font
		String fontName = AppResource.appText("ui.font");
		if (!fontName.equals("ui.font")) {
			setUIFont(new javax.swing.plaf.FontUIResource(fontName, Font.PLAIN, 11));
		}

		new MabiIcco(args).start();
	}
}

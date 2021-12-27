/*
 * Copyright (C) 2014-2021 たんらる
 */

package fourthline.mabiicco;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
	private final Splash splash = new Splash();

	public MabiIcco(String args[]) {
		this.args = args;
		splash.setVisible(true);
	}

	public void start() {
		splash.updateProgress(AppResource.appText("init.midi"), 10);
		initialize();
	}

	private void initialize() {
		try {
			// initialize
			MabiDLS.getInstance().initializeMIDI();
			splash.updateProgress("OK\n", 20);

			MMLTrack.setTempoAllowChardPartFunction(t -> {
				InstType type = MabiDLS.getInstance().getInstByProgram(t).getType();
				return type.allowTempoChordPart();
			});

			// loading DLS
			splash.updateProgress(AppResource.appText("init.dls"), 20);
			if ( !loadDLSFiles(20, 70) ) {
				JOptionPane.showMessageDialog(null, AppResource.appText("error.needDls"), "ERROR", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			splash.updateProgress("OK\n", 90);

			// create MainFrame
			ActionDispatcher dispatcher = ActionDispatcher.getInstance();
			MainFrame mainFrame = new MainFrame(dispatcher, dispatcher);
			mainFrame.setTransferHandler(new FileTransferHandler(dispatcher));
			splash.updateProgress("", 100);
			dispatcher.setMainFrame(mainFrame).initialize();
			if (dispatcher.recoveryCheck()) {
			} else if (args.length > 0) {
				dispatcher.checkAndOpenMMLFile(new File(args[0]));
			}
			mainFrame.setVisible(true);
			splash.dispose();
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
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
			splash.updateProgress("", (int)progress);
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

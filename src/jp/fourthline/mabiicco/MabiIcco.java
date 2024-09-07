/*
 * Copyright (C) 2014-2024 たんらる
 */

package jp.fourthline.mabiicco;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JOptionPane;

import jp.fourthline.mabiicco.midi.InstType;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mabiicco.ui.MainFrame;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLException;
import jp.fourthline.mmlTools.parser.MidiFile;


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
	public interface ISplash {
		void setVisible(boolean b);
		void updateProgress(String s, int v);
		void dispose();
	}
	private final String[] args;
	private final ISplash splash;

	private final MabiIccoProperties appProperties;
	private final MabiDLS dls;

	public MabiIcco(String[] args) {
		splash = (System.getProperty("mabiicco.splash") == null) ? new Splash() : new Splash2(); 
		this.args = args;
		splash.setVisible(true);
		appProperties = MabiIccoProperties.getInstance();
		dls = MabiDLS.getInstance();
	}

	public void start() throws Exception {
		splash.updateProgress(AppResource.appText("init.midi"), 10);
		initialize();
	}

	private boolean tempoAllowChordPart(int t) {
		var inst = dls.getInstByProgram(t);
		if (inst != null) {
			return inst.getType().allowTempoChordPart();
		}
		return true;
	}

	public boolean percussionMotionFix(int t) {
		if (appProperties.percussionMotionFix.get()) {
			var inst = dls.getInstByProgram(t);
			var type = inst.getType();
			return ( (type == InstType.KPUR) || (type == InstType.PERCUSSION) );
		}
		return false;
	}

	private void initialize() throws Exception {
		// initialize
		dls.initializeMIDI();
		splash.updateProgress("OK\n", 20);

		MMLTrack.setTempoAllowChordPartFunction(this::tempoAllowChordPart);
		MMLTrack.setPercussionMotionFixFunction(this::percussionMotionFix);

		if (appProperties.useDefaultSoundBank.get()) {
			dls.loadingDefaultSound();
		} else {
			// loading DLS
			splash.updateProgress(AppResource.appText("init.dls"), 20);
			if ( !tryloadDLSFiles(20, 70) ) {
				JOptionPane.showMessageDialog(null, AppResource.appText("message.useDefaultSoundbank"), AppResource.getAppTitle(), JOptionPane.INFORMATION_MESSAGE);
				appProperties.useDefaultSoundBank.set(true);
				dls.loadingDefaultSound();
			}
			splash.updateProgress("OK\n", 90);
		}

		if (!appProperties.useDefaultSoundBank.get()) {
			// 内蔵音源を使わないときはMIDファイル読み込み時のProgram変換を有効にする
			MidiFile.enableInstPatch();
		}

		// create MainFrame
		ActionDispatcher dispatcher = ActionDispatcher.getInstance();
		MainFrame mainFrame = new MainFrame(dispatcher, dispatcher);
		mainFrame.setTransferHandler(new FileTransferHandler(dispatcher));
		splash.updateProgress("", 100);
		dispatcher.setMainFrame(mainFrame).initialize();
		if (dispatcher.recoveryCheck()) {
		} else if (args.length > 0) {
			startOpen(dispatcher, args[0]);
		}
		mainFrame.setVisible(true);
		splash.dispose();
	}

	private void startOpen(ActionDispatcher dispatcher, String s) {
		File f = new File(s);
		if (!f.exists()) {
			String[] arg = LauncherSupport.getCommandLineArgs(args.length);
			if ( (arg == null) || (arg.length == 0) ) {
				return;
			}
			f = new File(arg[0]);
		}
		dispatcher.checkAndOpenMMLFile(f);
	}

	/**
	 * DLSファイルのロードを試みます.
	 * @return 1つ以上のInstrumentをロードできれば true.
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 */
	private boolean tryloadDLSFiles(double initialProgress, double endProgress) throws InvalidMidiDataException, IOException {
		List<File> dlsFiles = appProperties.getDlsFile();
		double progressStep = (endProgress - initialProgress) / dlsFiles.size();
		double progress = initialProgress;
		for (File file : dlsFiles) {
			dls.loadingDLSFile(file);
			progress += progressStep;
			splash.updateProgress("", (int)progress);
		}

		return dls.getAvailableInstByInstType(InstType.MAIN_INST_LIST).length > 0;
	}

	public static void main(String[] args) {
		try {
			var properties = MabiIccoProperties.getInstance();
			if (properties.uiscaleDisable.get()) {
				System.setProperty("sun.java2d.uiScale.enabled", "false");
			}

			properties.laf.get().update();

			// MMLエラーのローカライズ設定.
			MMLException.setLocalizeFunc(t -> AppResource.appText(t));

			new MabiIcco(args).start();
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				e.printStackTrace(new PrintStream(AppResource.getErrFile()));
			} catch (FileNotFoundException e1) {}
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
}

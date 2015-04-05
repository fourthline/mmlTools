/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mabiicco;

import java.awt.EventQueue;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.SwingUtilities;

import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.preloader.MabiIccoPreloaderNotification;
import fourthline.mabiicco.ui.MainFrame;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.stage.Stage;


public final class MabiIcco extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		notifyPreloader(new MabiIccoPreloaderNotification(AppResource.appText("init.midi"), 10));
		SwingUtilities.invokeLater(this::initialize);
	}

	private void initialize() {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

			// font
			String fontName = AppResource.appText("ui.font");
			if (!fontName.equals("ui.font")) {
				setUIFont(new javax.swing.plaf.FontUIResource(fontName, Font.PLAIN, 11));
			}

			// initialize
			MabiDLS.getInstance().initializeMIDI();
			notifyPreloader(new MabiIccoPreloaderNotification("OK\n", 20));

			notifyPreloader(new MabiIccoPreloaderNotification(AppResource.appText("init.dls"), 20));
			if ( !loadDLSFiles() ) {
				JOptionPane.showMessageDialog(null, AppResource.appText("error.needDls"), "ERROR", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			notifyPreloader(new MabiIccoPreloaderNotification("OK\n", 90));
		} catch (Exception | Error e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		EventQueue.invokeLater(() -> {
			ActionDispatcher dispatcher = ActionDispatcher.getInstance();
			MainFrame mainFrame = new MainFrame(dispatcher);
			mainFrame.setTransferHandler(new FileTransferHandler(dispatcher));
			notifyPreloader(new MabiIccoPreloaderNotification("", 100));
			dispatcher.setMainFrame(mainFrame).initialize();
			List<String> args = getParameters().getRaw();
			if (args.size() > 0) {
				dispatcher.checkAndOpenMMLFile(new File(args.get(0)));
			}
			try {
				notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
			} catch (IllegalStateException e) {}
			mainFrame.setVisible(true);
		});
	}

	/**
	 * DLSのロードを行います. 初回に失敗した場合は、DLSファイル選択ダイアログを表示します.
	 * @return 1つ以上のInstrumentをロードできれば true.
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 */
	private boolean loadDLSFiles() throws InvalidMidiDataException, IOException {
		MabiIccoProperties appProperties = MabiIccoProperties.getInstance();
		if (tryloadDLSFiles()) {
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

		return tryloadDLSFiles();
	}

	/**
	 * DLSファイルのロードを試みます.
	 * @return 1つ以上のInstrumentをロードできれば true.
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 */
	private boolean tryloadDLSFiles() throws InvalidMidiDataException, IOException {
		MabiDLS dls = MabiDLS.getInstance();
		MabiIccoProperties appProperties = MabiIccoProperties.getInstance();
		for (File file : appProperties.getDlsFile()) {
			dls.loadingDLSFile(file);
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
		Action detailsAction = chooser.getActionMap().get("viewTypeDetails");
		if (detailsAction != null) {
			detailsAction.actionPerformed(null);
		}

		return chooser;
	}

	public static void main(String args[]) {
		launch(args);
	}
}

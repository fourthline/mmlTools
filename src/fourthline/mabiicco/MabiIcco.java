/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.Synthesizer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.preloader.MabiIccoPreloaderNotification;
import fourthline.mabiicco.ui.MainFrame;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.stage.Stage;

import javax.swing.SwingUtilities;

public class MabiIcco extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		notifyPreloader(new MabiIccoPreloaderNotification(AppResource.getText("init.midi"), 10));
		SwingUtilities.invokeLater(() -> {
			initialize();
		});
	}

	public void initialize() {
		MabiIccoProperties appProperties = MabiIccoProperties.getInstance();

		try {
			// font
			String fontName = AppResource.getText("ui.font");
			if (!fontName.equals("ui.font")) {
				setUIFont(new javax.swing.plaf.FontUIResource(fontName, Font.PLAIN, 11));
			}
			UIManager.LookAndFeelInfo infos[] = UIManager.getInstalledLookAndFeels();
			for (UIManager.LookAndFeelInfo info : infos) {
				System.out.println(info.getClassName());
			}

			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

			// initialize
			MabiDLS.getInstance().initializeMIDI();
			notifyPreloader(new MabiIccoPreloaderNotification("OK\n", 20));

			notifyPreloader(new MabiIccoPreloaderNotification(AppResource.getText("init.dls"), 20));
			File file = new File( appProperties.getDlsFile() );
			if ( !file.exists() ) {
				/* DLSファイルがない場合 */
				JFileChooser fileChooser = new JFileChooser();
				FileFilter dlsFilter = new FileNameExtensionFilter(AppResource.getText("file.dls"), "dls");
				fileChooser.addChoosableFileFilter(dlsFilter);
				fileChooser.setFileFilter(dlsFilter);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int status = fileChooser.showOpenDialog(null);
				if (status == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();
				} else {
					JOptionPane.showMessageDialog(null, AppResource.getText("error.needDls"), "ERROR", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
			}

			MabiDLS.getInstance().initializeSound(file);
			loadSoundbank(20, 90);
			appProperties.setDlsFile(file.getPath());
			notifyPreloader(new MabiIccoPreloaderNotification("OK\n", 90));
		} catch (Exception | Error e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		EventQueue.invokeLater(() -> {
			ActionDispatcher dispatcher = ActionDispatcher.getInstance();
			MainFrame mainFrame = new MainFrame(dispatcher);
			notifyPreloader(new MabiIccoPreloaderNotification("", 100));
			dispatcher.setMainFrame(mainFrame);
			List<String> args = getParameters().getRaw();
			if (args.size() > 0) {
				dispatcher.openMMLFile(new File(args.get(0)));
			}
			notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
			mainFrame.setVisible(true);
		});
	}

	private void loadSoundbank(int startProgress, int endProgress) {
		InstClass insts[] = MabiDLS.getInstance().getInsts();
		Synthesizer synthesizer = MabiDLS.getInstance().getSynthesizer();

		double progress = startProgress;
		double delta = ((double)endProgress - startProgress) / insts.length;
		System.out.println(delta);
		System.out.println(insts.length);
		try {
			for (InstClass instrument : insts) {
				synthesizer.loadInstrument(instrument.getInstrument());
				progress += delta;
				notifyPreloader(new MabiIccoPreloaderNotification("", progress));
			}
		} catch (OutOfMemoryError e) {
			for (InstClass instrument : insts) {
				synthesizer.unloadInstrument(instrument.getInstrument());
			}
			throw e;
		}
	}

	private static void setUIFont(javax.swing.plaf.FontUIResource resource) {
		for (Object key : Collections.list(UIManager.getDefaults().keys())) {
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, resource);
			}
		}
	}

	public static void main(String args[]) {
		launch(args);
	}
}

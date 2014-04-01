/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.util.Collections;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.MainFrame;
import fourthline.mabiicco.ui.Startup;
import javafx.application.Application;
import javafx.stage.Stage;

public class MabiIccoFX extends Application {
	@Override
	public void start(Stage arg0) throws Exception {
		MabiIccoProperties appProperties = MabiIccoProperties.getInstance();

		try {
			setUIFont(new javax.swing.plaf.FontUIResource("Meiryo", Font.PLAIN, 11));
			UIManager.LookAndFeelInfo infos[] = UIManager.getInstalledLookAndFeels();
			for (UIManager.LookAndFeelInfo info : infos) {
				System.out.println(info.getClassName());
			}

			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {}

		final Startup window = new Startup();
		window.setVisible(true);

		try {
			window.printStatus("MIDI初期化中...");
			MabiDLS.getInstance().initializeMIDI();
			window.printStatus("OK\n");

			window.printStatus("DLSファイル読み込み中...");
			File file = new File( appProperties.getDlsFile() );
			if ( !file.exists() ) {
				/* DLSファイルがない場合 */
				JFileChooser fileChooser = new JFileChooser();
				FileFilter dlsFilter = new FileNameExtensionFilter("DLSファイル (*.dls)", "dls");
				fileChooser.addChoosableFileFilter(dlsFilter);
				fileChooser.setFileFilter(dlsFilter);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int status = fileChooser.showOpenDialog(window);
				if (status == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();
				} else {
					window.setVisible(false);
					JOptionPane.showMessageDialog(null, "DLSファイルが必要です。", "ERROR", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
			}

			MabiDLS.getInstance().initializeSound(file);
			appProperties.setDlsFile(file.getPath());
			window.printStatus("OK\n");
		} catch (Exception e) {
			e.printStackTrace();
			window.setVisible(false);
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ActionDispatcher dispatcher = ActionDispatcher.getInstance();
				MainFrame mainFrame = new MainFrame(dispatcher);
				dispatcher.setMainFrame(mainFrame);
				mainFrame.setVisible(true);
				window.setVisible(false);
			}
		});
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

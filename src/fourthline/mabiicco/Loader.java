/*
 * Copyright (C) 2014 たんらる
 */


package fourthline.mabiicco;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.MainFrame;
import fourthline.mabiicco.ui.Startup;

public class Loader {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		MabiIccoProperties appProperties = MabiIccoProperties.getInstance();

		try {
			UIManager.LookAndFeelInfo infos[] = UIManager.getInstalledLookAndFeels();

			System.out.println("getClassName:");

			for(int i = 0 ; i < infos.length ; i++){
				System.out.println(infos[i].getClassName());
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
			public void run() {
				ActionDispatcher dispatcher = new ActionDispatcher();
				MainFrame mainFrame = new MainFrame(dispatcher);
				mainFrame.getMMLSeqView().getFileState().setFileStateObserver(dispatcher);
				dispatcher.setMainFrame(mainFrame);
				mainFrame.setVisible(true);
				window.setVisible(false);
			}
		});
	}
}

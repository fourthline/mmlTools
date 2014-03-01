/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.MMLSeqView;
import fourthline.mabiicco.ui.MainFrame;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.parser.IMMLFileParser;
import fourthline.mmlTools.parser.MMLParseException;
import fourthline.mmlTools.parser.MMSFile;

public class ActionDispatcher implements ActionListener {

	private MainFrame mainFrame;
	private MMLSeqView mmlSeqView;

	// action commands
	public static final String VIEW_EXPAND = "viewExpand";
	public static final String VIEW_REDUCE = "viewReduce";
	public static final String PLAY = "play";
	public static final String STOP = "stop";
	public static final String PAUSE = "pause";
	public static final String FILE_OPEN = "fileOpen";
	public static final String NEW_FILE = "newFile";
	public static final String RELOAD_FILE = "reloadFile";
	public static final String QUIT = "quit";
	public static final String ADD_TRACK = "addTrack";
	public static final String REMOVE_TRACK = "removeTrack";
	public static final String TRACK_PROPERTY = "trackProperty";
	public static final String SET_START_POSITION = "setStartPosition";
	public static final String INPUT_FROM_CLIPBOARD = "inputFromClipboard";
	public static final String OUTPUT_TO_CLIPBOARD = "outputToClipboard";
	public static final String UNDO = "undo";
	public static final String REDO = "redo";
	public static final String SAVE_FILE = "save_file";
	public static final String SAVEAS_FILE = "saveas_file";

	private File openedFile = null;

	private FileFilter mmsFilter = new FileNameExtensionFilter("まきまびしーく形式 (*.mms)", "mms");
	private FileFilter mmiFilter = new FileNameExtensionFilter("MabiIcco形式 (*.mmi)", "mmi");
	private FileFilter allFilter = new FileNameExtensionFilter("すべての対応形式 (*.mmi, *.mms)", "mmi", "mms");


	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.mmlSeqView = mainFrame.getMMLSeqView();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals(VIEW_EXPAND)) {
			mmlSeqView.expandPianoViewWide();
			mmlSeqView.repaint();
		} else if (command.equals(VIEW_REDUCE)) {
			mmlSeqView.reducePianoViewWide();
			mmlSeqView.repaint();
		} else if (command.equals(STOP)) {
			MabiDLS.getInstance().getSequencer().stop();
			MabiDLS.getInstance().clearAllChannelPanpot();
			mainFrame.enableNoplayItems();
		} else if (command.equals(PAUSE)) {
			MabiDLS.getInstance().getSequencer().stop();
			MabiDLS.getInstance().clearAllChannelPanpot();
			mmlSeqView.pauseTickPosition();
			mainFrame.enableNoplayItems();
		} else if (command.equals(FILE_OPEN)) {
			openMMLFileAction();
		} else if (command.equals(NEW_FILE)) {
			newMMLFileAction();
		} else if (command.equals(RELOAD_FILE)) {
			reloadMMLFileAction();
		} else if (command.equals(QUIT)) {
			System.exit(0);
		} else if (command.equals(ADD_TRACK)) {
			mmlSeqView.addMMLTrack(null);
		} else if (command.equals(REMOVE_TRACK)) {
			mmlSeqView.removeMMLTrack();
		} else if (command.equals(TRACK_PROPERTY)) {
			mmlSeqView.editTrackPropertyAction();
		} else if (command.equals(SET_START_POSITION)) {
			mmlSeqView.setStartPosition();
		} else if (command.equals(PLAY)) {
			mmlSeqView.startSequence();
			mainFrame.disableNoplayItems();
		} else if (command.equals(INPUT_FROM_CLIPBOARD)) {
			mmlSeqView.inputClipBoardAction();
		} else if (command.equals(OUTPUT_TO_CLIPBOARD)) {
			mmlSeqView.outputClipBoardAction();
		} else if (command.equals(UNDO)) {
			mmlSeqView.undo();
		} else if (command.equals(REDO)) {
			mmlSeqView.redo();
		} else if (command.equals(SAVE_FILE)) {
			saveMMLFile(openedFile);
		} else if (command.equals(SAVEAS_FILE)) {
			saveMMLFileAction();
		}
	}

	private void openMMLFile(File file) {
		try {
			IMMLFileParser fileParser;
			if (file.toString().endsWith(".mms")) {
				fileParser = new MMSFile();
			} else {
				fileParser = new MMLScore();
			}
			MMLScore score = fileParser.parse(file);
			mmlSeqView.setMMLScore(score);

			mainFrame.setTitleAndFileName(file.getName());
			MabiIccoProperties.getInstance().setRecentFile(file.getPath());
		} catch (MMLParseException e) {
			JOptionPane.showMessageDialog(null, "読み込みに失敗しました", "ファイル形式が不正です", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void reloadMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			MabiDLS.getInstance().getSequencer().stop();
		}

		if (openedFile != null) {
			openMMLFile(openedFile);
		}
	}

	private void saveMMLFile(File file) {
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			mmlSeqView.getMMLScore().writeToOutputStream(outputStream);
			outputStream.close();
			mainFrame.setTitleAndFileName(file.getName());
			MabiIccoProperties.getInstance().setRecentFile(file.getPath());
		} catch (Exception e) {}
	}

	private void newMMLFileAction() {
		mainFrame.setTitleAndFileName(null);
		openedFile = null;
		mmlSeqView.initializeMMLTrack();
	}

	private void openMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			MabiDLS.getInstance().getSequencer().stop();
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String recentPath = MabiIccoProperties.getInstance().getRecentFile();
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(recentPath));
				fileChooser.addChoosableFileFilter(allFilter);
				fileChooser.addChoosableFileFilter(mmiFilter);
				fileChooser.addChoosableFileFilter(mmsFilter);
				fileChooser.setFileFilter(allFilter);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int status = fileChooser.showOpenDialog(null);
				if (status == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					openMMLFile(file);
					openedFile = file;
				}
			}
		});
	}

	private void saveMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			MabiDLS.getInstance().getSequencer().stop();
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String recentPath = MabiIccoProperties.getInstance().getRecentFile();
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(recentPath));
				fileChooser.addChoosableFileFilter(mmiFilter);
				fileChooser.setFileFilter(mmiFilter);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int status = fileChooser.showSaveDialog(null);
				if (status == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (!file.toString().endsWith(".mmi")) {
						file = new File(file+".mmi");
					}
					saveMMLFile(file);
					openedFile = file;
				}
			}
		});
	}
}

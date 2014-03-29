/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.MMLScorePropertyPanel;
import fourthline.mabiicco.ui.MMLSeqView;
import fourthline.mabiicco.ui.MainFrame;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.parser.IMMLFileParser;
import fourthline.mmlTools.parser.MMLParseException;
import fourthline.mmlTools.parser.MMSFile;

public class ActionDispatcher implements ActionListener, IFileStateObserver, IEditStateObserver {

	private MainFrame mainFrame;
	private MMLSeqView mmlSeqView;
	private IFileState fileState;
	private IEditState editState;

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
	public static final String CUT = "cut";
	public static final String COPY = "copy";
	public static final String PASTE = "paste";
	public static final String DELETE = "delete";
	public static final String SCORE_PROPERTY = "score_property";
	public static final String NEXT_TIME = "next_time";
	public static final String PREV_TIME = "prev_time";
	public static final String PART_CHANGE = "part_change";

	private File openedFile = null;

	private final FileFilter mmsFilter = new FileNameExtensionFilter("まきまびしーく形式 (*.mms)", "mms");
	private final FileFilter mmiFilter = new FileNameExtensionFilter("MabiIcco形式 (*.mmi)", "mmi");
	private final FileFilter allFilter = new FileNameExtensionFilter("すべての対応形式 (*.mmi, *.mms)", "mmi", "mms");

	private static final ActionDispatcher instance = new ActionDispatcher();
	public static ActionDispatcher getInstance() {
		return instance;
	}

	private ActionDispatcher() {}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.mmlSeqView = mainFrame.getMMLSeqView();
		this.fileState = this.mmlSeqView.getFileState();
		this.editState = this.mmlSeqView.getEditState();

		this.fileState.setFileStateObserver(this);
		this.editState.setEditStateObserver(this);
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
			if (checkCloseModifiedFileState()) {
				openMMLFileAction();
			}
		} else if (command.equals(NEW_FILE)) {
			if (checkCloseModifiedFileState()) {
				newMMLFileAction();
			}
		} else if (command.equals(RELOAD_FILE)) {
			reloadMMLFileAction();
		} else if (command.equals(QUIT)) {
			//  閉じる前に、変更が保存されていなければダイアログ表示する.
			if (checkCloseModifiedFileState()) {
				System.exit(0);
			}
		} else if (command.equals(ADD_TRACK)) {
			mmlSeqView.addMMLTrack(null);
		} else if (command.equals(REMOVE_TRACK)) {
			mmlSeqView.removeMMLTrack();
		} else if (command.equals(TRACK_PROPERTY)) {
			mmlSeqView.editTrackPropertyAction(mainFrame);
		} else if (command.equals(SET_START_POSITION)) {
			mmlSeqView.setStartPosition();
		} else if (command.equals(PLAY)) {
			mmlSeqView.startSequence();
			mainFrame.disableNoplayItems();
		} else if (command.equals(INPUT_FROM_CLIPBOARD)) {
			mmlSeqView.inputClipBoardAction(mainFrame);
		} else if (command.equals(OUTPUT_TO_CLIPBOARD)) {
			mmlSeqView.outputClipBoardAction(mainFrame);
		} else if (command.equals(UNDO)) {
			mmlSeqView.undo();
		} else if (command.equals(REDO)) {
			mmlSeqView.redo();
		} else if (command.equals(SAVE_FILE)) {
			saveMMLFile(openedFile);
		} else if (command.equals(SAVEAS_FILE)) {
			saveMMLFileAction();
		} else if (command.equals(CUT)) {
			editState.selectedCut();
		} else if (command.equals(COPY)) {
			editState.selectedCopy();
		} else if (command.equals(PASTE)) {
			editPasteAction();
		} else if (command.equals(DELETE)) {
			editState.selectedDelete();
		} else if (command.equals(SCORE_PROPERTY)) {
			scorePropertyAction();
		} else if (command.equals(NEXT_TIME)) {
			mmlSeqView.nextStepTimeTo(true);
		} else if (command.equals(PREV_TIME)) {
			mmlSeqView.nextStepTimeTo(false);
		} else if (command.equals(PART_CHANGE)) {
			mmlSeqView.partChange(mainFrame);
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
			MMLScore score = fileParser.parse(new FileInputStream(file));
			mmlSeqView.setMMLScore(score);

			openedFile = file;
			notifyUpdateFileState();
			MabiIccoProperties.getInstance().setRecentFile(file.getPath());
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(mainFrame, "読み込みに失敗しました", "指定されたファイルがありません", JOptionPane.WARNING_MESSAGE);
		} catch (MMLParseException e) {
			JOptionPane.showMessageDialog(mainFrame, "読み込みに失敗しました", "ファイル形式が不正です", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void reloadMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			MabiDLS.getInstance().getSequencer().stop();
		}

		if (openedFile != null) {
			if (fileState.isModified()) {
				int status = JOptionPane.showConfirmDialog(mainFrame, "いままでの変更が破棄されますが、よろしいですか？", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (status == JOptionPane.YES_OPTION) {
					openMMLFile(openedFile);
				}
			}
		}
	}

	private void saveMMLFile(File file) {
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			mmlSeqView.getMMLScore().writeToOutputStream(outputStream);
			mainFrame.setTitleAndFileName(file.getName());
			fileState.setOriginalBase();
			notifyUpdateFileState();
			MabiIccoProperties.getInstance().setRecentFile(file.getPath());
		} catch (Exception e) {}
	}

	private void newMMLFileAction() {
		openedFile = null;
		mmlSeqView.initializeMMLTrack();
		notifyUpdateFileState();
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
				int status = fileChooser.showOpenDialog(mainFrame);
				if (status == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					openMMLFile(file);
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
				showDialogSaveFile();
				notifyUpdateFileState();
			}
		});
	}

	/**
	 * 別名保存のダイアログ表示
	 * @return 保存した場合は trueを返す.
	 */
	private boolean showDialogSaveFile() {
		String recentPath = MabiIccoProperties.getInstance().getRecentFile();
		JFileChooser fileChooser = new JFileChooser();
		if (openedFile != null) {
			fileChooser.setSelectedFile(openedFile);
		} else {
			fileChooser.setCurrentDirectory(new File(recentPath));
		}
		fileChooser.addChoosableFileFilter(mmiFilter);
		fileChooser.setFileFilter(mmiFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int status = fileChooser.showSaveDialog(mainFrame);
		if (status == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.toString().endsWith(".mmi")) {
				file = new File(file+".mmi");
			}

			status = JOptionPane.YES_OPTION;
			if (file.exists()) {
				// すでにファイルが存在する場合の上書き警告表示.
				status = JOptionPane.showConfirmDialog(mainFrame, "すでにファイルが存在します。上書きしますか？", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			if (status == JOptionPane.YES_OPTION) {
				saveMMLFile(file);
				openedFile = file;
				return true;
			}
		}

		return false;
	}

	/**
	 * ファイルの変更状態をみて、アプリケーション終了ができるかどうかをチェックする.
	 * @return 終了できる状態であれば、trueを返す.
	 */
	private boolean checkCloseModifiedFileState() {
		if (!fileState.isModified()) {
			// 保存が必要な変更なし.
			return true;
		}

		// 保存するかどうかのダイアログ表示
		int status = JOptionPane.showConfirmDialog(mainFrame, "変更されていますが、閉じる前に保存しますか？", "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (status == JOptionPane.CANCEL_OPTION) {
			return false;
		} else if (status == JOptionPane.NO_OPTION) {
			return true;
		}

		if (openedFile == null) {
			// 新規ファイルなので、別名保存.
			if (showDialogSaveFile()) {
				return true;
			}
		} else {
			if (isSupportedSaveFile()) {
				// 上書き保存可.
				saveMMLFile(openedFile);
				return true;
			} else if (showDialogSaveFile()) {
				// ファイルOpenされているが、サポート外なので別名保存.
				return true;
			}
		}

		return false;
	}

	private boolean isSupportedSaveFile() {
		if (openedFile != null) {
			if (openedFile.getName().endsWith(".mmi")) {
				return true;
			}
		}

		return false;
	}

	private void scorePropertyAction() {
		MMLScorePropertyPanel propertyPanel = new MMLScorePropertyPanel();
		propertyPanel.showDialog(mainFrame, mmlSeqView.getMMLScore());
		mmlSeqView.repaint();
	}

	private void editPasteAction() {
		long startTick = mmlSeqView.getEditSequencePosition();
		editState.paste(startTick);
	}

	@Override
	public void notifyUpdateFileState() {
		mainFrame.setCanSaveFile(false);
		mainFrame.setTitleAndFileName(null);
		mainFrame.setCanReloadFile(false);
		if (openedFile != null) {
			if (fileState.isModified()) {
				// 上書き保存有効
				if (isSupportedSaveFile()) {
					mainFrame.setCanSaveFile(true);
				}
				mainFrame.setTitleAndFileName(openedFile.getName()+" (変更あり)");
				mainFrame.setCanReloadFile(true);
			} else {
				mainFrame.setTitleAndFileName(openedFile.getName());
			}
		}

		// undo-UI更新
		mainFrame.setCanUndo(fileState.canUndo());

		// redo-UI更新
		mainFrame.setCanRedo(fileState.canRedo());
	}

	@Override
	public void notifyUpdateEditState() {
		mainFrame.setSelectedEdit(editState.hasSelectedNote());
		mainFrame.setPasteEnable(editState.canPaste());
	}
}

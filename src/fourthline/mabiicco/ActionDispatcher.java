/*
 * Copyright (C) 2014-2021 たんらる
 */

package fourthline.mabiicco;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatLightLaf;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.About;
import fourthline.mabiicco.ui.MMLSeqView;
import fourthline.mabiicco.ui.MainFrame;
import fourthline.mabiicco.ui.WavoutPanel;
import fourthline.mabiicco.ui.color.ScaleColor;
import fourthline.mabiicco.ui.editor.MultiTracksVelocityChangeEditor;
import fourthline.mabiicco.ui.editor.MultiTracksViewEditor;
import fourthline.mabiicco.ui.editor.MMLTranspose;
import fourthline.mabiicco.ui.mml.MMLImportPanel;
import fourthline.mabiicco.ui.mml.MMLScorePropertyPanel;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.NanoTime;
import fourthline.mmlTools.parser.IMMLFileParser;
import fourthline.mmlTools.parser.MMLParseException;

public final class ActionDispatcher implements ActionListener, IFileStateObserver, IEditStateObserver {
	private MainFrame mainFrame;
	private MMLSeqView mmlSeqView;
	private IFileState fileState;
	private IEditState editState;

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Action {}

	// action commands
	@Action public static final String VIEW_SCALE_UP = "view_scale_up";
	@Action public static final String VIEW_SCALE_DOWN = "view_scale_down";
	@Action public static final String PLAY = "play";
	@Action public static final String STOP = "stop";
	@Action public static final String PAUSE = "pause";
	@Action public static final String FILE_OPEN = "fileOpen";
	@Action public static final String NEW_FILE = "newFile";
	@Action public static final String RELOAD_FILE = "reloadFile";
	@Action public static final String QUIT = "quit";
	@Action public static final String ADD_TRACK = "addTrack";
	@Action public static final String REMOVE_TRACK = "removeTrack";
	@Action public static final String DUPLICATE_TRACK = "duplicateTrack";
	@Action public static final String TRACK_PROPERTY = "trackProperty";
	@Action public static final String SET_START_POSITION = "setStartPosition";
	@Action public static final String INPUT_FROM_CLIPBOARD = "inputFromClipboard";
	@Action public static final String OUTPUT_TO_CLIPBOARD = "outputToClipboard";
	@Action public static final String UNDO = "undo";
	@Action public static final String REDO = "redo";
	@Action public static final String SAVE_FILE = "save_file";
	@Action public static final String SAVEAS_FILE = "saveas_file";
	@Action public static final String CUT = "cut";
	@Action public static final String COPY = "copy";
	@Action public static final String PASTE = "paste";
	@Action public static final String DELETE = "delete";
	@Action public static final String SCORE_PROPERTY = "score_property";
	@Action public static final String NEXT_TIME = "next_time";
	@Action public static final String PREV_TIME = "prev_time";
	@Action public static final String PART_CHANGE = "part_change";
	@Action public static final String CHANGE_NOTE_HEIGHT_INT = "change_note_height";
	@Action public static final String ADD_MEASURE = "add_measure";
	@Action public static final String REMOVE_MEASURE = "remove_measure";
	@Action public static final String ADD_BEAT = "add_beat";
	@Action public static final String REMOVE_BEAT = "remove_beat";
	@Action public static final String NOTE_PROPERTY = "note_property";
	@Action public static final String TRANSPOSE = "transpose";
	@Action public static final String TRACKS_EDIT = "tracks_edit";
	@Action public static final String TRACKS_VIEW = "tracks_view";
	@Action public static final String ABOUT = "about";
	@Action public static final String MIDI_EXPORT = "midi_export";
	@Action public static final String FILE_IMPORT = "file_import";
	@Action public static final String CLEAR_DLS = "clear_dls";
	@Action public static final String SELECT_ALL = "select_all";
	@Action public static final String SELECT_PREVIOUS_ALL = "select_previous_all";
	@Action public static final String SELECT_AFTER_ALL = "select_after_all";
	@Action public static final String SELECT_ALL_SAME_PITCH = "select_all_same_pitch";
	@Action public static final String MML_IMPORT = "mml_import";
	@Action public static final String MML_EXPORT = "mml_export";
	@Action public static final String SWITCH_TRACK_NEXT = "switch_track_next";
	@Action public static final String SWITCH_TRACK_PREV = "switch_track_prev";
	@Action public static final String SWITCH_MMLPART_NEXT = "switch_mmlpart_next";
	@Action public static final String SWITCH_MMLPART_PREV = "switch_mmlpart_prev";
	@Action public static final String TOGGLE_LOOP = "toggle_loop";
	@Action public static final String FILE_OPEN_WITH_HISTORY = "file_open_with_history";
	@Action public static final String ALL_CLEAR_TEMPO = "all_clear_tempo";
	@Action public static final String MML_GENERATE = "mml_generate";
	@Action public static final String WAVOUT = "wavout";
	@Action public static final String KEYBOARD_INPUT = "keyboard_input";
	@Action public static final String INPUT_EMPTY_CORRECTION = "input_empty_correction";
	@Action public static final String CHANGE_SCALE_COLOR = "change_scale_color";
	@Action public static final String REMOVE_RESTS_BETWEEN_NOTES = "remote_rests_between_notes";
	@Action public static final String CHANGE_UI = "change_ui";

	private final HashMap<String, Consumer<Object>> actionMap = new HashMap<>();

	private File openedFile = null;

	private final FileFilter mmsFilter = new FileNameExtensionFilter(AppResource.appText("file.mms"), "mms");
	private final FileFilter mmiFilter = new FileNameExtensionFilter(AppResource.appText("file.mmi"), "mmi");
	private final FileFilter mmlFilter = new FileNameExtensionFilter(AppResource.appText("file.mml"), "mml");
	private final FileFilter allFilter = new FileNameExtensionFilter(AppResource.appText("file.all"), "mmi", "mms", "mml", "mid");
	private final FileFilter midFilter = new FileNameExtensionFilter(AppResource.appText("file.mid"), "mid");
	private final FileFilter wavFilter = new FileNameExtensionFilter(AppResource.appText("file.wav"), "wav");

	private final JFileChooser openFileChooser;
	private final JFileChooser saveFileChooser;
	private final JFileChooser exportFileChooser;
	private final JFileChooser wavoutFileChooser;

	private static ActionDispatcher instance = null;
	public static ActionDispatcher getInstance() {
		if (instance == null) {
			instance = new ActionDispatcher();
		}
		return instance;
	}

	private ActionDispatcher() {
		openFileChooser = MabiIcco.createFileChooser();
		saveFileChooser = MabiIcco.createFileChooser();
		exportFileChooser = MabiIcco.createFileChooser();
		wavoutFileChooser = MabiIcco.createFileChooser();
	}

	public ActionDispatcher setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.mmlSeqView = mainFrame.getMMLSeqView();
		this.fileState = this.mmlSeqView.getFileState();
		this.editState = this.mmlSeqView.getEditState();

		this.fileState.setFileStateObserver(this);
		this.editState.setEditStateObserver(this);
		return this;
	}

	public void initialize() {
		initializeFileChooser();
		initializeActionMap();
		MabiDLS.getInstance().addTrackEndNotifier(() -> stopAction());
	}

	private void initializeFileChooser() {
		openFileChooser.addChoosableFileFilter(allFilter);
		openFileChooser.addChoosableFileFilter(mmiFilter);
		openFileChooser.addChoosableFileFilter(mmsFilter);
		openFileChooser.addChoosableFileFilter(mmlFilter);
		openFileChooser.addChoosableFileFilter(midFilter);
		saveFileChooser.addChoosableFileFilter(mmiFilter);
		exportFileChooser.addChoosableFileFilter(midFilter);
		wavoutFileChooser.addChoosableFileFilter(wavFilter);
	}

	private void initializeActionMap() {
		actionMap.put(VIEW_SCALE_UP, t -> mmlSeqView.getPianoRollScaler().expandPianoViewWide());
		actionMap.put(VIEW_SCALE_DOWN, t -> mmlSeqView.getPianoRollScaler().reducePianoViewWide());
		actionMap.put(STOP, t -> this.stopAction());
		actionMap.put(PAUSE, t -> this.pauseAction());
		actionMap.put(FILE_OPEN, t -> this.openMMLFileAction());
		actionMap.put(NEW_FILE, t -> this.newMMLFileAction());
		actionMap.put(RELOAD_FILE, t -> this.reloadMMLFileAction());
		actionMap.put(QUIT, t -> this.quitAction());
		actionMap.put(ADD_TRACK, t -> mmlSeqView.addMMLTrack(null));
		actionMap.put(REMOVE_TRACK, t -> mmlSeqView.removeMMLTrack());
		actionMap.put(DUPLICATE_TRACK, t -> this.duplicateMMLTrack());
		actionMap.put(TRACK_PROPERTY, t -> mmlSeqView.editTrackPropertyAction());
		actionMap.put(SET_START_POSITION, t -> mmlSeqView.setStartPosition());
		actionMap.put(PLAY, t -> this.playAction());
		actionMap.put(INPUT_FROM_CLIPBOARD, t -> mmlSeqView.inputClipBoardAction());
		actionMap.put(OUTPUT_TO_CLIPBOARD, t -> mmlSeqView.outputClipBoardAction());
		actionMap.put(UNDO, t -> mmlSeqView.undo());
		actionMap.put(REDO, t -> mmlSeqView.redo());
		actionMap.put(SAVE_FILE, t -> saveMMLFile(openedFile));
		actionMap.put(SAVEAS_FILE, t -> this.saveAsMMLFileAction());
		actionMap.put(CUT, t -> editState.selectedCut());
		actionMap.put(COPY, t -> editState.selectedCopy());
		actionMap.put(PASTE, t -> this.editPasteAction());
		actionMap.put(DELETE, t -> editState.selectedDelete());
		actionMap.put(SCORE_PROPERTY, t -> this.scorePropertyAction());
		actionMap.put(NEXT_TIME, t -> mmlSeqView.nextStepTimeTo(true));
		actionMap.put(PREV_TIME, t -> mmlSeqView.nextStepTimeTo(false));
		actionMap.put(PART_CHANGE, t -> mmlSeqView.partChange());
		actionMap.put(ADD_MEASURE, t -> this.addMeasure());
		actionMap.put(REMOVE_MEASURE, t -> this.removeMeasure());
		actionMap.put(ADD_BEAT, t -> this.addBeat());
		actionMap.put(REMOVE_BEAT, t -> this.removeBeat());
		actionMap.put(NOTE_PROPERTY, t -> editState.noteProperty());
		actionMap.put(TRANSPOSE, t -> new MMLTranspose().execute(mainFrame, mmlSeqView));
		actionMap.put(TRACKS_EDIT, t -> new MultiTracksVelocityChangeEditor(mainFrame, mmlSeqView).showDialog());
		actionMap.put(TRACKS_VIEW, t -> new MultiTracksViewEditor(mainFrame, mmlSeqView).showDialog());
		actionMap.put(ABOUT, t -> new About().show(mainFrame));
		actionMap.put(MIDI_EXPORT, t -> this.midiExportAction());
		actionMap.put(FILE_IMPORT, t -> this.fileImportAction());
		actionMap.put(CLEAR_DLS, t -> this.clearDLSInformation());
		actionMap.put(SELECT_ALL, t -> this.selectAction(() -> editState.selectAll()));
		actionMap.put(SELECT_PREVIOUS_ALL, t -> this.selectAction(() -> editState.selectPreviousAll()));
		actionMap.put(SELECT_AFTER_ALL, t -> this.selectAction(() -> editState.selectAfterAll()));
		actionMap.put(SELECT_ALL_SAME_PITCH, t -> this.selectAction(() -> editState.selectAllSamePitch()));
		actionMap.put(MML_IMPORT, t -> mmlSeqView.mmlImport());
		actionMap.put(MML_EXPORT, t -> mmlSeqView.mmlExport());
		actionMap.put(SWITCH_TRACK_NEXT, t -> mmlSeqView.switchTrack(true));
		actionMap.put(SWITCH_TRACK_PREV, t -> mmlSeqView.switchTrack(false));
		actionMap.put(SWITCH_MMLPART_NEXT, t -> mmlSeqView.switchMMLPart(true));
		actionMap.put(SWITCH_MMLPART_PREV, t -> mmlSeqView.switchMMLPart(false));
		actionMap.put(TOGGLE_LOOP, t -> this.toggleLoop());
		actionMap.put(CHANGE_NOTE_HEIGHT_INT, t -> this.changeNoteHeight(t));
		actionMap.put(FILE_OPEN_WITH_HISTORY, t -> this.fileOpenWithHistory(t));
		actionMap.put(ALL_CLEAR_TEMPO, t -> this.allClearTempo());
		actionMap.put(MML_GENERATE, t -> mmlSeqView.updateActivePart(true));
		actionMap.put(WAVOUT, t -> this.startWavOutAction());
		actionMap.put(KEYBOARD_INPUT, t -> mmlSeqView.showKeyboardInput());
		actionMap.put(INPUT_EMPTY_CORRECTION, t -> this.inputEmptyCorrection());
		actionMap.put(CHANGE_SCALE_COLOR, t -> this.changeScaleColor(t));
		actionMap.put(REMOVE_RESTS_BETWEEN_NOTES, t -> editState.removeRestsBetweenNotes());
		actionMap.put(CHANGE_UI, t -> this.changeUI());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		Consumer<Object> func = actionMap.get(command);
		if (func != null) {
			func.accept( e.getSource() );
		} else {
			System.err.println("not found Action: " + command);
		}
	}

	/**
	 * ノートの表示している高さを変更する.
	 * @param source    高さ設定indexのSupplier
	 */
	private void changeNoteHeight(Object source) {
		if (source instanceof IntSupplier) {
			int index = ((IntSupplier)source).getAsInt();
			mmlSeqView.setPianoRollHeightScaleIndex(index);
			MabiIccoProperties.getInstance().setPianoRollViewHeightScaleProperty(index);
		}
	}

	private MMLScore fileParse(File file) {
		MMLScore score = null;
		try {
			IMMLFileParser fileParser = IMMLFileParser.getParser(file);
			FileInputStream in = new FileInputStream(file);
			score = fileParser.parse(in);
			in.close();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(mainFrame,
					AppResource.appText("error.nofile"),
					AppResource.appText("error.read"),
					JOptionPane.WARNING_MESSAGE);
		} catch (MMLParseException e) {
			JOptionPane.showMessageDialog(mainFrame,
					AppResource.appText("error.invalid_file"),
					AppResource.appText("error.read"),
					JOptionPane.WARNING_MESSAGE);
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(mainFrame,
					e.getClass().getCanonicalName(),
					AppResource.appText("error.read"),
					JOptionPane.WARNING_MESSAGE);
		}

		// mabiicco由来のファイルであれば, generateされたものにする.
		if (score != null) {
			score = score.toGeneratedScore();
		}
		return score;
	}

	private void fileOpenWithHistory(Object o) {
		if (o instanceof IntSupplier) {
			int index = ((IntSupplier)o).getAsInt();
			File file = MabiIccoProperties.getInstance().getFileHistory()[index];
			if ( (file == null) || (!file.exists()) ) {
				JOptionPane.showMessageDialog(mainFrame,
						AppResource.appText("error.nofile"),
						AppResource.appText("error.read"),
						JOptionPane.WARNING_MESSAGE);
			} else {
				checkAndOpenMMLFile( file );
			}
		}
	}

	public void checkAndOpenMMLFile(File file) {
		if (checkCloseModifiedFileState()) {
			openMMLFile(file);
		}
	}

	private void openMMLFile(File file) {
		NanoTime time = NanoTime.start();
		MMLScore score = fileParse(file);
		if ( (score != null) && (score.getTrackCount() > 0) ) {
			// ミュートボタンの状態を反映させるために, 先にミュート解除する.
			MabiDLS.getInstance().all();
			mmlSeqView.setMMLScore(score);

			openedFile = file;
			notifyUpdateFileState();
			MabiIccoProperties.getInstance().setRecentFile(file.getPath());
			MabiIccoProperties.getInstance().setFileHistory(file);
			mainFrame.updateFileHistoryMenu();
		}
		showTime("open", time);
	}

	private void reloadMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			return;
		}

		if (openedFile != null) {
			if (fileState.isModified()) {
				int status = JOptionPane.showConfirmDialog(mainFrame, 
						AppResource.appText("message.throw"), 
						AppResource.getAppTitle(), 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
				if (status == JOptionPane.YES_OPTION) {
					openMMLFile(openedFile);
				}
			}
		}
	}

	private void quitAction() {
		//  閉じる前に、変更が保存されていなければダイアログ表示する.
		if (checkCloseModifiedFileState()) {
			System.exit(0);
		}
	}

	/**
	 * @param file
	 * @return　保存に成功した場合は true, 失敗した場合は false を返す.
	 */
	private boolean saveMMLFile(File file) {
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			mmlSeqView.getMMLScore().writeToOutputStream(outputStream);
			mainFrame.setTitleAndFileName(file.getName());
			fileState.setOriginalBase();
			notifyUpdateFileState();
			MabiIccoProperties.getInstance().setRecentFile(file.getPath());
			MabiIccoProperties.getInstance().setFileHistory(file);
			mainFrame.updateFileHistoryMenu();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(mainFrame, AppResource.appText("fail.saveFile"), "ERROR", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void newMMLFileAction() {
		if (checkCloseModifiedFileState()) {
			openedFile = null;
			mmlSeqView.initializeMMLTrack();
			mmlSeqView.setStartPosition();
			notifyUpdateFileState();
			MabiDLS.getInstance().all();
		}
	}

	private void openMMLFileAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			return;
		}

		if (checkCloseModifiedFileState()) {
			SwingUtilities.invokeLater(() -> {
				File file = fileOpenDialog();
				if (file != null) {
					openMMLFile(file);
				}
			});
		}
	}

	private File fileOpenDialog() {
		String recentPath = MabiIccoProperties.getInstance().getRecentFile();
		openFileChooser.setFileFilter(allFilter);
		openFileChooser.setAcceptAllFileFilterUsed(false);
		openFileChooser.setSelectedFile(null);
		openFileChooser.setCurrentDirectory(new File(recentPath).getParentFile());
		int status = openFileChooser.showOpenDialog(mainFrame);
		if (status == JFileChooser.APPROVE_OPTION) {
			File file = openFileChooser.getSelectedFile();
			return file;
		}
		return null;
	}

	private void saveAsMMLFileAction() {
		SwingUtilities.invokeLater(() -> {
			showDialogSaveFile();
			notifyUpdateFileState();
		});
	}

	/**
	 * ファイル保存のためのダイアログ表示.
	 * @return 保存ファイル, 保存ファイルがなければnull.
	 */
	private File showSaveDialog(JFileChooser fileChooser, String suffix) {
		String recentPath = MabiIccoProperties.getInstance().getRecentFile();
		if (openedFile != null) {
			fileChooser.setSelectedFile(openedFile);
		} else {
			fileChooser.setSelectedFile(null);
			fileChooser.setCurrentDirectory(new File(recentPath).getParentFile());
		}
		fileChooser.setAcceptAllFileFilterUsed(false);
		int status = fileChooser.showSaveDialog(mainFrame);
		if (status == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.toString().endsWith("."+suffix)) {
				file = new File(file+"."+suffix);
			}

			status = JOptionPane.YES_OPTION;
			if (file.exists()) {
				// すでにファイルが存在する場合の上書き警告表示.
				status = JOptionPane.showConfirmDialog(mainFrame, AppResource.appText("message.override")+"\n"+file.getName(), AppResource.getAppTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			if (status == JOptionPane.YES_OPTION) {
				return file;
			}
		}
		return null;
	}

	/**
	 * 別名保存のダイアログ表示
	 * @return 保存した場合は trueを返す.
	 */
	private boolean showDialogSaveFile() {
		saveFileChooser.setFileFilter(mmiFilter);
		File file = showSaveDialog(saveFileChooser, "mmi");
		if (file != null) {
			if (saveMMLFile(file)) {
				openedFile = file;
				return true;
			}
		}
		return false;
	}

	/**
	 * MIDI-exportダイアログ表示
	 */
	private void midiExportAction() {
		File file = showSaveDialog(exportFileChooser, "mid");
		if (file != null) {
			try {
				MidiSystem.write(MabiDLS.getInstance().createSequence(mmlSeqView.getMMLScore(), 0), 1, file);
			} catch (IOException | InvalidMidiDataException e) {
				JOptionPane.showMessageDialog(mainFrame, e.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Wavファイル出力
	 */
	private void startWavOutAction() {
		File file = showSaveDialog(wavoutFileChooser, "wav");

		if (file != null) {
			new WavoutPanel(mainFrame, mmlSeqView, file).showDialog();
		}
	}

	/**
	 * ファイルからTrack単位のインポートを行う.
	 */
	private void fileImportAction() {
		File file = fileOpenDialog();
		fileImport(file);
	}

	public void fileImport(File file) {
		if (file != null) {
			NanoTime time = NanoTime.start();
			MMLScore score = fileParse(file);
			showTime("import", time);
			if (score != null) {
				MabiIccoProperties.getInstance().setRecentFile(file.getPath());
				new MMLImportPanel(mainFrame, score, mmlSeqView).showDialog();
			}
		}
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
		int status = JOptionPane.showConfirmDialog(mainFrame, AppResource.appText("message.modifiedClose"), AppResource.getAppTitle(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
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
				return saveMMLFile(openedFile);
			} else if (showDialogSaveFile()) {
				// ファイルOpenされているが、サポート外なので別名保存.
				return true;
			}
		}

		return false;
	}

	private boolean isSupportedSaveFile() {
		if (openedFile != null) {
			if (openedFile.getName().toLowerCase().endsWith(".mmi")) {
				return true;
			}
		}

		return false;
	}

	private void scorePropertyAction() {
		MMLScorePropertyPanel propertyPanel = new MMLScorePropertyPanel();
		propertyPanel.showDialog(mainFrame, mmlSeqView.getMMLScore(), mmlSeqView.getFileState());
		mmlSeqView.repaint();
	}

	private void editPasteAction() {
		long startTick = mmlSeqView.getEditSequencePosition();
		editState.paste(startTick);
	}

	private void stopAction() {
		NanoTime time = NanoTime.start();
		MabiDLS.getInstance().getSequencer().stop();
		showTime("stop", time);
		mainFrame.enableNoplayItems();
	}

	private void pauseAction() {
		stopAction();
		mmlSeqView.pauseTickPosition();
	}

	private void playAction() {
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			pauseAction();
		} else {
			mmlSeqView.startSequence();
			mainFrame.disableNoplayItems();
		}
	}

	private void clearDLSInformation() {
		MabiIccoProperties properties = MabiIccoProperties.getInstance();
		StringBuilder sb = new StringBuilder(AppResource.appText("message.clear_dls"));
		properties.getDlsFile().stream().forEach(t -> {
			sb.append("\n * ").append(t.getName());
		});
		int status = JOptionPane.showConfirmDialog(mainFrame, 
				sb.toString(), 
				AppResource.appText("menu.clear_dls"), 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.WARNING_MESSAGE);
		if (status == JOptionPane.OK_OPTION) {
			properties.setDlsFile(null);
			System.out.println("clearDLSInformation");
		}
	}

	/**
	 * 選択アクション
	 * @param action
	 */
	private void selectAction(Runnable action) {
		action.run();
		mmlSeqView.repaint();
		notifyUpdateEditState();
	}

	private void addMeasure() {
		mmlSeqView.addTicks( mmlSeqView.getMMLScore().getMeasureTick() );
	}

	private void addBeat() {
		mmlSeqView.addTicks( mmlSeqView.getMMLScore().getBeatTick() );
	}

	private void removeMeasure() {
		mmlSeqView.removeTicks( mmlSeqView.getMMLScore().getMeasureTick() );
	}

	private void removeBeat() {
		mmlSeqView.removeTicks( mmlSeqView.getMMLScore().getBeatTick() );
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
				mainFrame.setTitleAndFileName(openedFile.getName()+" "+AppResource.appText("file.modified"));
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
		mainFrame.setRemoveRestsBetweenNotesEnable(editState.hasSelectedMultipleConsecutiveNotes());
	}

	private void toggleLoop() {
		MabiDLS dls = MabiDLS.getInstance();
		dls.setLoop( !dls.isLoop() );
		mainFrame.updateLoop( dls.isLoop() );
	}

	private void allClearTempo() {
		mmlSeqView.getMMLScore().getTempoEventList().clear();
		mmlSeqView.updateActivePart(true);
	}

	/**
	 * データ復旧.
	 * @return 復旧処理を実行したとき trueを返す.
	 */
	public boolean recoveryCheck() {
		File recoveryFile = new File(AppResource.appText("recover.filename"));
		if (recoveryFile.exists()) {
			int status = JOptionPane.showConfirmDialog(mainFrame,
					AppResource.appText("recover.message")+"\n"+recoveryFile.getName(),
					AppResource.appText("recover.title"),
					JOptionPane.YES_NO_OPTION);
			if (status != JOptionPane.OK_OPTION) {
				File renameFile = new File(recoveryFile.getAbsolutePath()+".bak");
				renameFile.delete();
				recoveryFile.renameTo(renameFile);
				return false;
			}
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(recoveryFile),"UTF-8"));
				String filename = reader.readLine();
				String data = reader.readLine();
				reader.close();
				recoveryFile.delete();
				boolean result = mmlSeqView.recovery(data);
				if (!result) {
					JOptionPane.showMessageDialog(mainFrame,
							"recover.fail", "recover.title", JOptionPane.WARNING_MESSAGE);
				}
				if ( result && (filename.length() > 0) ) {
					openedFile = new File(filename);
				}
				notifyUpdateFileState();
				return result;
			} catch (IOException e) {}
		}
		return false;
	}

	/**
	 * 復旧用データを書き出す.
	 */
	public void writeRecoveryData() {
		String filename = "";
		if (openedFile != null) {
			filename = openedFile.getAbsolutePath();
		}
		String data = mmlSeqView.getRecoveryData();

		try {
			File recoveryFile = new File(AppResource.appText("recover.filename"));
			PrintStream printStream = new PrintStream(new FileOutputStream(recoveryFile), false, "UTF-8");
			printStream.println(filename);
			printStream.println(data);
			printStream.close();
		} catch (IOException e) {}
	}

	public void showTime(String name, long ms) {
		if (mainFrame != null) {
			String text = name+" "+ms+"ms";
			mainFrame.setStatusText(text);
		}
	}

	public void showTime(String name, NanoTime time) {
		showTime(name, time.ms());
	}

	private void inputEmptyCorrection() {
		String value = JOptionPane.showInputDialog(mainFrame, AppResource.appText("mml.emptyCorrection"), MabiIccoProperties.getInstance().mmlEmptyCorrection.get());
		if (value != null) {
			MabiIccoProperties.getInstance().mmlEmptyCorrection.set(value);
		}
	}

	private void changeScaleColor(Object source) {
		if (source instanceof Supplier<?>) {
			Object o = ((Supplier<?>) source).get();
			if (o instanceof ScaleColor) {
				mmlSeqView.setScaleColor((ScaleColor)o);
			} else {
				System.err.println("changeScaleColor invalid param (not Supplier<ScaleColor>)");
			}
		} else {
			System.err.println("changeScaleColor invalid param (not Supplier)");
		}
	}

	private void duplicateMMLTrack() {
		MMLTrack track = mmlSeqView.getSelectedTrack().clone();
		mmlSeqView.addMMLTrack(track);
	}

	private void changeUI() {
		try {
			if (MabiIccoProperties.getInstance().useSystemLaF.get()) {
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			} else {
				UIManager.setLookAndFeel( new FlatLightLaf() );
			}
			SwingUtilities.updateComponentTreeUI(mainFrame);
		} catch (Exception e) {}
	}
}

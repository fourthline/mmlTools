/*
 * Copyright (C) 2013-2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import javax.sound.midi.Sequencer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.IEditState;
import jp.fourthline.mabiicco.IFileState;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mabiicco.ui.PianoRollView.NoteHeight;
import jp.fourthline.mabiicco.ui.PianoRollView.PaintMode;
import jp.fourthline.mabiicco.ui.color.ScaleColor;
import jp.fourthline.mabiicco.ui.editor.KeyboardEditor;
import jp.fourthline.mabiicco.ui.editor.MMLEditor;
import jp.fourthline.mabiicco.ui.editor.MMLScoreUndoEdit;
import jp.fourthline.mabiicco.ui.editor.MMLTextEditor;
import jp.fourthline.mabiicco.ui.mml.MMLInputPanel;
import jp.fourthline.mabiicco.ui.mml.MMLOutputPanel;
import jp.fourthline.mabiicco.ui.mml.MMLPartChangePanel;
import jp.fourthline.mabiicco.ui.mml.TrackPropertyPanel;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.Measure;
import jp.fourthline.mmlTools.core.MMLTicks;
import jp.fourthline.mmlTools.core.NanoTime;
import jp.fourthline.mmlTools.core.UndefinedTickException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

/**
 * 主表示部.
 * 
 * <pre>
 * MMLSeqView
 *  |
 *  +- {@link PianoRollView} (JScrollPane 内）
 *  |
 *  +- {@link KeyboardView} (JScrollPane の行ヘッダ）
 *  |
 *  +- {@link ColumnPanel} (JScrollPane の列ヘッダ）
 *  |
 *  +- {@link MMLTrackView} ({@link TrackTabbedPane} extends JTabbedPane 内)
 * </pre>
 */
public final class MMLSeqView extends AbstractMMLManager implements ChangeListener, ActionListener, MainView {
	private static final int INITIAL_TRACK_COUNT = 1;

	private final JScrollPane scrollPane;
	private final PianoRollView pianoRollView;
	private final PianoRollScaler pianoRollScaler;
	private final KeyboardView keyboardView;
	private final JTabbedPane tabbedPane;
	private final ColumnPanel columnView;

	private final MMLScoreUndoEdit undoEdit = new MMLScoreUndoEdit(this);

	private final MMLEditor editor;
	private final KeyboardEditor keyboardEditor;

	private final JPanel panel;
	private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(4);

	private final Frame parentFrame;

	/**
	 * Create the panel.
	 * @param parentFrame 関連付けるFrame
	 */
	public MMLSeqView(Frame parentFrame) {
		this.parentFrame = parentFrame;
		panel = new JPanel(false);
		panel.setLayout(new BorderLayout(0, 0));

		// Scroll View (KeyboardView, PianoRollView) - CENTER
		pianoRollView = new PianoRollView();
		keyboardView = new KeyboardView(this, pianoRollView);

		scrollPane = new JScrollPane(pianoRollView, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(pianoRollView.getNoteHeight());

		panel.add(scrollPane, BorderLayout.CENTER);
		pianoRollView.setViewportAndParent(scrollPane.getViewport(), this);

		// MMLTrackView (tab) - SOUTH
		tabbedPane = new TrackTabbedPane(this);
		tabbedPane.addChangeListener(this);
		tabbedPane.setPreferredSize(new Dimension(0, 200));
		panel.add(tabbedPane, BorderLayout.SOUTH);

		// create mml editor
		editor = new MMLEditor(parentFrame, keyboardView, pianoRollView, this);
		columnView = new ColumnPanel(parentFrame, pianoRollView, this, editor);

		// PianoRollScaler
		pianoRollScaler = new PianoRollScaler(this, pianoRollView, scrollPane, this, editor);

		// create keyboard editor
		keyboardEditor = new KeyboardEditor(parentFrame, this, keyboardView, editor, pianoRollView);

		scrollPane.setRowHeaderView(keyboardView);
		scrollPane.setColumnHeaderView(columnView);

		initialSetView();
		initializeMMLTrack();

		startSequenceThread();
	}

	public void setNoteAlignChanger(IntConsumer noteAlignChanger) {
		keyboardEditor.setNoteAlignChanger(noteAlignChanger);
	}

	public boolean recovery(String s) {
		boolean result = undoEdit.recover(s);
		System.out.println("recover: "+result);
		if (result) {
			mmlScore = mmlScore.toGeneratedScore(false);
			undoEdit.revertState();
			updateTrackTabIcon();
			updateActivePart(false);
			updateProgramSelect();
		}
		return result;
	}

	public String getRecoveryData() {
		return undoEdit.getBackupString();
	}

	private boolean currentEditMode = true;
	@Override
	public void repaint() {
		boolean editMode = MabiIccoProperties.getInstance().enableEdit.get();
		if (currentEditMode != editMode) {
			currentEditMode = editMode;
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				MMLTrackView view = (MMLTrackView) tabbedPane.getComponentAt(i);
				view.setVisibleMMLTextPanel(currentEditMode);
			}
			resetTrackView();
			tabbedPane.setPreferredSize(new Dimension(0, currentEditMode ? 200 : 60));
		}

		panel.repaint();
	}

	public JPanel getPanel() {
		return panel;
	}

	private void initialSetView() {
		EventQueue.invokeLater(() -> {
			// (ピアノロール全体の高さ / 2) - （表示領域 / 2）＝真ん中の座標。
			int y = (pianoRollView.getTotalHeight() / 2) - (scrollPane.getHeight() / 2);

			// 初期のView位置
			scrollPane.getViewport().setViewPosition(new Point(0, y));
		});
	}

	public void initializeMMLTrack() {
		mmlScore = new MMLScore();
		tabbedPane.removeAll();
		for (int i = 0; i < INITIAL_TRACK_COUNT; i++) {
			addMMLTrack(null);
		}

		panel.repaint();
		undoEdit.initState();
	}

	private String getNewTrackName() {
		LinkedList<String> list = new LinkedList<>();
		for (int i = 0; i <= MMLScore.MAX_TRACK; i++) {
			list.add("Track" + (i+1));
		}
		for (var track : mmlScore.getTrackList()) {
			list.remove(track.getTrackName());
		}
		return list.getFirst();
	}

	/**
	 * トラックの追加。作成したトラックを選択状態にします。
	 */
	@Override
	public void addMMLTrack(MMLTrack newTrack) {
		if (newTrack == null) {
			newTrack = new MMLTrack();
			newTrack.setTrackName( getNewTrackName() );
		}

		int trackIndex = mmlScore.addTrack(newTrack);
		if (trackIndex < 0) {
			return;
		}

		// トラックビューの追加
		tabbedPane.add(newTrack.getTrackName(), MMLTrackView.getInstance(trackIndex, this, this));
		tabbedPane.setSelectedIndex(trackIndex);
		updateTrackTabIcon();
		MabiDLS.getInstance().setMute(trackIndex, false);

		// エディタ更新
		updateActivePart(true);
		updateSelectedTrackAndMMLPart();
		updateProgramSelect();
	}

	/**
	 * 現在選択中のトラックを移動します.
	 * @param toIndex 移動先index
	 */
	@Override
	public void moveTrack(int toIndex) {
		int fromIndex = tabbedPane.getSelectedIndex();
		MabiDLS mabiDLS = MabiDLS.getInstance();

		boolean mute = mabiDLS.getMute(fromIndex);
		mmlScore.moveTrack(fromIndex, toIndex);

		if (fromIndex < toIndex) {
			for (int i = fromIndex+1; i < toIndex; i++) {
				mabiDLS.setMute(i, mabiDLS.getMute(i+1));
			}
		} else {
			for (int i = mmlScore.getTrackCount()-1; i > toIndex; i--) {
				mabiDLS.setMute(i, mabiDLS.getMute(i-1));
			}
		}

		mabiDLS.setMute(toIndex, mute);
		tabbedPane.setSelectedIndex(toIndex);

		resetTrackView();
		updateActivePart(false);
	}

	/**
	 * トラックの削除
	 * 現在選択中のトラックを削除します。
	 */
	public void removeMMLTrack() {
		int index = tabbedPane.getSelectedIndex();

		mmlScore.removeTrack(index);
		resetTrackView();

		// mute設定へ反映.
		for (int i = index; i < mmlScore.getTrackCount(); i++) {
			MabiDLS mabiDLS = MabiDLS.getInstance();
			mabiDLS.setMute(i, mabiDLS.getMute(i+1));
		}

		if (mmlScore.getTrackCount() == 0) {
			addMMLTrack(null);
		} else {
			// ピアノロール更新
			pianoRollView.repaint();
			// エディタ更新
			updateSelectedTrackAndMMLPart();
		}

		updateActivePart(true);
	}

	private void updateTrackTabIcon() {
		int len = tabbedPane.getTabCount();

		for (int i = 0; i < len; i++) {
			tabbedPane.setIconAt(i, PartButtonIcon.getInstance(1, i));
		}
	}

	/**
	 * 再生スタート（現在のシーケンス位置を使用）
	 */
	public void startSequence() {
		new Thread(() -> {
			NanoTime time = NanoTime.start();
			long startTick = pianoRollView.getSequencePosition();
			MabiDLS.getInstance().createSequenceAndStart(mmlScore, startTick);
			ActionDispatcher.getInstance().showTime("play", time);
		}).start();
	}

	/**
	 * 新規で複数のトラックをセットする。
	 */
	@Override
	public void setMMLScore(MMLScore score) {
		mmlScore = score;

		pianoRollView.repaint();
		tabbedPane.removeAll();

		int trackCount = 0;
		for (MMLTrack track : score.getTrackList()) {
			String name = track.getTrackName();
			if (name == null) {
				name = "Track"+(trackCount+1);
			}

			tabbedPane.add(name, MMLTrackView.getInstance(trackCount, this, this));
			trackCount++;
		}

		undoEdit.initState();
		initialSetView();
		pianoRollView.setSequenceTick(0);
		updateTrackTabIcon();
		updateActivePart(false);
		updateProgramSelect();
	}

	/**
	 * 現在のトラックにMMLを設定する。
	 */
	@Override
	public void setMMLselectedTrack(MMLTrack mml) {
		if (mmlScore.getTrackCount() == 1) {
			mmlScore.getTempoEventList().clear();
		}
		int index = tabbedPane.getSelectedIndex();
		mmlScore.setTrack(index, mml);
		tabbedPane.setTitleAt(index, mml.getTrackName());

		// 表示を更新
		MMLTrackView view = (MMLTrackView)tabbedPane.getComponentAt(index);
		view.updateTrack();
		updateSelectedTrackAndMMLPart();
		updateActivePart(false);
	}

	/**
	 * 現在選択中のトラックを取得する。
	 * @return 選択中のMMLTrack
	 */
	public MMLTrack getSelectedTrack() {
		int index = tabbedPane.getSelectedIndex();
		if (index < 0) {
			index = 0;
		}
		return mmlScore.getTrack(index);
	}

	public PaintMode getPaintMode() {
		return pianoRollView.getPaintMode();
	}

	public void setPaintMode(PaintMode mode) {
		pianoRollView.setPaintMode(mode);
	}

	public void editTrackPropertyAction() {
		MMLTrack track = getSelectedTrack();
		new TrackPropertyPanel(track, this).showDialog(parentFrame);
		tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), track.getTrackName());
	}

	private void setViewPosition(int x) {
		JViewport viewport = scrollPane.getViewport();
		Point point = viewport.getViewPosition();
		Dimension dim = viewport.getExtentSize();
		double x1 = point.getX();
		double x2 = x1 + dim.getWidth();

		if ( (x < x1) || (x > x2) ) {
			point.setLocation(x, point.getY());
			viewport.setViewPosition(point);
		}
	}

	public void setPianoRollHeightScale(NoteHeight nh) {
		pianoRollView.setNoteHeight(nh);
		keyboardView.updateHeight();
		panel.repaint();
	}

	/**
	 * 表示ライン表示を現在のシーケンス位置に戻す
	 */
	public void resetViewPosition() {
		setViewPosition(pianoRollView.getSequenceX());
	}

	/**
	 * シーケンスの現在位置を先頭に戻す
	 */
	public void setStartPosition() {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		if (!sequencer.isRunning()) {
			setViewPosition(0);
			pianoRollView.setSequenceTick(0);
			panel.repaint();
		} else {
			sequencer.setTempoInBPM(120);
			sequencer.setTickPosition(0);
		}
	}

	/**
	 * 現在のTickにシーケンスを設定する。（一時停止用）
	 */
	public void pauseTickPosition() {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		long tick = sequencer.getTickPosition();
		tick -= tick % MMLTicks.minimumTick();
		pianoRollView.setSequenceTick(tick);
	}

	public void inputClipBoardAction() {
		MMLInputPanel mmlInputDialog = new MMLInputPanel(parentFrame, getNewTrackName(), this);
		mmlInputDialog.showDialog();
	}

	public void outputClipBoardAction() {
		MMLOutputPanel outputPanel = new MMLOutputPanel(parentFrame, mmlScore.getTrackList(), mmlScore);
		outputPanel.showDialog();
	}

	public void mmlImport() {
		String text = MMLInputPanel.getClipboardString();
		if (new MMLTrack().setMML(text).isEmpty()) {
			return;
		}

		getSelectedTrack().setMabiMML(text);
		resetTrackView();
		undoEdit.saveState();
		panel.repaint();
	}

	public void mmlExport() {
		String text = getActiveTrack().getMabiMML();
		MMLOutputPanel.copyToClipboard(parentFrame, text, AppResource.appText("mml.output.done"));
	}

	private void updateSelectedTrackAndMMLPart() {
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		if (view != null) {
			view.updateMuteButton();
			int program = getActivePartProgram();

			editor.reset();

			InstClass inst = MabiDLS.getInstance().getInstByProgram(program);
			pianoRollView.setRelativeInst(inst);
			keyboardView.setRelativeInst(inst);

			pianoRollView.repaint();
			columnView.repaint();
			keyboardView.repaint();
		}
	}

	@Override
	public int getActiveTrackIndex() {
		return tabbedPane.getSelectedIndex();
	}

	@Override
	public int getActiveMMLPartIndex() {
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		return view.getSelectedMMLPartIndex();
	}

	@Override
	public MMLEventList getActiveMMLPart() {
		if (!currentEditMode) {
			return null;
		}
		MMLTrack track = getActiveTrack();
		return track.getMMLEventAtIndex(getActiveMMLPartIndex());
	}

	/**
	 * 編集時の音符基準長を設定します.
	 * @param alignTick 基準tick
	 */
	public void setEditAlign(int alignTick) {
		editor.setEditAlign(alignTick);
	}

	public PianoRollScaler getPianoRollScaler() {
		return this.pianoRollScaler;
	}

	/**
	 * MMLTrackViewのタブを再構築する.
	 * トラック追加/削除のundo, redo時に実行される.
	 */
	private void resetTrackView() {
		int selectedTab = tabbedPane.getSelectedIndex();
		int selectedPart = ((MMLTrackView) tabbedPane.getSelectedComponent()).getSelectedMMLPartIndex();

		tabbedPane.removeAll();
		int i = 0;
		for (MMLTrack track : mmlScore.getTrackList()) {
			tabbedPane.add(track.getTrackName(), MMLTrackView.getInstance(i, this, this));
			i++;
		}

		if (selectedTab >= i) {
			selectedTab = i-1;
		}
		if (mmlScore.getTrackCount() > 0) {
			MMLTrack track = mmlScore.getTrack(selectedTab);
			int program = track.getProgram();
			if (!InstClass.getEnablePartByProgram(program)[selectedPart]) {
				if ( (selectedPart == 3) && (track.getSongProgram() >= 0) ) {
				} else {
					selectedPart = InstClass.getFirstPartNumberOnProgram(program);
				}
			}

			tabbedPane.setSelectedIndex(selectedTab);
			((MMLTrackView) tabbedPane.getSelectedComponent()).setSelectMMLPartOfIndex(selectedPart);
			updateTrackTabIcon();
		}
	}

	public void undo() {
		if (undoEdit.canUndo()) {
			undoEdit.undo();
			mmlScore = mmlScore.toGeneratedScore(false);
			resetTrackView();
			updateSelectedTrackAndMMLPart();
			updateActivePart(false);
		}
	}

	public void redo() {
		if (undoEdit.canRedo()) {
			undoEdit.redo();
			mmlScore = mmlScore.toGeneratedScore(false);
			resetTrackView();
			updateSelectedTrackAndMMLPart();
			updateActivePart(false);
		}
	}

	public void nextStepTimeTo(boolean next) {
		int tick = (int) pianoRollView.getSequencePlayPosition();
		tick = Measure.nextMeasure(mmlScore, tick, next);
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		if (!sequencer.isRunning()) {
			pianoRollView.setSequenceTick(tick);
			panel.repaint();
		} else {
			// 移動先のテンポに設定する.
			int tempo = mmlScore.getTempoOnTick(tick);
			sequencer.setTickPosition(tick);
			sequencer.setTempoInBPM(tempo);
		}

		updatePianoRollView();
	}

	public void partChange() {
		MMLPartChangePanel panel = new MMLPartChangePanel(parentFrame, this, editor);
		panel.showDialog();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object sourceObject = e.getSource();
		if (sourceObject == tabbedPane) {
			updateSelectedTrackAndMMLPart();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		updateSelectedTrackAndMMLPart();
	}

	@Override
	public void generateActiveTrack() {
		// 単一のTrackだけを更新したいところであるが, 整合性を保つために全体をGenerateする. 
		updateActivePart(true);
	}

	@Override
	public void updateActivePart(boolean generate) {
		NanoTime time = NanoTime.start();
		if (generate) {
			try {
				mmlScore.generateAll();
			} catch (UndefinedTickException e) {
				EventQueue.invokeLater(() -> {
					String msg = AppResource.appText("fail.mml_modify") + "\n" + e.getMessage();
					JOptionPane.showMessageDialog(parentFrame, msg, AppResource.getAppTitle(), JOptionPane.WARNING_MESSAGE);
				});
				System.err.println("REVERT: " + e.getMessage());
				undoEdit.revertState();
				editor.reset();
			}
		}

		updateAllMMLPart();
		ActionDispatcher.getInstance().showTime("update", time);
	}

	private void updateAllMMLPart() {
		if (tabbedPane.getTabCount() != mmlScore.getTrackCount()) {
			resetTrackView();
			updateProgramSelect();
		}
		// すべての全パートMMLテキストを更新します. 
		int count = tabbedPane.getTabCount();
		for (int i = 0; i < count; i++) {
			MMLTrackView view = (MMLTrackView) tabbedPane.getComponentAt(i);
			view.updateTrack();
		}

		MabiDLS.getInstance().updateMidiControl(mmlScore);
		undoEdit.saveState();
		panel.repaint();
	}

	@Override
	public void updateActiveTrackProgram(int trackIndex, int program, int songProgram) {
		mmlScore.getTrack(trackIndex).setProgram(program);
		mmlScore.getTrack(trackIndex).setSongProgram(songProgram);

		updateSelectedTrackAndMMLPart();
		undoEdit.saveState();
		updateProgramSelect();
	}

	@Override
	public boolean selectTrackOnExistNote(int note, int tickOffset) {
		PaintMode mode = pianoRollView.getPaintMode();
		int activeTrackIndex = tabbedPane.getSelectedIndex();
		int trackCount = mmlScore.getTrackCount();
		for (int i = 0; i < trackCount; i++) {
			int partIndex = 0;
			int trackIndex = (i + activeTrackIndex) % trackCount;
			MMLTrack track = mmlScore.getTrack(trackIndex);
			if ( (mode == PaintMode.ALL_TRACK) || 
					( (mode == PaintMode.ACTIVE_TRACK) && (track == getSelectedTrack()))) {
				for (MMLEventList eventList : track.getMMLEventList()) {
					MMLNoteEvent noteEvent = eventList.searchOnTickOffset(tickOffset);
					if ( (noteEvent != null) && (note == noteEvent.getNote()) ) {
						tabbedPane.setSelectedIndex(trackIndex);
						MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
						view.setSelectMMLPartOfIndex(partIndex);
						updateSelectedTrackAndMMLPart();
						return true;
					}
					partIndex++;
				}
			}
		}

		return false;
	}

	public void switchTrack(boolean toNext) {
		int trackIndex = tabbedPane.getSelectedIndex();
		if (toNext) {
			if (trackIndex+1 < tabbedPane.getTabCount()) {
				trackIndex++;
			} else {
				trackIndex = 0;
			}
		} else {
			if (trackIndex-1 >= 0) {
				trackIndex--;
			} else {
				trackIndex = tabbedPane.getTabCount() - 1;
			}
		}
		tabbedPane.setSelectedIndex(trackIndex);
		updateSelectedTrackAndMMLPart();
	}

	public void switchMMLPart(boolean toNext) {
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		view.switchMMLPart(toNext);
		updateSelectedTrackAndMMLPart();
	}

	public IFileState getFileState() {
		return undoEdit;
	}

	public IEditState getEditState() {
		return editor;
	}

	public long getEditSequencePosition() {
		return pianoRollView.getSequencePosition();
	}

	public void addTicks(boolean isMeasure) {
		int tickPosition = (int) pianoRollView.getSequencePosition();
		mmlScore.addTicks(tickPosition, isMeasure);
		updateActivePart(true);
	}

	public void removeTicks(boolean isMeasure) {
		int tickPosition = (int) pianoRollView.getSequencePosition();
		mmlScore.removeTicks(tickPosition, isMeasure);
		updateActivePart(true);
	}

	// PianoRoll, Sequence系の描画を行うスレッドを開始します.
	private void startSequenceThread() {
		scheduledExecutor.scheduleWithFixedDelay(() -> {
			if (MabiDLS.getInstance().getSequencer().isRunning()) {
				EventQueue.invokeLater(() -> {
					updatePianoRollView();
				});
			}
		}, 500, 25, TimeUnit.MILLISECONDS);
	}

	@Override
	public void updatePianoRollView() {
		JViewport viewport = scrollPane.getViewport();
		Point point = viewport.getViewPosition();
		int note = pianoRollView.convertY2Note(point.y)-1;
		updatePianoRollView(note);
	}

	@Override
	public void updatePianoRollView(int note) {
		pianoRollView.updateRunningSequencePosition();
		int curPositionTick = (int) pianoRollView.getSequencePlayPosition();
		int curPositionX = pianoRollView.convertTicktoX(curPositionTick);
		var measure = new Measure(mmlScore, curPositionTick);
		int measuredPositionX = pianoRollView.convertTicktoX(measure.measuredTick());
		JViewport viewport = scrollPane.getViewport();
		Point point = viewport.getViewPosition();
		Dimension dim = viewport.getExtentSize();
		int x1 = point.x;
		int positionX = point.x;
		int x2 = pianoRollView.convertTicktoX(Measure.measuredTick(mmlScore, (int)pianoRollView.convertXtoTick(x1 + dim.width) - measure.getMeasureTick()));
		if ( (x1 > curPositionX) || (x1 + dim.width < curPositionX) ) {
			positionX = curPositionX;
		} else if (measuredPositionX > x2) {
			if (dim.width > pianoRollView.convertTicktoX(measure.getMeasureTick())) {
				if (pianoRollView.getWidth() - dim.width > x1) {
					positionX = measuredPositionX;
				}
			}
		}

		long positionY = pianoRollView.convertNote2Y(note);
		int y1 = point.y;
		int y2 = y1 + dim.height - pianoRollView.getNoteHeight() * 2;
		if (positionY < y1) {
		} else if (positionY > y2) {
			positionY -= dim.height;
			positionY += pianoRollView.getNoteHeight() * 2;
		} else {
			positionY = point.y;
		}
		point.setLocation(positionX, positionY);
		viewport.setViewPosition(point);
		scrollPane.repaint();
	}

	@Override
	public int getActivePartProgram() {
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		MMLTrack track = getSelectedTrack();
		if (view != null) {
			int part = view.getSelectedMMLPartIndex();
			if ( (part == 3) && (track.getSongProgram() >= 0) ) {
				return track.getSongProgram();
			}

			return track.getProgram();
		}

		return 0;
	}

	private void updateProgramSelect() {
		scheduledExecutor.submit(() -> MabiDLS.getInstance().loadRequiredInstruments(mmlScore));
	}

	public void showKeyboardInput() {
		editor.reset();
		repaint();
		keyboardEditor.setVisible(true);
	}

	public void setScaleColor(ScaleColor scaleColor) {
		pianoRollView.setScaleColor(scaleColor);
		pianoRollView.repaint();
	}

	@Override
	public long getSequencePosition() {
		return pianoRollView.getSequencePlayPosition();
	}

	public void mmlTextEditor() {
		new MMLTextEditor(parentFrame, this, pianoRollView).showDialog();
	}
}

/*
 * Copyright (C) 2013-2015 たんらる
 */

package fourthline.mabiicco.ui;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fourthline.mabiicco.IEditState;
import fourthline.mabiicco.IFileState;
import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.PianoRollView.PaintMode;
import fourthline.mabiicco.ui.editor.MMLEditor;
import fourthline.mabiicco.ui.editor.MMLScoreUndoEdit;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.UndefinedTickException;
import fourthline.mmlTools.core.MMLTicks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

public final class MMLSeqView implements IMMLManager, ChangeListener, ActionListener, MouseWheelListener {
	private static final int INITIAL_TRACK_COUNT = 1;

	private int trackCounter;

	private final JScrollPane scrollPane;
	private final PianoRollView pianoRollView;
	private final KeyboardView keyboardView;
	private final JTabbedPane tabbedPane;
	private final ColumnPanel columnView;

	private MMLScore mmlScore = new MMLScore();
	private final MMLScoreUndoEdit undoEdit = new MMLScoreUndoEdit(this);

	private final MMLInputPanel dialog = new MMLInputPanel(this);

	private final MMLEditor editor;

	private final JPanel panel;
	private JLabel timeView;
	private Thread timeViewUpdateThread;


	/**
	 * Create the panel.
	 */
	public MMLSeqView() {
		panel = new JPanel(false);
		panel.setLayout(new BorderLayout(0, 0));

		// Scroll View (KeyboardView, PianoRollView) - CENTER
		pianoRollView = new PianoRollView();
		keyboardView = new KeyboardView(this, pianoRollView);

		scrollPane = new JScrollPane(pianoRollView);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(pianoRollView.getNoteHeight());

		panel.add(scrollPane, BorderLayout.CENTER);
		pianoRollView.setViewportAndParent(scrollPane.getViewport(), this);

		// MMLTrackView (tab) - SOUTH
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(this);
		tabbedPane.setPreferredSize(new Dimension(0, 200));
		panel.add(tabbedPane, BorderLayout.SOUTH);

		// create mml editor
		editor = new MMLEditor(keyboardView, pianoRollView, this);
		pianoRollView.addMouseInputListener(editor);
		pianoRollView.addMouseWheelListener(this);
		columnView = new ColumnPanel(pianoRollView, this, editor);

		scrollPane.setRowHeaderView(keyboardView);
		scrollPane.setColumnHeaderView(columnView);

		initialSetView();
		initializeMMLTrack();

		startSequenceThread();
		startTimeViewUpdateThread();
	}

	public void repaint() {
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
		removeAllMMLTrack();
		trackCounter = 0;

		for (int i = 0; i < INITIAL_TRACK_COUNT; i++) {
			addMMLTrack(null);
		}

		panel.repaint();
		undoEdit.initState();
	}

	private String getNewTrackName() {
		trackCounter++;
		return "Track" + trackCounter;
	}

	/**
	 * トラックの追加。作成したトラックを選択状態にします。
	 */
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

		// エディタ更新
		updateActivePart(false);
		updateSelectedTrackAndMMLPart();
		updateProgramSelect();
	}

	private void removeAllMMLTrack() {
		mmlScore = new MMLScore();
		tabbedPane.removeAll();
	}

	/**
	 * トラックの削除
	 * 現在選択中のトラックを削除します。
	 */
	public void removeMMLTrack() {
		int index = tabbedPane.getSelectedIndex();

		mmlScore.removeTrack(index);
		resetTrackView();
		updateTrackTabIcon();

		if (mmlScore.getTrackCount() == 0) {
			addMMLTrack(null);
		} else {
			// ピアノロール更新
			pianoRollView.repaint();
			// エディタ更新
			updateSelectedTrackAndMMLPart();
		}

		undoEdit.saveState();
	}

	private void updateTrackTabIcon() {
		int len = tabbedPane.getTabCount();

		for (int i = 0; i < len; i++) {
			tabbedPane.setIconAt(i, PartButtonIconArray.getInstance(1, i));
		}
	}

	/**
	 * 全トラックにおける、指定Tick位置のテンポを取得する。
	 */
	private int getTempoInSequenceAtTick(long tick) {
		return mmlScore.getTempoOnTick(tick);
	}

	/**
	 * 再生スタート（現在のシーケンス位置を使用）
	 */
	public void startSequence() {
		new Thread(() -> {
			try {
				MabiDLS.getInstance().loadRequiredInstruments(mmlScore);
				Sequencer sequencer = MabiDLS.getInstance().getSequencer();
				Sequence sequence = MabiDLS.getInstance().createSequence(mmlScore);

				// 再生開始が先頭でない場合、そこのテンポに設定する必要がある。
				long startTick = pianoRollView.getSequencePosition();
				int tempo = getTempoInSequenceAtTick(startTick);
				System.out.printf("Sequence start: tick(%d), tempo(%d)\n", startTick, tempo);
				sequencer.setSequence(sequence);
				sequencer.setTickPosition(startTick);
				sequencer.setTempoInBPM(tempo);
				sequencer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * 新規で複数のトラックをセットする。
	 */
	public void setMMLScore(MMLScore score) {
		mmlScore = score;

		pianoRollView.repaint();
		tabbedPane.removeAll();
		undoEdit.initState();

		int trackCount = 0;
		for (MMLTrack track : score.getTrackList()) {
			String name = track.getTrackName();
			if (name == null) {
				name = "Track"+(trackCount+1);
			}

			tabbedPane.add(name, MMLTrackView.getInstance(trackCount, this, this));
			trackCount++;
		}

		initialSetView();
		pianoRollView.setSequenceTick(0);
		updateTrackTabIcon();
		updateActivePart(false);
		updateProgramSelect();
	}

	/**
	 * 現在のトラックにMMLを設定する。
	 */
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
	 */
	public MMLTrack getSelectedTrack() {
		int index = tabbedPane.getSelectedIndex();

		if (index < 0) {
			index = 0;
		}

		return mmlScore.getTrack(index);
	}

	@Override
	public MMLScore getMMLScore() {
		return mmlScore;
	}

	public PaintMode getPaintMode() {
		return pianoRollView.getPaintMode();
	}

	public void setPaintMode(PaintMode mode) {
		pianoRollView.setPaintMode(mode);
	}

	public void editTrackPropertyAction(Frame parentFrame) {
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

	public void setPianoRollHeightScaleIndex(int index) {
		pianoRollView.setNoteHeightIndex(index);
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

	public void inputClipBoardAction(Frame parentFrame) {
		dialog.showDialog(parentFrame, getNewTrackName());
	}

	public void outputClipBoardAction(Frame parentFrame) {
		MMLOutputPanel outputPanel = new MMLOutputPanel(parentFrame, mmlScore.getTrackList());
		outputPanel.showDialog();
	}

	private void updateSelectedTrackAndMMLPart() {
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		if (view != null) {
			view.updateMuteButton();
			int program = getActivePartProgram();

			editor.reset();
			pianoRollView.setPitchRange(MabiDLS.getInstance().getInstByProgram(program));

			pianoRollView.repaint();
		}
	}

	@Override
	public int getActiveTrackIndex() {
		int trackIndex = tabbedPane.getSelectedIndex();
		return trackIndex;
	}

	@Override
	public MMLEventList getActiveMMLPart() {
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		int mmlPartIndex = view.getSelectedMMLPartIndex();
		MMLTrack track = mmlScore.getTrack(getActiveTrackIndex());
		return track.getMMLEventAtIndex(mmlPartIndex);
	}

	/**
	 * 編集時の音符基準長を設定します.
	 */
	public void setEditAlign(int alignTick) {
		editor.setEditAlign(alignTick);
	}


	private int viewScaleIndex = 0;
	private final double viewScaleTable[] = { 6, 5, 4, 3, 2, 1.5, 1, 0.75, 0.5, 0.375, 0.25 };

	/**
	 * ピアノロールビューの表示を1段階拡大します.
	 */
	public void expandPianoViewWide() {
		if (viewScaleIndex+1 < viewScaleTable.length) {
			viewScaleIndex++;
		}

		pianoRollView.setWideScale(viewScaleTable[viewScaleIndex]);
		repaint();
	}

	/**
	 * ピアノロールビューの表示を1段階縮小します.
	 */
	public void reducePianoViewWide() {
		if (viewScaleIndex-1 >= 0) {
			viewScaleIndex--;
		}

		pianoRollView.setWideScale(viewScaleTable[viewScaleIndex]);
		repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int modifiers = e.getModifiers();
		int rotation = e.getWheelRotation();
		if (modifiers == InputEvent.CTRL_MASK) {
			if (rotation > 0) {
				expandPianoViewWide();
			} else {
				reducePianoViewWide();
			}
		} else {
			for (MouseWheelListener listener : scrollPane.getMouseWheelListeners()) {
				listener.mouseWheelMoved(e);
			}
		}
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
		MMLTrack track = mmlScore.getTrack(selectedTab);
		int program = track.getProgram();
		if (InstClass.getEnablePartByProgram(program)[selectedPart] == false) {
			if ( (selectedPart == 3) && (track.getSongProgram() >= 0) ) {
			} else {
				selectedPart = InstClass.getFirstPartNumberOnProgram(program);
			}
		}

		tabbedPane.setSelectedIndex(selectedTab);
		((MMLTrackView) tabbedPane.getSelectedComponent()).setSelectMMLPartOfIndex(selectedPart);
		updateTrackTabIcon();
	}

	public void undo() {
		if (undoEdit.canUndo()) {
			undoEdit.undo();
			resetTrackView();
			updateSelectedTrackAndMMLPart();
			updateActivePart(false);
		}
	}

	public void redo() {
		if (undoEdit.canRedo()) {
			undoEdit.redo();
			resetTrackView();
			updateSelectedTrackAndMMLPart();
			updateActivePart(false);
		}
	}

	public void nextStepTimeTo(boolean next) {
		try {
			int step = MMLTicks.getTick(mmlScore.getBaseOnly());
			int deltaTick = mmlScore.getTimeCountOnly() * step;
			Sequencer sequencer = MabiDLS.getInstance().getSequencer();
			long tick = pianoRollView.getSequencePlayPosition();
			if (next) {
				tick += deltaTick;
			} else {
				tick -= step;
			}
			tick -= tick % deltaTick;
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
		} catch (UndefinedTickException e) {}
	}

	public void partChange(Frame parentFrame) {
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
	public void updateActivePart(boolean generate) {
		if (generate) {
			try {
				mmlScore.generateAll();
			} catch (UndefinedTickException e) {
				System.err.println("REVERT: " + e.getMessage());
				undoEdit.revertState();
				editor.reset();
			}
		}

		updateAllMMLPart();
	}

	private void updateAllMMLPart() {
		if (tabbedPane.getComponentCount() != mmlScore.getTrackCount()) {
			resetTrackView();
			updateProgramSelect();
		}
		// すべての全パートMMLテキストを更新します. 
		int count = tabbedPane.getComponentCount();
		for (int i = 0; i < count; i++) {
			MMLTrackView view = (MMLTrackView) tabbedPane.getComponentAt(i);
			view.updateTrack();
		}

		MabiDLS.getInstance().updatePanpot(mmlScore);
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
		int trackIndex = 0;
		for (MMLTrack track : mmlScore.getTrackList()) {
			int partIndex = 0;
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
			trackIndex++;
		}

		return false;
	}

	public void setTimeView(JLabel timeView) {
		this.timeView = timeView;
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

	public void addTicks(int tick) {
		int tickPosition = (int) pianoRollView.getSequencePosition();
		mmlScore.addTicks(tickPosition, tick);
		updateActivePart(true);
	}

	public void removeTicks(int tick) {
		int tickPosition = (int) pianoRollView.getSequencePosition();
		mmlScore.removeTicks(tickPosition, tick);
		updateActivePart(true);
	}

	// TimeViewを更新するためのスレッドを開始します.
	private void startTimeViewUpdateThread() {
		if (timeViewUpdateThread != null) {
			return;
		}
		// TODO: びみょう・・・？
		timeViewUpdateThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				updateTimeView();
			}
		});
		timeViewUpdateThread.start();
	}

	private void updateTimeView() {
		long position = pianoRollView.getSequencePlayPosition();
		List<MMLTempoEvent> tempoList = mmlScore.getTempoEventList();
		long time = MMLTempoEvent.getTimeOnTickOffset(tempoList, (int)position);
		int totalTick = mmlScore.getTotalTickLength();
		long totalTime = MMLTempoEvent.getTimeOnTickOffset(tempoList, totalTick);
		int tempo = MMLTempoEvent.searchOnTick(tempoList, (int)position);

		String str = String.format("time %d:%02d.%d/%d:%02d.%d (t%d)", 
				(time/60/1000), (time/1000%60), (time/100%10),
				(totalTime/60/1000), (totalTime/1000%60), (totalTime/100%10),
				tempo);
		if (timeView != null) {
			timeView.setText(str);
		}
	}

	private Thread updateViewThread;
	// PianoRoll, Sequence系の描画を行うスレッドを開始します.
	private void startSequenceThread() {
		if (updateViewThread != null) {
			return;
		}
		updateViewThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (MabiDLS.getInstance().getSequencer().isRunning()) {
					EventQueue.invokeLater(() -> {
						updatePianoRollView();
					});
				}
			}
		});
		updateViewThread.start();
	}

	private void updatePianoRollView() {
		pianoRollView.updateRunningSequencePosition();
		int measure = pianoRollView.getMeasureWidth();
		long position = pianoRollView.getSequencePlayPosition();
		position = pianoRollView.convertTicktoX(position);
		position -= position % measure;
		JViewport viewport = scrollPane.getViewport();
		Point point = viewport.getViewPosition();
		Dimension dim = viewport.getExtentSize();
		int x1 = point.x;
		int x2 = x1 + dim.width - measure;
		if ( (position < x1) || (position > x2) ) {
			/* ビュー外にあるので、現在のポジションにあわせる */
			if (position + dim.width > pianoRollView.getWidth()) {
				position = (pianoRollView.getWidth() - dim.width);
				position -= position % measure;
			}
			point.setLocation(position, point.getY());
			viewport.setViewPosition(point);
		}
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
		new Thread(() -> {
			MabiDLS.getInstance().loadRequiredInstruments(mmlScore);
		}).start();
	}
}

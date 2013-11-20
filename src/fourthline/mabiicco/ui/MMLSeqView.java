/*
 * Copyright (C) 2013 たんらる
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

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.editor.MMLEditor;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.optimizer.MMLStringOptimizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MMLSeqView extends JPanel implements IMMLManager, ChangeListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -479890612015524747L;

	private static final int INITIAL_TRACK_COUNT = 1;

	private int trackCounter;

	private JScrollPane scrollPane;
	private PianoRollView pianoRollView;
	private KeyboardView keyboardView;
	private JTabbedPane tabbedPane;
	private ColumnPanel columnView;

	private MMLScore mmlScore = new MMLScore();

	private MMLInputPanel dialog = new MMLInputPanel(this);

	private MMLEditor editor;

	private JLabel timeView;
	private Thread timeViewUpdateThread;



	/**
	 * Create the panel.
	 */
	public MMLSeqView() {
		super(false);
		setLayout(new BorderLayout(0, 0));

		// Scroll View (KeyboardView, PianoRollView) - CENTER
		pianoRollView = new PianoRollView();
		keyboardView = new KeyboardView();

		scrollPane = new JScrollPane(pianoRollView);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(AbstractMMLView.HEIGHT);

		scrollPane.setRowHeaderView(keyboardView);
		columnView = new ColumnPanel(pianoRollView, this);
		scrollPane.setColumnHeaderView(columnView);

		add(scrollPane, BorderLayout.CENTER);
		pianoRollView.setViewportAndParent(scrollPane.getViewport(), this);


		// MMLTrackView (tab) - SOUTH
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(this);
		tabbedPane.setPreferredSize(new Dimension(0, 180));
		add(tabbedPane, BorderLayout.SOUTH);

		// create mml editor
		editor = new MMLEditor(keyboardView, pianoRollView, this);
		pianoRollView.addMouseInputListener(editor);

		initialSetView();
		initializeMMLTrack();

		startSequenceThread();
		startTimeViewUpdateThread();
	}

	private void initialSetView() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// (ピアノロール全体の高さ / 2) - （表示領域 / 2）＝真ん中の座標。
				int y = (pianoRollView.getHeight() / 2) - (scrollPane.getHeight() / 2);

				// 初期のView位置
				scrollPane.getViewport().setViewPosition(new Point(0, y));
			}
		});
	}



	public void initializeMMLTrack() {
		removeAllMMLTrack();
		trackCounter = 0;

		for (int i = 0; i < INITIAL_TRACK_COUNT; i++) {
			addMMLTrack(null);
		}

		repaint();
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
			newTrack = new MMLTrack("");
			newTrack.setTrackName( getNewTrackName() );
		}

		int trackIndex = mmlScore.addTrack(newTrack);
		if (trackIndex < 0) {
			return;
		}

		// トラックビューの追加
		tabbedPane.add(newTrack.getTrackName(), new MMLTrackView(newTrack, trackIndex, this, this));
		tabbedPane.setSelectedIndex(trackIndex);

		// ピアノロール更新
		pianoRollView.repaint();

		// エディタ更新
		updateTempoRoll();
		updateSelectedTrackAndMMLPart();
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
		tabbedPane.remove(index);

		// MMLTrackViewのチャンネルを更新する.
		for (int i = index; i < tabbedPane.getComponentCount(); i++) {
			MMLTrackView view = (MMLTrackView) (tabbedPane.getComponentAt(i));
			view.setChannel(i);
		}

		if (mmlScore.getTrackCount() == 0) {
			addMMLTrack(null);
		} else {
			// ピアノロール更新
			pianoRollView.repaint();
			// エディタ更新
			updateSelectedTrackAndMMLPart();
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
		new Thread(new Runnable() {
			public void run() {
				try {
					Sequencer sequencer = MabiDLS.getInstance().getSequencer();
					Sequence sequence = mmlScore.createSequence();

					// 再生開始が先頭でない場合、そこのテンポに設定する必要がある。
					long startTick = pianoRollView.getSequencePossition();
					int tempo = getTempoInSequenceAtTick(startTick);
					System.out.printf("Sequence start: tick(%d), tempo(%d)\n", startTick, tempo);
					sequencer.setSequence(sequence);
					sequencer.setTickPosition(startTick);
					sequencer.setTempoInBPM(tempo);
					sequencer.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
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

		int trackCount = score.getTrackCount();
		for (int i = 0; i < trackCount; i++) {
			MMLTrack track = score.getTrack(i);
			String name = track.getTrackName();
			if (name == null) {
				name = "Track"+(i+1);
			}

			tabbedPane.add(name, new MMLTrackView(track, i, this, this));
		}

		initialSetView();
		pianoRollView.setSequenceX(0);
		updateTempoRoll();
		repaint();
	}

	/**
	 * 現在のトラックにMMLを設定する。
	 */
	public void setMMLselectedTrack(MMLTrack mml) {
		int index = tabbedPane.getSelectedIndex();

		mmlScore.setTrack(index, mml);
		tabbedPane.setTitleAt(index, mml.getTrackName());

		// 表示を更新
		MMLTrackView view = (MMLTrackView)tabbedPane.getComponentAt(index);
		view.setMMLTrack(mml);
		updateSelectedTrackAndMMLPart();
		updateTempoRoll();
		repaint();
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

	public void editTrackPropertyAction() {
		MMLTrack track = getSelectedTrack();
		new TrackPropertyPanel(track).showDialog();
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
			pianoRollView.setSequenceX(0);
			repaint();
		} else {
			sequencer.setTickPosition(0);
			sequencer.setTempoInBPM(120);
		}
	}

	/**
	 * 現在のTickにシーケンスを設定する。（一時停止用）
	 */
	public void pauseTickPosition() {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		int x = pianoRollView.convertTicktoX( sequencer.getTickPosition() );
		pianoRollView.setSequenceX(x);
	}

	public void inputClipBoardAction() {
		dialog.showDialog(getNewTrackName());
	}

	public void outputClipBoardAction() {
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		String mml = view.getMMLText();

		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();
		clip.setContents(new StringSelection(mml), null);
	}

	private void updateSelectedTrackAndMMLPart() {
		int trackIndex = tabbedPane.getSelectedIndex();
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		if (view != null) {
			int channel = view.getChannel();
			keyboardView.setChannel(channel);
			int mmlPartIndex = view.getSelectedMMLPartIndex();

			// ピアノロールビューにアクティブトラックとアクティブパートを設定します.
			editor.setMMLEventList(mmlScore.getTrack(trackIndex).getMMLEventList(mmlPartIndex));
			pianoRollView.repaint();
			System.out.printf("stateChanged(): %d, %d\n", channel, mmlPartIndex);
		}
	}

	@Override
	public MMLEventList getActiveMMLPart() {
		int trackIndex = tabbedPane.getSelectedIndex();
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		int mmlPartIndex = view.getSelectedMMLPartIndex();
		return mmlScore.getTrack(trackIndex).getMMLEventList(mmlPartIndex);
	}

	/**
	 * 編集時の音符基準長を設定します.
	 */
	public void setEditAlign(int alignTick) {
		editor.setEditAlign(alignTick);
	}


	/**
	 * ピアノロールビューの表示を1段階拡大します.
	 */
	public void expandPianoViewWide() {
		double scale = pianoRollView.getWideScale();
		if ( (3.0 < scale) && (scale <= 6.0) ) {
			scale -= 3.0;
		} else if ( (1.0 < scale) && (scale <= 3.0) ) {
			scale -= 2.0;
		} else if (scale == 1.0) {
			scale = 0.5;
		}

		pianoRollView.setWideScale(scale);
	}

	/**
	 * ピアノロールビューの表示を1段階縮小します.
	 */
	public void reducePianoViewWide() {
		double scale = pianoRollView.getWideScale();
		if ( (3.0 <= scale) && (scale < 6.0) ) {
			scale += 3.0;
		} else if ( (1.0 <= scale) && (scale < 3.0) ) {
			scale += 2.0;
		} else if (scale == 0.5) {
			scale = 1.0;
		}

		pianoRollView.setWideScale(scale);
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
	public void updateActivePart() {
		int trackIndex = tabbedPane.getSelectedIndex();
		MMLTrackView view = (MMLTrackView) tabbedPane.getSelectedComponent();
		int mmlPartIndex = view.getSelectedMMLPartIndex();
		MMLTrack track = mmlScore.getTrack(trackIndex);

		// TODO: 他のパートのtickLength増加でパート1に関しては、tickLength追従が必要.
		if (mmlPartIndex > 0) {
			MMLEventList eventList = track.getMMLEventList(mmlPartIndex);
			String mml = eventList.toMMLString();
			String optimizedMML = new MMLStringOptimizer(mml).toString();
			view.setPartMMLString(mmlPartIndex, optimizedMML);
		}

		updateTempoRoll();
	}

	@Override
	public void updateTempoRoll() {
		// すべての全パートMMLテキストを更新します. 
		int count = tabbedPane.getComponentCount();
		for (int i = 0; i < count; i++) {
			MMLTrackView view = (MMLTrackView) tabbedPane.getComponentAt(i);
			MMLTrack track = mmlScore.getTrack(i);

			String mmlStrings[] = track.getMMLStrings();
			view.setPartMMLString(0, mmlStrings[0]);
			view.setPartMMLString(1, mmlStrings[1]);
			view.setPartMMLString(2, mmlStrings[2]);
		}

		pianoRollView.repaint();
	}

	@Override
	public void updateActiveTrackProgram(int program) {
		getSelectedTrack().setProgram(program);
	}

	public void setTimeView(JLabel timeView) {
		this.timeView = timeView;
	}

	// TimeViewを更新するためのスレッドを開始します.
	private void startTimeViewUpdateThread() {
		if (timeViewUpdateThread != null) {
			return;
		}
		// TODO: びみょう・・・？
		timeViewUpdateThread = new Thread(
				new Runnable() {
					@Override
					public void run() {
						while (true) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							updateTimeView();
						}
					}
				});
		timeViewUpdateThread.start();
	}

	private void updateTimeView() {
		long position = pianoRollView.getSequencePlayPosition();
		List<MMLTempoEvent> tempoList = mmlScore.getTempoEventList();
		double time = MMLTempoEvent.getTimeOnTickOffset(tempoList, (int)position);
		int totalTick = mmlScore.getTotalTickLength();
		double totalTime = MMLTempoEvent.getTimeOnTickOffset(tempoList, totalTick);
		int tempo = MMLTempoEvent.searchOnTick(tempoList, (int)position);

		String str = String.format("time %d:%02d/%d:%02d (t%d)", 
				(int)(time/60), (int)(time%60),
				(int)(totalTime/60), (int)(totalTime%60),
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
		updateViewThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					updatePianoRollView();
				}
			}
		});
		updateViewThread.start();
	}

	// TODO: Play方式をリアルタイム式にすれば、これを統合可能.
	private void updatePianoRollView() {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		if (sequencer.isRunning()) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					long position = pianoRollView.getSequencePlayPosition();
					position = pianoRollView.convertTicktoX(position);
					JViewport viewport = scrollPane.getViewport();
					Point point = viewport.getViewPosition();
					Dimension dim = viewport.getExtentSize();
					double x1 = point.getX();
					double x2 = x1 + dim.getWidth();
					if ( (position < x1) || (position > x2) ) {
						/* ビュー外にあるので、現在のポジションにあわせる */
						point.setLocation(position, point.getY());
						viewport.setViewPosition(point);
					}
					scrollPane.repaint();
				}
			});
		}
	}
}

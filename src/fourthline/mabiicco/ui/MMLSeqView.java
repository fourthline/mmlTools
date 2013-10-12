/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fourthline.mmlTools.parser.MMLTrack;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

public class MMLSeqView extends JPanel implements INotifyMMLTrackProperty, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -479890612015524747L;

	private JScrollPane scrollPane;
	private PianoRollView pianoRollView;
	private KeyboardView keyboardView;
	private JTabbedPane tabbedPane;


	/**
	 * Create the panel.
	 */
	public MMLSeqView() {
		setLayout(new BorderLayout(0, 0));

		// Scroll View (KeyboardView, PianoRollView) - CENTER
		pianoRollView = new PianoRollView();
		keyboardView = new KeyboardView();

		scrollPane = new JScrollPane(pianoRollView);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		scrollPane.setRowHeaderView(keyboardView);
		scrollPane.setColumnHeaderView(pianoRollView.getRulerPanel());

		add(scrollPane, BorderLayout.CENTER);
		pianoRollView.setViewportAndParent(scrollPane.getViewport(), this);


		// MMLTrackView (tab) - SOUTH
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(this);

		tabbedPane.setPreferredSize(new Dimension(0, 180));
		add(tabbedPane, BorderLayout.SOUTH);

		initialSetView();
		initializeMMLTrack();
	}
	
	private void initialSetView() {
		// TODO: 初期のView位置
		scrollPane.getViewport().setViewPosition(new Point(0, 250));
	}

	/**
	 * Tabによるトラック変更のイベント処理
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		MMLTrack selectedTrack = getSelectedTrack();

		pianoRollView.setMMLTrack(selectedTrack);
		keyboardView.setMMLTrack(selectedTrack);

		repaint();
	}



	public void initializeMMLTrack() {
		for (int i = 0; i < 5; i++) {
			String name = "Track"+(i+1);
			addMMLTrack(name);
		}
	}


	/**
	 * トラックの追加
	 */
	public void addMMLTrack(String title) {
		addMMLTrack(title, "");
	}


	/**
	 * トラックの追加, MML書式
	 */
	public void addMMLTrack(String title, String mml) {
		MMLTrack track = new MMLTrack(mml);
		track.setName(title);
		// トラックの追加
		tabbedPane.add(title, new MMLTrackView(track));
	}


	/**
	 * トラックの削除
	 * 現在選択中のトラックを削除します。
	 */
	public void removeMMLTrack() {
		int index = tabbedPane.getSelectedIndex();
		tabbedPane.remove(index);
	}


	/**
	 * MIDIシーケンスを作成します。
	 * @throws InvalidMidiDataException 
	 */
	public Sequence createSequence() throws InvalidMidiDataException {
		Sequence sequence = new Sequence(Sequence.PPQ, 96);

		int count = tabbedPane.getComponentCount();
		for (int i = 0; i < count; i++) {
			MMLTrackView view = (MMLTrackView)(tabbedPane.getComponentAt(i));
			MMLTrack mmlTrack = view.getMMLTrack();
			mmlTrack.convertMidiTrack(sequence.createTrack(), i);
		}

		return sequence;
	}


	/**
	 * 新規で複数のトラックをセットする。
	 */
	public void setMMLTracks(MMLTrack track[]) {
		tabbedPane.removeAll();
		pianoRollView.setMMLTrack(track);

		for (int i = 0; i < track.length; i++) {
			String name = track[i].getName();
			if (name == null) {
				name = "Track"+(i+1);
			}

			tabbedPane.add(name, new MMLTrackView(track[i]));
		}

		initialSetView();
	}

	/**
	 * 現在選択中のトラックを取得する。
	 */
	public MMLTrack getSelectedTrack() {
		int index = tabbedPane.getSelectedIndex();

		if (index < 0) {
			index = 0;
		}

		MMLTrackView view = (MMLTrackView)(tabbedPane.getComponentAt(index));
		MMLTrack mmlTrack = view.getMMLTrack();

		return mmlTrack;
	}

	@Override
	public void setTrackProperty(MMLTrack track) {
		tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), track.getName());
	}
}

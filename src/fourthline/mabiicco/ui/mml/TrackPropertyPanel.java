/*
 * Copyright (C) 2013-2022 たんらる
 */

package fourthline.mabiicco.ui.mml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.editor.NumberSpinner;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLTicks;
import fourthline.mmlTools.core.UndefinedTickException;


public final class TrackPropertyPanel extends JPanel {
	private static final long serialVersionUID = 7599129671956571455L;
	private JTextField trackNameField;
	private JSlider panpotSlider;

	// 楽器部のオプション
	private final MMLOutputOptions instOption;

	// 歌部のオプション
	private final MMLOutputOptions songOption;

	private final IMMLManager mmlManager;

	private final MMLTrack track;
	private final MMLTrack sandTrack;

	/**
	 * Create the dialog.
	 */
	public TrackPropertyPanel(MMLTrack track, IMMLManager mmlManager) {
		super();
		int commonStartOffset = track.getCommonStartOffset();
		this.track = track;
		this.sandTrack = track.clone();
		this.mmlManager = mmlManager;
		this.instOption = new MMLOutputOptions(0, t -> this.sandTrack.setStartDelta(t - commonStartOffset), t -> this.sandTrack.setAttackDelayCorrect(t));
		this.songOption = new MMLOutputOptions(3, t -> this.sandTrack.setStartSongDelta(t - commonStartOffset), t -> this.sandTrack.setAttackSongDelayCorrect(t));
		initialize();
	}

	private void initialize() {
		setLayout(null);

		// トラック名
		add(newJLabel(AppResource.appText("track_property.trackname"), 20, 23, 100, 14));

		trackNameField = new JTextField();
		trackNameField.setBounds(120, 20, 200, 19);
		trackNameField.setEditable(true);
		add(trackNameField);
		trackNameField.setColumns(10);

		// パンポット
		panpotSlider = new JSlider();
		panpotSlider.setSnapToTicks(true);
		panpotSlider.setPaintTicks(true);
		panpotSlider.setValue(64);
		panpotSlider.setMinorTickSpacing(16);
		panpotSlider.setMajorTickSpacing(16);
		panpotSlider.setMaximum(128);
		panpotSlider.setBounds(120, 60, 200, 23);
		add(panpotSlider);

		JLabel panpotL = new JLabel("L64");
		panpotL.setBounds(120, 80, 30, 23);
		add(panpotL);
		JLabel panpotC = new JLabel("0");
		panpotC.setBounds(217, 80, 30, 23);
		add(panpotC);
		JLabel panpotR = new JLabel("R64");
		panpotR.setBounds(302, 80, 30, 23);
		add(panpotR);
		add(newJLabel(AppResource.appText("track_property.panpot"), 20, 60, 100, 14));

		// MML出力オプション（楽器部）
		add(instOption.createMMLOptionPanel(AppResource.appText("track_propert.mmlOptions1"), 5, 120));
		// MML出力オプション（歌部）
		add(songOption.createMMLOptionPanel(AppResource.appText("track_propert.mmlOptions2"), 5, 210));

		// 初期値設定
		int commonStartOffset = track.getCommonStartOffset();
		trackNameField.setText(track.getTrackName());
		panpotSlider.setValue(track.getPanpot());
		instOption.setValue(track.getStartDelta() + commonStartOffset, track.getAttackDelayCorrect());
		songOption.setValue(track.getStartSongDelta() + commonStartOffset, track.getAttackSongDelayCorrect());
	}

	private JLabel newJLabel(String text, int x, int y, int width, int height) {
		var v = new JLabel(text);
		v.setBounds(x, y, width, height);
		return v;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 300);
	}

	private void applyProperty() {
		int commonStartOffset = track.getCommonStartOffset();
		track.setTrackName( trackNameField.getText() );
		track.setPanpot( panpotSlider.getValue() );
		try {
			track.setStartDelta( instOption.getStartOffset() - commonStartOffset);
			track.setStartSongDelta( songOption.getStartOffset() - commonStartOffset);
		}  catch (IllegalArgumentException e) {
			// エラーメッセージの表示.
			JOptionPane.showMessageDialog(this, AppResource.appText("error.startOffset"), AppResource.getAppTitle(), JOptionPane.WARNING_MESSAGE);
			return;
		}
		track.setAttackDelayCorrect( instOption.getAttackDelayCorrect() );
		track.setAttackSongDelayCorrect( songOption.getAttackDelayCorrect() );
		mmlManager.updateActivePart(true);
	}

	public List<String> getLabelText() {
		List<String> list = new ArrayList<>();
		List.of(instOption, songOption).forEach(t -> {
			list.add(t.startOffsetText.getText());
			list.add(t.attackDelayCorrectText.getText());
		});
		return list;
	}

	public void showDialog(Frame parentFrame) {
		int status = JOptionPane.showConfirmDialog(parentFrame, 
				this,
				AppResource.appText("track_property"), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (status == JOptionPane.OK_OPTION) {
			applyProperty();
		}
	}

	/**
	 * MML出力オプション要素
	 */
	private class MMLOutputOptions {
		private final JSpinner startOffsetSpinner = NumberSpinner.createSpinner(0, 0, 96*4096, 1);
		private final JLabel startOffsetText = new JLabel();
		private final JSpinner attackDelayCorrectSpinner = NumberSpinner.createSpinner(0, -48, 48, 1);
		private final JLabel attackDelayCorrectText = new JLabel();
		private final int partIndex;

		private final Consumer<Integer> ss1;
		private final Consumer<Integer> ss2;

		private MMLOutputOptions(int partIndex, Consumer<Integer> ss1, Consumer<Integer> ss2) {
			this.partIndex = partIndex;
			this.ss1 = ss1;
			this.ss2 = ss2;
		}

		/**
		 * MMLオプション系のパネルを生成する
		 * @param title
		 * @param x
		 * @param y
		 * @return
		 */
		private JPanel createMMLOptionPanel(String title, int x, int y) {
			String deltaTitle = AppResource.appText("track_property.startDelta");
			String delayCorrectTitle = AppResource.appText("track_property.attackDelayCorrect");
			JPanel mmlOptionsPanel = new JPanel();
			mmlOptionsPanel.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
			mmlOptionsPanel.setBounds(x, y, 340, 80);
			mmlOptionsPanel.setLayout(null);
			add(mmlOptionsPanel);
			// startDelta
			mmlOptionsPanel.add(newJLabel(deltaTitle, 20, 20, 170, 14));
			startOffsetSpinner.setBounds(160, 20, 100, 20);
			mmlOptionsPanel.add(startOffsetSpinner);
			startOffsetText.setBounds(270, 23, 120, 14);
			mmlOptionsPanel.add(startOffsetText);
			startOffsetSpinner.addChangeListener(e -> updateStartOffsetText());
			// attack delay correct
			mmlOptionsPanel.add(newJLabel(delayCorrectTitle, 20, 50, 140, 14));
			attackDelayCorrectSpinner.setBounds(160, 50, 50, 20);
			mmlOptionsPanel.add(attackDelayCorrectSpinner);
			attackDelayCorrectText.setBounds(220, 53, 120, 14);
			mmlOptionsPanel.add(attackDelayCorrectText);
			attackDelayCorrectSpinner.addChangeListener(e -> updateDelayTickText());

			return mmlOptionsPanel;
		}

		private void setValue(int startOffset, int attackDelayCorrect) {
			startOffsetSpinner.setValue(startOffset);
			attackDelayCorrectSpinner.setValue(attackDelayCorrect);
			updateLabelAll();
		}

		private int getStartOffset() {
			return (Integer) startOffsetSpinner.getValue();
		}

		private int getAttackDelayCorrect() {
			return (Integer) attackDelayCorrectSpinner.getValue();
		}

		private void updateLabelAll() {
			updateStartOffsetText();
			updateDelayTickText();
		}

		private void updateStartOffsetText() {
			updateLabelStartOffset(startOffsetText, (Integer) startOffsetSpinner.getValue());
		}

		/**
		 * スタートOffsetの表記更新
		 * @param o
		 * @param tick
		 */
		private void updateLabelStartOffset(JLabel o, int tick) {
			var score = mmlManager.getMMLScore();
			String s = score.getBarTextTick(tick);
			o.setText(s);
			ss1.accept(tick);
			try {
				sandTrack.generate();
				o.setForeground(Color.BLACK);
			} catch (UndefinedTickException e) {
				o.setForeground(Color.RED);
			}
		}

		private void updateDelayTickText() {
			updateLabelTickToMML(attackDelayCorrectText, (Integer) attackDelayCorrectSpinner.getValue(), partIndex);
		}

		/**
		 * 遅延補正の表記更新
		 * @param o
		 * @param tick
		 * @param partIndex
		 */
		private void updateLabelTickToMML(JLabel o, int tick, int partIndex) {
			int startOffset = track.getStartOffset(partIndex);
			var tempoList = track.getGlobalTempoList();
			long d1 = MMLTempoEvent.getTimeOnTickOffset(tempoList, startOffset);
			long d2 = MMLTempoEvent.getTimeOnTickOffset(tempoList, startOffset+Math.abs(tick));
			long deltaTime = d2 - d1;
			String s = "";
			if (tick != 0) {
				try {
					s = new MMLTicks("L", Math.abs(tick)).toMMLText();
					if (tick < 0) {
						s = "-" + s;
					}
					s = "=" + s;
				} catch (UndefinedTickException e) {
					s = "=N/A";
				}
			}
			o.setText((tick < 0 ? "-" : "") + deltaTime + "ms" + s);
			ss2.accept(tick);
			try {
				sandTrack.generate();
				o.setForeground(Color.BLACK);
			} catch (UndefinedTickException e) {
				o.setForeground(Color.RED);
			}
		}
	}
}

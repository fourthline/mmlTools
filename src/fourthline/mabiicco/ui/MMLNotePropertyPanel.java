/*
 * Copyright (C) 2013-2015 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;

import fourthline.mabiicco.AppResource;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.core.TuningBase;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


/**
 * ノートプロパティを編集するためのダイアログ表示で用いるPanelです.
 *
 * onlySelectedNoteOption
 *   TRUE： 選択した音符のみを変更
 * 音量コマンド（onlySelectedNoteOptionがFALSE時のみ）
 *   FALSE：  一つ前の音符と同じ音量に後続の音符も変更する = 音量コマンド削除
 *   TRUE:    後続の音符も変更（音量コマンドを挿入する）
 */
public final class MMLNotePropertyPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 646262293010195918L;

	private JSpinner velocityValueField;
	private JSpinner velocityValueField2;
	private JCheckBox velocityCheckBox; // 音量コマンド
	private JCheckBox tuningNoteCheckBox;
	private JCheckBox onlySelectedNoteOption;
	private JCheckBox incDecrVelocityEditOption;
	private JComboBox<TuningBase> tuningBaseList;
	private MMLNoteEvent noteEvent[];
	private MMLEventList eventList;

	// 調律属性は指定されたノートの調律属性がすべて同じ場合に編集可能
	private boolean enableTuningEdit;

	public void showDialog() {
		int status = JOptionPane.showConfirmDialog(null, 
				this,
				AppResource.appText("note.properties"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (status == JOptionPane.OK_OPTION) {
			applyProperty();
		}
	}

	/**
	 * Create the panel.
	 */
	public MMLNotePropertyPanel() {
		this(null, null);
	}

	/**
	 * Create the panel.
	 */
	public MMLNotePropertyPanel(MMLNoteEvent noteEvent[], MMLEventList eventList) {
		super();
		setLayout(null);

		velocityValueField = new JSpinner(new SpinnerNumberModel(8, 0, MMLNoteEvent.MAX_VOL, 1));
		velocityValueField.setBounds(240, 20, 50, 19);
		add(velocityValueField);

		velocityCheckBox = new JCheckBox(AppResource.appText("note.properties.velocity"));
		velocityCheckBox.setBounds(40, 20, 150, 21);
		velocityCheckBox.addActionListener(this);
		add(velocityCheckBox);

		onlySelectedNoteOption = new JCheckBox(AppResource.appText("note.properties.onlySelectedNoteOption"));
		onlySelectedNoteOption.setBounds(60, 50, 200, 21);
		onlySelectedNoteOption.addActionListener(this);
		add(onlySelectedNoteOption);

		incDecrVelocityEditOption = new JCheckBox(AppResource.appText("note.properties.incdecr"));
		incDecrVelocityEditOption.setBounds(60, 80, 180, 21);
		incDecrVelocityEditOption.addActionListener(this);
		add(incDecrVelocityEditOption);

		velocityValueField2 = new JSpinner(new SpinnerListModel(createIncDecrVelocityValues()));
		velocityValueField2.getModel().setValue("0");
		velocityValueField2.setEditor(new IncDecrVelocityEditor(velocityValueField2));
		velocityValueField2.setBounds(240, 80, 50, 19);
		add(velocityValueField2);

		tuningNoteCheckBox = new JCheckBox(AppResource.appText("note.properties.tuning"));
		tuningNoteCheckBox.setBounds(40, 110, 180, 21);
		tuningNoteCheckBox.addActionListener(this);
		add(tuningNoteCheckBox);

		tuningBaseList = new JComboBox<>(TuningBase.values());
		tuningBaseList.setBounds(240, 110, 50, 21);
		add(tuningBaseList);

		this.noteEvent = noteEvent;
		this.eventList = eventList;
		setNoteEvent();
		updateView();
	}

	private class IncDecrVelocityEditor extends JSpinner.ListEditor {
		private static final long serialVersionUID = 5700739082350714367L;

		public IncDecrVelocityEditor(JSpinner spinner) {
			super(spinner);
			getTextField().setHorizontalAlignment(JTextField.RIGHT);
		}
	}

	private List<?> createIncDecrVelocityValues() {
		ArrayList<String> list = new ArrayList<>();
		for (int i = -MMLNoteEvent.MAX_VOL; i <= MMLNoteEvent.MAX_VOL; i++) {
			if (i <= 0) {
				list.add(""+i);
			} else {
				list.add("+"+i);
			}
		}

		return list;
	}

	private void setNoteEvent() {
		if (noteEvent == null) {
			return;
		}

		// 調律音
		TuningBase first = noteEvent[0].getTuningBase();
		if (first != null) {
			tuningNoteCheckBox.setSelected(true);
			tuningBaseList.setSelectedItem(first);
		} else {
			tuningNoteCheckBox.setSelected(false);
		}
		enableTuningEdit = true;
		for (MMLNoteEvent note : noteEvent) {
			if (!Objects.equals(first, note.getTuningBase())) {
				enableTuningEdit = false;
				break;
			}
		}

		MMLNoteEvent prevNote = eventList.searchPrevNoteOnTickOffset(noteEvent[0].getTickOffset());
		int velocity = noteEvent[0].getVelocity();
		velocityValueField.setValue(velocity);
		if (prevNote.getVelocity() != velocity) {
			velocityCheckBox.setSelected(true);
		} else {
			velocityCheckBox.setSelected(false);
		}
	}

	/**
	 * パネルの情報をノートに反映します.
	 */
	public void applyProperty() {
		int incDecrValue = Integer.parseInt((String) velocityValueField2.getValue());
		for (MMLNoteEvent targetNote : noteEvent) {
			Integer value = (Integer) velocityValueField.getValue();
			if (onlySelectedNoteOption.isSelected()) {
				if (!incDecrVelocityEditOption.isSelected()) {
					// 選択されたノートのみの音量を変更
					targetNote.setVelocity(value.intValue());
				} else {
					// 増減量による音量変更
					targetNote.setVelocity( targetNote.getVelocity() + incDecrValue );
				}
			} else {
				// 音量コマンドにより後続のノートも更新する
				if (velocityCheckBox.isSelected()) {
					eventList.setVelocityCommand(targetNote, value.intValue());
				} else {
					eventList.unsetVelocityCommand(targetNote);
				}
			}

			// 調律設定が有効な場合は、調律属性も更新する.
			if (tuningNoteCheckBox.isEnabled()) {
				TuningBase base = tuningBaseList.getItemAt(tuningBaseList.getSelectedIndex());
				targetNote.setTuningNote( tuningNoteCheckBox.isSelected() ? base : null );
			}
		}
	}

	private void updateView() {
		boolean velocityCommand = velocityCheckBox.isSelected();
		boolean onlySelect = onlySelectedNoteOption.isSelected();
		boolean incrDecrValue = incDecrVelocityEditOption.isSelected();

		// 音量コマンドチェックBox
		velocityCheckBox.setEnabled( !onlySelect );

		// 音量入力欄
		velocityValueField.setEnabled( (velocityCommand && !onlySelect) || (!incrDecrValue && (velocityCommand || onlySelect)) );

		// 増減入力チェックBox		
		incDecrVelocityEditOption.setEnabled( onlySelect );

		// 増減入力欄
		velocityValueField2.setEnabled( onlySelect && incrDecrValue );

		tuningNoteCheckBox.setEnabled(enableTuningEdit);
		tuningBaseList.setEnabled( (enableTuningEdit && tuningNoteCheckBox.isSelected()) );
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 150);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateView();
	}
}

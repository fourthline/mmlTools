/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;
import java.util.function.IntConsumer;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;

import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.IPlayNote;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.PianoRollView;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.parser.MMLEventParser;

import static fourthline.mabiicco.AppResource.appText;


/**
 * キーボード入力による編集.
 */
public final class KeyboardEditor implements KeyListener, Receiver {

	private final JDialog dialog;

	private final IMMLManager mmlManager;
	private final IPlayNote player;
	private final IEditAlign editAlign;
	private final PianoRollView pianoRollView;
	private final int initOct = 4;
	private final int minOct = 0;
	private final int maxOct = 8;

	private MidiDevice midiDevice = null;

	private MMLNoteEvent editNote = null;
	private final JPanel panel = new JPanel(new BorderLayout());
	private final JComboBox<MidiDevice.Info> midiList = new JComboBox<>();
	private final JSpinner velocityValueField = NumberSpinner.createSpinner(MMLNoteEvent.INIT_VOL, 0, MMLNoteEvent.MAX_VOL, 1);
	private final JSpinner octaveValueField = NumberSpinner.createSpinner(initOct, minOct, maxOct, 1);
	private IntConsumer noteAlignChanger;

	public KeyboardEditor(Frame parentFrame, IMMLManager mmlManager, IPlayNote player, IEditAlign editAlign, PianoRollView pianoRollView) {
		this.mmlManager = mmlManager;
		this.player = player;
		this.editAlign = editAlign;
		this.pianoRollView = pianoRollView;
		dialog = new JDialog(parentFrame, appText("edit.keyboard.input"), true);
		initializePanel();
	}

	public void setNoteAlignChanger(IntConsumer noteAlignChanger) {
		this.noteAlignChanger = noteAlignChanger;
	}

	private JPanel createLPanel(Component c) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(c, BorderLayout.WEST);
		return panel;
	}

	private void initialSpinnerProperty(JSpinner spinner) {
		JTextField field = ((JSpinner.NumberEditor) spinner.getEditor()).getTextField();
		spinner.setFocusable(false);
		field.setEditable(false);
		field.setFocusable(false);
	}

	private void initializePanel() {
		JPanel panel1 = new JPanel();
		panel1.add(new JLabel(appText("edit.velocity")));
		initialSpinnerProperty(velocityValueField);
		panel1.add(velocityValueField);

		JPanel panel2 = new JPanel();
		panel2.add(new JLabel(appText("edit.octave")));
		initialSpinnerProperty(octaveValueField);
		panel2.add(octaveValueField);

		JPanel midiPanel = new JPanel();
		midiPanel.add(new JLabel("Midi Device: "));
		midiList.setFocusable(false);
		midiList.addActionListener(t -> {
			synchronized (this) {
				if (midiDevice != null) {
					midiDevice.close();
				}
				MidiDevice.Info info = midiList.getItemAt(midiList.getSelectedIndex());
				if (info == null) return;
				System.out.println("midi select > \""+info.getName()+"\"");
				try {
					MidiDevice device = MidiSystem.getMidiDevice(info);
					if (!device.isOpen()) {
						device.open();
						Transmitter transmitter = MidiSystem.getMidiDevice(info).getTransmitter();
						transmitter.setReceiver(this);
						midiDevice = device;
					}
					MabiIccoProperties.getInstance().midiInputDevice.set(info.getName());
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
			}
		});
		midiPanel.add(midiList);

		JPanel nPanel = new JPanel();
		nPanel.setLayout(new BoxLayout(nPanel, BoxLayout.Y_AXIS));
		nPanel.add(createLPanel(new JLabel(appText("edit.keyboard.input.description"))));
		nPanel.add(createLPanel(panel1));
		nPanel.add(createLPanel(panel2));
		nPanel.add(createLPanel(midiPanel));

		panel.add(nPanel, BorderLayout.NORTH);

		dialog.addKeyListener(this);
		dialog.getContentPane().add(panel);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);
	}

	public void setVisible(boolean b) {
		String initialMidiName = MabiIccoProperties.getInstance().midiInputDevice.get();
		midiList.removeAllItems();
		MabiDLS.getInstance().getMidiInDevice().forEach(t -> midiList.addItem(t));

		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();
		MMLNoteEvent prevNote = activePart.searchPrevNoteOnTickOffset(tickOffset);
		if (prevNote == null) {
			velocityValueField.setValue(MMLNoteEvent.INIT_VOL);
		} else {
			velocityValueField.setValue(prevNote.getVelocity());
		}

		// 設定値にもっているMIDIデバイスを選択する.
		for (int i = 0; i < midiList.getItemCount(); i++) {
			MidiDevice.Info info = midiList.getItemAt(i);
			System.out.println("midi search > \""+initialMidiName+"\" \""+info.getName()+"\"");
			if (info.getName().equals(initialMidiName)) {
				midiList.setSelectedIndex(i);
				break;
			}
		}

		dialog.pack();
		dialog.setVisible(b);
	}



	private void addNote(char code) {
		int octave = ((Integer) octaveValueField.getValue()).intValue();
		int note = MMLEventParser.firstNoteNumber("o"+octave+code);
		addNote(note);
	}

	private void addNote(int note) {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		int velocity = ((Integer) velocityValueField.getValue()).intValue();
		int nextTick = tickOffset + editAlign.getEditAlign();
		nextTick -= nextTick % editAlign.getEditAlign();
		MMLNoteEvent noteEvent = new MMLNoteEvent(note, nextTick-tickOffset, tickOffset, velocity);
		activePart.addMMLNoteEvent(noteEvent);
		player.playNote(note, velocity);
		pianoRollView.setSequenceTick(nextTick);
		mmlManager.updateActivePart(true);
		mmlManager.updatePianoRollView(note);
		this.editNote = noteEvent;
	}

	private void addRest() {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		MMLNoteEvent noteEvent = activePart.searchOnTickOffset(tickOffset);
		activePart.deleteMMLEvent(noteEvent);
		int nextTick = tickOffset + editAlign.getEditAlign();
		nextTick -= nextTick % editAlign.getEditAlign();
		pianoRollView.setSequenceTick(nextTick);
		mmlManager.updateActivePart(true);
		mmlManager.updatePianoRollView();
		this.editNote = null;
	}

	private void backDelete() {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		int nextTick = tickOffset - editAlign.getEditAlign();
		nextTick -= nextTick % editAlign.getEditAlign();
		MMLNoteEvent deleteEvent = activePart.searchOnTickOffset(nextTick);
		activePart.deleteMMLEvent(deleteEvent);
		pianoRollView.setSequenceTick(nextTick);
		mmlManager.updateActivePart(true);

		MMLNoteEvent prevNote = activePart.searchPrevNoteOnTickOffset(nextTick);
		if (prevNote != null) {
			mmlManager.updatePianoRollView(prevNote.getNote());
		} else {
			mmlManager.updatePianoRollView();
		}
		this.editNote = null;
	}

	private void octaveChange(char code) {
		SpinnerModel model = octaveValueField.getModel();
		try {
			if (code == '<') {
				octaveValueField.setValue( model.getPreviousValue() );
			} else if (code == '>') {
				octaveValueField.setValue( model.getNextValue() );
			}
		} catch (IllegalArgumentException e) {}
		this.editNote = null;
	}

	private void addSharpFlat(char code) {
		if (editNote == null) {
			return;
		}
		if ( (code == '+') || (code == '#') ) {
			editNote.setNote(editNote.getNote()+1);
			player.playNote(editNote.getNote(), editNote.getVelocity());
			mmlManager.updateActivePart(true);
		} else if (code == '-') {
			editNote.setNote(editNote.getNote()-1);
			player.playNote(editNote.getNote(), editNote.getVelocity());
			mmlManager.updateActivePart(true);
		}
		mmlManager.updatePianoRollView(editNote.getNote());
		this.editNote = null;
	}

	private void addEditTick() {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();

		MMLNoteEvent noteEvent = activePart.searchOnTickOffset(tickOffset-1);
		if (noteEvent != null) {
			noteEvent = noteEvent.clone();
			int nextTick = tickOffset + editAlign.getEditAlign();
			nextTick -= nextTick % editAlign.getEditAlign();
			noteEvent.setTick(nextTick - noteEvent.getTickOffset());
			activePart.addMMLNoteEvent(noteEvent);
			pianoRollView.setSequenceTick(nextTick);
			mmlManager.updateActivePart(true);
			mmlManager.updatePianoRollView(noteEvent.getNote());
			this.editNote = noteEvent;
		}
	}

	private void cursorMove(boolean forward) {
		int tickOffset = (int) pianoRollView.getSequencePosition();
		MMLEventList activePart = mmlManager.getActiveMMLPart();
		int nextTick = tickOffset + (forward?editAlign.getEditAlign():-editAlign.getEditAlign());
		nextTick -= nextTick % editAlign.getEditAlign();
		pianoRollView.setSequenceTick(nextTick);

		MMLNoteEvent prevNote = activePart.searchPrevNoteOnTickOffset(nextTick);
		if (prevNote != null) {
			mmlManager.updatePianoRollView(prevNote.getNote());
		} else {
			mmlManager.updatePianoRollView();
		}
		this.editNote = null;
	}

	private void velocityChange(boolean up) {
		SpinnerModel model = velocityValueField.getModel();
		try {
			velocityValueField.setValue( up ? model.getNextValue() : model.getPreviousValue() );
		} catch (IllegalArgumentException e) {}
	}

	private void changeNoteAlign(char code) {
		char select[] = {
				'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
		};
		for (int i = 0; i < select.length; i++) {
			if (select[i] == code) {
				noteAlignChanger.accept(i);
			}
		}
	}

	private void pressAction(char code) {
		if ( (code >= 'a') && (code <= 'g') || (code >= 'A') && (code <= 'G') ) {
			addNote(code);
		} else if ( (code == 'r') || (code == 'R') ) {
			addRest();
		} else if (code == KeyEvent.VK_BACK_SPACE) {
			backDelete();
		} else if ( (code == '<') || (code == '>') ) {
			octaveChange(code);
		} else if ( (code == '+') || (code == '#') || (code == '-') ) {
			addSharpFlat(code);
		} else if (code == ' ') {
			addEditTick();
		} else if (code == KeyEvent.VK_ESCAPE) {
			dialog.setVisible(false);
		} else if (code == KeyEvent.VK_LEFT) {
			cursorMove(false);
		} else if (code == KeyEvent.VK_RIGHT) {
			cursorMove(true);
		} else if (code == KeyEvent.VK_DOWN) {
			velocityChange(false);
		} else if (code == KeyEvent.VK_UP) {
			velocityChange(true);
		} else {
			changeNoteAlign(code);
		}
	}

	private void releaseAction() {
		player.offNote();
	}

	private Optional<Character> inputCode = Optional.empty();
	@Override
	public void keyTyped(KeyEvent e) {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		synchronized (this) {
			if (sequencer.isRunning()) {
				inputCode = Optional.empty();
				return;
			}
			if (!inputCode.isPresent() || (inputCode.get() != e.getKeyChar()) ) {
				char code = e.getKeyChar();
				pressAction(code);
				inputCode = Optional.of(code);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if ( (code == KeyEvent.VK_LEFT) || (code == KeyEvent.VK_RIGHT)
				|| (code == KeyEvent.VK_UP) || (code == KeyEvent.VK_DOWN)) {
			pressAction((char)code);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		synchronized (this) {
			if (inputCode.isPresent() && (inputCode.get() == e.getKeyChar())) {
				releaseAction();
				inputCode = Optional.empty();
			}
		}
	}

	private int midiNote = Integer.MIN_VALUE;
	private synchronized void shortMessageAction(ShortMessage msg) {
		int command = msg.getCommand();
		int data1 = msg.getData1();
		int data2 = msg.getData2();

		switch (command) {
		case ShortMessage.CONTROL_CHANGE:
			if (data1 == 64) { // Hold1
				if (data2 > 0) {
					addEditTick();
				}
			}
			break;
		case ShortMessage.NOTE_ON:
			if (data2 > 0) {
				int note = data1 - 12;
				int velocity = data2 / 8;
				System.out.println("NOTE ON: "+note+" v"+velocity);
				player.playNote(note, velocity);
				midiNote = note;
				addNote(note);
				break;
			}
			// data2 == 0 は Note Off.
		case ShortMessage.NOTE_OFF:
			int note = data1 - 12;
			if (note == midiNote) {
				player.offNote();
				midiNote = Integer.MIN_VALUE;
			}
			break;
		}
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		if (sequencer.isRunning()) {
			return;
		}
		System.out.print(timeStamp+" > ");
		if (message instanceof MetaMessage) {
			System.out.println("MetaMessage");
		} else if (message instanceof ShortMessage) {
			System.out.println("ShortMessage");
			shortMessageAction((ShortMessage)message);
		} else if (message instanceof SysexMessage) {
			System.out.println("Sysex");
		} else {
			System.out.println("Unknown MIDI message.");
		}
	}

	@Override
	public void close() {}

}

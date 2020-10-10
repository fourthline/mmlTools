/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.IntConsumer;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;

import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.IPlayNote;
import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.PianoRollView;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.parser.MMLEventParser;

import static fourthline.mabiicco.AppResource.appText;


/**
 * キーボード入力による編集.
 */
public final class KeyboardEditor {

	private static boolean debug = false;
	public static void setDebug(boolean b) {
		debug = b;
	}

	private final JDialog dialog;

	private final IMMLManager mmlManager;
	private final IPlayNote player;
	private final IEditAlign editAlign;
	private final PianoRollView pianoRollView;
	private final int initOct = 4;
	private final int minOct = 0;
	private final int maxOct = 8;

	private final JPanel panel = new JPanel(new BorderLayout());
	private final JComboBox<MidiDevice.Info> midiList = new JComboBox<>();
	private final JSpinner velocityValueField = NumberSpinner.createSpinner(MMLNoteEvent.INIT_VOL, 0, MMLNoteEvent.MAX_VOL, 1);
	private final JSpinner octaveValueField = NumberSpinner.createSpinner(initOct, minOct, maxOct, 1);
	private final JRadioButton monoButton = new JRadioButton(appText("edit.midi_device.mono"));
	private final JRadioButton chordButton = new JRadioButton(appText("edit.midi_device.chord"));
	private IntConsumer noteAlignChanger;

	private final CharKeyboard charKeyboard = new CharKeyboard();
	private final MidiKeyboard midiKeyboard = new MidiKeyboard();

	/** 単音/和音入力は排他 */
	private IKeyboardAction currentAction = null;

	private synchronized boolean tryLock(IKeyboardAction action) {
		if (currentAction == null) {
			if (debug) System.out.println("lock   "+action.getClass().getSimpleName());
			currentAction = action;
			return true;
		} else if (currentAction == action) {
			return true;
		}
		return false;
	}

	private synchronized void unlock(IKeyboardAction action) {
		if (currentAction != action) {
			throw new IllegalArgumentException("keyboardAction unlock");
		}
		if (debug) System.out.println("unlock "+action.getClass().getSimpleName());
		currentAction = null;
	}

	public boolean isEmpty() {
		return (currentAction == null);
	}

	public KeyboardEditor(Frame parentFrame, IMMLManager mmlManager, IPlayNote player, IEditAlign editAlign, PianoRollView pianoRollView) {
		this.mmlManager = mmlManager;
		this.player = player;
		this.editAlign = editAlign;
		this.pianoRollView = pianoRollView;
		if (parentFrame != null) {
			dialog = new JDialog(parentFrame, appText("edit.keyboard.input"), true, parentFrame.getGraphicsConfiguration());
		} else {
			dialog = new JDialog(parentFrame, appText("edit.keyboard.input"), true);
		}
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

		JPanel panel3 = new JPanel();
		panel3.add(new JLabel(appText("edit.midi_device")));
		midiList.setFocusable(false);
		midiList.addActionListener(new MidiEventListener());
		panel3.add(midiList);

		JPanel panel4 = new JPanel();
		panel4.add(new JLabel(appText("edit.midi_device.input_method")));
		ButtonGroup buttonGroup = new ButtonGroup();
		monoButton.setFocusable(false);
		chordButton.setFocusable(false);
		buttonGroup.add(monoButton);
		buttonGroup.add(chordButton);
		selectMidiModeButton();
		monoButton.addActionListener(t -> changeEditor(false));
		chordButton.addActionListener(t -> changeEditor(true));
		panel4.add(monoButton);
		panel4.add(chordButton);

		JPanel nPanel = new JPanel();
		nPanel.setLayout(new BoxLayout(nPanel, BoxLayout.Y_AXIS));
		nPanel.add(createLPanel(new JLabel(appText("edit.keyboard.input.description"))));
		nPanel.add(createLPanel(panel1));
		nPanel.add(createLPanel(panel2));
		nPanel.add(createLPanel(panel3));
		nPanel.add(createLPanel(panel4));

		panel.add(nPanel, BorderLayout.NORTH);

		dialog.addKeyListener(charKeyboard);
		dialog.getContentPane().add(panel);
		dialog.setResizable(false);
	}

	private void editorInit() {
		currentAction = null;
		charKeyboard.clear();
		midiKeyboard.clear();
	}

	public void setVisible(boolean b) {
		if (!b) {
			dialog.setVisible(b);
			return;
		}
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

		editorInit();
		midiKeyboard.initPartList();

		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(b);
	}

	private int nextTick(int tickOffset) {
		int tickLength = editAlign.getEditAlign();
		int nextTick = tickOffset + tickLength;
		return nextTick;
	}

	private int prevTick(int tickOffset) {
		int tickLength = editAlign.getEditAlign();
		int nextTick = tickOffset - tickLength;
		return nextTick;
	}

	/**
	 * MIDI入力モードを変更する. 入力中のモード変更は禁止する.
	 * @param chordInput    単音は false, 和音は true.
	 */
	public void changeEditor(boolean chordInput) {
		if (currentAction == null) {
			MabiIccoProperties.getInstance().midiChordInput.set(chordInput);
			charKeyboard.clear();
			midiKeyboard.clear();
		}
		selectMidiModeButton();
	}

	/**
	 * MIDI入力モードに基づいてボタンの選択状態を設定する.
	 */
	private void selectMidiModeButton() {
		boolean chordInput = MabiIccoProperties.getInstance().midiChordInput.get();
		if (monoButton.isSelected() == chordInput) {
			monoButton.setSelected(!chordInput);
		}
		if (chordButton.isSelected() != chordInput) {
			chordButton.setSelected(chordInput);
		}
	}

	private interface IKeyboardAction {
		void clear();
		void pressNote(int note);
		void addTick();
		void releaseNote(int note);
		boolean isPlay();
	}

	/**
	 * キーボード/MIDI: 単音入力
	 */
	private final class CharKeyboard implements KeyListener, IKeyboardAction {
		private MMLNoteEvent editNote = null;
		private int playNote = Integer.MIN_VALUE;
		private char typeCode = 0;

		private int charToNote(char code) {
			int octave = ((Integer) octaveValueField.getValue()).intValue();
			int note = MMLEventParser.firstNoteNumber("o"+octave+code);
			return note;
		}

		@Override
		public synchronized void clear() {
			editNote = null;
			playNote = Integer.MIN_VALUE;
			typeCode = 0;
		}

		@Override
		public synchronized void pressNote(int note) {
			MMLEventList activePart = mmlManager.getActiveMMLPart();
			int velocity = ((Integer) velocityValueField.getValue()).intValue();
			int tickOffset = (int) pianoRollView.getSequencePosition();
			int nextTick = nextTick(tickOffset);
			MMLNoteEvent noteEvent = new MMLNoteEvent(note, nextTick-tickOffset, tickOffset, velocity);
			activePart.addMMLNoteEvent(noteEvent);
			playNote = note;
			player.playNote(note, velocity);
			pianoRollView.setSequenceTick(nextTick);
			mmlManager.updateActivePart(true);
			mmlManager.updatePianoRollView(note);
			this.editNote = noteEvent;
		}

		private synchronized void addRest() {
			int tickOffset = (int) pianoRollView.getSequencePosition();
			int nextTick = nextTick(tickOffset);
			MMLEventList activePart = mmlManager.getActiveMMLPart();
			MMLNoteEvent noteEvent = activePart.searchOnTickOffset(tickOffset);
			activePart.deleteMMLEvent(noteEvent);
			pianoRollView.setSequenceTick(nextTick);
			mmlManager.updateActivePart(true);
			mmlManager.updatePianoRollView();
			this.editNote = null;
		}

		private void backDelete() {
			int tickOffset = (int) pianoRollView.getSequencePosition();
			MMLEventList activePart = mmlManager.getActiveMMLPart();

			int prevTick = prevTick(tickOffset);
			MMLNoteEvent deleteEvent = activePart.searchOnTickOffset(prevTick);
			activePart.deleteMMLEvent(deleteEvent);
			pianoRollView.setSequenceTick(prevTick);
			mmlManager.updateActivePart(true);

			MMLNoteEvent prevNote = activePart.searchPrevNoteOnTickOffset(prevTick);
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
				int note = editNote.getNote();
				if (note < 107) {
					note++;
				}
				editNote.setNote(note);
			} else if (code == '-') {
				int note = editNote.getNote();
				if (note >= 0) {
					note--;
				}
				editNote.setNote(note);
			} else {
				return;
			}
			int note = editNote.getNote();
			playNote = note;
			player.playNote(note, editNote.getVelocity());
			mmlManager.updateActivePart(true);
			mmlManager.updatePianoRollView(editNote.getNote());
		}

		@Override
		public synchronized void addTick() {
			int tickOffset = (int) pianoRollView.getSequencePosition();
			MMLEventList activePart = mmlManager.getActiveMMLPart();
			if (editNote == null) {
				return;
			}

			int nextTick = nextTick(tickOffset);
			editNote.setTick(nextTick - editNote.getTickOffset());
			activePart.addMMLNoteEvent(editNote);
			pianoRollView.setSequenceTick(nextTick);
			mmlManager.updateActivePart(true);
			mmlManager.updatePianoRollView(editNote.getNote());
		}

		private void cursorMove(boolean forward) {
			int tickOffset = (int) pianoRollView.getSequencePosition();
			MMLEventList activePart = mmlManager.getActiveMMLPart();
			int nextTick = forward ? nextTick(tickOffset) : prevTick(tickOffset);
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
			if (!tryLock(this)) {
				return;
			}
			if ( (code >= 'a') && (code <= 'g') || (code >= 'A') && (code <= 'G') ) {
				int note = charToNote(code);
				if (playNote != note) {
					pressNote(note);
				}
			} else if ( (code == 'r') || (code == 'R') ) {
				addRest();
			} else if (code == KeyEvent.VK_BACK_SPACE) {
				backDelete();
			} else if ( (code == '<') || (code == '>') ) {
				octaveChange(code);
			} else if ( (code == '+') || (code == '#') || (code == '-') ) {
				addSharpFlat(code);
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
			} else if ( (code >= '0') && (code <= '9') ) {
				changeNoteAlign(code);
			}
			if (!isPlay()) {
				unlock(this);
			}
		}

		@Override
		public boolean isPlay() {
			return (playNote != Integer.MIN_VALUE);
		}

		@Override
		public synchronized void releaseNote(int note) {
			if ( (note != Integer.MIN_VALUE) && (playNote == note) ) {
				playNote = Integer.MIN_VALUE;
				player.offNote();
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			Sequencer sequencer = MabiDLS.getInstance().getSequencer();
			synchronized (this) {
				if (sequencer.isRunning()) {
					return;
				}
				char code = e.getKeyChar();
				// スペースキーは現在の入力に対して実施する.
				if (code == ' ') {
					if (currentAction != null) {
						currentAction.addTick();
					}
					return;
				}
				if (typeCode != code) {
					typeCode = code;
					pressAction(code);
				}
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == ' ') {
				return;
			}
			int code = e.getKeyCode();
			if ( (code == KeyEvent.VK_LEFT) || (code == KeyEvent.VK_RIGHT)
					|| (code == KeyEvent.VK_UP) || (code == KeyEvent.VK_DOWN)) {
				pressAction((char)code);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			synchronized (this) {
				char keyChar = e.getKeyChar();
				if ( typeCode != keyChar ) {
					return;
				}
				typeCode = 0;
				if ( (keyChar != ' ') && (playNote != Integer.MIN_VALUE) ) {
					if (tryLock(this)) {
						unlock(this);
					}
				}
				releaseNote(playNote);
			}
		}
	}

	/**
	 * MIDI: 和音入力
	 */
	private final class MidiKeyboard implements IKeyboardAction {
		private final ArrayList<MMLNoteEvent> chord = new ArrayList<>();
		private final ArrayList<MMLEventList> partList = new ArrayList<>();
		private void initPartList() {
			partList.clear();
			int program = mmlManager.getActivePartProgram();
			boolean enablePart[] = InstClass.getEnablePartByProgram(program);
			int trackIndex = mmlManager.getActiveTrackIndex();
			MMLTrack activeTrack = mmlManager.getMMLScore().getTrack(trackIndex);
			for (int i = 0; i < enablePart.length; i++) {
				if (enablePart[i]) {
					partList.add(activeTrack.getMMLEventAtIndex(i));
				}
			}
		}

		@Override
		public synchronized void clear() {
			chord.clear();
		}

		@Override
		public synchronized void pressNote(int note) {
			if (chord.size() >= partList.size()) {
				return;
			}
			int tickOffset = (int) pianoRollView.getSequencePosition();
			int tickLength = editAlign.getEditAlign();
			if (!chord.isEmpty()) {
				tickLength = chord.get(0).getTick();
			}
			int velocity = ((Integer)velocityValueField.getValue()).intValue();
			if (!chord.stream().anyMatch(t -> t.getNote() == note)) {
				MMLNoteEvent addNoteEvent = new MMLNoteEvent(note, tickLength, tickOffset, velocity);

				chord.add(addNoteEvent);
			}
			Iterator<MMLNoteEvent> noteList = chord.iterator();
			for (int i = 0; i < partList.size(); i++) {
				if (noteList.hasNext()) {
					partList.get(i).addMMLNoteEvent( noteList.next() );
				} else {
					MMLNoteEvent deleteNoteEvent = partList.get(i).searchOnTickOffset(tickOffset);
					if (deleteNoteEvent != null) {
						partList.get(i).deleteMMLEvent(deleteNoteEvent);
					}
				}
			}
			int playNote[] = new int[chord.size()];
			for (int i = 0; i < playNote.length; i++) {
				playNote[i] = chord.get(i).getNote();
			}
			player.playNote(playNote, velocity);
			mmlManager.updateActivePart(true);
			mmlManager.updatePianoRollView(note);
		}

		@Override
		public synchronized void addTick() {
			for (int i = 0; i < chord.size(); i++) {
				MMLNoteEvent note = chord.get(i);
				partList.get(i).deleteMMLEvent(note);
				note.setTick( note.getTick() + editAlign.getEditAlign() );
				partList.get(i).addMMLNoteEvent(note);
			}
			mmlManager.updateActivePart(true);
			mmlManager.updatePianoRollView();
		}

		@Override
		public synchronized void releaseNote(int note) {
			if (chord.size() == 0) {
				return;
			}
			int nextTick = chord.get(0).getEndTick();
			for (MMLNoteEvent n : chord) {
				if (n.getNote() == note) {
					chord.remove(n);
					break;
				}
			}
			if (chord.size() == 0) {
				player.offNote();
				pianoRollView.setSequenceTick(nextTick);
				mmlManager.updateActivePart(true);
				mmlManager.updatePianoRollView();
			}
		}

		@Override
		public boolean isPlay() {
			return (chord.size() != 0);
		}
	}

	/**
	 * MIDIデバイスからのイベント処理を行います.
	 */
	private final class MidiEventListener implements Receiver, ActionListener {
		private MidiDevice midiDevice = null;

		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() != midiList) {
				return;
			}
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

		private synchronized void shortMessageAction(ShortMessage msg) {
			int command = msg.getCommand();
			int data1 = msg.getData1();
			int data2 = msg.getData2();
			int note = data1 - 12;
			IKeyboardAction action = MabiIccoProperties.getInstance().midiChordInput.get() ? midiKeyboard : charKeyboard;

			if (!tryLock(action)) {
				return;
			}
			switch (command) {
			case ShortMessage.CONTROL_CHANGE:
				if (data1 == 64) { // Hold1
					if (data2 > 0) {
						action.addTick();
					}
				}
				break;
			case ShortMessage.NOTE_ON:
				if (data2 > 0) {
					action.pressNote(note);
					break;
				}
				// data2 == 0 は Note Off.
			case ShortMessage.NOTE_OFF:
				action.releaseNote(note);
				break;
			}
			if (!action.isPlay()) {
				unlock(action);
			}
		}

		@Override
		public void send(MidiMessage message, long timeStamp) {
			Sequencer sequencer = MabiDLS.getInstance().getSequencer();
			if (sequencer.isRunning()) {
				return;
			}
			if (message.getStatus() == ShortMessage.ACTIVE_SENSING) {
				return;
			}
			if (message instanceof ShortMessage) {
				shortMessageAction((ShortMessage)message);
			}
		}

		@Override
		public void close() {}
	}

	public KeyListener getKeyListener() {
		return charKeyboard;
	}

	public Receiver getReciever() {
		ActionListener actionListener[] = midiList.getActionListeners();
		for (ActionListener action : actionListener) {
			if (action instanceof Receiver) {
				return (Receiver) action;
			}
		}
		return null;
	}
}

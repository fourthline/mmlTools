/*
 * Copyright (C) 2017-2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.function.IntConsumer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JButton;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.midi.IPlayNote;
import jp.fourthline.mabiicco.ui.AbstractMMLManager;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.PianoRollView;
import jp.fourthline.mabiicco.ui.editor.IEditAlign;
import jp.fourthline.mabiicco.ui.editor.KeyboardEditor;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.UndefinedTickException;

public class KeyboardEditorTest extends UseLoadingDLS {

	private KeyboardEditor editor;
	private IMMLManager mmlManager;
	private IPlayNote player;
	private IEditAlign editAlign;
	private PianoRollView pianoRollView;
	private Receiver reciver;
	private KeyListener key;

	private class StubMmlManager extends AbstractMMLManager {
		private MMLScore score;

		private StubMmlManager() {
			score = new MMLScore();
			score.addTrack(new MMLTrack().setMML(""));
		}
		@Override
		public MMLScore getMMLScore() {
			return score;
		}

		@Override
		public void setMMLScore(MMLScore score) {}

		@Override
		public int getActiveTrackIndex() {
			return 0;
		}

		@Override
		public MMLEventList getActiveMMLPart() {
			return score.getTrack(0).getMMLEventAtIndex(0);
		}

		@Override
		public void updateActivePart(boolean generate) {
			try {
				score.generateAll();
			} catch (UndefinedTickException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void updateActiveTrackProgram(int trackIndex, int program, int songProgram) {}

		@Override
		public int getActivePartProgram() {
			return 0;
		}

		@Override
		public boolean selectTrackOnExistNote(int note, int tickOffset) {
			return false;
		}

		@Override
		public void setMMLselectedTrack(MMLTrack track) {}

		@Override
		public void addMMLTrack(MMLTrack track) {}

		@Override
		public void moveTrack(int toIndex) {}

		@Override
		public void updatePianoRollView() {}

		@Override
		public void updatePianoRollView(int note) {}

		@Override
		public void generateActiveTrack() {}

		@Override
		public MMLTrack getActiveTrack() {
			return score.getTrack(getActiveTrackIndex());
		}
		@Override
		public int getActiveMMLPartIndex() {
			return 0;
		}
	}

	private class StubPlayer implements IPlayNote {
		@Override
		public void playNote(int note, int velocity) {}

		@Override
		public void playNote(int[] note, int velocity) {}

		@Override
		public void offNote() {}
	}

	private class StubAlign implements IEditAlign, IntConsumer {
		private int index = 1;
		private int align[] = { 192, 48, 24 };
		@Override
		public int getEditAlign() {
			return align[index];
		}

		@Override
		public void accept(int value) {
			index = value;
		}
	}

	private boolean originalMidiSetting;
	private String originalMidiDeviceSetting;

	@Before
	public void setup() {
		KeyboardEditor.setDebug(true);
		originalMidiSetting = MabiIccoProperties.getInstance().midiChordInput.get();
		MabiIccoProperties.getInstance().midiChordInput.set(true);
		originalMidiDeviceSetting = MabiIccoProperties.getInstance().midiInputDevice.get();

		mmlManager = new StubMmlManager();
		player = new StubPlayer();
		StubAlign align = new StubAlign();
		editAlign = align;
		pianoRollView = new PianoRollView();
		editor = new KeyboardEditor((Frame)null, mmlManager, player, editAlign, pianoRollView);
		editor.setNoteAlignChanger(align);

		reciver = editor.getReciever();
		key = editor.getKeyListener();
	}

	@After
	public void cleanup() {
		MabiIccoProperties.getInstance().midiChordInput.set(originalMidiSetting);
		MabiIccoProperties.getInstance().midiInputDevice.set(originalMidiDeviceSetting);
		KeyboardEditor.setDebug(false);
	}

	private Component dummyComponent = new JButton();
	private void keyTyped(char keyChar) {
		key.keyTyped(new KeyEvent(dummyComponent, 0, 0, 0, 0, keyChar));
	}

	private void keyReleased(char keyChar) {
		key.keyReleased(new KeyEvent(dummyComponent, 0, 0, 0, 0, keyChar));
	}

	private void keyPressed(int keyCode) {
		key.keyPressed(new KeyEvent(dummyComponent, 0, 0, 0, keyCode, '?'));
	}

	private void keyReleased(int keyCode) {
		key.keyReleased(new KeyEvent(dummyComponent, 0, 0, 0, keyCode, '?'));
	}

	private void midiNoteOn(int note) throws InvalidMidiDataException {
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, note, 110), 0);
	}

	private void midiNoteOff(int note) throws InvalidMidiDataException {
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, note, 0), 0);
	}

	@Test
	public void testCharKeyboard() {
		MMLNoteEvent note1 = new MMLNoteEvent(48, 48, 0, 8);
		MMLNoteEvent note2 = new MMLNoteEvent(48, 96, 0, 8);
		MMLNoteEvent note3 = new MMLNoteEvent(50, 48, 96, 8);
		MMLNoteEvent note4 = new MMLNoteEvent(24, 48, 192, 8);
		MMLNoteEvent note5 = new MMLNoteEvent(48, 48, 192+48, 8);
		List<MMLNoteEvent> eventList = mmlManager.getActiveMMLPart().getMMLNoteEventList();
		assertEquals(0, eventList.size());
		assertEquals(0, pianoRollView.getSequencePosition());

		// 1音目
		assertTrue(editor.isEmpty());
		keyTyped('c');
		assertEquals(1, eventList.size());
		assertEquals(note1, eventList.get(0));
		assertFalse(editor.isEmpty());
		assertEquals(48, pianoRollView.getSequencePosition());

		// スペースのばし
		keyTyped(' ');
		assertEquals(1, eventList.size());
		assertEquals(note2, eventList.get(0));
		assertFalse(editor.isEmpty());
		assertEquals(96, pianoRollView.getSequencePosition());

		// キーリリース
		keyReleased(' ');
		keyTyped('d');
		assertEquals(2, eventList.size());
		assertEquals(note3, eventList.get(1));
		assertFalse(editor.isEmpty());
		assertEquals(144, pianoRollView.getSequencePosition());

		keyReleased('c');
		assertFalse(editor.isEmpty());
		keyReleased('d');
		assertTrue(editor.isEmpty());
		assertEquals(144, pianoRollView.getSequencePosition());

		// 休符挿入
		keyTyped('r');
		assertTrue(editor.isEmpty());
		assertEquals(192, pianoRollView.getSequencePosition());
		keyReleased('r');
		assertTrue(editor.isEmpty());
		assertEquals(2, eventList.size());
		assertEquals(192, pianoRollView.getSequencePosition());

		// オクターブをさげる
		keyTyped('<');
		keyReleased('<');
		keyTyped('<');
		keyReleased('<');

		assertTrue(editor.isEmpty());
		keyTyped('c');
		assertEquals(3, eventList.size());
		assertEquals(note4, eventList.get(2));
		assertFalse(editor.isEmpty());
		assertEquals(192+48, pianoRollView.getSequencePosition());

		// オクターブをあげる
		keyTyped('>');
		keyReleased('>');
		keyTyped('>');
		keyReleased('>');

		assertTrue(editor.isEmpty());
		keyTyped('c');
		assertFalse(editor.isEmpty());
		keyReleased('c');
		assertEquals(4, eventList.size());
		assertEquals(note5, eventList.get(3));
		assertTrue(editor.isEmpty());
		assertEquals(192+96, pianoRollView.getSequencePosition());

		assertEquals("MML@cl8drn24c,,;", mmlManager.getMMLScore().getTrack(0).getMabiMML());
	}

	@Test
	public void testCharKeyboard_SharpFlat() {
		MMLTrack expect = new MMLTrack().setMML("MML@l8c+d+g+>c2");

		keyTyped('c');
		keyTyped('+');
		keyReleased('+');
		keyReleased('c');
		assertTrue(editor.isEmpty());
		keyTyped('e');
		keyTyped('-');
		keyReleased('-');
		keyReleased('e');
		assertTrue(editor.isEmpty());
		keyTyped('g');
		keyTyped('#');
		keyReleased('#');
		keyReleased('g');
		assertTrue(editor.isEmpty());

		// スペースのばし
		keyTyped('b');
		keyTyped('+');
		keyTyped(' ');
		keyReleased(' ');
		keyTyped(' ');
		keyReleased(' ');
		keyTyped(' ');
		keyReleased(' ');
		keyReleased('+');
		keyReleased('b');
		assertTrue(editor.isEmpty());

		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());
	}

	@Test
	public void testCharKeyboard_Velocity_Back() {
		MMLTrack expect1 = new MMLTrack().setMML("MML@v10c8v8c8,,;");
		MMLTrack expect2 = new MMLTrack().setMML("MML@r8c8,,;");

		// 音量Up
		keyPressed(KeyEvent.VK_UP);
		keyReleased(KeyEvent.VK_UP);
		keyPressed(KeyEvent.VK_UP);
		keyReleased(KeyEvent.VK_UP);

		keyTyped('c');
		keyReleased('c');

		// 音量Down
		keyPressed(KeyEvent.VK_DOWN);
		keyReleased(KeyEvent.VK_DOWN);
		keyPressed(KeyEvent.VK_DOWN);
		keyReleased(KeyEvent.VK_DOWN);

		keyTyped('c');
		keyReleased('c');

		assertTrue(editor.isEmpty());
		assertEquals(expect1.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		// 左移動
		assertEquals(96, pianoRollView.getSequencePosition());
		keyPressed(KeyEvent.VK_LEFT);
		keyReleased(KeyEvent.VK_LEFT);
		assertEquals(48, pianoRollView.getSequencePosition());
		assertTrue(editor.isEmpty());

		// バックスペース
		keyTyped((char)KeyEvent.VK_BACK_SPACE);
		keyReleased((char)KeyEvent.VK_BACK_SPACE);
		assertTrue(editor.isEmpty());
		assertEquals(expect2.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		// 右移動
		assertEquals(0, pianoRollView.getSequencePosition());
		keyPressed(KeyEvent.VK_RIGHT);
		keyReleased(KeyEvent.VK_RIGHT);
		keyPressed(KeyEvent.VK_RIGHT);
		keyReleased(KeyEvent.VK_RIGHT);
		assertEquals(96, pianoRollView.getSequencePosition());
		assertTrue(editor.isEmpty());

		// バックスペース
		keyTyped((char)KeyEvent.VK_BACK_SPACE);
		keyReleased((char)KeyEvent.VK_BACK_SPACE);
		assertTrue(editor.isEmpty());
		assertEquals("MML@,,;", mmlManager.getMMLScore().getTrack(0).getMabiMML());
	}

	@Test
	public void testCharKeyboard_ChangeNoteAlign() {
		MMLTrack expect = new MMLTrack().setMML("MML@c2c16;");

		// 1番目のalign指定
		keyTyped('1');
		keyReleased('1');
		keyTyped('c');
		keyReleased('c');

		// 3番目のalign指定
		keyTyped('3');
		keyReleased('3');
		keyTyped('c');
		keyReleased('c');
		assertTrue(editor.isEmpty());
		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());
	}

	@Test
	public void testMidiKeyboard() throws InterruptedException, InvalidMidiDataException {
		MMLTrack expect1 = new MMLTrack().setMML("MML@<c8,<e8,<g8;");
		MMLTrack expect2 = new MMLTrack().setMML("MML@<c8c,<e8e,<g8g;");
		new Thread(() -> editor.setVisible(true)).start();
		Thread.sleep(500);

		// 和音入力
		midiNoteOn(48);
		assertFalse(editor.isEmpty());
		midiNoteOn(52);
		assertFalse(editor.isEmpty());
		midiNoteOn(55);
		assertFalse(editor.isEmpty());

		midiNoteOff(48);
		assertFalse(editor.isEmpty());
		midiNoteOff(52);
		assertFalse(editor.isEmpty());
		midiNoteOff(55);
		assertTrue(editor.isEmpty());
		assertEquals(expect1.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		// 途中のスペース入力
		midiNoteOn(48);
		assertFalse(editor.isEmpty());
		midiNoteOn(52);
		assertFalse(editor.isEmpty());
		midiNoteOn(55);
		assertFalse(editor.isEmpty());

		keyTyped(' ');
		keyReleased(' ');

		midiNoteOff(48);
		assertFalse(editor.isEmpty());
		midiNoteOff(52);
		assertFalse(editor.isEmpty());
		midiNoteOff(55);
		assertTrue(editor.isEmpty());
		assertEquals(expect2.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		editor.setVisible(false);
	}

	@Test
	public void testMidiKeyboard_ChangeEditor() throws InterruptedException, InvalidMidiDataException {
		MMLTrack expect = new MMLTrack().setMML("MML@l8<ceg,,;");
		new Thread(() -> editor.setVisible(true)).start();
		Thread.sleep(500);

		editor.changeEditor(false);

		// MIDIキーボードの単音入力
		midiNoteOn(48);
		assertFalse(editor.isEmpty());
		midiNoteOn(52);
		assertFalse(editor.isEmpty());
		midiNoteOn(55);
		assertFalse(editor.isEmpty());

		midiNoteOff(48);
		assertFalse(editor.isEmpty());
		midiNoteOff(52);
		assertFalse(editor.isEmpty());
		midiNoteOff(55);
		assertTrue(editor.isEmpty());
		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		editor.setVisible(false);
	}

	@Test
	public void testLock() throws InterruptedException, InvalidMidiDataException {
		MMLTrack expect = new MMLTrack().setMML("MML@<c8>c8,,;");
		new Thread(() -> editor.setVisible(true)).start();
		Thread.sleep(500);

		// MIDI入力中の文字入力禁止
		midiNoteOn(48);
		assertFalse(editor.isEmpty());
		keyTyped('c');
		assertFalse(editor.isEmpty());
		keyReleased('c');
		assertFalse(editor.isEmpty());
		midiNoteOff(48);
		assertTrue(editor.isEmpty());

		// 文字入力中のMIDI入力禁止
		keyTyped('c');
		assertFalse(editor.isEmpty());
		midiNoteOn(48);
		assertFalse(editor.isEmpty());
		midiNoteOff(48);
		keyReleased('c');
		assertTrue(editor.isEmpty());

		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		editor.setVisible(false);
	}

	@Test
	public void testLock_changeEditor() throws InterruptedException, InvalidMidiDataException {
		MMLTrack expect = new MMLTrack().setMML("MML@<c8c8,,;");
		new Thread(() -> editor.setVisible(true)).start();
		Thread.sleep(500);

		// MIDI入力中のモード切替禁止
		midiNoteOn(48);
		assertFalse(editor.isEmpty());
		editor.changeEditor(false);
		assertFalse(editor.isEmpty());
		midiNoteOff(48);
		assertTrue(editor.isEmpty());

		// MIDI入力中のモード切替禁止
		midiNoteOn(48);
		assertFalse(editor.isEmpty());
		editor.changeEditor(true);
		assertFalse(editor.isEmpty());
		midiNoteOff(48);
		assertTrue(editor.isEmpty());

		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		editor.setVisible(false);
	}

	@Test
	public void testRelease() throws InterruptedException, InvalidMidiDataException {
		new Thread(() -> editor.setVisible(true)).start();
		Thread.sleep(500);

		// リリースのみ.
		midiNoteOff(48);
		assertTrue(editor.isEmpty());
		keyReleased('c');
		assertTrue(editor.isEmpty());

		editor.setVisible(false);
	}
}

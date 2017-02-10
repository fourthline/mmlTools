/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

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

import fourthline.mabiicco.ui.PianoRollView;
import fourthline.UseLoadingDLS;
import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.IPlayNote;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.UndefinedTickException;

public class KeyboardEditorTest extends UseLoadingDLS {

	private KeyboardEditor editor;
	private IMMLManager mmlManager;
	private IPlayNote player;
	private IEditAlign editAlign;
	private PianoRollView pianoRollView;

	private class StubMmlManager implements IMMLManager {
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
	}

	@After
	public void cleanup() {
		MabiIccoProperties.getInstance().midiChordInput.set(originalMidiSetting);
		MabiIccoProperties.getInstance().midiInputDevice.set(originalMidiDeviceSetting);
		KeyboardEditor.setDebug(false);
	}

	@Test
	public void testCharKeyboard() {
		MMLNoteEvent note1 = new MMLNoteEvent(48, 48, 0, 8);
		MMLNoteEvent note2 = new MMLNoteEvent(48, 96, 0, 8);
		MMLNoteEvent note3 = new MMLNoteEvent(50, 48, 96, 8);
		MMLNoteEvent note4 = new MMLNoteEvent(24, 48, 192, 8);
		MMLNoteEvent note5 = new MMLNoteEvent(48, 48, 192+48, 8);
		KeyListener key = editor.getKeyListener();
		List<MMLNoteEvent> eventList = mmlManager.getActiveMMLPart().getMMLNoteEventList();
		assertEquals(0, eventList.size());
		assertEquals(0, pianoRollView.getSequencePosition());

		// 1音目
		assertTrue(editor.isEmpty());
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertEquals(1, eventList.size());
		assertEquals(note1, eventList.get(0));
		assertFalse(editor.isEmpty());
		assertEquals(48, pianoRollView.getSequencePosition());

		// スペースのばし
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, ' '));
		assertEquals(1, eventList.size());
		assertEquals(note2, eventList.get(0));
		assertFalse(editor.isEmpty());
		assertEquals(96, pianoRollView.getSequencePosition());

		// キーリリース
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, ' '));
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'd'));
		assertEquals(2, eventList.size());
		assertEquals(note3, eventList.get(1));
		assertFalse(editor.isEmpty());
		assertEquals(144, pianoRollView.getSequencePosition());

		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertFalse(editor.isEmpty());
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'd'));
		assertTrue(editor.isEmpty());
		assertEquals(144, pianoRollView.getSequencePosition());

		// 休符挿入
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'r'));
		assertTrue(editor.isEmpty());
		assertEquals(192, pianoRollView.getSequencePosition());
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'r'));
		assertTrue(editor.isEmpty());
		assertEquals(2, eventList.size());
		assertEquals(192, pianoRollView.getSequencePosition());

		// オクターブをさげる
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '<'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, '<'));
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '<'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, '<'));

		assertTrue(editor.isEmpty());
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertEquals(3, eventList.size());
		assertEquals(note4, eventList.get(2));
		assertFalse(editor.isEmpty());
		assertEquals(192+48, pianoRollView.getSequencePosition());

		// オクターブをあげる
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '>'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, '>'));
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '>'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, '>'));

		assertTrue(editor.isEmpty());
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertFalse(editor.isEmpty());
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertEquals(4, eventList.size());
		assertEquals(note5, eventList.get(3));
		assertTrue(editor.isEmpty());
		assertEquals(192+96, pianoRollView.getSequencePosition());

		assertEquals("MML@cl8drn24c,,;", mmlManager.getMMLScore().getTrack(0).getMabiMML());
	}

	@Test
	public void testCharKeyboard_SharpFlat() {
		MMLTrack expect = new MMLTrack().setMML("MML@l8c+d+g+");
		KeyListener key = editor.getKeyListener();

		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '+'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, '+'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertTrue(editor.isEmpty());
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'e'));
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '-'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, '-'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'e'));
		assertTrue(editor.isEmpty());
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'g'));
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '#'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, '#'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'g'));
		assertTrue(editor.isEmpty());

		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());
	}

	@Test
	public void testCharKeyboard_Velocity_Back() {
		MMLTrack expect1 = new MMLTrack().setMML("MML@v10c8v8c8,,;");
		MMLTrack expect2 = new MMLTrack().setMML("MML@r8c8,,;");
		KeyListener key = editor.getKeyListener();

		// 音量Up
		key.keyPressed(new KeyEvent(new JButton(), 0, 0, 0, KeyEvent.VK_UP, '?'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  KeyEvent.VK_UP, '?'));
		key.keyPressed(new KeyEvent(new JButton(), 0, 0, 0, KeyEvent.VK_UP, '?'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  KeyEvent.VK_UP, '?'));

		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));

		// 音量Down
		key.keyPressed(new KeyEvent(new JButton(), 0, 0, 0, KeyEvent.VK_DOWN, '?'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  KeyEvent.VK_DOWN, '?'));
		key.keyPressed(new KeyEvent(new JButton(), 0, 0, 0, KeyEvent.VK_DOWN, '?'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  KeyEvent.VK_DOWN, '?'));

		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));

		assertTrue(editor.isEmpty());
		assertEquals(expect1.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		// 左移動
		assertEquals(96, pianoRollView.getSequencePosition());
		key.keyPressed(new KeyEvent(new JButton(), 0, 0, 0, KeyEvent.VK_LEFT, '?'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  KeyEvent.VK_LEFT, '?'));
		assertEquals(48, pianoRollView.getSequencePosition());
		assertTrue(editor.isEmpty());

		// バックスペース
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, (char)KeyEvent.VK_BACK_SPACE));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  0, (char)KeyEvent.VK_BACK_SPACE));
		assertTrue(editor.isEmpty());
		assertEquals(expect2.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		// 右移動
		assertEquals(0, pianoRollView.getSequencePosition());
		key.keyPressed(new KeyEvent(new JButton(), 0, 0, 0, KeyEvent.VK_RIGHT, '?'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  KeyEvent.VK_RIGHT, '?'));
		key.keyPressed(new KeyEvent(new JButton(), 0, 0, 0, KeyEvent.VK_RIGHT, '?'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  KeyEvent.VK_RIGHT, '?'));
		assertEquals(96, pianoRollView.getSequencePosition());
		assertTrue(editor.isEmpty());

		// バックスペース
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, (char)KeyEvent.VK_BACK_SPACE));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  0, (char)KeyEvent.VK_BACK_SPACE));
		assertTrue(editor.isEmpty());
		assertEquals("MML@,,;", mmlManager.getMMLScore().getTrack(0).getMabiMML());
	}

	@Test
	public void testCharKeyboard_ChangeNoteAlign() {
		MMLTrack expect = new MMLTrack().setMML("MML@c2c16;");
		KeyListener key = editor.getKeyListener();

		// 1番目のalign指定
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '1'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  0, '1'));
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));

		// 3番目のalign指定
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, '3'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0,  0, '3'));
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertTrue(editor.isEmpty());
		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());
	}

	@Test
	public void testMidiKeyboard() throws InterruptedException, InvalidMidiDataException {
		MMLTrack expect1 = new MMLTrack().setMML("MML@<c8,<e8,<g8;");
		MMLTrack expect2 = new MMLTrack().setMML("MML@<c8c,<e8e,<g8g;");
		new Thread(() -> editor.setVisible(true)).start();
		Thread.sleep(500);

		Receiver reciver = editor.getReciever();
		KeyListener key = editor.getKeyListener();

		// 和音入力
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 110), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 52, 110), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 55, 110), 0);
		assertFalse(editor.isEmpty());

		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 0), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 52, 0), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 55, 0), 0);
		assertTrue(editor.isEmpty());
		assertEquals(expect1.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		// 途中のスペース入力
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 110), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 52, 110), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 55, 110), 0);
		assertFalse(editor.isEmpty());

		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, ' '));
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, ' '));

		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 0), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 52, 0), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 55, 0), 0);
		assertTrue(editor.isEmpty());
		assertEquals(expect2.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		editor.setVisible(false);
	}

	@Test
	public void testMidiKeyboard_ChangeEditor() throws InterruptedException, InvalidMidiDataException {
		MMLTrack expect = new MMLTrack().setMML("MML@l8<ceg,,;");
		new Thread(() -> editor.setVisible(true)).start();
		Thread.sleep(500);

		Receiver reciver = editor.getReciever();
		editor.changeEditor(false);

		// MIDIキーボードの単音入力
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 110), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 52, 110), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 55, 110), 0);
		assertFalse(editor.isEmpty());

		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 0), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 52, 0), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 55, 0), 0);
		assertTrue(editor.isEmpty());
		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		editor.setVisible(false);
	}

	@Test
	public void testLock() throws InterruptedException, InvalidMidiDataException {
		MMLTrack expect = new MMLTrack().setMML("MML@<c8>c8,,;");
		new Thread(() -> editor.setVisible(true)).start();
		Thread.sleep(500);

		Receiver reciver = editor.getReciever();
		KeyListener key = editor.getKeyListener();

		// MIDI入力中の文字入力禁止
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 110), 0);
		assertFalse(editor.isEmpty());
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertFalse(editor.isEmpty());
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 0), 0);
		assertTrue(editor.isEmpty());

		// 文字入力中のMIDI入力禁止
		key.keyTyped(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 110), 0);
		assertFalse(editor.isEmpty());
		reciver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 0), 0);
		key.keyReleased(new KeyEvent(new JButton(), 0, 0, 0, 0, 'c'));
		assertTrue(editor.isEmpty());

		assertEquals(expect.getMabiMML(), mmlManager.getMMLScore().getTrack(0).getMabiMML());

		editor.setVisible(false);
	}
}

/*
 * Copyright (C) 2015-2017 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JViewport;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.IEditStateObserver;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.midi.IPlayNote;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.MMLSeqView;
import jp.fourthline.mabiicco.ui.MMLTrackView;
import jp.fourthline.mabiicco.ui.PianoRollView;
import jp.fourthline.mmlTools.MMLBuilder;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.UndefinedTickException;

public final class MMLEditorTest extends UseLoadingDLS {

	private static boolean activePartSwitch;
	@BeforeClass
	public static void initialize() {
		activePartSwitch = MabiIccoProperties.getInstance().activePartSwitch.get();
	}

	@AfterClass
	public static void cleanup() {
		MabiIccoProperties.getInstance().activePartSwitch.set( activePartSwitch );
	}

	private MMLEditor editor;
	private IMMLManager mmlManager;
	private PianoRollView pianoRollView;
	private int playingNote = -1;

	@Before
	public void initializeObj() throws Exception {
		this.mmlManager = new MMLSeqView(null, null);
		MMLTrackView.getInstance(0, null, mmlManager).setSelectMMLPartOfIndex(0);
		Field f = MMLSeqView.class.getDeclaredField("pianoRollView");
		f.setAccessible(true);
		JViewport viewport = new JViewport();
		this.pianoRollView = (PianoRollView) f.get(this.mmlManager);
		int height = pianoRollView.getTotalHeight();
		viewport.setBounds(0, 0, 200, height);
		this.pianoRollView.setViewportAndParent(viewport, mmlManager);
		this.editor = new MMLEditor(null, new IPlayNote() {
			@Override
			public void playNote(int note, int velocity) {
				playingNote = note;
			}

			@Override
			public void playNote(int[] note, int velocity) {
				playingNote = note[0];
			}

			@Override
			public void offNote() {
				playingNote = -1;
			}
		}, this.pianoRollView, this.mmlManager);

		editor.setEditStateObserver(new IEditStateObserver() {
			@Override
			public void notifyUpdateEditState() {}
		});
	}

	private EditMode getEditMode() throws Exception {
		Field f = MMLEditor.class.getDeclaredField("editMode");
		f.setAccessible(true);
		return (EditMode) f.get(editor);
	}

	/**
	 * ノートを挿入して移動する.
	 * @throws Exception
	 */
	@Test
	public void test_insertMoveNote() throws Exception {
		List<MMLNoteEvent> noteEventList = mmlManager.getActiveMMLPart().getMMLNoteEventList();

		assertEquals(0, noteEventList.size());
		assertEquals(-1, playingNote);
		assertEquals(false, editor.hasSelectedNote());

		int note1 = pianoRollView.convertY2Note(200);
		int note2 = pianoRollView.convertY2Note(100);
		long tickOffset1 = 0;
		long tickOffset2 = pianoRollView.convertXtoTick(100);
		tickOffset2 -= ( tickOffset2 % editor.getEditAlign() );
		MouseEvent mouseEvent1 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON1_DOWN_MASK, 0, 200, 1, false);
		MouseEvent mouseEvent2 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON1_DOWN_MASK, 100, 100, 1, false);

		// マウスクリックによる挿入ノート候補選択状態.
		assertEquals(EditMode.SELECT, getEditMode());
		editor.mousePressed(mouseEvent1);
		assertEquals(EditMode.INSERT, getEditMode());
		assertEquals(true, editor.hasSelectedNote());
		assertEquals(note1, playingNote);
		assertEquals(0, noteEventList.size());

		// マウスリリースによる挿入確定.
		editor.mouseReleased(mouseEvent1);
		assertEquals(EditMode.SELECT, getEditMode());
		assertEquals(false, editor.hasSelectedNote());
		assertEquals(1, noteEventList.size());
		assertEquals(note1, noteEventList.get(0).getNote());
		assertEquals(tickOffset1, noteEventList.get(0).getTickOffset());
		assertEquals(-1, playingNote);

		// 追加したノートをクリック.
		editor.mousePressed(mouseEvent1);
		assertEquals(EditMode.MOVE, getEditMode());
		assertEquals(true, editor.hasSelectedNote());
		assertEquals(note1, playingNote);
		assertEquals(tickOffset1, noteEventList.get(0).getTickOffset());

		// ノート移動.
		editor.mouseDragged(mouseEvent2);
		assertEquals(EditMode.MOVE, getEditMode());
		assertEquals(true, editor.hasSelectedNote());
		assertEquals(note2, playingNote);
		assertEquals(tickOffset2, noteEventList.get(0).getTickOffset());

		// マウスリリースによる挿入確定.
		editor.mouseReleased(mouseEvent1);
		assertEquals(EditMode.SELECT, getEditMode());
		assertEquals(true, editor.hasSelectedNote());
		assertEquals(1, noteEventList.size());
		assertEquals(note2, noteEventList.get(0).getNote());
		assertEquals(tickOffset2, noteEventList.get(0).getTickOffset());
		assertEquals(-1, playingNote);
	}

	/**
	 * ノートの端をつまんで tickを変更する.
	 * @throws Exception
	 */
	@Test
	public void test_lengthModify() throws Exception {
		List<MMLNoteEvent> noteEventList = mmlManager.getActiveMMLPart().getMMLNoteEventList();

		// 初期ノートをつまんで、1/4 -> 1/2 にする.
		int note = 80;
		MMLNoteEvent note1 = new MMLNoteEvent(note, 96, 0);
		MMLNoteEvent note2 = new MMLNoteEvent(note, 96*2, 0);
		noteEventList.add(new MMLNoteEvent(note, 96, 0));

		int y = pianoRollView.convertNote2Y(note);
		int x1 = pianoRollView.convertTicktoX(95);
		int x2 = pianoRollView.convertTicktoX(96*2-1);
		MouseEvent mouseEvent1 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON1_DOWN_MASK, x1, y, 1, false);
		MouseEvent mouseEvent2 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON1_DOWN_MASK, x2, y, 1, false);

		// マウスクリックでノートの長さ変更開始.
		assertEquals(note1.toString(), noteEventList.get(0).toString());
		assertEquals(EditMode.SELECT, getEditMode());
		editor.mousePressed(mouseEvent1);
		assertEquals(EditMode.LENGTH, getEditMode());
		assertEquals(true, editor.hasSelectedNote());
		assertEquals(note, playingNote);
		assertEquals(1, noteEventList.size());

		// 変更位置までドラッグ.
		editor.mouseDragged(mouseEvent2);
		assertEquals(EditMode.LENGTH, getEditMode());
		assertEquals(true, editor.hasSelectedNote());
		assertEquals(note, playingNote);
		assertEquals(1, noteEventList.size());

		// 変更確定.
		editor.mouseReleased(mouseEvent2);
		assertEquals(EditMode.SELECT, getEditMode());
		assertEquals(false, editor.hasSelectedNote());
		assertEquals(-1, playingNote);
		assertEquals(1, noteEventList.size());
		assertEquals(note2.toString(), noteEventList.get(0).toString());
	}

	/**
	 * 範囲選択.
	 * @throws Exception
	 */
	private void check_areaSelect() throws Exception {
		MMLEventList eventList = mmlManager.getActiveMMLPart();

		// note1iのノートだけを範囲選択してカット&ペースト, コピー&ペースト, delete.
		int note1i = 80;
		int note2i = 40;
		MMLNoteEvent note1 = new MMLNoteEvent(note1i, 96, 0);
		MMLNoteEvent note2 = new MMLNoteEvent(note2i, 96, 96);
		MMLNoteEvent note3 = new MMLNoteEvent(note1i, 96, 96+96);
		eventList.addMMLNoteEvent(note1);
		eventList.addMMLNoteEvent(note2);
		eventList.addMMLNoteEvent(note3);

		int y = pianoRollView.convertNote2Y(note1i);
		int x1 = pianoRollView.convertTicktoX(30);
		int x2 = pianoRollView.convertTicktoX(96+96+30);

		// 右クリック指定.
		MouseEvent mouseEvent1 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON3_DOWN_MASK, x1, y-1, 1, false);
		MouseEvent mouseEvent2 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON3_DOWN_MASK, x2, y+1, 1, false);

		// エリア右クリックによる選択開始.
		assertEquals(EditMode.SELECT, getEditMode());
		editor.mousePressed(mouseEvent1);
		assertEquals(EditMode.AREA, getEditMode());
		assertEquals(false, editor.hasSelectedNote()); // まだ選択できるノートはない.
		assertEquals(3, eventList.getMMLNoteEventList().size());

		// ドラッグによるエリア確定.
		editor.mouseDragged(mouseEvent2);
		assertEquals(EditMode.AREA, getEditMode());
		assertEquals(true, editor.hasSelectedNote()); // 選択状態になる.

		// マウスリリース.
		editor.mouseReleased(mouseEvent2);
		assertEquals(EditMode.SELECT, getEditMode());
		assertEquals(true, editor.hasSelectedNote());
	}

	/**
	 * 範囲選択後Cutする.
	 * @throws Exception
	 */
	@Test
	public void test_areaSelectCut() throws Exception {
		MMLEventList eventList = mmlManager.getActiveMMLPart();
		// 範囲選択.
		check_areaSelect();

		// カット&ペースト.
		assertEquals(">>g+4<<<e4>>>g+4", MMLBuilder.create(eventList).toMMLString());
		editor.selectedCut();
		assertEquals("r4<e4", MMLBuilder.create(eventList).toMMLString());
		editor.paste(96+96);
		assertEquals("r4<e4>>>g+4r4g+4", MMLBuilder.create(eventList).toMMLString());
	}

	/**
	 * 範囲選択後Copyする.
	 * @throws Exception
	 */
	@Test
	public void test_areaSelectCopy() throws Exception {
		MMLEventList eventList = mmlManager.getActiveMMLPart();
		// 範囲選択.
		check_areaSelect();

		// コピー&ペースト.
		assertEquals(">>g+4<<<e4>>>g+4", MMLBuilder.create(eventList).toMMLString());
		editor.selectedCopy();
		assertEquals(">>g+4<<<e4>>>g+4", MMLBuilder.create(eventList).toMMLString());
		editor.paste(96+96+96);
		assertEquals(">>g+4<<<e4>>>g+4g+4r4g+4", MMLBuilder.create(eventList).toMMLString());
	}

	/**
	 * 範囲選択後Deleteする.
	 * @throws Exception
	 */
	@Test
	public void test_areaSelectDelete() throws Exception {
		MMLEventList eventList = mmlManager.getActiveMMLPart();
		// 範囲選択.
		check_areaSelect();

		// delete.
		assertEquals(">>g+4<<<e4>>>g+4", MMLBuilder.create(eventList).toMMLString());
		editor.selectedDelete();
		assertEquals("r4<e4", MMLBuilder.create(eventList).toMMLString());
	}

	/**
	 * すべて選択する. 選択されたことはDeleteして消えたことで判定.
	 * @throws UndefinedTickException
	 */
	@Test
	public void test_selectAll() throws UndefinedTickException {
		mmlManager.setMMLselectedTrack(new MMLTrack().setMML("MML@ccc,ddd,eee"));
		editor.selectAll();
		editor.selectedDelete();
		int index = mmlManager.getActiveTrackIndex();
		assertEquals("MML@<>,ddd,eee;", mmlManager.getMMLScore().getTrack(index).getOriginalMML());
	}

	/**
	 * @param mml 入力MML
	 * @param expect 期待MML
	 * @param mode Changeするモード
	 * @param select 選択したノートを使う
	 * @throws Exception
	 */
	private void check_changePart(String mml, String expect, MMLEditor.ChangePartAction mode, boolean select) throws Exception {
		MMLTrack track = new MMLTrack().setMML(mml);
		mmlManager.setMMLselectedTrack(track);
		MMLEventList from = track.getMMLEventAtIndex(0);
		MMLEventList to = track.getMMLEventAtIndex(2);

		if (select) {
			// select
			MMLNoteEvent note = new MMLEventList("ccc").getMMLNoteEventList().get(1);

			int y = pianoRollView.convertNote2Y( note.getNote());
			int x = pianoRollView.convertTicktoX( note.getTickOffset() );
			MouseEvent e = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON1_DOWN_MASK, x+1, y+1, 1, false);

			editor.mousePressed(e);
			assertEquals(EditMode.MOVE, getEditMode());
			editor.mouseReleased(e);
			assertEquals(EditMode.SELECT, getEditMode());
		}

		editor.changePart(from, to, select, mode);
		mmlManager.updateActivePart(true);
		assertEquals(expect, track.getOriginalMML());
	}

	/**
	 * パートごとの交換.
	 * @throws Exception
	 */
	@Test
	public void test_changePartSwap() throws Exception {
		String mml =    "MML@ccc,ddd,eee;";
		String expect = "MML@eee,ddd,ccc;";
		check_changePart(mml, expect, MMLEditor.ChangePartAction.SWAP, false);
	}

	/**
	 * パートごとの移動.
	 * @throws Exception
	 */
	@Test
	public void test_changePartMove() throws Exception {
		String mml =    "MML@ccc,ddd,eee;";
		String expect = "MML@<>,ddd,ccc;";
		check_changePart(mml, expect, MMLEditor.ChangePartAction.MOVE, false);
	}

	/**
	 * パートごとのコピー
	 * @throws Exception
	 */
	@Test
	public void test_changePartCopy() throws Exception {
		String mml =    "MML@ccc,ddd,eee;";
		String expect = "MML@ccc,ddd,ccc;";
		check_changePart(mml, expect, MMLEditor.ChangePartAction.COPY, false);
	}

	/**
	 * 選択された部分の交換.
	 * @throws Exception
	 */
	@Test
	public void test_changePartSelectedSwap() throws Exception {
		String mml =    "MML@ccc,ddd,eee;";
		String expect = "MML@cec,ddd,ece;";
		check_changePart(mml, expect, MMLEditor.ChangePartAction.SWAP, true);
	}

	/**
	 * 選択された部分の移動.
	 * @throws Exception
	 */
	@Test
	public void test_changePartSelectedMove() throws Exception {
		String mml =    "MML@ccc,ddd,eee;";
		String expect = "MML@crc,ddd,ece;";
		check_changePart(mml, expect, MMLEditor.ChangePartAction.MOVE, true);
	}

	/**
	 * 選択された部分のコピー.
	 * @throws Exception
	 */
	@Test
	public void test_changePartSelectedCopy() throws Exception {
		String mml =    "MML@ccc,ddd,eee;";
		String expect = "MML@ccc,ddd,ece;";
		check_changePart(mml, expect, MMLEditor.ChangePartAction.COPY, true);
	}

	/**
	 * Shiftで複数選択してDelete.
	 */
	@Test
	public void test_shiftSelectAndDelete() {
		MMLTrack track = new MMLTrack().setMML("MML@ccddeeff;");
		mmlManager.setMMLselectedTrack(track);

		// select
		MMLNoteEvent note1 = track.getMMLEventAtIndex(0).getMMLNoteEventList().get(2);
		MMLNoteEvent note2 = track.getMMLEventAtIndex(0).getMMLNoteEventList().get(5);
		int y1 = pianoRollView.convertNote2Y( note1.getNote());
		int y2 = pianoRollView.convertNote2Y( note2.getNote());
		int x1 = pianoRollView.convertTicktoX( note1.getTickOffset() );
		int x2 = pianoRollView.convertTicktoX( note2.getTickOffset() );
		MouseEvent e1 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON1_DOWN_MASK, x1+1, y1+1, 1, false);
		MouseEvent e2 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, x2+1, y2+1, 1, false);

		editor.mousePressed(e1);
		editor.mouseReleased(e1);
		editor.mousePressed(e2);
		editor.mouseReleased(e2);
		editor.selectedDelete();

		assertEquals("MML@ccr1ff,,;", track.getOriginalMML());
	}

	/**
	 * クリック時に他のパートを選択する.
	 */
	@Test
	public void test_selectOtherPartMode() {
		MMLTrack track = new MMLTrack().setMML("MML@,ccc;");
		mmlManager.setMMLselectedTrack(track);
		MabiIccoProperties.getInstance().activePartSwitch.set(true);

		// select
		MMLNoteEvent note1 = track.getMMLEventAtIndex(1).getMMLNoteEventList().get(2);
		int y1 = pianoRollView.convertNote2Y( note1.getNote());
		int x1 = pianoRollView.convertTicktoX( note1.getTickOffset() );
		MouseEvent e1 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON1_DOWN_MASK, x1+1, y1+1, 1, false);

		// メロディーパート.
		assertTrue(track.getMMLEventAtIndex(0) == mmlManager.getActiveMMLPart());

		editor.mousePressed(e1);
		editor.mouseReleased(e1);

		// 和音パート.
		assertTrue(track.getMMLEventAtIndex(1) == mmlManager.getActiveMMLPart());
	}

	/**
	 * 右クリックして, 指定された処理を実行後のMMLをテストする.
	 * @param input
	 * @param expect
	 * @param selectIndex1 右クリックするノートindex
	 * @param selectIndex2 index1と違う値であれば範囲選択にする。ただし、Noteの高さは同じであること。
	 * @param r
	 */
	private void checkOneSelectActionButton3(String input, String expect, int selectIndex1, int selectIndex2,Runnable r) {
		MMLTrack track = new MMLTrack().setMML(input);
		mmlManager.setMMLselectedTrack(track);

		// select
		MMLNoteEvent note1 = track.getMMLEventAtIndex(0).getMMLNoteEventList().get(selectIndex1);
		int y1 = pianoRollView.convertNote2Y( note1.getNote() );
		int x1 = pianoRollView.convertTicktoX( note1.getTickOffset() );
		MouseEvent e1 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON3_DOWN_MASK, x1+1, y1+1, 1, false);

		MMLNoteEvent note2 = track.getMMLEventAtIndex(0).getMMLNoteEventList().get(selectIndex2);
		int y2 = pianoRollView.convertNote2Y( note2.getNote() );
		int x2 = pianoRollView.convertTicktoX( note2.getTickOffset() );
		MouseEvent e2 = new MouseEvent(pianoRollView, 0, 0, InputEvent.BUTTON3_DOWN_MASK, x2+1, y2-1, 1, false);

		if (selectIndex1 == selectIndex2) {
			editor.mousePressed(e1);
			editor.mouseReleased(e1);
		} else {
			editor.mousePressed(e2);
			editor.mouseDragged(e1);
			editor.mouseReleased(e1);
		}

		r.run();

		assertEquals(expect, track.getOriginalMML());
	}

	@Test
	public void test_selectPreviousAll() {
		checkOneSelectActionButton3("MML@aabebaa;", "MML@r1baa,,;", 3, 3, () -> {
			editor.selectPreviousAll();
			editor.selectedDelete();
		});
	}

	@Test
	public void test_selectAfterAll() {
		checkOneSelectActionButton3("MML@aabebaa;", "MML@aab,,;", 3, 3, () -> {
			editor.selectAfterAll();
			editor.selectedDelete();
		});
	}

	@Test
	public void test_remoteRestsBetweenNotes1() {
		// 連続音間の休符削除
		checkOneSelectActionButton3("MML@arara;", "MML@a2a2a,,;", 0, 2, () -> {
			boolean b = editor.hasSelectedMultipleConsecutiveNotes();
			assertEquals(true, b);
			editor.removeRestsBetweenNotes();
		});
	}

	@Test
	public void test_remoteRestsBetweenNotes2() {
		// 連続ではない
		checkOneSelectActionButton3("MML@ardra;", "MML@ardra,,;", 0, 2, () -> {
			boolean b = editor.hasSelectedMultipleConsecutiveNotes();
			assertEquals(false, b);
			editor.removeRestsBetweenNotes();
		});
	}

	@Test
	public void test_remoteRestsBetweenNotes3() {
		// 連続音間の休符削除2
		checkOneSelectActionButton3("MML@reerererereerere,,;", "MML@ree2e2e2e2ee2ere,,;", 1, 7, () -> {
			boolean b = editor.hasSelectedMultipleConsecutiveNotes();
			assertEquals(true, b);
			editor.removeRestsBetweenNotes();
		});
	}
}

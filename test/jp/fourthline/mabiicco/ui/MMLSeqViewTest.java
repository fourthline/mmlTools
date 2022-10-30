/*
 * Copyright (C) 2015-2022 たんらる
 */

package jp.fourthline.mabiicco.ui;


import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.midi.InstType;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.Marker;

public final class MMLSeqViewTest extends UseLoadingDLS {

	private static boolean viewMarker;
	@BeforeClass
	public static void initialize() {
		viewMarker = MabiIccoProperties.getInstance().enableViewMarker.get();
		MabiIccoProperties.getInstance().enableViewMarker.set(true);
	}

	@AfterClass
	public static void cleanup() {
		MabiIccoProperties.getInstance().enableViewMarker.set(viewMarker);
	}

	private MMLSeqView obj;

	@Before
	public void initilizeObj() {
		obj = new MMLSeqView(null, null);
	}

	@Before @After
	public void initMute() {
		for (int i = 0; i < MMLScore.MAX_TRACK; i++) {
			MabiDLS.getInstance().setMute(i, false);
		}
	}

	private Object getField(String fieldName) throws Exception {
		Field f = MMLSeqView.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(obj);
	}

	@Test
	public void testUndoRedo() {
		MMLScore score = new MMLScore();

		// rank9 のMML生成.
		StringBuilder sb = new StringBuilder("MML@");
		String rank9 = "Rank 9 ( 800, 0, 0 )";
		String rank1 = "Rank 1 ( 1200, 0, 0 )";
		Stream.iterate(0, i -> i++).limit(800).forEach(t -> sb.append('a'));
		score.addTrack(new MMLTrack().setMML( sb.toString() ));
		obj.setMMLScore(score);
		assertEquals(1, obj.getMMLScore().getTrackCount());

		// redo, undoは無効.
		assertEquals(false, obj.getFileState().canRedo());
		assertEquals(false, obj.getFileState().canUndo());
		assertEquals(false, obj.getFileState().isModified());

		// generate前は *Rank表記.
		assertEquals("*"+rank9, obj.getSelectedTrack().mmlRankFormat());
		obj.updateActivePart(false);

		// updateActivePart　で generate なし.
		assertEquals("*"+rank9, obj.getSelectedTrack().mmlRankFormat());

		// updateActivePart　で generate.
		obj.updateActivePart(true);
		assertEquals(rank9, obj.getSelectedTrack().mmlRankFormat());

		// redo, undoは無効.
		assertEquals(false, obj.getFileState().canRedo());
		assertEquals(false, obj.getFileState().canUndo());
		assertEquals(false, obj.getFileState().isModified());

		// Track追加.
		Stream.iterate(0, i -> i++).limit(400).forEach(t -> sb.append('a'));
		obj.addMMLTrack(new MMLTrack().setMML( sb.toString() ));
		assertEquals(2, obj.getMMLScore().getTrackCount());
		obj.updateActivePart(true);

		// undoは有効.
		assertEquals(false, obj.getFileState().canRedo());
		assertEquals(true, obj.getFileState().canUndo());
		assertEquals(true, obj.getFileState().isModified());

		// undo実行.
		obj.undo();
		assertEquals(true, obj.getFileState().canRedo());
		assertEquals(false, obj.getFileState().canUndo());
		assertEquals(false, obj.getFileState().isModified());
		assertEquals(1, obj.getMMLScore().getTrackCount());

		// Rank表記は generate後.
		assertEquals(rank9, obj.getSelectedTrack().mmlRankFormat());
		obj.updateActivePart(true);
		String recoveryData = obj.getRecoveryData();

		// redo実行.
		obj.redo();
		assertEquals(false, obj.getFileState().canRedo());
		assertEquals(true, obj.getFileState().canUndo());
		assertEquals(true, obj.getFileState().isModified());
		assertEquals(2, obj.getMMLScore().getTrackCount());

		// 次のTrackを選択.
		obj.switchTrack(true);
		// Rank表記は generate後.
		assertEquals(rank1, obj.getSelectedTrack().mmlRankFormat());

		// Rank9のデータへリカバリー.
		System.out.println(recoveryData);
		obj.recovery(recoveryData);
		System.out.println(obj.getRecoveryData());
		obj.updateActivePart(true);
		System.out.println(recoveryData);
		System.out.println(obj.getRecoveryData());
		assertEquals(1, obj.getMMLScore().getTrackCount());
		assertEquals(rank9, obj.getSelectedTrack().mmlRankFormat());
		assertEquals(recoveryData, obj.getRecoveryData());
	}

	@Test
	public void testAddRemoveTrack() {
		assertEquals(1, obj.getMMLScore().getTrackCount());

		// Track追加.
		obj.addMMLTrack(null);
		assertEquals(2, obj.getMMLScore().getTrackCount());
		assertEquals("Track2", obj.getSelectedTrack().getTrackName());

		// 21個ふやす.
		Stream.iterate(0, i -> i++).limit(21).forEach(t -> {
			obj.addMMLTrack(null);
		});
		assertEquals(15+8, obj.getMMLScore().getTrackCount());
		assertEquals("Track23", obj.getSelectedTrack().getTrackName());

		// 最大
		obj.addMMLTrack(null);
		assertEquals(24, obj.getMMLScore().getTrackCount());
		assertEquals("Track24", obj.getSelectedTrack().getTrackName());

		// 増えない.
		obj.addMMLTrack(null);
		assertEquals(24, obj.getMMLScore().getTrackCount());
		assertEquals("Track24", obj.getSelectedTrack().getTrackName());

		// 22個へらす.
		Stream.iterate(0, i -> i++).limit(22).forEach(t -> {
			obj.removeMMLTrack();
		});
		assertEquals(2, obj.getMMLScore().getTrackCount());
		assertEquals("Track2", obj.getSelectedTrack().getTrackName());

		// 最小
		obj.removeMMLTrack();
		assertEquals(1, obj.getMMLScore().getTrackCount());
		assertEquals("Track1", obj.getSelectedTrack().getTrackName());

		// へらない.
		obj.removeMMLTrack();
		assertEquals(1, obj.getMMLScore().getTrackCount());
		assertEquals("Track26", obj.getSelectedTrack().getTrackName());
	}

	@Test
	public void testMute() {
		assertEquals(1, obj.getMMLScore().getTrackCount());

		// Track追加.
		obj.addMMLTrack(null);
		obj.addMMLTrack(null);
		obj.addMMLTrack(null);
		assertEquals(4, obj.getMMLScore().getTrackCount());

		// Track3をMute設定.
		MabiDLS.getInstance().toggleMute(2);
		assertEquals(false, MabiDLS.getInstance().getMute(0));
		assertEquals(false, MabiDLS.getInstance().getMute(1));
		assertEquals(true,  MabiDLS.getInstance().getMute(2));
		assertEquals(false, MabiDLS.getInstance().getMute(3));

		// Track2を選択.
		obj.switchTrack(false);
		obj.switchTrack(false);
		assertEquals("Track2", obj.getSelectedTrack().getTrackName());

		// Track削除.
		obj.removeMMLTrack();

		// Track2がMute, Track3はMute解除.
		assertEquals(false, MabiDLS.getInstance().getMute(0));
		assertEquals(true,  MabiDLS.getInstance().getMute(1));
		assertEquals(false, MabiDLS.getInstance().getMute(2));
		assertEquals(false, MabiDLS.getInstance().getMute(3));

		// 新規TrackはMute解除.
		MabiDLS.getInstance().toggleMute(3);
		assertEquals(true, MabiDLS.getInstance().getMute(3));
		obj.addMMLTrack(null);
		assertEquals(false, MabiDLS.getInstance().getMute(3));
	}

	@Test
	public void test_setMMLselectedTrack() {
		MMLScore score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@ct150cc"));
		obj.setMMLScore(score);
		assertEquals(1, obj.getMMLScore().getTrackCount());
		assertEquals(150, obj.getMMLScore().getTempoOnTick(96));

		// t150テンポがクリアされてることを確認する.
		obj.setMMLselectedTrack(new MMLTrack().setMML("MML@t130ccc"));
		assertEquals(1, obj.getMMLScore().getTrackCount());
		assertEquals(130, obj.getMMLScore().getTempoOnTick(96));
	}

	private void check_nextStepTime(MMLSeqView obj, int time) {
		int tick = 96 * time;
		assertEquals(0, obj.getEditSequencePosition());
		obj.nextStepTimeTo(true);
		assertEquals(tick, obj.getEditSequencePosition());
		obj.nextStepTimeTo(true);

		// 右側はOver許容.
		assertEquals(tick*2, obj.getEditSequencePosition());
		obj.nextStepTimeTo(false);
		assertEquals(tick, obj.getEditSequencePosition());
		obj.nextStepTimeTo(false);
		assertEquals(0, obj.getEditSequencePosition());

		// 0が最小.
		obj.nextStepTimeTo(false);
		assertEquals(0, obj.getEditSequencePosition());
	}

	@Test
	public void test_nextStepTimeTo() {
		// 4/4
		obj.setMMLselectedTrack(new MMLTrack().setMML("MML@c1"));
		check_nextStepTime(obj, 4);

		// 3/4
		obj.getMMLScore().setTimeCountOnly(3);
		check_nextStepTime(obj, 3);
	}

	@Test
	public void test_addRemoveTicks() {
		obj.setMMLselectedTrack(new MMLTrack().setMML("MML@cccc,dddd,eeee,ffff;"));
		obj.addMMLTrack(new MMLTrack().setMML("MML@gggg,aaaa,bbbb,>cccc;"));
		assertEquals(96*4, obj.getMMLScore().getTotalTickLength());

		// 2拍目に1拍追加する.
		obj.getMMLScore().setTimeCountOnly(2);
		obj.nextStepTimeTo(true);
		obj.addTicks(false);
		assertEquals("MML@ccrcc,ddrdd,eeree,ffrff;", obj.getMMLScore().getTrack(0).getOriginalMML());
		assertEquals("MML@ggrgg,aaraa,bbrbb,>ccrcc;", obj.getMMLScore().getTrack(1).getOriginalMML());
		assertEquals(96*5, obj.getMMLScore().getTotalTickLength());

		// 先頭の1拍を削除する.
		obj.nextStepTimeTo(false);
		obj.removeTicks(false);
		assertEquals("MML@crcc,drdd,eree,frff;", obj.getMMLScore().getTrack(0).getOriginalMML());
		assertEquals("MML@grgg,araa,brbb,>crcc;", obj.getMMLScore().getTrack(1).getOriginalMML());
		assertEquals(96*4, obj.getMMLScore().getTotalTickLength());
	}

	private void checkImage(PianoRollView view, String filename) throws Exception {
		JViewport viewport = new JViewport();
		int width = view.convertTicktoX( obj.getMMLScore().getTotalTickLength() );
		int height = view.getTotalHeight();
		viewport.setExtentSize(new Dimension(width, height));

		// PianoRollView画像の作成.
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		view.setSize(width, height);
		view.setViewportAndParent(viewport, obj);
		view.paintComponent(image.getGraphics());
		ImageIO.write(image, "png", new File(filename+"_pianoRoll.png"));

		// ColumnPanel画像の作成.
		ColumnPanel columnView = (ColumnPanel) getField("columnView");
		BufferedImage image2 = new BufferedImage(width, columnView.getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);
		columnView.setSize(width, columnView.getPreferredSize().height);
		columnView.paintComponent(image2.getGraphics());
		ImageIO.write(image2, "png", new File(filename+"_column.png"));

		// KeyboardView画像の作成.
		KeyboardView keyboardView = (KeyboardView) getField("keyboardView");
		keyboardView.playNote(80, 11);
		BufferedImage image3 = new BufferedImage(keyboardView.getPreferredSize().width, height, BufferedImage.TYPE_INT_ARGB);
		keyboardView.paintComponent(image3.getGraphics());
		keyboardView.offNote();
		ImageIO.write(image3, "png", new File(filename+"_keyboard.png"));
	}

	@Test
	public void test_pianoRollView() throws Exception {
		PianoRollView view = (PianoRollView) getField("pianoRollView");
		obj.setMMLselectedTrack(new MMLTrack().setMML("MML@t160l1cccc,l1dddd,l1eeee,l1ffff;"));
		obj.addMMLTrack(new MMLTrack().setMML("MML@>l1cccc,>l1dddd,>l1eeee,>l1ffff;"));
		obj.addMMLTrack(new MMLTrack().setMML("MML@<l1cccc,<l1dddd,<l1eeee,<l1ffff;"));

		obj.getMMLScore().getMarkerList().add(new Marker("Marker", 96*3));

		// 拡大
		assertEquals(6.0, view.getWideScale(), 0.001);
		Stream.of(6.0, 5.0, 4.0, 3.0, 2.0, 1.5, 1.0, 0.75, 0.5, 0.375, 0.25, 0.1) .forEach(t -> {
			assertEquals(t.doubleValue(), view.getWideScale(), 0.001);
			obj.getPianoRollScaler().expandPianoViewWide(0);
		});
		assertEquals(0.1, view.getWideScale(), 0.001);
		checkImage(view, "sample1");

		// 縮小
		Stream.of(0.1, 0.25, 0.375, 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0) .forEach(t -> {
			assertEquals(t.doubleValue(), view.getWideScale(), 0.001);
			obj.getPianoRollScaler().reducePianoViewWide(0);
		});
		assertEquals(6.0, view.getWideScale(), 0.001);
		checkImage(view, "sample2");
	}

	/**
	 * 編集できず REVERTする.
	 */
	@Test
	public void test_editRevert() {
		obj.getActiveMMLPart().addMMLNoteEvent(new MMLNoteEvent(10, 1, 0));
		assertFalse(obj.getFileState().isModified());
		assertEquals(1, obj.getActiveMMLPart().getMMLNoteEventList().size());
		obj.updateActivePart(true);
		assertEquals(0, obj.getActiveMMLPart().getMMLNoteEventList().size());
		assertFalse(obj.getFileState().isModified());
	}

	/**
	 * 楽器の変更.
	 */
	@Test
	public void test_updateActiveTrackProgram() {
		assertFalse(obj.getFileState().isModified());
		obj.updateActiveTrackProgram(obj.getActiveTrackIndex(), obj.getActivePartProgram()+1, 0);
		assertTrue(obj.getFileState().isModified());
	}

	/**
	 * 任意のfuncを実行したあとのActiveTrackを検査する.
	 */
	private class TP1 {
		private final Runnable func;
		private final int track;
		private final int part;
		/**
		 * @param func 実行する内容.
		 * @param track funcを実行したあとに期待するTrackのIndex.
		 * @param part funcを実行したあとに期待するPartのIndex.
		 */
		private TP1(Runnable func, int track, int part) {
			this.func = func;
			this.track = track;
			this.part = part;
		}
		private void check() {
			func.run();
			MMLEventList expect = obj.getMMLScore().getTrack(track).getMMLEventAtIndex(part);
			MMLEventList actual = obj.getActiveMMLPart();
			System.out.println(track+" "+part+" "+expect+" <=> "+actual);
			assertSame( expect, actual );
		}
	}

	@Test
	public void test_switchTrack() {
		obj.setMMLselectedTrack(new MMLTrack().setMML("MML@c"));
		obj.addMMLTrack(new MMLTrack().setMML("MML@d"));
		obj.addMMLTrack(new MMLTrack().setMML("MML@e"));
		Runnable toNext = () -> obj.switchTrack(true);
		Runnable toPrev = () -> obj.switchTrack(false);

		Stream.of(
				new TP1(toNext, 0, 0),
				new TP1(toPrev, 2, 0),
				new TP1(toPrev, 1, 0),
				new TP1(toPrev, 0, 0),
				new TP1(toNext, 1, 0),
				new TP1(toNext, 2, 0),
				new TP1(toNext, 0, 0)).forEach(t -> t.check());
	}

	@Test
	public void test_switchMMLPart() {
		obj.setMMLselectedTrack(new MMLTrack().setMML("MML@a,b,c,d"));
		obj.addMMLTrack(null);
		obj.addMMLTrack(new MMLTrack().setMML("MML@e,f,g,a"));
		Runnable toNext = () -> obj.switchMMLPart(true);
		Runnable toPrev = () -> obj.switchMMLPart(false);

		Stream.of(
				new TP1(toNext, 2, 1),
				new TP1(toNext, 2, 2),
				new TP1(toNext, 2, 0),
				new TP1(toPrev, 2, 2),
				new TP1(toPrev, 2, 1),
				new TP1(toPrev, 2, 0)).forEach(t -> t.check());

		// コーラスパート有効.
		obj.updateActiveTrackProgram(obj.getActiveTrackIndex(), 1, 100);
		obj.updateActivePart(true);
		Stream.of(
				new TP1(toNext, 2, 1),
				new TP1(toNext, 2, 2),
				new TP1(toNext, 2, 3),
				new TP1(toNext, 2, 0)).forEach(t -> t.check());

		// 打楽器 & コーラス
		obj.updateActiveTrackProgram(obj.getActiveTrackIndex(), 66, 100);
		obj.updateActivePart(true);
		Stream.of(
				new TP1(toNext, 2, 3),
				new TP1(toNext, 2, 0),
				new TP1(toNext, 2, 3),
				new TP1(toNext, 2, 0),
				new TP1(toPrev, 2, 3),
				new TP1(toPrev, 2, 0),
				new TP1(toPrev, 2, 3),
				new TP1(toPrev, 2, 0)).forEach(t -> t.check());

		// 打楽器
		obj.updateActiveTrackProgram(obj.getActiveTrackIndex(), 66, MMLTrack.NO_CHORUS);
		obj.updateActivePart(true);
		Stream.of(
				new TP1(toNext, 2, 0),
				new TP1(toPrev, 2, 0)).forEach(t -> t.check());

		// 歌
		obj.updateActiveTrackProgram(obj.getActiveTrackIndex(), 120, MMLTrack.NO_CHORUS);
		obj.updateActivePart(true);
		Stream.of(
				new TP1(toNext, 2, 3),
				new TP1(toPrev, 2, 3)).forEach(t -> t.check());
	}

	@Test
	public void test_moveTrack1() {
		MMLTrack track1 = new MMLTrack().setMML("MML@c");
		MMLTrack track2 = new MMLTrack().setMML("MML@d");
		MMLTrack track3 = new MMLTrack().setMML("MML@e");
		obj.setMMLselectedTrack(track1);
		obj.addMMLTrack(track2);
		obj.addMMLTrack(track3);

		MabiDLS dls = MabiDLS.getInstance();
		dls.setMute(1, true);

		assertEquals(false, dls.getMute(0));
		assertEquals(true, dls.getMute(1));
		assertEquals(false, dls.getMute(2));

		obj.moveTrack(0); // 2 - 0

		assertSame(track3, obj.getMMLScore().getTrack(0));
		assertSame(track1, obj.getMMLScore().getTrack(1));
		assertSame(track2, obj.getMMLScore().getTrack(2));
		assertEquals(false, dls.getMute(0));
		assertEquals(false, dls.getMute(1));
		assertEquals(true, dls.getMute(2));

		obj.moveTrack(2); // 0 - 2

		assertSame(track1, obj.getMMLScore().getTrack(0));
		assertSame(track2, obj.getMMLScore().getTrack(1));
		assertSame(track3, obj.getMMLScore().getTrack(2));
		assertEquals(false, dls.getMute(0));
		assertEquals(true, dls.getMute(1));
		assertEquals(false, dls.getMute(2));
	}


	@Test
	public void test_changeInst() throws Exception {
		MMLTrack track1 = createMMLTrack(1200, 400, 400, 1200, false);
		MMLScore score = new MMLScore();
		score.addTrack(track1);
		obj.setMMLScore(score);

		JTabbedPane tabbedPane = (JTabbedPane) getField("tabbedPane");
		MMLTrackView view = (MMLTrackView) tabbedPane.getComponentAt(0);

		assertEquals(false, obj.getFileState().canUndo());
		assertEquals(false, obj.getFileState().canRedo());
		assertEquals(false, obj.getSelectedTrack().isExcludeSongPart());
		assertEquals("*Rank 1 ( 1200, 400, 400, 1200 )", view.getRankText());

		// 歌パートを除外する
		view.getSongComboBox().setSelectedIndex(3);
		assertEquals(true, obj.getSelectedTrack().isExcludeSongPart());
		assertEquals("*Rank 7` ( 1200, 400, 400 )", view.getRankText());

		// 歌の音源に変更する
		view.getComboBox().setSelectedIndex(39);
		assertEquals(InstType.VOICE, MabiDLS.getInstance().getInstByProgram(obj.getSelectedTrack().getProgram()).getType());
		assertEquals(false, obj.getSelectedTrack().isExcludeSongPart());
		assertEquals("*Rank 1 ( 1200, 400, 400, 1200 )", view.getRankText());

		assertEquals(true, obj.getFileState().canUndo());
		assertEquals(false, obj.getFileState().canRedo());

		// undo
		obj.undo();
		assertEquals(0, obj.getMMLScore().getTrack(0).getProgram());
		assertEquals(-2, obj.getMMLScore().getTrack(0).getSongProgram());
		assertEquals(true, obj.getFileState().canUndo());
		assertEquals(true, obj.getFileState().canRedo());
		assertEquals(true, obj.getSelectedTrack().isExcludeSongPart());
		assertEquals("Rank 7` ( 1200, 400, 400 )", view.getRankText());

		// undo
		obj.undo();
		assertEquals(0, obj.getMMLScore().getTrack(0).getProgram());
		assertEquals(-1, obj.getMMLScore().getTrack(0).getSongProgram());
		assertEquals(false, obj.getFileState().canUndo());
		assertEquals(true, obj.getFileState().canRedo());
		assertEquals(false, obj.getSelectedTrack().isExcludeSongPart());
		assertEquals("Rank 1 ( 1200, 400, 400, 1200 )", view.getRankText());
	}
}

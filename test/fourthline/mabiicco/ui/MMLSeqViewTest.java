/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.ui;


import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

public class MMLSeqViewTest {

	@BeforeClass
	public static void initialize() {
		MabiDLS midi = MabiDLS.getInstance();
		try {
			midi.initializeMIDI();
			midi.loadingDLSFile(new File(MabiDLS.DEFALUT_DLS_PATH));
		} catch (InvalidMidiDataException | IOException | MidiUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		MMLSeqView obj = new MMLSeqView();
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

		// Track追加.
		Stream.iterate(0, i -> i++).limit(400).forEach(t -> sb.append('a'));
		obj.addMMLTrack(new MMLTrack().setMML( sb.toString() ));
		assertEquals(2, obj.getMMLScore().getTrackCount());
		obj.updateActivePart(true);

		// undoは有効.
		assertEquals(false, obj.getFileState().canRedo());
		assertEquals(true, obj.getFileState().canUndo());

		// undo実行.
		obj.undo();
		assertEquals(true, obj.getFileState().canRedo());
		assertEquals(false, obj.getFileState().canUndo());

		// Rank表記は generate後.
		assertEquals(rank9, obj.getSelectedTrack().mmlRankFormat());

		// redo実行.
		obj.redo();
		assertEquals(false, obj.getFileState().canRedo());
		assertEquals(true, obj.getFileState().canUndo());

		// 次のTrackを選択.
		obj.switchTrack(true);
		// Rank表記は generate後.
		assertEquals(rank1, obj.getSelectedTrack().mmlRankFormat());
	}
}

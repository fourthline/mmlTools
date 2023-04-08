/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.MMLSeqView;
import jp.fourthline.mmlTools.MMLBuilder;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.UndefinedTickException;

public class MMLTextEditorTest extends UseLoadingDLS {

	private MMLSeqView obj;

	@Before
	public void setup() {
		MMLBuilder.setMMLVZeroTempo(false);
		MMLScore.setMMLFix64(true);
	}

	@Before
	public void initilizeObj() {
		obj = new MMLSeqView(null);
	}

	@Test
	public void test_edit() throws UndefinedTickException {
		obj.addMMLTrack(new MMLTrack().setMML("MML@rt150cdc;"));
		obj.addMMLTrack(new MMLTrack());
		obj.updateActivePart(true);
		assertEquals(3, obj.getMMLScore().getTrackCount());

		var editor = new MMLTextEditor(null, obj, null);
		assertEquals("MML@rt150cdct120,,;", obj.getMMLScore().getTrack(1).getMabiMML());
		editor.setMML("t100");
		editor.applyAction();

		assertEquals("MML@t100rt150cdct100,,;", obj.getMMLScore().getTrack(1).getMabiMML());

		assertEquals(obj.getMMLScore().getTrack(0).getGlobalTempoList(), obj.getMMLScore().getTrack(1).getGlobalTempoList());
		assertEquals(obj.getMMLScore().getTrack(0).getGlobalTempoList(), obj.getMMLScore().getTrack(2).getGlobalTempoList());
	}

	@Test
	public void test_cancel() throws UndefinedTickException {
		obj.addMMLTrack(new MMLTrack().setMML("MML@rt150cdc;"));
		obj.addMMLTrack(new MMLTrack());
		obj.updateActivePart(true);
		assertEquals(3, obj.getMMLScore().getTrackCount());

		var editor = new MMLTextEditor(null, obj, null);
		assertEquals("MML@rt150cdct120,,;", obj.getMMLScore().getTrack(1).getMabiMML());
		editor.setMML("t100");
		editor.cancelAction();

		assertEquals("MML@rt150cdct120,,;", obj.getMMLScore().getTrack(1).getMabiMML());

		assertEquals(obj.getMMLScore().getTrack(0).getGlobalTempoList(), obj.getMMLScore().getTrack(1).getGlobalTempoList());
		assertEquals(obj.getMMLScore().getTrack(0).getGlobalTempoList(), obj.getMMLScore().getTrack(2).getGlobalTempoList());
	}
}

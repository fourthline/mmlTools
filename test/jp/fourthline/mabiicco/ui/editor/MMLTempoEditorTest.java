/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.MMLSeqView;
import jp.fourthline.mmlTools.MMLTempoEvent;
import jp.fourthline.mmlTools.MMLTrack;

public final class MMLTempoEditorTest extends UseLoadingDLS {

	private final IMMLManager mmlManager = new MMLSeqView(null, null);
	private final MMLTempoEditor editor = new MMLTempoEditor(null, mmlManager, null, null);

	@Test
	public void test_tempoConvert_01() {
		MMLTrack track1 = new MMLTrack().setMML("MML@t120cccccccc,c2c2c2c2c2c2c2c2;");
		mmlManager.addMMLTrack(track1);
		editor.updateTempoList(List.of(new MMLTempoEvent(240, 0)), 0, false, true, true);
		mmlManager.updateActivePart(true);
		assertEquals("MML@t240l2cccccccc,l1cccccccc,;", track1.getOriginalMML());
		editor.updateTempoList(List.of(new MMLTempoEvent(240, 0), new MMLTempoEvent(60, 384)), 384, false, true, true);
		mmlManager.updateActivePart(true);
		assertEquals("MML@t240l2cct60l8cccccc,c1ccccccc,;", track1.getOriginalMML());
	}

	@Test
	public void test_tempoConvert_02() {
		MMLTrack track1 = new MMLTrack().setMML("MML@t99a16b9t88c7c2c1.,d4.e8.f32.,l64aaaaaaaaaaaaaaaa");
		mmlManager.addMMLTrack(track1);
		editor.updateTempoList(List.of(new MMLTempoEvent(200, 0)), 0, false, true, true);
		mmlManager.updateActivePart(true);
		assertEquals("MML@t200a8b6&b18c&c14l1c&c9&c38c.&c.&c4.&c27,d2&d7.&d10e.&e20f16&f22,l32aaaaa29aaaaaal27aaa29aa;", track1.getOriginalMML());
	}

	@Test
	public void test_tempoConvert_03() {
		// 連続する同じテンポを変換ありで削除する
		MMLTrack track1 = new MMLTrack().setMML("MML@r7r16l64cv0c16.t85rv8cr4r9cr9cr4r16c+r4v0c8.t85r16v8<g+r4r16g+r16g+r4r16.ar19.ar8v0c16.t85r8r9v8a,,;");
		mmlManager.addMMLTrack(track1);
		List<MMLTempoEvent> list = new ArrayList<>(mmlManager.getMMLScore().getTempoEventList());
		list.remove(1);
		editor.updateTempoList(list, 0, false, true, false);
		mmlManager.updateActivePart(true);
		assertEquals("MML@r7r16l64cv0c16.t85rv8cr4r9cr9cr4r16c+r4v0c8.r16v8<g+r4r16g+r16g+r4r16.ar19.ar8v0c16.t85r8r9v8a,,;", track1.getOriginalMML());
	}
}

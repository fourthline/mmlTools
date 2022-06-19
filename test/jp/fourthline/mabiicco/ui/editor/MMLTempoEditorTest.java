/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.MMLSeqView;
import jp.fourthline.mmlTools.MMLTrack;

public final class MMLTempoEditorTest extends UseLoadingDLS {

	private final IMMLManager mmlManager = new MMLSeqView(null, null);
	private final MMLTempoEditor editor = new MMLTempoEditor(null, mmlManager, null, null);

	@Test
	public void test_tempoConvert_01() {
		MMLTrack track1 = new MMLTrack().setMML("MML@t120cccccccc,c2c2c2c2c2c2c2c2;");
		mmlManager.addMMLTrack(track1);
		editor.tempoConvert(240, 0);
		mmlManager.updateActivePart(true);
		assertEquals("MML@t240l2cccccccc,l1cccccccc,;", track1.getOriginalMML());
		editor.tempoConvert(60, 384);
		mmlManager.updateActivePart(true);
		assertEquals("MML@t240l2cct60l8cccccc,c1ccccccc,;", track1.getOriginalMML());
	}

	@Test
	public void test_tempoConvert_02() {
		MMLTrack track1 = new MMLTrack().setMML("MML@t99a16b9t88c7c2c1.,d4.e8.f32.,l64aaaaaaaaaaaaaaaa");
		mmlManager.addMMLTrack(track1);
		editor.tempoConvert(200, 0);
		mmlManager.updateActivePart(true);
		assertEquals("MML@t200a8b6&b18c&c14l1c&c9&c38c.&c&c2&c5&c7.,d2.&d17e3l16&e.f&f22,l32aaaaaaa29aaaal27aa29aaa29;", track1.getOriginalMML());
	}
}

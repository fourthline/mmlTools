/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import java.util.function.Supplier;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.mml.MMLManagerStub;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLScoreSerializer;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.MMLVerifyException;

public final class DrumConverterTest extends UseLoadingDLS {

	@Test
	public final void test() throws MMLExceptionList, MMLVerifyException {
		var mmlManager = new MMLManagerStub();
		var mmlScore = mmlManager.getMMLScore();
		var track = new MMLTrack().setMML("MML@o0cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+aa+b;");
		mmlScore.addTrack(track);

		String mml1 = track.getOriginalMML();
		String mml2 = "MML@<dddddddddddddddddddddddddddddddddddn11<fb+an15b<fn45g>>b<g>gn23eb+f>edff+gg+aa+b>cc+dd+eff+gg+aa+b>cc+dd+eff+gg+ao3dddddddddddddddddddddddddd,,;";

		Supplier<RangeMode> source = () -> RangeMode.ALL_TRACK;

		// 楽器がドラムではないのでなにもしないパターン.
		assertFalse(DrumConverter.isDrumTrack(track));
		DrumConverter.getInstance().midDrum2MabiDrum(source, mmlManager);
		track.generate();

		assertEquals(mml1, track.getOriginalMML());

		// 楽器をドラムに変更して実行, インポートしたデータでないと変換しない.
		track.setProgram(27);
		assertFalse(DrumConverter.isDrumTrack(track));

		// 現在のデータをインポートしたデータとして設定
		track.setImportedData(MMLScoreSerializer.toStringImportedData(track.getMMLEventList()));
		assertTrue(DrumConverter.isDrumTrack(track));
		DrumConverter.getInstance().midDrum2MabiDrum(source, mmlManager);
		track.generate();

		assertEquals(mml2, track.getOriginalMML());
	}
}

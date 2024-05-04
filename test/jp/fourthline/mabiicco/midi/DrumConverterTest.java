package jp.fourthline.mabiicco.midi;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.mml.MMLManagerStub;
import jp.fourthline.mmlTools.MMLExceptionList;
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

		// 楽器がドラムではないのでなにもしないパターン.
		assertFalse(DrumConverter.isDrumTrack(track));
		DrumConverter.midDrum2MabiDrum(mmlManager);
		track.generate();

		assertEquals(mml1, track.getOriginalMML());

		// 楽器をドラムに変更して実行.
		track.setProgram(27);
		assertTrue(DrumConverter.isDrumTrack(track));
		DrumConverter.midDrum2MabiDrum(mmlManager);
		track.generate();

		assertEquals(mml2, track.getOriginalMML());
	}
}

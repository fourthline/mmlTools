/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import org.junit.Test;

import fourthline.mmlTools.core.MMLTools;

/**
 * @author fourthline
 *
 */
public class MMLTrackTest {

	/**
	 * Test method for {@link fourthline.mmlTools.MMLTrack#getMMLStrings()}.
	 */
	@Test
	public void testGetMMLStrings() {
		MMLTrack track = new MMLTrack("MML@aaa,bbb,ccc,ddd;");
		String expect[] = {
				"a8t150&a8aa", // melodyパートのみテンポ指定.
				"b8&b8bb",
				"c8&c8cc",
				"d8t150&d8dd"
		};
		new MMLTempoEvent(150, 48).appendToListElement(track.getGlobalTempoList());
		String mml[] = track.getMMLStrings();

		assertArrayEquals(expect, mml);
	}


	private void checkPlayTimeAndMabinogiTime(String mml) {
		MMLTrack track = new MMLTrack(mml);
		MMLTools tools = new MMLTools(mml);
		try {
			tools.parseMMLforMabinogi();
			tools.parsePlayMode(false);
			double expectPlayTime = tools.getPlayTime();
			double expectMabinogiTime = tools.getMabinogiTime();
			System.out.printf("playTime: %f, mabinogiTime: %f\n", expectPlayTime, expectMabinogiTime);

			assertEquals(expectPlayTime, track.getPlayTime(), 0.001);
			assertEquals(expectMabinogiTime, track.getMabinogiTime(), 0.001);
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
	}

	/**
	 * test aaaa
	 * @throws Exception
	 */
	@Test
	public void testTime_0() throws Exception {
		String mml = "MML@aaaa;";

		checkPlayTimeAndMabinogiTime(mml);
	}

	/**
	 * テンポ変速 途中で切れるタイプ
	 */
	@Test
	public void testPlayingShort() throws Exception {
		String mml = "MML@t90cccccccccccct150cccc,eeeeeeeeeeeeeeeedddd,;";

		checkPlayTimeAndMabinogiTime(mml);
	}

	/**
	 * テンポ変速　終わらないタイプ
	 */
	@Test
	public void testPlayingLong() throws Exception {
		String mml = "MML@t150cccccccccccct90cccc,eeeeeeeeeeeeeeee,;";

		checkPlayTimeAndMabinogiTime(mml);
	}

	/**
	 * テンポ変速　終わらないタイプ
	 */
	@Test
	public void testPlayingLong2() throws Exception {
		String mml = "MML@t150cccccccccccct90c,eeeeeeeeeeeeeeee;";

		checkPlayTimeAndMabinogiTime(mml);
	}

	/**
	 * テンポ変速 途中で切れるタイプのMML補正
	 */
	@Test
	public void testPlayingShortMML() throws Exception {
		String mml       = "MML@t90cccccccccccct150cccc,eeeeeeeeeeeeeeeedddd,;";
		String expectMML = "MML@t90cccccccccccct150ccccr1,eeeeeeeeeeeeeeeedddd,;";

		MMLTrack track = new MMLTrack(mml);
		assertEquals(expectMML, track.getMMLString());
	}

	/**
	 * テンポ変速　終わらないタイプのMML補正
	 */
	@Test
	public void testPlayingLongMML() throws Exception {
		String mml =       "MML@t150cccccccccccct90cccc,eeeeeeeeeeeeeeee,;";
		String expectMML = "MML@t150cccccccccccct90cccct150,eeeeeeeeeeeeeeee,;";

		MMLTrack track = new MMLTrack(mml);
		assertEquals(expectMML, track.getMMLString());
	}

	/**
	 * テンポ変速　終わらないタイプのMML補正
	 */
	@Test
	public void testPlayingLong2MML() throws Exception {
		String mml =       "MML@t150cccccccccccct90c,eeeeeeeeeeeeeeee;";
		String expectMML = "MML@t150cccccccccccct90cr2.v0c64t150,eeeeeeeeeeeeeeee,;";

		MMLTrack track = new MMLTrack(mml);
		assertEquals(expectMML, track.getMMLString());
	}

	/**
	 * テンポを跨ぐ場合の分割Tick-Undefined（最小Tick近似）
	 */
	@Test
	public void testGeneric00() {
		String mml =       "MML@ggt150gg,rr8r16.a24aa;";
		String expectMML = "MML@ggt150ggr64,rr8r16.a32&a64aa,;";

		MMLTrack track = new MMLTrack(mml);
		assertEquals(expectMML, track.getMMLString());
	}

	/**
	 * メロディが短い場合の、後続複数のテンポ指定
	 */
	@Test
	public void testTempo_all0() throws Exception {
		String mml =        "MML@,t150c1c1t120c1c1t130c1c1;";
		String expectMML1 = "MML@t150l1rrt120rrt130,l1cccccc,;";
		String expectMML2 = "MML@t150v0l1cct120cct130rrv0c64t150,l1cccccc,;";

		MMLTrack track = new MMLTrack(mml);
		assertEquals(expectMML1, track.getMMLString(false, false));
		assertEquals(expectMML2, track.getMMLString());
	}
}

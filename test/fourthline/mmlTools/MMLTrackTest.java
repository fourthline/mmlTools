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
				"d8&d8dd" // TODO: 歌パートはどうなるんだろ？
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

			assertEquals(expectPlayTime, track.getPlayTime(), 0.00001);
			assertEquals(expectMabinogiTime, track.getMabinogiTime(), 0.00001);
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
}

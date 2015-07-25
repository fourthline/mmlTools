/*
 * Copyright (C) 2013-2015 たんらる
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
	 * @throws UndefinedTickException
	 */
	@Test
	public void testGetMMLStrings() throws UndefinedTickException {
		MMLTrack track = new MMLTrack().setMML("MML@aaa,bbb,ccc,ddd;");
		String expect[] = {
				"a8t150v0a8v8aa", // melodyパートのみテンポ指定.
				"bbb",
				"ccc",
				"d8t150v0d8v8dd"
		};
		new MMLTempoEvent(150, 48).appendToListElement(track.getGlobalTempoList());
		String mml[] = track.generate().getMabiMMLArray();

		assertArrayEquals(expect, mml);
	}


	private void checkPlayTimeAndMabinogiTime(String mml) {
		MMLTrack track = new MMLTrack().setMML(mml);
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

		MMLTrack track = new MMLTrack().setMML(mml);
		assertEquals(expectMML, track.generate().getMabiMML());
	}

	/**
	 * テンポ変速　終わらないタイプのMML補正
	 */
	@Test
	public void testPlayingLongMML() throws Exception {
		String mml =       "MML@t150cccccccccccct90cccc,eeeeeeeeeeeeeeee,;";
		String expectMML = "MML@t150cccccccccccct90cccct150,eeeeeeeeeeeeeeee,;";

		MMLTrack track = new MMLTrack().setMML(mml);
		assertEquals(expectMML, track.generate().getMabiMML());
	}

	/**
	 * テンポ変速　終わらないタイプのMML補正
	 */
	@Test
	public void testPlayingLong2MML() throws Exception {
		String mml =       "MML@t150cccccccccccct90c,eeeeeeeeeeeeeeee;";
		String expectMML = "MML@t150cccccccccccct90cr2.v0c64t150,eeeeeeeeeeeeeeee,;";

		MMLTrack track = new MMLTrack().setMML(mml);
		assertEquals(expectMML, track.generate().getMabiMML());
	}

	/**
	 * テンポを跨ぐ場合の分割Tick-Undefined（最小Tick近似） = Exception
	 * @throws UndefinedTickException
	 */
	@Test(expected=UndefinedTickException.class)
	public void testGeneric00() throws UndefinedTickException {
		String mml =       "MML@ggt150gg,rr8r16.a24aa;";
		String expectMML = "MML@ggt150ggr64,rr8r16.a32&a64aa,;";

		MMLTrack track = new MMLTrack().setMML(mml);
		assertEquals(expectMML, track.generate().getMabiMML());
	}

	/**
	 * メロディが短い場合の、後続複数のテンポ指定
	 */
	@Test
	public void testTempo_all0() throws Exception {
		String mml =        "MML@,t150c1c1t120c1c1t130c1c1;";
		String expectMML1 = "MML@t150l1rrt120rrt130,l1cccccc,;";
		String expectMML2 = "MML@t150v0l1cct120cct130rrv0c64t150,l1cccccc,;";

		MMLTrack track = new MMLTrack().setMML(mml);
		assertEquals(expectMML1, track.generate().getOriginalMML());
		assertEquals(expectMML2, track.generate().getMabiMML());
	}

	/**
	 * tempo tail
	 * v0c補正と重なっている音は鳴らない場合があります. (mabi)
	 * @throws UndefinedTickException
	 */
	@Test
	public void testTempo_tail0() throws UndefinedTickException {
		String mml =        "MML@,c2t130&c1.t200r1t180";
		String expectMML1 = "MML@r2t130l1r.t200rt180,l1c&c,;";
		String expectMML2 = "MML@v0c2t130r1.,l1c&c,;";

		MMLTrack track = new MMLTrack().setMML(mml);
		assertEquals(expectMML1, track.generate().getOriginalMML());
		assertEquals(expectMML2, track.generate().getMabiMML());
	}

	@Test
	public void testEquals_0() {
		MMLTrack track1 = new MMLTrack().setMML("MML@aaa");
		MMLTrack track2 = new MMLTrack().setMML("MML@a8.&a16aa");
		MMLTrack track3 = new MMLTrack().setMML("MML@aab");
		MMLTrack track4 = new MMLTrack().setMML("MML@at150aa");
		MMLTrack track5 = new MMLTrack().setMML("MML@aav10a");;
		MMLTrack track6 = new MMLTrack().setMML("MML@aaa.");
		MMLTrack track7 = new MMLTrack().setMML("MML@t120aaa");
		MMLTrack track8 = new MMLTrack().setMML("MML@l16a&a&a&al4aa");
		MMLTrack track9 = new MMLTrack().setMML("MML@l16a&a&a&al4aa");

		assertTrue( track1.equals(track2) );
		assertFalse( track1.equals(track3) );
		assertFalse( track1.equals(track4) );
		assertFalse( track1.equals(track5) );
		assertFalse( track1.equals(track6) );
		assertFalse( track1.equals(track7) );
		assertFalse( track1.equals(track8) );
		assertTrue( track8.equals(track9) );
	}

	@Test
	public void test_generate0() {
		MMLTrack track = new MMLTrack();
		System.out.println(track.mmlRankFormat());
		assertEquals('R', track.mmlRankFormat().charAt(0)); // generated mml
	}

	@Test
	public void test_generate1() {
		MMLTrack track = new MMLTrack().setMML("MML@a4a4a4a4");
		System.out.println(track.mmlRankFormat());
		assertEquals('*', track.mmlRankFormat().charAt(0)); // not generated mml
	}

	/**
	 * 不要な終端テンポ（時間計算）.
	 * @throws UndefinedTickException
	 */
	@Test
	public void test_generateTailTempo0() throws UndefinedTickException {
		String input  = "MML@t120ddddddddrrrrt60,cccccccc";
		String expect = "MML@t120dddddddd,cccccccc,;";
		MMLTrack track = new MMLTrack().setMML(input);
		track.generate();
		assertEquals(expect, track.getMabiMML());
	}
}

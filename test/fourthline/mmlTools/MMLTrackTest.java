/*
 * Copyright (C) 2013-2022 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fourthline.mmlTools.core.MMLTools;
import fourthline.mmlTools.core.UndefinedTickException;

/**
 * @author fourthline
 *
 */
public class MMLTrackTest {
	@Before
	public void setup() {
		MMLTrack.setTempoAllowChordPart(false);
	}

	@After
	public void cleanup() {
		MMLTrack.setTempoAllowChordPart(true);
	}

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

	@Test
	public void testGetMMLStringsMusicQ() throws UndefinedTickException {
		MMLTrack.setTempoAllowChordPart(true);
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
	@Ignore // 2017/01/07: MusicQアップデートでtailFix不要.
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
	@Ignore // 2017/01/07: MusicQアップデートでtailFix不要.
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
	@Ignore // 2017/01/07: MusicQアップデートでtailFix不要.
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
	@Ignore // 2017/01/07: MusicQアップデートでtailFix不要.
	@Test
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
		String expectMML2 = "MML@t150l1rv0dt120rdt130,l1cccccc,;";

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
		String expectMML2 = "MML@v0d2t130,l1c&c,;";

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
		assertFalse( track1.equals("") );
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

	@Test
	public void test_empty() {
		MMLTrack track = new MMLTrack();
		assertTrue( track.isEmpty() );

		track.setMML("MML@aaa;");
		assertFalse( track.isEmpty() );
	}

	@Test
	public void test_setPanpot() {
		MMLTrack track = new MMLTrack();
		assertEquals(64, track.getPanpot());

		track.setPanpot(-1);
		assertEquals(0, track.getPanpot());

		track.setPanpot(128);
		assertEquals(127, track.getPanpot());
	}

	@Test
	public void test_invalidMML() {
		MMLTrack track = new MMLTrack();
		assertEquals("MML@,,;", track.getOriginalMML());
		assertEquals("MML@,,;", track.getMabiMML());
		track.setMML("aaa");
		assertEquals("MML@,,;", track.getOriginalMML());
		assertEquals("MML@,,;", track.getMabiMML());
	}

	@Test
	public void test_parseGameFormat() {
		String mml = "タイトル : mml1\n作曲者 : author\nメロディー : AAA\n和音 1 : BBB\n和音 2 : CCC\n歌 : DDD\n";
		String expect = "MML@AAA,BBB,CCC,DDD;";
		MMLTrack track = new MMLTrack().setMML(mml);
		assertEquals(expect, track.getOriginalMML());
		assertEquals(expect, track.getMabiMML());
	}

	@Test
	public void test_parseGameFormat_invalid() {
		String mml = "タイトル : mml1\n作曲者 : author\nメロディー : AAA\n和音 1 : BBB\n和音 2 : CCC\nDDD\n";
		String expect = "MML@,,;";
		MMLTrack track = new MMLTrack().setMML(mml);
		assertEquals(expect, track.getOriginalMML());
		assertEquals(expect, track.getMabiMML());
	}

	@Test
	public void test_musicq_tempo_00() throws UndefinedTickException {
		MMLTrack.setTempoAllowChordPart(true);

		String mml = "MML@a&a&a,rrt120,c";
		String expect = "MML@a2.,v0d2t120,c;";
		MMLTrack track = new MMLTrack().setMML(mml).generate();
		assertEquals(expect, track.getMabiMML());
	}

	@Test
	public void test_musicq_tempo_01() throws UndefinedTickException {
		MMLTrack.setTempoAllowChordPart(true);

		String mml = "MML@a&a&a,rrt120,rc";
		String expect = "MML@a2.,,rct120;";
		MMLTrack track = new MMLTrack().setMML(mml).generate();
		assertEquals(expect, track.getMabiMML());
	}

	@Test
	public void test_musicq_tempo_02() throws UndefinedTickException {
		MMLTrack.setTempoAllowChordPart(true);

		String mml = "MML@a&a&a,rrt120,rrc";
		String expect = "MML@a2.,,v0c2t120v8c;";
		MMLTrack track = new MMLTrack().setMML(mml).generate();
		assertEquals(expect, track.getMabiMML());
	}

	@Test
	public void test_musicq_tempo_03() throws UndefinedTickException {
		MMLTrack.setTempoAllowChordPart(true);

		String mml = "MML@a&a&a,rrt120,rc&c";
		String expect = "MML@a2.,v0c2t120,rc2;";
		MMLTrack track = new MMLTrack().setMML(mml).generate();
		assertEquals(expect, track.getMabiMML());
	}

	@Test
	public void test_musicq_tempo_04() throws UndefinedTickException {
		MMLTrack.setTempoAllowChordPart(true);

		String mml = "MML@a2,l1c&c&c&c,l1<a&a;";
		String expect = "MML@l2av0dt121l1r.v0dt123,l1c.&c.&c,l1<a&at122;";
		MMLTrack track = new MMLTrack().setMML(mml);
		track.getGlobalTempoList().add(new MMLTempoEvent(121, 96*4));
		track.getGlobalTempoList().add(new MMLTempoEvent(122, 96*8));
		track.getGlobalTempoList().add(new MMLTempoEvent(123, 96*12));
		track.generate();
		assertEquals(expect, track.getMabiMML());
	}

	/**
	 * 和音にテンポ出力する場合に他のパートと音符が重ならないようにする
	 * @throws UndefinedTickException
	 */
	@Test
	public void test_musicq_tempo_05() throws UndefinedTickException {
		MMLTrack.setTempoAllowChordPart(true);

		String mml = "MML@c1&c1,rrt230,;";
		String expect = "MML@l1c&c,v0d2t230,;";
		MMLTrack track = new MMLTrack().setMML(mml);
		track.generate();
		assertEquals(expect, track.getMabiMML());
	}

	@Test
	public void test_clone() throws UndefinedTickException {
		MMLTrack t1 = new MMLTrack().setMML("MML@aaat130,b8c6,rc,rrd;");
		t1.setPanpot(1);
		t1.setProgram(2);
		t1.setProgram(-2);

		MMLTrack t2 = t1.clone();

		assertEquals("MML@aaat130,b8c6,rc,rrd;", t2.getMabiMML());
		assertEquals("MML@aaat130,b8c6,rc,rrd;", t2.getOriginalMML());
		assertEquals(t1.getPanpot(), t2.getPanpot());
		assertEquals(t1.getProgram(), t2.getProgram());
		assertEquals(t1.getSongProgram(), t2.getSongProgram());

		t1.getMMLEventList().get(0).getMMLNoteEventList().get(0).setNote(99);
		t1.generate();

		assertEquals("MML@o8d+o4aa,b8c6,rc,r2d;", t1.getMabiMML());
		assertEquals("MML@aaat130,b8c6,rc,rrd;", t2.getMabiMML());
		assertEquals("MML@aaat130,b8c6,rc,rrd;", t2.getOriginalMML());

		MMLTrack t3 = t1.clone();

		assertEquals("MML@o8d+o4aa,b8c6,rc,r2d;", t3.getMabiMML());
		assertEquals("MML@o8d+o4aat130,b8c6,rc,r2dt130;", t3.getOriginalMML());
	}

	@Test
	public void test_startOffset_01() {
		var track = new MMLTrack(0, 0, 0).setMML("MML@a,b,c,d;");

		Arrays.asList(0, 1, 2, 3).forEach(t -> 
		assertEquals(0, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));

		// スタートオフセットの設定で全体のノート移動をチェックする
		track.setStartOffset(96);
		Arrays.asList(0, 1, 2, 3).forEach(t -> 
		assertEquals(96, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));
	}

	@Test
	public void test_startOffset_02() {
		var track = new MMLTrack(96, 0, 0).setMML("MML@a,b,c,d;");

		// 最初からスタートオフセットを設定している場合のチェック
		Arrays.asList(0, 1, 2, 3).forEach(t -> 
		assertEquals(96, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));

		// 個別スタート位置の場合はノート移動しない
		track.setStartDelta(-48);
		track.setStartSongDelta(-48);
		Arrays.asList(0, 1, 2).forEach(t -> 
		assertEquals(96, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));
		Arrays.asList(3).forEach(t -> 
		assertEquals(96, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));

		// 再設定
		track.setStartDelta(-96);
		track.setStartSongDelta(-96);
		Arrays.asList(0, 1, 2).forEach(t -> 
		assertEquals(96, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));
		Arrays.asList(3).forEach(t -> 
		assertEquals(96, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));
	}

	@Test
	public void test_startOffset_04() {
		var track = new MMLTrack(96, 0, 0).setMML("MML@ra,rb,rc,rd;");

		// 最初からスタートオフセットを設定している場合のチェック
		Arrays.asList(0, 1, 2, 3).forEach(t -> 
		assertEquals(192, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));

		// 個別スタート位置の場合はノート移動しない
		track.setStartDelta(48);
		track.setStartSongDelta(48);
		Arrays.asList(0, 1, 2).forEach(t -> 
		assertEquals(192, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));
		Arrays.asList(3).forEach(t -> 
		assertEquals(192, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));

		// 再設定
		track.setStartDelta(96);
		track.setStartSongDelta(96);
		Arrays.asList(0, 1, 2).forEach(t -> 
		assertEquals(192, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));
		Arrays.asList(3).forEach(t -> 
		assertEquals(192, track.getMMLEventAtIndex(t).getMMLNoteEventList().get(0).getTickOffset()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_startOffset_03() {
		var track = new MMLTrack(96, 0, 0).setMML("MML@a,b,c,d;");

		// マイナスオフセットは反映しない.
		track.setStartDelta(-97);
	}

	@Test
	public void test_startOffset_mml() throws UndefinedTickException {
		var track = new MMLTrack(384, 0, -96).setMML("MML@a,b,c,d;");
		track.getGlobalTempoList().add(new MMLTempoEvent(180, 0));
		track.getGlobalTempoList().add(new MMLTempoEvent(140, 96));
		track.getGlobalTempoList().add(new MMLTempoEvent(110, 192));
		track.getGlobalTempoList().add(new MMLTempoEvent(100, 384));
		track.generate();

		assertEquals("MML@t100a,b,c,t110d;", track.getMabiMML());
	}

	@Test
	public void test_startOffset_mml2() throws UndefinedTickException {
		MMLTrack.setTempoAllowChordPart(true);
		var track = new MMLTrack(384, 0, -96).setMML("MML@a,b,c,d;");
		track.getGlobalTempoList().add(new MMLTempoEvent(180, 0));
		track.getGlobalTempoList().add(new MMLTempoEvent(140, 96));
		track.getGlobalTempoList().add(new MMLTempoEvent(110, 192));
		track.getGlobalTempoList().add(new MMLTempoEvent(100, 384));
		track.generate();

		assertEquals("MML@t100a,b,c,t110d;", track.getMabiMML());
	}

	/**
	 * アタック遅延補正のテスト
	 * @throws UndefinedTickException
	 */
	@Test
	public void set_attackDelayCorrect_1() throws UndefinedTickException {
		var track = new MMLTrack(0, 0, 0).setMML("MML@aa,bb,cc,dd;");
		track.setAttackDelayCorrect(-6);
		track.setAttackSongDelayCorrect(-12);

		track.generate();
		assertEquals("MML@aa,bb,cc,dd;", track.getOriginalMML());
		assertEquals("MML@a8&a9a,b8&b9b,c8&c9c,d8&d16.d;", track.getMabiMML());

		MMLTrack.setTempoAllowChordPart(true);
		track.generate();
		assertEquals("MML@aa,bb,cc,dd;", track.getOriginalMML());
		assertEquals("MML@a8&a9a,b8&b9b,c8&c9c,d8&d16.d;", track.getMabiMML());
	}
}

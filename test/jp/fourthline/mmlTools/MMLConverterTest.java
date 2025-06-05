/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.mmlTools.core.MMLTickTable.Switch;
import jp.fourthline.mmlTools.optimizer.MMLStringOptimizer;

public final class MMLConverterTest {

	private MMLConverter exporter;

	@Before
	public void setup() {
		exporter = new MMLConverter(Switch.MB);
		MMLBuilder.setMMLVZeroTempo(false);
	}

	@After
	public void cleanup() {
		MMLBuilder.setMMLVZeroTempo(true);
		MMLStringOptimizer.setOptimizeLevel(MMLStringOptimizer.GEN2);
	}

	private void test(String mml, String expect1, String expect2) throws Exception {
		try {
			MMLTrack track = new MMLTrack().setMML(mml);
			track.generate();
			String result = exporter.convertMML(track.getMMLEventList());
			assertEquals(expect1, track.getMabiMML());
			assertEquals(expect2, result);
		} catch (MMLExceptionList | MMLVerifyException e) {
			throw e;
		}
	}

	@Test
	public void test1() throws Exception {
		var mml = "MML@c8c8c8c8,c8c8c8c8,c16c16c16c16";
		var expect1 = "MML@l8cccc,l8cccc,l16cccc;";
		var expect2 = "MML@l8cccc,l8cccc,l16cccc;";
		test(mml, expect1, expect2);
	}

	@Test
	public void test64dot() throws Exception {
		var mml = "MML@c64.c64.c64.c64.";
		var expect1 = "MML@l42cccc,,;";
		var expect2 = "MML@l64.cccc;";
		test(mml, expect1, expect2);
	}

	@Test(expected = MMLExceptionList.class)
	public void test2() throws Exception {
		var mml = "MML@l38cc+d5";
		var expect1 = "MML@l38cc+d5,,;";
		var expect2 = "MML@l57.cc+d6&d32;";
		test(mml, expect1, expect2);
	}

	@Test
	public void test() throws Exception {
		String mml = "MML@c64c64c64c64c64d42";
		var expect1 = "MML@l64cccccd42,,;";
		var expect2 = "MML@l64cccccd.;";
		test(mml, expect1, expect2);
	}

	@Test
	public void tick1152() throws MMLExceptionList {
		MMLEventList list = new MMLEventList("l1.c&c");
		assertEquals(1152, list.getLastNote().getTick());
		String result = exporter.convertMML(List.of(list));
		assertEquals("MML@l1.c&c;", result);
	}

	@Test
	public void testTempo() throws Exception {
		String mml = "MML@rrrT150rrrccc,rrrrrrrd,e";
		var expect1 = "MML@r2.t150r2.ccc,rr1.d,e;";
		var expect2 = "MML@r2.t150r2.ccc,r1.rd,e;";
		test(mml, expect1, expect2);
	}

	@Test
	public void testOption() throws Exception {
		String mml = "MML@rrrT150rrrccc,rrrrrrrd,o1cco7do1ccc";
		MMLTrack track = new MMLTrack().setMML(mml);
		track.generate();
		assertEquals("MML@r2.t150r2.ccc,r1.rd,o1cco7do1ccc;", exporter.convertMML(track.getMMLEventList()));

		exporter.setOption(true, true);
		assertEquals("MML@r2.t150r2.ccc,r2.t150r1d,o1ccn86t150ccc;", exporter.convertMML(track.getMMLEventList()));
	}

	@Test
	public void test3() throws MMLExceptionList {
		// GEN3最適化のときにAltのキャッシュをクリアする
		var list = new MMLEventList("");
		MMLStringOptimizer.setOptimizeLevel(MMLStringOptimizer.GEN3);
		list.addMMLNoteEvent(new MMLNoteEvent(34, 6, 807));
		list.addMMLNoteEvent(new MMLNoteEvent(34, 6, 828));
		String mml = exporter.convertMML(List.of(list));
		assertEquals("MML@r1.r2r16l64rr.<<a+rr.a+;", mml);
	}
}

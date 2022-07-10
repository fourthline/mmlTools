/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import jp.fourthline.mmlTools.core.UndefinedTickException;

public class MMLTempoConverterTest {

	@Test
	public void test_staticFunc() {
		MMLTempoEvent t0 = new MMLTempoEvent(120, 0);
		ArrayList<MMLTempoEvent> tempoList = new ArrayList<>();
		tempoList.add(new MMLTempoEvent(90, 96));

		assertEquals(96, MMLTempoConverter.getTickOffsetOnTime(tempoList, 500), 0.01);
		assertEquals(120, MMLTempoEvent.searchOnTick(tempoList, 0));
		assertTrue(t0.equals(MMLTempoEvent.getMaxTempoEvent(tempoList)));
	}

	@Test
	public void test_getTimeOnTickOffset() {
		List<MMLTempoEvent> empty = List.of();
		List<MMLTempoEvent> t240 = List.of(new MMLTempoEvent(240, 0));
		List<MMLTempoEvent> t60 = List.of(new MMLTempoEvent(60, 0));
		double delta = 0.01;

		assertEquals(5.2, MMLTempoConverter.getTimeOnTickOffset(empty, 1), delta);
		assertEquals(10.42, MMLTempoConverter.getTimeOnTickOffset(empty, 2), delta);
		assertEquals(15.63, MMLTempoConverter.getTimeOnTickOffset(empty, 3), delta);
		assertEquals(20.83, MMLTempoConverter.getTimeOnTickOffset(empty, 4), delta);
		assertEquals(26.04, MMLTempoConverter.getTimeOnTickOffset(empty, 5), delta);
		assertEquals(31.25, MMLTempoConverter.getTimeOnTickOffset(empty, 6), delta);
		assertEquals(36.46, MMLTempoConverter.getTimeOnTickOffset(empty, 7), delta);
		assertEquals(41.67, MMLTempoConverter.getTimeOnTickOffset(empty, 8), delta);
		assertEquals(46.88, MMLTempoConverter.getTimeOnTickOffset(empty, 9), delta);
		assertEquals(52.08, MMLTempoConverter.getTimeOnTickOffset(empty, 10), delta);

		assertEquals(2.60, MMLTempoConverter.getTimeOnTickOffset(t240, 1), delta);
		assertEquals(5.20, MMLTempoConverter.getTimeOnTickOffset(t240, 2), delta);
		assertEquals(7.81, MMLTempoConverter.getTimeOnTickOffset(t240, 3), delta);
		assertEquals(10.42, MMLTempoConverter.getTimeOnTickOffset(t240, 4), delta);
		assertEquals(13.02, MMLTempoConverter.getTimeOnTickOffset(t240, 5), delta);
		assertEquals(15.63, MMLTempoConverter.getTimeOnTickOffset(t240, 6), delta);
		assertEquals(18.23, MMLTempoConverter.getTimeOnTickOffset(t240, 7), delta);
		assertEquals(20.83, MMLTempoConverter.getTimeOnTickOffset(t240, 8), delta);
		assertEquals(23.44, MMLTempoConverter.getTimeOnTickOffset(t240, 9), delta);
		assertEquals(26.04, MMLTempoConverter.getTimeOnTickOffset(t240, 10), delta);

		assertEquals(10.42, MMLTempoConverter.getTimeOnTickOffset(t60, 1), delta);
		assertEquals(20.83, MMLTempoConverter.getTimeOnTickOffset(t60, 2), delta);
		assertEquals(31.25, MMLTempoConverter.getTimeOnTickOffset(t60, 3), delta);
		assertEquals(41.67, MMLTempoConverter.getTimeOnTickOffset(t60, 4), delta);
		assertEquals(52.08, MMLTempoConverter.getTimeOnTickOffset(t60, 5), delta);
		assertEquals(62.50, MMLTempoConverter.getTimeOnTickOffset(t60, 6), delta);
		assertEquals(72.92, MMLTempoConverter.getTimeOnTickOffset(t60, 7), delta);
		assertEquals(83.33, MMLTempoConverter.getTimeOnTickOffset(t60, 8), delta);
		assertEquals(93.75, MMLTempoConverter.getTimeOnTickOffset(t60, 9), delta);
		assertEquals(104.17, MMLTempoConverter.getTimeOnTickOffset(t60, 10), delta);
	}

	@Test
	public void test_getTickOffsetOnTime() {
		List<MMLTempoEvent> empty = List.of();
		List<MMLTempoEvent> t240 = List.of(new MMLTempoEvent(240, 0));
		List<MMLTempoEvent> t60 = List.of(new MMLTempoEvent(60, 0));
		double delta = 0.01;

		assertEquals(0.20, MMLTempoConverter.getTickOffsetOnTime(empty, 1), delta);
		assertEquals(0.38, MMLTempoConverter.getTickOffsetOnTime(empty, 2), delta);
		assertEquals(0.58, MMLTempoConverter.getTickOffsetOnTime(empty, 3), delta);
		assertEquals(0.77, MMLTempoConverter.getTickOffsetOnTime(empty, 4), delta);
		assertEquals(0.96, MMLTempoConverter.getTickOffsetOnTime(empty, 5), delta);
		assertEquals(1.15, MMLTempoConverter.getTickOffsetOnTime(empty, 6), delta);
		assertEquals(1.34, MMLTempoConverter.getTickOffsetOnTime(empty, 7), delta);
		assertEquals(1.54, MMLTempoConverter.getTickOffsetOnTime(empty, 8), delta);
		assertEquals(1.73, MMLTempoConverter.getTickOffsetOnTime(empty, 9), delta);
		assertEquals(1.92, MMLTempoConverter.getTickOffsetOnTime(empty, 10), delta);

		assertEquals(0.38, MMLTempoConverter.getTickOffsetOnTime(t240, 1), delta);
		assertEquals(0.77, MMLTempoConverter.getTickOffsetOnTime(t240, 2), delta);
		assertEquals(1.15, MMLTempoConverter.getTickOffsetOnTime(t240, 3), delta);
		assertEquals(1.54, MMLTempoConverter.getTickOffsetOnTime(t240, 4), delta);
		assertEquals(1.92, MMLTempoConverter.getTickOffsetOnTime(t240, 5), delta);
		assertEquals(2.30, MMLTempoConverter.getTickOffsetOnTime(t240, 6), delta);
		assertEquals(2.69, MMLTempoConverter.getTickOffsetOnTime(t240, 7), delta);
		assertEquals(3.07, MMLTempoConverter.getTickOffsetOnTime(t240, 8), delta);
		assertEquals(3.46, MMLTempoConverter.getTickOffsetOnTime(t240, 9), delta);
		assertEquals(3.84, MMLTempoConverter.getTickOffsetOnTime(t240, 10), delta);

		assertEquals(0.10, MMLTempoConverter.getTickOffsetOnTime(t60, 1), delta);
		assertEquals(0.20, MMLTempoConverter.getTickOffsetOnTime(t60, 2), delta);
		assertEquals(0.29, MMLTempoConverter.getTickOffsetOnTime(t60, 3), delta);
		assertEquals(0.38, MMLTempoConverter.getTickOffsetOnTime(t60, 4), delta);
		assertEquals(0.48, MMLTempoConverter.getTickOffsetOnTime(t60, 5), delta);
		assertEquals(0.58, MMLTempoConverter.getTickOffsetOnTime(t60, 6), delta);
		assertEquals(0.67, MMLTempoConverter.getTickOffsetOnTime(t60, 7), delta);
		assertEquals(0.77, MMLTempoConverter.getTickOffsetOnTime(t60, 8), delta);
		assertEquals(0.86, MMLTempoConverter.getTickOffsetOnTime(t60, 9), delta);
		assertEquals(0.96, MMLTempoConverter.getTickOffsetOnTime(t60, 10), delta);
		assertEquals(1.06, MMLTempoConverter.getTickOffsetOnTime(t60, 11), delta);
	}

	@Test
	public void test_getTimeOnTickOffset_getTickOffsetOnTime_01() {
		List<MMLTempoEvent> empty = List.of();
		List<MMLTempoEvent> t240 = List.of(new MMLTempoEvent(240, 0));
		List<MMLTempoEvent> t60 = List.of(new MMLTempoEvent(60, 0));

		for (int i = 0; i < MMLEvent.MAX_TICK; i++) {
			for (List<MMLTempoEvent> t : Arrays.asList(empty, t240, t60)) {
				double time = MMLTempoConverter.getTimeOnTickOffset(t, i);
				assertEquals(i, MMLTempoConverter.getTickOffsetOnTime(t, time), 0.01);
			}
		}
	}

	@Test
	public void test_getTimeOnTickOffset_getTickOffsetOnTime_02() {
		var tempoList = Arrays.asList(
				new MMLTempoEvent(240, 0),
				new MMLTempoEvent(60, 2001),
				new MMLTempoEvent(77, 80007),
				new MMLTempoEvent(255, 703498));

		for (int i = 0; i < MMLEvent.MAX_TICK; i++) {
			double time = MMLTempoConverter.getTimeOnTickOffset(tempoList, i);
			assertEquals(i, MMLTempoConverter.getTickOffsetOnTime(tempoList, time), 0.01);
		}
	}

	@Test
	public void test_getTimeOnTickOffset_getTickOffsetOnTime_03() {
		var t110 = List.of(new MMLTempoEvent(110, 0));
		var tempoList = Arrays.asList(
				new MMLTempoEvent(60, 0),
				new MMLTempoEvent(255, 2001),
				new MMLTempoEvent(255, 4001),
				new MMLTempoEvent(113, 80007),
				new MMLTempoEvent(90, 703498));
		MMLTempoConverter converter = new MMLTempoConverter(tempoList, tempoList);
		MMLTempoConverter converter2 = new MMLTempoConverter(t110, tempoList);

		for (int i = 0; i < MMLEvent.MAX_TICK; i++) {
			assertEquals(i, converter.convertEvent(i, true));
			converter2.convertEvent(i, true);
		}
		assertEquals("0.000/3840000", converter.getConversionDiff());
		assertEquals("960861.628/3840000", converter2.getConversionDiff());
	}

	@Test
	public void test_convert01() throws UndefinedTickException {
		var score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@t140ccct90ddd"));
		score.getMarkerList().add(new Marker("test", 96));

		var converter = MMLTempoConverter.convert(score, List.of());
		assertEquals("2.714/13", converter.getConversionDiff());

		score.generateAll();
		assertEquals("MML@c8l11&cc7&c13c8&cl3ddd,,;", score.getTrack(0).getMabiMML());
		assertEquals(new Marker("test", 82), score.getMarkerList().get(0));
	}

	@Test
	public void test_convert02() throws UndefinedTickException {
		var t1 = new MMLTempoEvent(120, 768);
		var score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@fefefefe,,;"));
		score.getTempoEventList().add(t1);

		MMLTempoConverter.convert(score, List.of(new MMLTempoEvent(240, 0), t1));
		score.generateAll();
		assertEquals("MML@t240l2fefefefet120,,;", score.getTrack(0).getOriginalMML());
	}

	@Test
	public void test_convert03() throws UndefinedTickException {
		var t1 = new MMLTempoEvent(120, 768);
		var score = new MMLScore();
		score.addTrack(new MMLTrack().setMML("MML@fefefefe,,;"));
		score.getTempoEventList().add(t1);

		MMLTempoConverter.convert(score, List.of(new MMLTempoEvent(60, 0), t1));
		score.generateAll();
		assertEquals("MML@t60l8fefefefet120,,;", score.getTrack(0).getOriginalMML());
	}
}

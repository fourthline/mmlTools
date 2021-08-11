/*
 * Copyright (C) 2017-2021 たんらる
 */

package fourthline.mmlTools.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fourthline.mmlTools.ComposeRank;

public final class MMLTextTest {

	@Before
	public void setup() {
		MMLText.setMelodyEmptyStr("");
	}

	@After
	public void cleanup() {
		MMLText.setMelodyEmptyStr("");
	}

	@Test
	public void test_null() {
		MMLText text = new MMLText().setMMLText(null, null, null, null);
		assertEquals("MML@,,;", text.getMML());
	}

	@Test
	public void test_splitMML() {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < 2000; i++) {
			sb1.append('a');
			sb2.append('b');
		}
		sb1.append(sb2);
		String s = sb1.toString();
		MMLText text = new MMLText().setMMLText(s, s, s, s);
		List<MMLText> list = text.splitMML(ComposeRank.getTopRank());
		assertEquals(8, list.size());
		StringBuilder expect1 = new StringBuilder();
		expect1.append("MML@").append(s.substring(0, 1200))
		.append(',').append(s.substring(0, 800))
		.append(',').append(s.substring(0, 500))
		.append(',').append(s.substring(0, 1200))
		.append(';');
		assertEquals(expect1.toString(), list.get(0).getMML());

		StringBuilder expect2 = new StringBuilder();
		expect2.append("MML@").append(s.substring(1200, 2400))
		.append(',').append(s.substring(800, 1600))
		.append(',').append(s.substring(500, 1000))
		.append(',').append(s.substring(1200, 2400))
		.append(';');
		assertEquals(expect2.toString(), list.get(1).getMML());
	}

	@Test
	public void test_splitMML_melody_empty() {
		MMLText.setMelodyEmptyStr("<>");
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < 2000; i++) {
			sb1.append('a');
			sb2.append('b');
		}
		sb1.append(sb2);
		String s = sb1.toString();
		MMLText text = new MMLText().setMMLText("", s, s, "");
		List<MMLText> list = text.splitMML(ComposeRank.getTopRank());
		assertEquals(8, list.size());
		StringBuilder expect1 = new StringBuilder();
		expect1.append("MML@").append("<>")
		.append(',').append(s.substring(0, 800))
		.append(',').append(s.substring(0, 500))
		.append(';');
		assertEquals(expect1.toString(), list.get(0).getMML());

		StringBuilder expect2 = new StringBuilder();
		expect2.append("MML@").append("<>")
		.append(',').append(s.substring(800, 1600))
		.append(',').append(s.substring(500, 1000))
		.append(';');
		assertEquals(expect2.toString(), list.get(1).getMML());
	}

	@Test
	public void test_melodyEmpty_01() {
		MMLText text = new MMLText().setMMLText("", "a", "b", "");
		assertEquals("MML@,a,b;", text.getMML());
	}

	@Test
	public void test_melodyEmpty_02() {
		MMLText.setMelodyEmptyStr("<>");
		MMLText text = new MMLText().setMMLText("", "a", "b", "");
		assertEquals("MML@<>,a,b;", text.getMML());
	}

	@Test
	public void test_melodyEmpty_03() {
		MMLText.setMelodyEmptyStr("<>");
		MMLText text = new MMLText().setMMLText("", "", "b", "");
		assertEquals("MML@<>,,b;", text.getMML());
	}

	@Test
	public void test_melodyEmpty_04() {
		MMLText.setMelodyEmptyStr("<>");
		MMLText text = new MMLText().setMMLText("", "a", "", "");
		assertEquals("MML@<>,a,;", text.getMML());
	}

	@Test
	public void test_melodyEmpty_05() {
		MMLText.setMelodyEmptyStr("<>");
		MMLText text = new MMLText().setMMLText("", "a", "b", "c");
		assertEquals("MML@,a,b,c;", text.getMML());
	}

	@Test
	public void test_melodyEmpty_06() {
		MMLText.setMelodyEmptyStr("<>");
		MMLText text = new MMLText().setMMLText("", "", "", "c");
		assertEquals("MML@,,,c;", text.getMML());
	}

	@Test
	public void test_melodyEmpty_07() {
		MMLText.setMelodyEmptyStr("<>");
		MMLText text = new MMLText().setMMLText("", "", "", "");
		assertEquals("MML@,,;", text.getMML());
	}
}

/*
　* Copyright (C) 2017 たんらる
　*/

package fourthline.mmlTools.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fourthline.mmlTools.ComposeRank;

public final class MMLTextTest {

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
}

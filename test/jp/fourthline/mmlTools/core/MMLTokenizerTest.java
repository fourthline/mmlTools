/*
 * Copyright (C) 2016 たんらる
 */

package jp.fourthline.mmlTools.core;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;

public class MMLTokenizerTest {
	@Test
	public void test_token() {
		String expect[] = { "a16", "b16.", "C", "d+++++", "FH--", "tu80" };
		MMLTokenizer tokenizer = new MMLTokenizer("a16b16.Cd+++++FH--tu80");
		ArrayList<String> result = new ArrayList<>();
		while (tokenizer.hasNext()) {
			String s = tokenizer.next();
			result.add( s );
			System.out.println( s );
		}

		assertArrayEquals(expect, result.toArray());
	}
}

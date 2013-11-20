/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools;

import static org.junit.Assert.*;

import org.junit.Test;

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
		MMLTrack track = new MMLTrack("MML@aaa,bbb,ccc;");
		String expect[] = {
				"a8t150r8a4a4", // melodyパートのみテンポ指定.
				"b8r8b4b4",
				"c8r8c4c4",
		};
		new MMLTempoEvent(150, 48).appendToListElement(track.getGlobalTempoList());
		String mml[] = track.getMMLStrings();

		assertArrayEquals(expect, mml);
	}

}

/*
 * Copyright (C) 2022-2025 たんらる
 */

package jp.fourthline.mmlTools;

import jp.fourthline.mmlTools.core.MMLTokenizer;
import jp.fourthline.mmlTools.optimizer.AAOptimizer;
import jp.fourthline.mmlTools.optimizer.OxLxFixedOptimizer;


/**
 * ArcheAge向けMML出力
 */
public final class AAMMLExport extends MMLConverter {

	@Override
	protected String convert(String mml[]) {
		return toAAText(mml);
	}

	public static String toAAText(String mml[]) {
		var optimizer = new OxLxFixedOptimizer(true);
		int part = Math.min(mml.length, 10);
		for (int i = 0; i < part; i++) {
			if ( (i > 0) && (mml[i].length() > 0) ) {
				optimizer.nextToken(",");
			}
			optimizer.resetOctave();
			new MMLTokenizer(mml[i]).forEachRemaining(optimizer::nextToken);
		}
		String text = optimizer.getMinString();

		// 音量修正
		var volOptimizer = new AAOptimizer();
		new MMLTokenizer(text).forEachRemaining(volOptimizer::nextToken);
		return volOptimizer.getMinString();
	}
}

/*
 * Copyright (C) 2022 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.fourthline.mmlTools.core.MMLTokenizer;
import jp.fourthline.mmlTools.core.UndefinedTickException;
import jp.fourthline.mmlTools.optimizer.AAOptimizer;
import jp.fourthline.mmlTools.optimizer.OxLxFixedOptimizer;


/**
 * ArcheAge向けMML出力
 */
public final class AAMMLExport {
	public AAMMLExport() {}

	private int partCount;

	public String convertMML(List<MMLEventList> mmlParts, List<MMLTempoEvent> globalTempoList) {
		try {
			List<String>list = getGenericMMLStrings(mmlParts, globalTempoList);
			var array = list.toArray(String[]::new);
			partCount = array.length;
			String text = toAAText(array);
			return text;
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
		return "";
	}

	public int getPartCount() {
		return partCount;
	}

	/**
	 * 最適化前のMML列を取得する.
	 * @return
	 * @throws UndefinedTickException
	 */
	private List<String> getGenericMMLStrings(List<MMLEventList> mmlParts, List<MMLTempoEvent> globalTempoList) throws UndefinedTickException {
		int count = mmlParts.size();
		List<String> mmlList = new ArrayList<>();
		LinkedList<MMLTempoEvent> localTempoList = new LinkedList<>(globalTempoList);

		for (int i = 0; i < count; i++) {
			MMLEventList eventList = mmlParts.get(i);
			String mml = MMLBuilder.create(eventList, 0, MMLBuilder.INIT_OCT).toMMLStringMusicQ(localTempoList, null);
			if (mml.length() > 0) {
				mmlList.add(mml);
			}

		}
		return mmlList;
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

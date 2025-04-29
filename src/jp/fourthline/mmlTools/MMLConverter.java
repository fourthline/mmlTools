/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.fourthline.mmlTools.core.MMLTickTable;
import jp.fourthline.mmlTools.core.MMLTickTable.Switch;

public abstract class MMLConverter {
	private int partCount;
	protected boolean allTempoPart = false;

	public String convertMML(List<MMLEventList> mmlParts, List<MMLTempoEvent> globalTempoList) throws MMLExceptionList {
		MMLTickTable.getInstance().tableSwitch(Switch.MB);
		try {
			List<String>list = getGenericMMLStrings(mmlParts, globalTempoList);
			var array = list.toArray(String[]::new);
			partCount = array.length;
			String text = convert(array);
			return text;
		} catch (MMLExceptionList e) {
			throw e;
		} finally {
			MMLTickTable.getInstance().tableSwitch(null);
		}
	}

	public int getPartCount() {
		return partCount;
	}

	/**
	 * 最適化前のMML列を取得する.
	 * @return
	 * @throws MMLExceptionList 
	 */
	private List<String> getGenericMMLStrings(List<MMLEventList> mmlParts, List<MMLTempoEvent> globalTempoList) throws MMLExceptionList {
		int count = mmlParts.size();
		List<String> mmlList = new ArrayList<>();
		LinkedList<MMLTempoEvent> localTempoList = new LinkedList<>(globalTempoList);
		var errList = new ArrayList<MMLExceptionList.Entry>();

		for (int i = 0; i < count; i++) {
			MMLEventList eventList = mmlParts.get(i);
			try {
				String mml = MMLBuilder.create(eventList, 0, MMLBuilder.INIT_OCT).toMMLStringMusicQ(allTempoPart ? new LinkedList<>(globalTempoList) : localTempoList, null);
				if (mml.length() > 0) {
					mmlList.add(mml);
				}
			} catch (MMLExceptionList e) {
				errList.addAll(e.getErr());
			}

		}
		if (!errList.isEmpty()) {
			throw new MMLExceptionList(errList);
		}
		return mmlList;
	}

	protected abstract String convert(String mml[]);
}

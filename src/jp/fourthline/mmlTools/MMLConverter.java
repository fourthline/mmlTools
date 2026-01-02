/*
 * Copyright (C) 2025-2026 たんらる
 */

package jp.fourthline.mmlTools;

import java.util.ArrayList;
import java.util.List;

import jp.fourthline.mmlTools.core.MMLTickTable;
import jp.fourthline.mmlTools.core.MMLTickTable.Switch;
import jp.fourthline.mmlTools.optimizer.MMLStringOptimizer;

public class MMLConverter {
	private final Switch useTable;
	private boolean allowNOpt = false;

	public MMLConverter(Switch useTable) {
		this.useTable = useTable;
	}

	public void setOption(boolean allowNOpt) {
		this.allowNOpt = allowNOpt;
	}

	public String convertMML(List<MMLEventList> list) throws MMLExceptionList {
		MMLTickTable.getInstance().tableInvSwitch(useTable);
		String mml = null;
		try {
			mml = getGenericMMLStrings(list);
		} catch (Exception e) {
			throw e;
		} finally {
			MMLTickTable.getInstance().tableInvSwitch(null);
		}
		return mml;
	}

	private String optimize(String mml) {
		var optimizer = new MMLStringOptimizer(mml).setDisableNopt(!allowNOpt);
		var r = optimizer.optimizeGen2();
		return r;
	}

	private boolean verify(MMLEventList eventList, String mml) {
		var verifyList = new MMLEventList(mml, null, 0);
		var s1 = verifyList.getMMLNoteEventList().stream().filter(t -> t.getVelocity() > 0).toList().toString();
		var s2 = eventList.getMMLNoteEventList().stream().filter(t -> t.getVelocity() > 0).toList().toString();
		var r = s1.equals(s2);
		if (!r) {
			System.out.println(mml);
			System.out.println(s2);
			System.out.println(s1);
		}
		return r;
	}

	private List<MMLEventList> makeRelationPart(List<MMLEventList> eventList, int index) {
		var list = new ArrayList<MMLEventList>();
		int n = eventList.size();
		for (int i = 1; i < n; i++) {
			list.add(eventList.get((index + i) % n));
		}
		return list;
	}

	/**
	 * 最適化済みのMML列を取得する.
	 * @return
	 * @throws MMLExceptionList 
	 */
	private String getGenericMMLStrings(List<MMLEventList> eventList) throws MMLExceptionList {
		StringBuilder sb = new StringBuilder();
		var errList = new ArrayList<MMLExceptionList.Entry>();
		List<MMLTempoEvent> localTempoList = !eventList.isEmpty() ? new ArrayList<>(eventList.get(0).getGlobalTempoList()) : List.of();

		try {
			for (int i = 0; i < eventList.size(); i++) {
				var t = eventList.get(i);
				var mml = MMLBuilder.create(t, 0, MMLBuilder.INIT_OCT).setNoTempoVZeroCombine().toMMLStringMusicQ(localTempoList, makeRelationPart(eventList, i));
				mml = optimize(mml);

				// verify
				if (!verify(t, mml)) {
					return null;
				}
				if (mml.length() > 0) {
					if (sb.length() > 0) {
						sb.append(',');
					}
					sb.append( mml );
				}
			}
		} catch (MMLExceptionList e) {
			errList.addAll(e.getErr());
		}
		if (!errList.isEmpty()) {
			throw new MMLExceptionList(errList);
		}
		sb.insert(0, "MML@");
		sb.append(";");
		return sb.toString();
	}
}

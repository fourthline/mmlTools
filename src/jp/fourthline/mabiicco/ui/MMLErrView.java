/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.MMLExceptionList;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.Measure;

public final class MMLErrView {

	private final Vector<Vector<String>> dataList;

	public MMLErrView(MMLScore score) {
		dataList = makeList(score);
	}

	Vector<Vector<String>> getDataList() {
		return dataList;
	}

	private Vector<String> makeData(MMLScore score, MMLTrack track, int trackIndex, int partIndex, int tick, String msg) {
		var v = new Vector<String>();
		v.add(Integer.toString(trackIndex+1));
		v.add(track.getTrackName());
		if (partIndex >= 0) {
			v.add(MMLTrackView.MMLPART_NAME[ partIndex ]);
		} else {
			v.add("-");
		}
		if (tick >= 0) {
			v.add(new Measure(score, tick).toString());
		} else {
			v.add("-");
		}
		v.add(msg);
		return v;
	}

	/**
	 * 表示するデータを作成する.
	 */
	private Vector<Vector<String>> makeList(MMLScore score) {
		Vector<Vector<String>> list = new Vector<>();
		var vErrList = score.getVerifyErr();
		var errList = new ArrayList<>(score.getMMLErr());
		int trackIndex = 0;
		for (var track : score.getTrackList()) {
			int partIndex = 0;

			// for MMLVerifyException
			for (var item : vErrList) {
				if (track == item.getTrack()) {
					list.add(makeData(score, track, trackIndex, -1, -1, item.getLocalizedMessage()));
				}
			}

			// for MMLExceptionList
			for (var part : track.getMMLEventList()) {
				var eList = new ArrayList<MMLExceptionList.Entry>();  // すでに表示したアイテムを登録するリスト (重複表示しないため)
				for (var item : errList) {
					if (part.getMMLNoteEventList().contains(item.getNote()) && !eList.contains(item)) {
						eList.add(item);
						list.add(makeData(score, track, trackIndex, partIndex, item.getNote().getTickOffset(), item.getException().getLocalizedMessage()));
					}
				}
				errList.removeAll(eList); // 表示済みのデータを消しておく (他のトラック & パートでみる必要がない)
				partIndex++;
			}
			trackIndex++;
		}
		return list;
	}

	public void showMMLErrList(Frame parentFrame) {
		Vector<String> column = new Vector<>();
		column.add(AppResource.appText("#"));
		column.add(AppResource.appText("track"));
		column.add(AppResource.appText("part"));
		column.add(AppResource.appText("mml.err.position"));
		column.add(AppResource.appText("mml.err.value"));

		JTable table = new JTable(new DefaultTableModel(dataList, column) {
			private static final long serialVersionUID = 5392169416298424707L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		table.getColumnModel().getColumn(0).setMinWidth(20);
		table.getColumnModel().getColumn(0).setMaxWidth(20);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(1).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.getColumnModel().getColumn(3).setMaxWidth(100);
		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setRequestFocusEnabled(false);
		table.setFocusable(false);
		table.setRowSelectionAllowed(false);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		String title = AppResource.appText("menu.mmlErrList");

		var panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(new JLabel(""+dataList.size()), BorderLayout.SOUTH);
		JOptionPane.showMessageDialog(parentFrame, panel, title, JOptionPane.PLAIN_MESSAGE);
	}
}

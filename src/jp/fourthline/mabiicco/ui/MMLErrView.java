/*
 * Copyright (C) 2023-2025 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.Measure;
import jp.fourthline.mmlTools.logger.MMLLogger;
import jp.fourthline.mmlTools.logger.LogMessage;
import jp.fourthline.mmlTools.logger.LogMessage.PartMessage;
import jp.fourthline.mmlTools.logger.LogMessage.TrackMessage;

public final class MMLErrView {

	private final Vector<Vector<Object>> dataList;

	public MMLErrView(MMLScore score) {
		dataList = makeList(score);
	}

	Vector<Vector<Object>> getDataList() {
		return dataList;
	}

	private static enum Type {
		ERROR,
		WARN;
		private final String text;
		private Type() {
			text = AppResource.appText("mml.err.type." + name().toLowerCase());
		}
	}

	private static final class DataAttr {
		private final MMLScore score;
		private final MMLTrack track;
		private Stream<Vector<Object>> stream = Stream.empty();

		private DataAttr(MMLScore score, MMLTrack track) {
			this.score = score;
			this.track = track;
		}

		private Stream<Vector<Object>> toStream() {
			return stream;
		}

		private int getTrackIndex() {
			return score.getTrackList().indexOf(track);
		}

		private DataAttr makeDataRelationTrack(Type type, List<? extends TrackMessage> list) {
			stream = Stream.concat(stream,
					list.stream()
					.filter(t -> track == t.getTrack())
					.map(item -> makeData(type, item))
					);
			return this;
		}

		private DataAttr makeDataRelationPart(Type type, List<? extends PartMessage> list) {
			var messageMap = list.stream().collect(Collectors.groupingBy(PartMessage::getRelationPart));
			stream = Stream.concat(stream,
					track.getMMLEventList().stream()
					.map(messageMap::get)
					.filter(t -> t != null)
					.flatMap(List::stream)
					.map(item -> makeData(type, item))
					);
			return this;
		}

		private Vector<Object> makeData(Type type, PartMessage entry) {
			return makeData(type, track.getMMLEventList().indexOf(entry.getRelationPart()), entry.getTickOffset(), entry.getLocalizedMessage());
		}

		private Vector<Object> makeData(Type type, LogMessage entry) {
			return makeData(type, -1, -1, entry.getLocalizedMessage());
		}

		private Vector<Object> makeData(Type type, int partIndex, int tick, String msg) {
			var v = new Vector<Object>();
			v.add(getTrackIndex()+1);
			v.add(track.getTrackName());
			// パート
			if (partIndex >= 0) {
				v.add(MMLTrackView.MMLPART_NAME[ partIndex ]);
			} else {
				v.add("-");
			}
			// 位置
			if (tick >= 0) {
				v.add(new Measure(score, tick).toString());
			} else {
				v.add("-");
			}
			// 区分
			v.add(type.text);
			v.add(msg);
			return v;
		}
	}

	/**
	 * 表示するデータを作成する.
	 */
	private Vector<Vector<Object>> makeList(MMLScore score) {
		var vErrList = score.getVerifyErr();
		var errList = score.getMMLErr();
		return score.getTrackList().stream()
				.flatMap(track -> new DataAttr(score, track)
						.makeDataRelationTrack(Type.ERROR, vErrList)              // for MMLVerifyException
						.makeDataRelationPart(Type.ERROR, errList)                // for MMLExceptionList
						.makeDataRelationPart(Type.WARN, MMLLogger.logger(track).getEntryList())  // for MMLLogger
						.toStream()
						)
				.collect(Collectors.toCollection(Vector::new));
	}

	public void showMMLErrList(Frame parentFrame) {
		Vector<String> column = new Vector<>();
		column.add(AppResource.appText("#"));
		column.add(AppResource.appText("track"));
		column.add(AppResource.appText("part"));
		column.add(AppResource.appText("mml.err.position"));
		column.add(AppResource.appText("mml.err.type"));
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
		table.getColumnModel().getColumn(2).setMinWidth(80);
		table.getColumnModel().getColumn(2).setMaxWidth(80);
		table.getColumnModel().getColumn(3).setMinWidth(80);
		table.getColumnModel().getColumn(3).setMaxWidth(80);
		table.getColumnModel().getColumn(4).setMinWidth(60);
		table.getColumnModel().getColumn(4).setMaxWidth(60);
		table.getColumnModel().getColumn(5).setCellRenderer(new TooltipRenderer());
		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setRequestFocusEnabled(false);
		table.setFocusable(false);
		table.setRowSelectionAllowed(false);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(640, 400));
		String title = AppResource.appText("menu.mmlErrList");

		var panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(new JLabel(""+dataList.size()), BorderLayout.SOUTH);
		JOptionPane.showMessageDialog(parentFrame, panel, title, JOptionPane.PLAIN_MESSAGE);
	}

	private static class TooltipRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 6013889934698748903L;

		@Override
		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value != null) {
				String text = value.toString();
				setToolTipText(text);
			} else {
				setToolTipText(null);
			}

			return this;
		}
	}
}

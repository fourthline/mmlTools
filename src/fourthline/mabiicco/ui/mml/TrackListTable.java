/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.ui.mml;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.ComposeRank;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.MMLText;

public final class TrackListTable extends JTable {
	private static final long serialVersionUID = -710966050907225119L;

	private final class InCheckTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -5732476297298041942L;
		private final String[] columnNames = new String[] {
				"",
				AppResource.appText("mml.output.trackName"),
				AppResource.appText("mml.output.instrument"),
				AppResource.appText("mml.output.rank")
		};
		private final boolean checkBox;
		private final boolean checkValue[];
		private final ArrayList<String[]> dataList = new ArrayList<>();
		private final ArrayList<ComposeRank> rankList = new ArrayList<>();

		private InCheckTableModel(List<MMLTrack> trackList, boolean checkBox) {
			this.checkBox = checkBox;
			checkValue = new boolean[trackList.size()];
			for (MMLTrack track : trackList) {
				InstClass inst = MabiDLS.getInstance().getInstByProgram(track.getProgram());
				dataList.add(new String[] {
						track.getTrackName(),
						inst.toString(),
						track.mmlRankFormat()
				});
				rankList.add(track.mmlRank());
			}
		}

		private InCheckTableModel(MMLTrack track, List<MMLText> textList) {
			this.checkBox = false;
			this.checkValue = null;
			for (MMLText mml : textList) {
				InstClass inst = MabiDLS.getInstance().getInstByProgram(track.getProgram());
				dataList.add(new String[] {
						track.getTrackName(),
						inst.toString(),
						mml.mmlRankFormat()
				});
				rankList.add(mml.mmlRank());
			}
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (!checkBox) {
				col++;
			}
			if (col == 0) {
				return Boolean.class;
			}
			return String.class;
		}

		@Override
		public int getColumnCount() {
			int count = 4;
			if (!checkBox) {
				count--;
			}
			return count;
		}

		@Override
		public int getRowCount() {
			return dataList.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (!checkBox) {
				columnIndex++;
			}
			if (columnIndex == 0) {
				return checkValue[rowIndex];
			} else {
				return dataList.get(rowIndex)[columnIndex-1];
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (!checkBox) {
				columnIndex++;
			}
			return columnNames[ columnIndex ];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (!checkBox) {
				columnIndex++;
			}
			if (columnIndex == 0) {
				return true;
			}
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (!checkBox) {
				columnIndex++;
			}
			if (columnIndex != 0) {
				return;
			}
			checkValue[rowIndex] = aValue.equals(Boolean.TRUE) ? true : false;
		}
	}

	private final InCheckTableModel checkTableModel;

	public TrackListTable(List<MMLTrack> trackList) {
		this(trackList, false);
	}

	/**
	 * MMLTrackのJTableを作成します.
	 * @param trackList
	 * @param checkBox trueであれば, 最初の列にチェックボックスを作成します.
	 */
	public TrackListTable(List<MMLTrack> trackList, boolean checkBox) {
		super();
		checkTableModel = new InCheckTableModel(trackList, checkBox);
		initialize(checkBox);
	}

	public TrackListTable(MMLTrack track, List<MMLText> textList) {
		super();
		checkTableModel = new InCheckTableModel(track, textList);
		initialize(false);
	}

	private void initialize(boolean checkBox) {
		setModel( checkTableModel );
		if (checkBox) {
			getColumnModel().getColumn(0).setPreferredWidth(0);
			getColumnModel().getColumn(3).setPreferredWidth(180);
			setRowSelectionAllowed(false);
		} else {
			getColumnModel().getColumn(2).setPreferredWidth(180);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setRowSelectionInterval(0, 0);
		}
		getTableHeader().setReorderingAllowed(false);
		setFocusable(false);
	}

	/**
	 * 現在のチェック状態を表す配列を返します.
	 * @return
	 */
	public boolean[] getCheckList() {
		return checkTableModel.checkValue;
	}

	/**
	 * count分の項目にチェックをつけます
	 * @param count
	 */
	public void setInitialCheck(int count) {
		for (int i = 0; i < checkTableModel.checkValue.length; i++) {
			if (i < count) {
				checkTableModel.checkValue[i] = true;
			}
		}
	}

	public boolean selectedRowCanSplit() {
		int row = getSelectedRow();
		ComposeRank rank = checkTableModel.rankList.get(row);
		return !rank.canCompose();
	}
}

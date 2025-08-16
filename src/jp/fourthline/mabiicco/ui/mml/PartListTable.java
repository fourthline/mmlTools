/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.ui.mml;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.midi.InstClass;
import jp.fourthline.mabiicco.midi.MabiDLS;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLText;

public final class PartListTable extends JTable {
	private static final long serialVersionUID = -331949379948206213L;
	public static final String ERR = "error";

	private static final class InCheckTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -8842111776415664364L;
		private final String[] columnNames = new String[] {
				"",
				"#",
				AppResource.appText("mml.output.trackName"),
				AppResource.appText("mml.output.instrument"),
				AppResource.appText("mml.output.partname"),
				AppResource.appText("mml.output.textCount")
		};
		private final boolean checkBox;
		private final boolean[] checkValue;
		private final List<Object[]> dataList = new ArrayList<>();
		private final List<MMLEventList> eventList = new ArrayList<>();
		private final int maxCount;
		private int activePartIndex = -1;

		private static final List<String> partNameList = List.of("melody", "chord1", "chord2", "song");

		private static String getInstName(InstClass inst) {
			if (inst == null) return "";
			return inst.toString();
		}

		private InCheckTableModel(List<MMLTrack> trackList, boolean checkBox, int maxCount, MMLEventList activePart) {
			this.checkBox = checkBox;
			this.maxCount = maxCount;
			int trackIndex = 0;
			for (MMLTrack track : trackList) {
				InstClass inst = MabiDLS.getInstance().getInstByProgram(track.getProgram());
				InstClass songInst = MabiDLS.getInstance().getInstByProgram(track.getSongProgram());
				boolean[] instEnable = InstClass.getEnablePartByProgram(track.getProgram());
				boolean[] songExEnable = InstClass.getEnablePartByProgram(track.getSongProgram());
				int num = trackIndex + 1;
				var trackName = track.getTrackName();
				var text = new MMLText().setMMLText( track.getMabiMML() );
				for (int i = 0; i < instEnable.length; i++) {
					if (instEnable[i] || songExEnable[i]) {
						var instName = instEnable[i] ? getInstName(inst) : getInstName(songInst);
						var partName = AppResource.appText(partNameList.get(i));
						dataList.add(new Object[] {
								((num > 0) ? (num) : ""),
								((num > 0) ? trackName : ""),
								((num > 0) || (i == 3) ? instName : ""),
								partName,
								text.getText(i).length()
						});
						var currentEventList = track.getMMLEventAtIndex(i);
						if (activePart == currentEventList) {
							activePartIndex = eventList.size();
						}
						eventList.add(currentEventList);
						num = 0;
					}
				}
				trackIndex++;
			}
			checkValue = new boolean[ eventList.size() ];
		}

		@Override
		public Class<?> getColumnClass(int col){
			return getValueAt(0, col).getClass();
		}

		@Override
		public int getColumnCount() {
			int count = columnNames.length;
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
				if (maxCount == 1) {
					return activePartIndex != rowIndex;  // ラジオボタン版
				} else if (getCheckCount() < maxCount) {
					return true;
				}
				return checkValue[rowIndex];
			}
			return false;
		}

		public int getCheckCount() {
			int count = 0;
			for (boolean b : checkValue) {
				if (b) count++;
			}
			return count;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (!checkBox) {
				columnIndex++;
			}
			if (columnIndex != 0) {
				return;
			}
			if (maxCount == 1) {
				if (rowIndex != activePartIndex) {
					for (int i = 0; i < checkValue.length; i++) {
						checkValue[i] = (i == rowIndex);
					}
				}
			} else {
				checkValue[rowIndex] = aValue.equals(Boolean.TRUE);
			}
		}
	}

	// 参考: JTable$BooleanRenderer
	private static class CellRenderer<T extends JToggleButton> implements TableCellRenderer, UIResource {
		private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		private final T c;

		private CellRenderer(T c) {
			this.c = c;
			c.setHorizontalAlignment(JLabel.CENTER);
			c.setBorderPainted(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				c.setForeground(table.getSelectionForeground());
				c.setBackground(table.getSelectionBackground());
			}
			else {
				c.setForeground(table.getForeground());
				c.setBackground(table.getBackground());
			}
			c.setSelected((value != null && ((Boolean)value).booleanValue()));

			if (hasFocus) {
				c.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			} else {
				c.setBorder(noFocusBorder);
			}

			if (table instanceof PartListTable pt) {
				if (pt.checkTableModel.maxCount > 1) {
					// チェックカウントを超えていた場合、非選択のチェックボックスを無効化する
					var enable = (pt.checkTableModel.getCheckCount() < pt.checkTableModel.maxCount) || c.isSelected();
					c.setEnabled(enable);
				}
				c.setEnabled(pt.checkTableModel.activePartIndex != row);
			}

			return c;
		}
	}

	private static class CheckBoxCellRenderer extends CellRenderer<JCheckBox> {
		private CheckBoxCellRenderer() {
			super(new JCheckBox());
		}
	}

	private static class RadioButtonCellRenderer extends CellRenderer<JRadioButton>  {
		private RadioButtonCellRenderer() {
			super(new JRadioButton());
		}
	}

	private static class RadioCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
		private static final long serialVersionUID = 8201198936617567579L;
		private JRadioButton c = new JRadioButton();

		private RadioCellEditor() {
			super();
			c.addActionListener(this);
			c.setHorizontalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Object getCellEditorValue() {
			return c.isSelected();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			if (table instanceof PartListTable pt) {
				c.setEnabled(pt.checkTableModel.activePartIndex != row);
			}
			if (value instanceof Boolean b) {
				c.setSelected(b);
			}
			return c;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEditingStopped();
		}
	}

	private final InCheckTableModel checkTableModel;

	/**
	 * MMLTrackのJTableを作成します.
	 * @param trackList
	 * @param checkBox trueであれば, 最初の列にチェックボックスを作成します.
	 */
	public PartListTable(List<MMLTrack> trackList, boolean checkBox, int maxCount) {
		this(trackList, checkBox, maxCount, null);
	}

	public PartListTable(List<MMLTrack> trackList, boolean checkBox, int maxCount, MMLEventList activePart) {
		super();
		checkTableModel = new InCheckTableModel(trackList, checkBox, maxCount, activePart);
		initialize(checkBox, maxCount);
	}

	private void initialize(boolean checkBox, int maxCount) {
		setModel( checkTableModel );
		var columnModel = getColumnModel();
		if (checkBox) {
			if (maxCount > 1) {
				columnModel.getColumn(0).setCellRenderer(new CheckBoxCellRenderer());
			} else {
				columnModel.getColumn(0).setCellRenderer(new RadioButtonCellRenderer());
				columnModel.getColumn(0).setCellEditor(new RadioCellEditor());
			}
			columnModel.getColumn(0).setPreferredWidth(0);
			columnModel.getColumn(1).setPreferredWidth(20);
			columnModel.getColumn(1).setMaxWidth(20);
			columnModel.getColumn(5).setMaxWidth(180);
			setRowSelectionAllowed(false);
		} else {
			columnModel.getColumn(0).setPreferredWidth(20);
			columnModel.getColumn(0).setMaxWidth(20);
			columnModel.getColumn(4).setMaxWidth(180);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setRowSelectionInterval(0, 0);
		}
		getTableHeader().setReorderingAllowed(false);
		setFocusable(false);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		var c = super.prepareRenderer(renderer, row, column);
		if (c instanceof JComponent jc) {
			var v = getValueAt(row, 1);
			if ((v != null) && (!v.toString().isBlank())) {
				var border = BorderFactory.createMatteBorder(1, 0, 0, 0, gridColor);
				jc.setBorder(BorderFactory.createCompoundBorder(border, jc.getBorder()));
			}
		}
		return c;
	}

	public List<MMLEventList> getCheckedEventList() {
		var list = new ArrayList<MMLEventList>();
		for (int i = 0; i < checkTableModel.checkValue.length; i++) {
			if (checkTableModel.checkValue[i]) {
				list.add(checkTableModel.eventList.get(i));
			}
		}
		return list;
	}
}

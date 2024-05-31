/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco;

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import sun.swing.FilePane;

/**
 * Detail表示をデフォルトにするファイルChooser
 *   - UI変更した場合はオブジェクト生成からやり直すこと.
 */
public final class FixFileChooser extends JFileChooser implements PropertyChangeListener {
	private static final long serialVersionUID = -346484285443111970L;

	private static final String VIEW_TYPE_DETAILS = "viewTypeDetails";
	private static final String VIEW_TYPE = "viewType";

	private final JTable detailsTable;

	public FixFileChooser() {
		super();
		viewDetails();
		detailsTable = findChildComponent(this, JTable.class);

		// リスト表示から詳細表示に変更した場合にも名前列の幅修正を行う.
		addViewTypeAction();
	}

	private void viewDetails() {
		var detailsAction = getActionMap().get(VIEW_TYPE_DETAILS);
		if (detailsAction != null) {
			detailsAction.actionPerformed(null);
		}
	}

	private <T> T findChildComponent(Container container, Class<T> cls) {
		for (var c : container.getComponents()) {
			if (cls.isInstance(c)) {
				return cls.cast(c);
			} else if (c instanceof Container ct) {
				var cc = findChildComponent(ct, cls);
				if (cc != null) {
					return cc;
				}
			}
		}
		return null;
	}

	@Override
	public void setCurrentDirectory(File dir) {
		super.setCurrentDirectory(dir);
		fixNameColumnWidth();
	}

	/**
	 * ディレクトリ移動で、Name列の幅が小さくなってしまう問題の対策.
	 */
	private void fixNameColumnWidth() {
		if (detailsTable != null) {
			if (detailsTable.getParent().getParent() instanceof JScrollPane sc) {
				// リストが少ないときにスクロールバー表示/非表示の確定タイミングが違ってViewPortの幅が古い値になっているので, 常時表示する.
				sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			}
			int viewWidth = detailsTable.getParent().getSize().width;
			int tableWidth = detailsTable.getPreferredSize().width;
			if (tableWidth < viewWidth) {
				var nameCol = detailsTable.getColumnModel().getColumn(0);
				nameCol.setPreferredWidth(nameCol.getPreferredWidth() + viewWidth - tableWidth);
			}
		}
	}

	private void addViewTypeAction() {
		var filePane = findChildComponent(this, FilePane.class);
		if (filePane != null) {
			filePane.addPropertyChangeListener(VIEW_TYPE, this);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// リスト -> 詳細 変更時にも幅調整を行う.
		if (evt.getNewValue().equals(FilePane.VIEWTYPE_DETAILS)) {
			fixNameColumnWidth();
		}
	}
}

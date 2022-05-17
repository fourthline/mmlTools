/*
 * Copyright (C) 2015-2022 たんらる
 */

package jp.fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.OptionalInt;

import javax.swing.JTabbedPane;
import javax.swing.TransferHandler;

import jp.fourthline.mabiicco.midi.MabiDLS;


public final class TrackTabbedPane extends JTabbedPane implements DragGestureListener {
	private static final long serialVersionUID = -5972755124410009800L;

	private OptionalInt targetIndex = OptionalInt.empty();

	public TrackTabbedPane(IMMLManager mmlManager) {
		super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
		setTransferHandler(new TrackTabTransferHandler(this, mmlManager));
	}

	public boolean updateTargetIndex(OptionalInt index) {
		targetIndex = index;
		repaint();
		return index.isPresent();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (targetIndex.isPresent()) {
			Rectangle rect = getBoundsAt(targetIndex.getAsInt());
			int x = rect.x;
			int y = rect.height;
			int[] xPoints = { x-4, x+4, x, x, x };
			int[] yPoints = { 0, 0, 4, y, 4 };
			g.setColor(Color.CYAN);
			g.fillPolygon(xPoints, yPoints, xPoints.length);
			g.setColor(Color.BLUE);
			g.drawPolygon(xPoints, yPoints, xPoints.length);
		}
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent event) {
		Component c = event.getComponent();
		if (!(c instanceof TrackTabbedPane)) {
			return;
		}
		if (MabiDLS.getInstance().getSequencer().isRunning()) {
			// 再生中はドラッグ移動を許可しない
			return;
		}
		TrackTabbedPane tabbedPane = (TrackTabbedPane) c;

		if (tabbedPane.getTabCount() > 1) {
			event.startDrag(DragSource.DefaultMoveDrop, new TrackTabTransfer(tabbedPane));
		}
	}

	private final class TrackTabTransfer implements Transferable {
		private final TrackTabbedPane content;
		private final DataFlavor[] f = {
				new DataFlavor(TrackTabbedPane.class, "obj/TabbedPane")
		};
		private TrackTabTransfer(TrackTabbedPane c) {
			this.content = c;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return f;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return true;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return content;
		}
	}


	private final class TrackTabTransferHandler extends TransferHandler {
		private static final long serialVersionUID = -145263268894584091L;

		private final DataFlavor dataFlavor;
		private final TrackTabbedPane pane;
		private final IMMLManager mmlManager;

		public TrackTabTransferHandler(TrackTabbedPane pane, IMMLManager mmlManager) {
			super();
			this.pane = pane;
			this.mmlManager = mmlManager;
			dataFlavor = new DataFlavor(TrackTabbedPane.class, "obj/TabbedPane");
		}

		private OptionalInt getTargetIndex(Point p) {
			for (int i = 0; i < pane.getTabCount(); i++) {
				if (pane.getBoundsAt(i).contains(p)) {
					if (pane.getSelectedIndex() != i) {
						return OptionalInt.of(i);
					} else {
						break;
					}
				}
			}
			return OptionalInt.empty();
		}

		@Override
		public boolean canImport(TransferSupport support) {
			int action = support.getDropAction();
			if (action == MOVE) {
				if (support.isDataFlavorSupported(dataFlavor)) {
					Point dropPoint = support.getDropLocation().getDropPoint();
					return pane.updateTargetIndex(getTargetIndex(dropPoint));
				}
			}
			return false;
		}

		@Override
		public boolean importData(TransferSupport support) {
			Point dropPoint = support.getDropLocation().getDropPoint();
			int toIndex = getTargetIndex(dropPoint).getAsInt();

			mmlManager.moveTrack(toIndex);
			pane.updateTargetIndex(OptionalInt.empty());
			return true;
		}
	}
}

/*
 * Copyright (C) 2015 たんらる
 */

package jp.fourthline.mabiicco.ui.color;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public final class ColorPicker implements ColorSelectionModel {
	private final JColorChooser chooser;
	private final PreviewPane pane;
	private final ArrayList<ChangeListener> listener = new ArrayList<>();

	private ColorPicker() {
		pane = new PreviewPane(this);
		chooser = new JColorChooser(this);
	}

	private void show() {
		chooser.setPreviewPanel(pane);
		setSelectedTrackColor();
		JOptionPane.showConfirmDialog(null, 
				chooser,
				"Color",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
	}

	private void setSelectedTrackColor() {
		listener.forEach(e -> e.stateChanged(new ChangeEvent(this)));
		pane.repaint();
	}

	@Override
	public Color getSelectedColor() {
		return pane.getSelectedTrackBaseColor().getBaseColor();
	}

	@Override
	public void setSelectedColor(Color color) {
		pane.getSelectedTrackBaseColor().setColor(color);	
		setSelectedTrackColor();
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		this.listener.add(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		this.listener.remove(listener);
	}

	static class PreviewPane extends JPanel implements MouseListener {
		private static final long serialVersionUID = -8305524044560054314L;

		private final int WIDTH = 32;
		private final int HEIGHT = 14;
		private int selectedIndex = 0;

		private ColorPicker parent;

		public PreviewPane(ColorPicker parent) {
			this.parent = parent;
			setPreferredSize(new Dimension(WIDTH*ColorPalette.getInstanceSize(), HEIGHT*6));
			addMouseListener(this);
		}

		public ColorPalette getSelectedTrackBaseColor() {
			return ColorPalette.getInstance(selectedIndex);
		}

		private void drawRect(Graphics g, Color rectColor, Color fillColor, int track, int part) {
			int x = track * WIDTH;
			int y = (part+1) * HEIGHT;
			g.setColor(fillColor);
			g.fillRect(x, y, WIDTH-2, HEIGHT-2);
			g.setColor(rectColor);
			g.drawRect(x, y, WIDTH-3, HEIGHT-3);
		}

		private void drawSample(Graphics g) {
			for (int i = 0; i < ColorPalette.getInstanceSize(); i++) {
				ColorPalette colorPalette = ColorPalette.getInstance(i);
				{
					Color fillColor = colorPalette.getActiveFillColor();
					Color rectColor = colorPalette.getActiveRectColor();
					drawRect(g, rectColor, fillColor, i, 0);
				}
				for (int partIndex = 0; partIndex < 4; partIndex++) {
					Color fillColor = colorPalette.getPartFillColor(partIndex);
					Color rectColor = colorPalette.getPartRectColor(partIndex);
					drawRect(g, rectColor, fillColor, i, partIndex+1);
				}
			}
		}

		private void drawMarker(Graphics g) {
			g.setColor(Color.BLACK);
			g.drawArc(selectedIndex*WIDTH+WIDTH/2-6, 0, 10, 10, 0, 360);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			drawMarker(g);
			drawSample(g);
		}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getPoint().x;
			selectedIndex = x / WIDTH;
			parent.setSelectedTrackColor();
		}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
	}

	public static void main(String args[]) {
		ColorPicker colorPicker = new ColorPicker();
		colorPicker.show();
	}
}

/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.fx;

import java.util.OptionalInt;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mabiicco.ui.IViewTargetMarker;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.Marker;


public final class ColumnView implements IViewTargetMarker {

	private static final Color BEAT_BORDER_COLOR = Color.color(0.4f, 0.4f, 0.4f);
	private static final Color TEMPO_MAKER_FILL_COLOR = Color.color(0.4f, 0.8f, 0.8f);
	private static final Color MAKER_FILL_COLOR = Color.color(0.2f, 0.8f, 0.2f);
	private static final Color TARGET_MAKER_FILL_COLOR = Color.color(0.9f, 0.7f, 0.0f, 0.6f);

	private final PianoRollView pianoRollView;
	private final IMMLManager mmlManager;

	private OptionalInt targetMarker = OptionalInt.empty();

	private Canvas canvas;

	public ColumnView(Canvas canvas, PianoRollView pianoRollView, IMMLManager mmlManager) {
		this.canvas = canvas;
		this.pianoRollView = pianoRollView;
		this.mmlManager = mmlManager;
		paint();
	}

	private void paint() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		paintComponent(gc);
	}
	
	private void paintComponent(GraphicsContext gc) {
		paintRuler(gc);
		paintMarker(gc);
		paintTempoEvents(gc);
		pianoRollView.paintSequenceLine(gc, getHeight());
		paintTargetMarker(gc);
	}
	
	private int getHeight() {
		return (int) canvas.getHeight();
	}

	private int getWidth() {
		return (int) canvas.getWidth();
	}

	/**
	 * ルーラを表示します。
	 */
	private void paintRuler(GraphicsContext gc) {
		long measure = mmlManager.getMMLScore().getMeasureTick();
		long length = pianoRollView.convertXtoTick( getWidth() );
		gc.setFill(BEAT_BORDER_COLOR);
		int count = 0;
		for (long i = 0; i < length; i += measure) {
			int x = pianoRollView.convertTicktoX(i);
			int y1 = 0;
			int y2 = getHeight();
			gc.fillRect(x, y1, 1, y2);

			String s = "" + (count++);
			gc.fillText(s, x+2, y1+10);
		}
	}

	/**
	 * テンポを表示します.
	 */
	private void paintTempoEvents(GraphicsContext gc) {
		MMLScore score = mmlManager.getMMLScore();

		for (MMLTempoEvent tempoEvent : score.getTempoEventList()) {
			int tick = tempoEvent.getTickOffset();
			int x = pianoRollView.convertTicktoX(tick);
			String s = "t" + tempoEvent.getTempo();
			drawMarker(gc, s, x, TEMPO_MAKER_FILL_COLOR, 0);
		}
	}

	private void paintMarker(GraphicsContext gc) {
		if (MabiIccoProperties.getInstance().getEnableViewMarker()) {
			MMLScore score = mmlManager.getMMLScore();

			for (Marker marker : score.getMarkerList()) {
				int tick = marker.getTickOffset();
				int x = pianoRollView.convertTicktoX(tick);
				drawMarker(gc, marker.getName(), x, MAKER_FILL_COLOR, -5);
			}
		}
	}

	private void drawMarker(GraphicsContext gc, String s, int x, Color color, int dy) {
		double xPoints[] = { x-3, x+3, x+3, x, x-3 };
		double yPoints[] = { 16, 16, 22, 25, 22 };
		for (int i = 0; i < yPoints.length; i++) {
			yPoints[i] += dy;
		}

		// label
		gc.setFill(Color.BLACK);
		gc.fillText(s, x+6, 24+dy);

		// icon
		gc.setFill(color);
		gc.fillPolygon(xPoints, yPoints, xPoints.length);
	}

	private void paintTargetMarker(GraphicsContext gc) {
		if (!targetMarker.isPresent()) {
			return;
		}

		int x = pianoRollView.convertTicktoX( targetMarker.getAsInt() );
		double xPoints[] = { x-5, x+5, x+5, x, x-5 };
		double yPoints[] = { 8, 8, 20, 25, 20 };

		// icon
		gc.setFill(TARGET_MAKER_FILL_COLOR);
		gc.fillPolygon(xPoints, yPoints, xPoints.length);
	}

	@Override
	public void PaintOnTarget(int tickOffset) {}


	@Override
	public void PaintOff() {}

}

/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import javax.sound.midi.Sequencer;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import fourthline.mabiicco.MabiIccoProperties;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.editor.IEditAlign;
import fourthline.mabiicco.ui.editor.IMarkerEditor;
import fourthline.mabiicco.ui.editor.MarkerEditor;
import fourthline.mabiicco.ui.editor.MMLTempoEditor;
import fourthline.mmlTools.MMLNoteEvent;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTempoEvent;
import fourthline.mmlTools.Marker;


public final class ColumnPanel extends JPanel implements MouseListener, MouseMotionListener, IViewTargetMarker {
	private static final long serialVersionUID = -6609938350741425221L;

	private static final Color BEAT_BORDER_COLOR = new Color(0.4f, 0.4f, 0.4f);
	private static final Color TEMPO_MAKER_FILL_COLOR = new Color(0.4f, 0.8f, 0.8f);
	private static final Color MAKER_FILL_COLOR = new Color(0.2f, 0.8f, 0.2f);
	private static final Color TARGET_MAKER_FILL_COLOR = new Color(0.9f, 0.7f, 0.0f, 0.6f);

	private final PianoRollView pianoRollView;
	private final IMMLManager mmlManager;
	private final IEditAlign editAlign;

	private final JPopupMenu popupMenu = new JPopupMenu();
	private final ArrayList<IMarkerEditor> markerEditor = new ArrayList<>();

	private OptionalInt targetMarker = OptionalInt.empty();

	public ColumnPanel(Frame parentFrame, PianoRollView pianoRollView, IMMLManager mmlManager, IEditAlign editAlign) {
		super();
		this.pianoRollView = pianoRollView;
		this.mmlManager = mmlManager;
		this.editAlign = editAlign;
		addMouseListener(this);
		addMouseMotionListener(this);

		markerEditor.add( new MMLTempoEditor(parentFrame, mmlManager, editAlign, this) );
		markerEditor.add( new MarkerEditor(parentFrame, mmlManager, editAlign, this) );

		// popupMenu に各MenuItemを登録する.
		markerEditor.forEach(t -> t.getMenuItems().forEach(popupMenu::add));
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), 26);
	}

	@Override
	public int getWidth() {
		int width = pianoRollView.getWidth();
		return width;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g.create();

		paintRuler(g2);
		paintMarker(g2);
		paintTempoEvents(g2);
		pianoRollView.paintSequenceLine(g2, getHeight());
		paintTargetMarker(g2);

		g2.dispose();
	}

	/**
	 * ルーラを表示します。
	 */
	private void paintRuler(Graphics2D g) {
		long measure = mmlManager.getMMLScore().getMeasureTick();
		long length = pianoRollView.convertXtoTick( getWidth() );
		g.setColor(BEAT_BORDER_COLOR);
		int count = 0;
		for (long i = 0; i < length; i += measure) {
			int x = pianoRollView.convertTicktoX(i);
			int y1 = 0;
			int y2 = getHeight();
			g.drawLine(x, y1, x, y2);

			String s = "" + (count++);
			g.drawString(s, x+2, y1+10);
		}
	}

	/**
	 * テンポを表示します.
	 */
	private void paintTempoEvents(Graphics2D g) {
		MMLScore score = mmlManager.getMMLScore();

		for (MMLTempoEvent tempoEvent : score.getTempoEventList()) {
			int tick = tempoEvent.getTickOffset();
			int x = pianoRollView.convertTicktoX(tick);
			String s = "t" + tempoEvent.getTempo();
			drawMarker(g, s, x, TEMPO_MAKER_FILL_COLOR, 0);
		}
	}

	private void paintMarker(Graphics2D g) {
		if (MabiIccoProperties.getInstance().getEnableViewMarker()) {
			MMLScore score = mmlManager.getMMLScore();

			for (Marker marker : score.getMarkerList()) {
				int tick = marker.getTickOffset();
				int x = pianoRollView.convertTicktoX(tick);
				drawMarker(g, marker.getName(), x, MAKER_FILL_COLOR, -5);
			}
		}
	}

	private void drawMarker(Graphics2D g, String s, int x, Color color, int dy) {
		int xPoints[] = { x-3, x+3, x+3, x, x-3 };
		int yPoints[] = { 16, 16, 22, 25, 22 };
		for (int i = 0; i < yPoints.length; i++) {
			yPoints[i] += dy;
		}

		// label
		g.setColor(Color.DARK_GRAY);
		g.drawString(s, x+6, 24+dy);

		// icon
		g.setColor(color);
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		g.setColor(BEAT_BORDER_COLOR);
		g.drawPolygon(xPoints, yPoints, xPoints.length);
	}

	private void paintTargetMarker(Graphics2D g) {
		if (!targetMarker.isPresent()) {
			return;
		}

		int x = pianoRollView.convertTicktoX( targetMarker.getAsInt() );
		int xPoints[] = { x-5, x+5, x+5, x, x-5 };
		int yPoints[] = { 8, 8, 20, 25, 20 };

		// icon
		g.setColor(TARGET_MAKER_FILL_COLOR);
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		g.setColor(BEAT_BORDER_COLOR);
		g.drawPolygon(xPoints, yPoints, xPoints.length);
	}

	private void setSequenceBar(int x) {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		if (!sequencer.isRunning()) {
			long tick = pianoRollView.convertXtoTick(x);
			tick -= tick % editAlign.getEditAlign();
			pianoRollView.setSequenceTick(tick);
			repaint();
			pianoRollView.repaint();
		} else {
			long tick = pianoRollView.convertXtoTick(x);
			// 移動先のテンポに設定する.
			int tempo = mmlManager.getMMLScore().getTempoOnTick(tick);
			sequencer.setTickPosition(tick);
			sequencer.setTempoInBPM(tempo);
			System.out.printf("Sequence update: tick(%d), tempo(%d)\n", tick, tempo);
		}
	}

	private void playAllNoteOnTick(int x) {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		if (!MabiIccoProperties.getInstance().getEnableClickPlay()) {
			return;
		}
		if (!sequencer.isRunning()) {
			MMLScore score = mmlManager.getMMLScore();
			long tick = pianoRollView.convertXtoTick(x);
			int trackIndex = 0;
			List<MMLNoteEvent[]> noteListArray = score.getNoteListOnTickOffset(tick);
			for (MMLNoteEvent[] noteList : noteListArray) {
				int program = score.getTrack(trackIndex).getProgram();
				if (x < 0) {
					MabiDLS.getInstance().playNotes(null, program, trackIndex);
				} else {
					MabiDLS.getInstance().playNotes(noteList, program, trackIndex);
				}

				trackIndex++;
			}
		}
	}

	private void popupAction(Component component, int x, int y) {
		int targetTick = (int)pianoRollView.convertXtoTick(x);
		int delta = (int)pianoRollView.convertXtoTick(6);

		// クリックした位置に、テンポ/マーカー イベントがあれば削除モードになります.
		markerEditor.forEach(t -> t.activateEditMenuItem(targetTick, delta));

		popupMenu.show(component, x, y);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		if (SwingUtilities.isLeftMouseButton(e)) {
		} else if (SwingUtilities.isRightMouseButton(e)) {
			popupAction(e.getComponent(), x, y);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();

		if (SwingUtilities.isLeftMouseButton(e)) {
			setSequenceBar(x);
			playAllNoteOnTick(x);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			playAllNoteOnTick(-1);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		if (SwingUtilities.isLeftMouseButton(e)) {
			playAllNoteOnTick(x);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void PaintOnTarget(int tickOffset) {
		this.targetMarker = OptionalInt.of( tickOffset );
		repaint();
	}

	@Override
	public void PaintOff() {
		this.targetMarker = OptionalInt.empty();
		repaint();
	}
}

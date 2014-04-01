/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.sound.midi.Sequencer;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mabiicco.ui.editor.IEditAlign;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTempoEvent;

/**
 *
 */
public class ColumnPanel extends AbstractMMLView implements MouseListener, ActionListener {
	private static final long serialVersionUID = -6609938350741425221L;

	private static final Color BEAT_BORDER_COLOR = new Color(0.4f, 0.4f, 0.4f);
	private static final Color MAKER_FILL_COLOR = new Color(0.4f, 0.8f, 0.8f);

	private final PianoRollView pianoRollView;
	private final IMMLManager mmlManager;
	private final IEditAlign editAlign;

	private final JPopupMenu popupMenu = new JPopupMenu();
	private final JMenuItem insertTempoMenu;
	private final JMenuItem editTempoMenu;
	private final JMenuItem deleteTempoMenu;

	private final String INSERT_TEMPO = "insert_tempo";
	private final String EDIT_TEMPO   = "edit_tempo";
	private final String DELETE_TEMPO = "delete_tempo";

	public ColumnPanel(PianoRollView pianoRollView, IMMLManager mmlManager, IEditAlign editAlign) {
		super();
		this.pianoRollView = pianoRollView;
		this.mmlManager = mmlManager;
		this.editAlign = editAlign;
		addMouseListener(this);

		insertTempoMenu = newPopupMenu("テンポ挿入");
		insertTempoMenu.setActionCommand(INSERT_TEMPO);
		editTempoMenu = newPopupMenu("テンポ編集");
		editTempoMenu.setActionCommand(EDIT_TEMPO);
		deleteTempoMenu = newPopupMenu("テンポ削除");
		deleteTempoMenu.setActionCommand(DELETE_TEMPO);
	}

	private JMenuItem newPopupMenu(String name) {
		JMenuItem menu = new JMenuItem(name);
		menu.addActionListener(this);
		popupMenu.add(menu);
		return menu;
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
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g.create();
		/**
		 * メジャーを表示します。
		 */
		paintRuler(g2);
		paintTempoEvents(g2);
		pianoRollView.paintSequenceLine(g2, getHeight());

		g2.dispose();
	}

	/**
	 * ルーラを表示します。
	 */
	private void paintRuler(Graphics2D g) {
		int width = getWidth();
		g.setColor(BEAT_BORDER_COLOR);
		int incr = pianoRollView.getMeasureWidth();
		int count = 0;
		for (int i = 0; i < width; i += incr) {
			int x = i;
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
		Iterable<MMLTempoEvent> tempoIterator = score.getTempoEventList();

		for (MMLTempoEvent tempoEvent : tempoIterator) {
			int tick = tempoEvent.getTickOffset();
			int x = pianoRollView.convertTicktoX(tick);
			String s = "t" + tempoEvent.getTempo();
			g.setColor(Color.BLACK);
			g.drawString(s, x+6, 24);
			drawTempoMarker(g, x);
		}
	}

	private void drawTempoMarker(Graphics2D g, int x) {
		int xPoints[] = { x-3, x+3, x+3, x, x-3 };
		int yPoints[] = { 16, 16, 22, 25, 22 };
		g.setColor(MAKER_FILL_COLOR);
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		g.setColor(BEAT_BORDER_COLOR);
		g.drawPolygon(xPoints, yPoints, xPoints.length);
	}

	private void setSequenceBar(int x) {
		Sequencer sequencer = MabiDLS.getInstance().getSequencer();
		if (!sequencer.isRunning()) {
			long tick = pianoRollView.convertXtoTick(x);
			tick -= tick % editAlign.getEditAlign();
			pianoRollView.setSequenceX(pianoRollView.convertTicktoX(tick));
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

	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();

		if (actionCommand.equals(INSERT_TEMPO)) {
			int tempo = showTempoInputDialog("テンポの挿入", 120);
			if (tempo < 0) {
				return;
			}
			List<MMLTempoEvent> tempoList =  mmlManager.getMMLScore().getTempoEventList();
			// tempo align
			int tick = targetTick - (targetTick % this.editAlign.getEditAlign());
			MMLTempoEvent insertTempo = new MMLTempoEvent(tempo, tick);
			insertTempo.appendToListElement(tempoList);
			System.out.println("insert tempo." + tempo);
		} else if (actionCommand.equals(EDIT_TEMPO)) {
			int tempo = showTempoInputDialog("テンポの編集", targetTempoEvent.getTempo());
			if (tempo < 0) {
				return;
			}
			targetTempoEvent.setTempo(tempo);
		} else if (actionCommand.equals(DELETE_TEMPO)) {
			mmlManager.getMMLScore().getTempoEventList().remove(targetTempoEvent);
			System.out.println("delete tempo.");
		}

		mmlManager.updateActivePart();
	}

	private int showTempoInputDialog(String title, int tempo) {
		JPanel panel = new JPanel();
		panel.add(new JLabel("テンポ（32～255）"));
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(tempo, 32, 255, 1));
		spinner.setFocusable(false);
		panel.add(spinner);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(panel, BorderLayout.CENTER);

		int status = JOptionPane.showConfirmDialog(null, cPanel, title, JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
			return ((Integer) spinner.getValue()).intValue();
		}

		return -1;
	}

	private MMLTempoEvent getTempoEventOnTick(int tick) {
		List<MMLTempoEvent> tempoList = mmlManager.getMMLScore().getTempoEventList();
		for (MMLTempoEvent tempoEvent : tempoList) {
			int tickOffset = tempoEvent.getTickOffset();
			int x = pianoRollView.convertTicktoX(tickOffset);
			int tickX1 = (int)pianoRollView.convertXtoTick(x-6);
			int tickX2 = (int)pianoRollView.convertXtoTick(x+6);
			if ( (tick > tickX1) && 
					(tick < tickX2) ) {
				return tempoEvent;
			}
		}

		return null;
	}

	private MMLTempoEvent targetTempoEvent;
	private void popupAction(Component component, int x, int y) {
		targetTick = (int)pianoRollView.convertXtoTick(x);
		targetTick -= targetTick % 6;

		targetTempoEvent = getTempoEventOnTick(targetTick);
		// クリックした位置に、テンポイベントがあれば削除モードになります.
		if (targetTempoEvent == null) {
			insertTempoMenu.setEnabled(true);
			editTempoMenu.setEnabled(false);
			deleteTempoMenu.setEnabled(false);
		} else {
			insertTempoMenu.setEnabled(false);
			editTempoMenu.setEnabled(true);
			deleteTempoMenu.setEnabled(true);
		}

		popupMenu.show(component, x, y);
		System.out.println("targetTick: " + targetTick);
	}

	private int targetTick;
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
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();

		if (SwingUtilities.isLeftMouseButton(e)) {
			setSequenceBar(x);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}

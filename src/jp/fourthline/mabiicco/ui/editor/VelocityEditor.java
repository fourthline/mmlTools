/*
 * Copyright (C) 2023-2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.KeyboardView;
import jp.fourthline.mabiicco.ui.PianoRollScaler.ChangeScaleListener;
import jp.fourthline.mabiicco.ui.PianoRollView;
import jp.fourthline.mabiicco.ui.RightIcon;
import jp.fourthline.mabiicco.ui.SettingButtonGroupItem;
import jp.fourthline.mabiicco.ui.UIUtils;
import jp.fourthline.mabiicco.ui.color.ColorManager;
import jp.fourthline.mabiicco.ui.color.ColorSet;
import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.Measure;

public final class VelocityEditor extends JPanel implements MouseInputListener, ActionListener, ChangeScaleListener, Supplier<VelocityEditor.RangeMode> {
	private static final long serialVersionUID = -4676324927393791707L;

	public static int HEIGHT = 120;
	private final IMMLManager mmlManager;
	private final PianoRollView pianoRollView;
	private final IEditContext editContext;

	private boolean isEdit = false;
	private EditMode mode = EditMode.PENCIL_MODE;

	private RangeMode rangeMode = RangeMode.SELECTED_PART;

	/**
	 * Velocity表示幅
	 */
	public enum VelocityWidth implements SettingButtonGroupItem {
		W2(2), W4(4), W6(6), W8(8), W10(10);
		private final int w;
		VelocityWidth(int width) {
			this.w = width;
		}
		@Override
		public String getButtonName() {
			return w + "px";
		}
		@Override
		public String toString() {
			return getButtonName();
		}
	}

	public enum RangeMode implements SettingButtonGroupItem {
		SELECTED_PART("paintMode.active_part", false, false),
		SELECTED_TRACK("paintMode.active_track", true, false),
		ALL_TRACK("paintMode.all_track", true, true)
		;
		private final String buttonName;
		private final boolean curTrack;
		private final boolean all;
		private RangeMode(String name, boolean curTrack, boolean all) {
			this.buttonName = name;
			this.curTrack = curTrack;
			this.all = all;
		}

		private interface PartAction {
			void action(int trackIndex, int partIndex);
		}

		public void action(VelocityEditor velocityEditor, PartAction func) {
			var mmlManager = velocityEditor.mmlManager;
			int trackIndex = mmlManager.getActiveTrackIndex();
			int partIndex = mmlManager.getActiveMMLPartIndex();

			int trackCount = mmlManager.getMMLScore().getTrackCount();
			int partCount = mmlManager.getMMLScore().getTrack(trackIndex).getMMLEventList().size();

			// Other Track
			if (all) {
				for (int i = 0; i < trackCount; i++) {
					if (trackIndex != i) {
						if (mmlManager.getMMLScore().getTrack(i).isVisible()) {
							for (int p = 0; p < partCount; p++) {
								func.action(i, p);
							}
						}
					}
				}
			}

			// Active Track & Active Part以外
			if (curTrack) {
				if (mmlManager.getMMLScore().getTrack(trackIndex).isVisible()) {
					for (int i = 0; i < partCount; i++) {
						if (partIndex != i) {
							func.action(trackIndex, i);
						}
					}
				}
			}

			// Active Part
			func.action(trackIndex, partIndex);
		}

		@Override
		public String getButtonName() {
			return buttonName;
		}
	}

	/**
	 * 補助線用
	 */
	private static final BasicStroke dashStroke = new BasicStroke(1.0f, 
			BasicStroke.CAP_BUTT, 
			BasicStroke.JOIN_MITER, 
			10.0f, 
			new float[] { 2.0f, 10.0f }, 
			0.0f);

	/**
	 * ノート幅用
	 */
	private static final BasicStroke velocityDashStroke = new BasicStroke(1.0f, 
			BasicStroke.CAP_BUTT, 
			BasicStroke.JOIN_MITER, 
			10.0f, 
			new float[] { 4.0f, 6.0f }, 
			0.0f);

	public VelocityEditor(IMMLManager mmlManager, PianoRollView pianoRollView, IEditContext editContext) {
		this.mmlManager = mmlManager;
		this.pianoRollView = pianoRollView;
		this.editContext = editContext;
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), HEIGHT);
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	public int getWidth() {
		revalidate();
		return pianoRollView.getNewWidth();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g.create();

		paintMeasure(g2);
		paintVelocityLine(g2);
		paintVelocityNote(g2);
		mode.paintEditMode(this, g2);
		paintViewBorder(g2);

		g2.dispose();
	}

	/**
	 * Velocityに対応するyを算出する.
	 * @param velocity
	 * @return
	 */
	private static int velocityToY(int velocity) {
		return HEIGHT - velocityToH(velocity) - 3;
	}

	/**
	 * Velocityの高さを算出する.
	 * @param velocity
	 * @return
	 */
	private static int velocityToH(int velocity) {
		return (velocity * 7);
	}

	/**
	 * Viewのyに対応するVelocityを算出する
	 * @param y
	 * @return
	 */
	private static int yToVelocity(int y) {
		int velocity = (HEIGHT - y) / 7;
		if (velocity < 0)  velocity = 0;
		if (velocity > MMLNoteEvent.MAX_VOL) velocity = MMLNoteEvent.MAX_VOL;
		return velocity;
	}

	/**
	 * TickをViewのxへ変換する.
	 * @param tick
	 * @return
	 */
	private int tickToX(int tick) {
		return pianoRollView.convertTicktoX( tick );
	}

	private int velocityWidth() {
		return MabiIccoProperties.getInstance().velocityWidth.get().w;
	}

	private void paintViewBorder(Graphics2D g) {
		int x = 0;
		int y = 0;
		int width = getWidth();
		int h2 = HEIGHT;

		g.setColor(getBackground());
		g.fillRect(x, y, 2, h2);
		g.fillRect(x+width-2, y, 2, h2);

		g.setColor(this.getForeground());
		g.drawRect(x+2, y+1, width-5, h2-2);
	}

	private void paintMeasure(Graphics2D g) {
		MMLScore score = mmlManager.getMMLScore();
		g.setColor(this.getForeground());
		int y1 = 2;
		int y2 = y1 + HEIGHT - 4;
		for (int tick = 0; tickToX(tick) < getWidth(); tick = Measure.nextMeasure(score, tick, true)) {
			int x = tickToX(tick);
			if (x > 0) {
				g.drawLine(x, y1, x, y2);
			}
		}
	}
	private void paintVelocityLine(Graphics2D g) {
		Stroke oldStroke = g.getStroke();
		g.setStroke(dashStroke);
		g.setColor(this.getForeground());

		List.of(0, 8, 15).forEach(i -> {
			int y = velocityToY(i);
			g.drawLine(0, y, getWidth(), y);
		});

		g.setStroke(oldStroke);
	}

	private MMLEventList getPart(int trackIndex, int partIndex) {
		return mmlManager.getMMLScore().getTrack(trackIndex).getMMLEventAtIndex(partIndex);
	}

	private void paintVelocityNote(Graphics2D g) {
		rangeMode.action(this, (a, b) -> paintVelocityNote(g, a, b));
	}

	private void paintVelocityNote(Graphics2D g, int trackIndex, int partIndex) {
		MMLEventList eventList = getPart(trackIndex, partIndex);
		if (eventList == null) {
			return;
		}

		Color rectColor = ColorManager.defaultColor().getActiveRectColor(trackIndex);
		Color fillColor = ColorManager.defaultColor().getActiveFillColor(trackIndex);

		for (MMLNoteEvent noteEvent : eventList.getMMLNoteEventList()) {
			int x = tickToX(noteEvent.getTickOffset());
			int velocity = noteEvent.getVelocity();
			if (velocity < 0)  velocity = 0;
			if (velocity > MMLNoteEvent.MAX_VOL) velocity = MMLNoteEvent.MAX_VOL;

			// 実のノート長を点線で描画
			int width = Math.max(2, pianoRollView.convertTicktoX( noteEvent.getTick() ));
			if (x + velocityWidth() < x + width) {
				int x2 = x + width - 1;
				int y1 = velocityToY(velocity);
				int y2 = velocityToY(0);
				g.setColor(this.getBackground());
				g.drawLine(x, y1, x2, y1);   // 横の補助線を上塗り

				var oldStroke = g.getStroke();
				g.setColor(rectColor);
				g.setStroke(velocityDashStroke);
				g.drawLine(x + velocityWidth(), y1, x2, y1);
				g.drawLine(x2, y1, x2, y2);
				g.setStroke(oldStroke);
			}

			// 現状用の表示
			UIUtils.drawRect(g, rectColor, fillColor, x, velocityToY(velocity), velocityWidth(), velocityToH(velocity)+1);
		}
	}

	boolean isEdit() {
		return isEdit;
	}

	EditMode getEditMode() {
		return mode;
	}

	void setMode(EditMode mode) {
		this.mode = mode;
	}

	public Insets getBorderInsets(Component c, Insets insets) {
		insets.set(0, 0, HEIGHT, 0);
		return insets;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			mode.start(this, e.getX(), e.getY());
			isEdit = true;
			repaint();
		} else {
			isEdit = false;
			repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (isEdit) {
			mode.update(this, e.getX(), e.getY());
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (isEdit && SwingUtilities.isLeftMouseButton(e)) {
			mode.applyEnd(this);
			isEdit = false;
		}
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (!isEdit) {
			if (mode.move(this, -1, -1)) {
				repaint();
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!isEdit) {
			if (mode.move(this, e.getX(), e.getY())) {
				repaint();
			}
		}
	}

	@Override
	public void changeScale(int x) {
		var p = mode.getCurrentPoint();
		if (isEdit) {
			mode.update(this, x, p.y);
		} else {
			mode.move(this, x, p.y);
		}
	}

	public abstract static sealed class EditMode permits EditMode.PencilMode, EditMode.LineMode {
		public static final EditMode PENCIL_MODE = new PencilMode();
		public static final EditMode LINE_MODE = new LineMode();

		private static final class PencilMode extends EditMode {
			private PencilMode() {
				super("pencil");
			}

			private Point currentPoint = new Point();
			private int currentTickOffset = -1;
			private int currentVelocity = -1;

			@Override
			void start(VelocityEditor velocityEditor, int x, int y) {
				editNoteMap.clear();
				currentPoint.x = x;
				currentPoint.y = y;
				currentTickOffset =  currentVelocity = -1;
				update(velocityEditor, x, y);
			}

			@Override
			void update(VelocityEditor velocityEditor, int x, int y) {
				// xと前のxが大きく離れていたらループして複数処理する.
				int delta = velocityEditor.velocityWidth() / 2;
				Point endPoint = new Point(x, y);
				if (currentPoint.x <= x) {
					// 右方向
					for (int i = currentPoint.x; i < x - delta; i += delta) {
						updateNoteVelocity(velocityEditor, endPoint, i);
					}
				} else {
					// 左方向
					for (int i = currentPoint.x; i > x + delta; i -= delta) {
						updateNoteVelocity(velocityEditor, endPoint, i);
					}
				}
				updateNoteVelocity(velocityEditor, endPoint, x);
				move(velocityEditor, x, y);
			}

			private void updateNoteVelocity(VelocityEditor velocityEditor, Point endPoint, int x) {
				velocityEditor.rangeMode.action(velocityEditor, (a, b) -> updateNoteVelocity(velocityEditor, endPoint, x, a, b));
			}

			private void updateNoteVelocity(VelocityEditor velocityEditor, Point endPoint, int x, int trackIndex, int partIndex) {
				var activePart = velocityEditor.getPart(trackIndex, partIndex);
				for (var noteEvent : activePart.getMMLNoteEventList()) {
					int noteX = velocityEditor.tickToX(noteEvent.getTickOffset());
					if ( (noteX <= x) && (noteX + velocityEditor.velocityWidth() > x) ) {
						if (!velocityEditor.editContext.hasSelectedNote() || velocityEditor.editContext.isSelectedNote(noteEvent)) {
							currentTickOffset = noteEvent.getTickOffset();
							currentVelocity = xtoVelocity(currentPoint, endPoint, x);
							editNoteMap.put(currentTickOffset, currentVelocity);
						}
					}
				}
			}

			@Override
			boolean move(VelocityEditor velocityEditor, int x, int y) {
				currentPoint.x = x;
				currentPoint.y = y;
				return true;
			}

			@Override
			Point getCurrentPoint() {
				return currentPoint;
			}

			@Override
			public void paintEditMode(VelocityEditor velocityEditor, Graphics2D g) {
				if (velocityEditor.isEdit) {
					if (currentTickOffset >= 0) {
						int x = velocityEditor.tickToX(currentTickOffset) + velocityEditor.velocityWidth();
						int y = velocityToY(currentVelocity);
						paintVelocityString(g, new Point(x, y), false);
					}
					paintEditNoteMap(velocityEditor, g);
				}
				if ( (currentPoint.x >= 0) && (currentPoint.y >= 0) ) {
					paintActiveLine(velocityEditor, g);
				}
			}

			/**
			 * マウス位置で対象ノートがある場合に上部に表示する
			 * @param velocityEditor
			 * @param g
			 */
			private void paintActiveLine(VelocityEditor velocityEditor, Graphics2D g) {
				velocityEditor.rangeMode.action(velocityEditor, (a, b) -> paintActiveLine(velocityEditor, g, a, b));
			}

			private void paintActiveLine(VelocityEditor velocityEditor, Graphics2D g, int trackIndex, int partIndex) {
				var activePart = velocityEditor.getPart(trackIndex, partIndex);
				for (var noteEvent : activePart.getMMLNoteEventList()) {
					int noteX = velocityEditor.tickToX(noteEvent.getTickOffset());
					if ( (noteX <= currentPoint.x) && (noteX + velocityEditor.velocityWidth() > currentPoint.x) ) {
						if (!velocityEditor.editContext.hasSelectedNote() || velocityEditor.editContext.isSelectedNote(noteEvent)) {
							int width = 8;
							int halfWidth = width / 2;
							int[] xPoints = { noteX+1, noteX+halfWidth, noteX+width };
							int[] yPoints = { 2, 10, 2 };

							Color rectColor = ColorManager.defaultColor().getActiveRectColor(trackIndex);
							g.setColor(rectColor);
							g.fillPolygon(xPoints, yPoints, xPoints.length);
							g.setColor(velocityEditor.getForeground());
							g.drawPolygon(xPoints, yPoints, xPoints.length);

							break;
						}
					}
				}
			}
		}

		private static final class LineMode extends EditMode {
			private LineMode() {
				super("line");
			}
			private Point startPoint = new Point();
			private Point endPoint = new Point();

			private Point leftPoint() {
				return (startPoint.x < endPoint.x) ? startPoint : endPoint;
			}

			private Point rightPoint() {
				return (startPoint.x < endPoint.x) ? endPoint : startPoint;
			}

			@Override
			void start(VelocityEditor velocityEditor, int x, int y) {
				startPoint.x = endPoint.x = x;
				startPoint.y = endPoint.y = y;
				editNoteMap.clear();
			}

			@Override
			void update(VelocityEditor velocityEditor, int x, int y) {
				endPoint.x = x;
				endPoint.y = y;
				updateEditNote(velocityEditor);
			}

			private void updateEditNote(VelocityEditor velocityEditor) {
				editNoteMap.clear();
				velocityEditor.rangeMode.action(velocityEditor, (a, b) -> updateEditNote(velocityEditor, a, b));
			}

			private void updateEditNote(VelocityEditor velocityEditor, int trackIndex, int partIndex) {
				MMLEventList activePart = velocityEditor.getPart(trackIndex, partIndex);
				if (activePart == null) {
					return;
				}

				for (MMLNoteEvent noteEvent : activePart.getMMLNoteEventList()) {
					int tickOffset = noteEvent.getTickOffset();
					int x = velocityEditor.tickToX(tickOffset) + velocityEditor.velocityWidth() / 2 - 1;   // 真ん中で基準をとる
					int velocity = xtoVelocity(startPoint, endPoint, x);
					if (velocity < 0)  velocity = 0;
					if (velocity > MMLNoteEvent.MAX_VOL) velocity = MMLNoteEvent.MAX_VOL;
					// 更新用の表示
					if (leftPoint().x <= x) {
						if (rightPoint().x <= x) {
							break;
						}
						if (!velocityEditor.editContext.hasSelectedNote() || velocityEditor.editContext.isSelectedNote(noteEvent)) {
							editNoteMap.put(tickOffset, velocity);
							System.out.println(editNoteMap);
						}
					}
				}
			}

			@Override
			Point getCurrentPoint() {
				return endPoint;
			}

			@Override
			public void paintEditMode(VelocityEditor velocityEditor, Graphics2D g) {
				if (velocityEditor.isEdit) {
					Point leftPoint = leftPoint();
					Point rightPoint = rightPoint();
					g.setColor(editColor.get());
					g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
					paintVelocityString(g, leftPoint, true);
					paintVelocityString(g, rightPoint, false);

					paintEditNoteMap(velocityEditor, g);
				}
			}
		}

		private static final ColorSet editColor = ColorSet.create(new Color(32, 32, 255), new Color(0, 128, 255));
		private static final ColorSet editFillColor = ColorSet.create(new Color(0, 0, 0, 64), new Color(128, 128, 128, 64));
		private final String text;

		protected Map<Integer, Integer> editNoteMap = new TreeMap<>();
		abstract void start(VelocityEditor velocityEditor, int x, int y);
		abstract void update(VelocityEditor velocityEditor, int x, int y);
		boolean move(VelocityEditor velocityEditor, int x, int y) { return false; }
		abstract Point getCurrentPoint();

		final void applyEnd(VelocityEditor velocityEditor) {
			velocityEditor.rangeMode.action(velocityEditor, (a, b) -> applyEnd(velocityEditor, a, b));
			editNoteMap.clear();
			velocityEditor.mmlManager.updateActivePart(true);
		}

		final void applyEnd(VelocityEditor velocityEditor, int trackIndex, int partIndex) {
			var activePart = velocityEditor.getPart(trackIndex, partIndex);
			for (var noteEvent : activePart.getMMLNoteEventList()) {
				var o = editNoteMap.get(noteEvent.getTickOffset());
				if (o != null) {
					noteEvent.setVelocity(o);
				}
			}
		}

		abstract void paintEditMode(VelocityEditor velocityEditor, Graphics2D g);

		private EditMode(String text) {
			this.text = AppResource.appText("velocity_editor.mode." + text);
		}

		Map<Integer, Integer> getEditNoteMap() {
			return editNoteMap;
		}

		protected final void paintEditNoteMap(VelocityEditor velocityEditor, Graphics2D g) {
			int width = velocityEditor.velocityWidth();
			editNoteMap.forEach((tickOffset, velocity) -> {
				int x = velocityEditor.tickToX(tickOffset);
				g.setColor(editFillColor.get());
				g.fillRect(x, velocityToY(velocity), width, velocityToH(velocity));
				g.setColor(editColor.get());
				g.drawRect(x, velocityToY(velocity), width, velocityToH(velocity));
				g.drawRect(x+1, velocityToY(velocity)+1, width-2, velocityToH(velocity)-2);
			});
		}

		protected final void paintVelocityString(Graphics2D g, Point point, boolean isleft) {
			g.setColor(editColor.get());
			int velocity = yToVelocity(point.y);
			int x = point.x + (isleft ? -24 : 12);
			int y = point.y + ((velocity > 8) ? 12 : -4);
			g.drawString("v"+velocity, x, y);
		}

		private final String getText() {
			return text;
		}

		private static int xtoVelocity(Point startPoint, Point endPoint, int x) {
			if (startPoint.x != endPoint.x) { 
				if (endPoint.x == x) {
					return yToVelocity(endPoint.y);
				} else if (startPoint.x == x) {
					return yToVelocity(startPoint.y);
				} else {
					double x2 = ((double)x - startPoint.x) / (endPoint.x - startPoint.x);
					double y2 = ((endPoint.y - startPoint.y) * x2) + startPoint.y;
					return yToVelocity((int)y2);
				}
			} else {
				return yToVelocity(endPoint.y);
			}
		}
	}



	public static final class VelocityEditorHeader extends JPanel {
		private static final long serialVersionUID = 317664857011489248L;

		private final Dimension size = new Dimension(KeyboardView.HEADER_WIDTH, HEIGHT);

		private static class ModeButton extends JToggleButton {
			private static final long serialVersionUID = 2077168652765502025L;
			private final EditMode mode;
			private ModeButton(String name, EditMode mode, ActionListener actionListener, ButtonGroup buttonGroup) {
				this(name, mode, actionListener, buttonGroup, false);
			}
			private ModeButton(String name, EditMode mode, ActionListener actionListener, ButtonGroup buttonGroup, boolean selected) {
				super(name, selected);
				this.mode = mode;
				addActionListener(actionListener);
				buttonGroup.add(this);
				setToolTipText(mode.getText());
				setFocusable(false);
			}
			public EditMode getMode() {
				return mode;
			}
		}

		public VelocityEditorHeader(ActionListener actionListener, Supplier<RangeMode> rangeMode) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			ButtonGroup group = new ButtonGroup();
			var b1 = new ModeButton("P", EditMode.PENCIL_MODE, actionListener, group, true);
			var b2 = new ModeButton("L", EditMode.LINE_MODE, actionListener, group);

			// サブボタン
			var bt = new JButton(new RightIcon(10));
			bt.addActionListener(event -> {
				var menu = new JPopupMenu();
				UIUtils.createGroupMenu(menu, AppResource.appText("velocity_editor.width"), MabiIccoProperties.getInstance().velocityWidth);
				menu.show(this, bt.getBounds().x+bt.getBounds().width, bt.getBounds().y);
			});
			bt.setFocusable(false);

			// Rangeボタン
			var br = new JButton("R");
			br.setToolTipText(AppResource.appText("velocity_editor.range_button"));
			br.addActionListener(event -> {
				var menu = new JPopupMenu();
				UIUtils.createGroupMenu(menu, RangeMode.values(), actionListener, rangeMode.get());
				menu.show(this, br.getBounds().x+br.getBounds().width, br.getBounds().y);
			});
			br.setFocusable(false);

			JToolBar toolbar = UIUtils.createToolBar(JToolBar.VERTICAL);
			toolbar.add(bt);
			toolbar.add(br);
			toolbar.addSeparator();
			toolbar.add(b1);
			toolbar.add(b2);

			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			panel.setOpaque(false);
			panel.add(toolbar);
			add(panel);
		}

		@Override
		public Dimension getPreferredSize() {
			return size;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g.create();

			paintVelocityHeaderString(g2, 0);
			paintVelocityHeaderString(g2, 8);
			paintVelocityHeaderString(g2, 15);

			g2.dispose();
		}

		private void paintVelocityHeaderString(Graphics2D g, int velocity) {
			g.setColor(this.getForeground());
			int x = 38;
			int y = velocityToY(velocity);
			g.drawString("v" + velocity, x, y);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var src = e.getSource();
		if (src instanceof VelocityEditorHeader.ModeButton button) {
			mode = button.getMode();
		} else if (src instanceof Supplier button) {
			var obj = button.get();
			if (obj instanceof RangeMode rm) {
				rangeMode = rm;
				repaint();
			}
		}
	}

	@Override
	public RangeMode get() {
		return rangeMode;
	}

	void setRangeMode(RangeMode mode) {
		rangeMode = mode;
	}
}

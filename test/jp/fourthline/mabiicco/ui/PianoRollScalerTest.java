/*
 * Copyright (C) 2023 たんらる
 */

package jp.fourthline.mabiicco.ui;

import static org.junit.Assert.*;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Field;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ActionDispatcher;

public final class PianoRollScalerTest extends UseLoadingDLS {

	private MainFrame frame;
	private MMLSeqView obj;
	private JScrollPane scrollPane;
	private PianoRollScaler pianoRollScaler;
	private JViewport viewport;

	private Object getField(String fieldName) throws Exception {
		Field f = MMLSeqView.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(obj);
	}

	@Before
	public void initilizeObj() throws Exception {
		ActionDispatcher dispatcher = ActionDispatcher.getInstance();
		frame = new MainFrame(dispatcher, dispatcher);
		dispatcher.setMainFrame(frame).initialize();
		frame.setSize(640, 480);
		obj = frame.getMMLSeqView();
		scrollPane = (JScrollPane) getField("scrollPane");
		pianoRollScaler = (PianoRollScaler) getField("pianoRollScaler");
		viewport = scrollPane.getViewport();
		frame.setVisible(true);
	}

	@After
	public void cleanup() {
		frame.setVisible(false);
	}

	private Point mouseWheelMoved(MouseWheelEvent e) throws InterruptedException {
		pianoRollScaler.mouseWheelMoved(e);
		Thread.sleep(10);
		return viewport.getViewPosition();
	}

	@Test
	public void test_scrollV() throws Exception {
		Point p = viewport.getViewPosition();
		assertEquals(536, viewport.getWidth());
		assertEquals(84, viewport.getHeight());
		assertEquals(0, p.x);
		assertEquals(436, p.y);

		var e1 = new MouseWheelEvent(viewport, 0, 0, 0, 0, 0, 0, false, 0, 0, -1);
		var e2 = new MouseWheelEvent(viewport, 0, 0, 0, 0, 0, 0, false, 0, 0, 1);

		// 縦スクロール
		for (int i= 404; i >= 0; i -= 32) {
			p = mouseWheelMoved(e1);
			assertEquals(0, p.x);
			assertEquals(i, p.y);
		}
		assertEquals(20, p.y);
		p = mouseWheelMoved(e1);
		assertEquals(0, p.y);
		for (int i= 32; i <= 768; i += 32) {
			p = mouseWheelMoved(e2);
			assertEquals(0, p.x);
			assertEquals(i, p.y);
		}
		assertEquals(768, p.y);
		p = mouseWheelMoved(e2);
		assertEquals(788, p.y);
	}

	@Test
	public void test_scrollH() throws Exception {
		Point p = viewport.getViewPosition();
		assertEquals(536, viewport.getWidth());
		assertEquals(84, viewport.getHeight());
		assertEquals(0, p.x);
		assertEquals(436, p.y);

		var e1 = new MouseWheelEvent(viewport, 0, 0, InputEvent.SHIFT_DOWN_MASK, 0, 0, 0, false, 0, 0, -1);
		var e2 = new MouseWheelEvent(viewport, 0, 0, InputEvent.SHIFT_DOWN_MASK, 0, 0, 0, false, 0, 0, 1);

		obj.getMMLScore().setUserViewMeasure(20);
		obj.repaint();
		Thread.sleep(1000);

		// 横スクロール
		assertEquals(6.0, pianoRollScaler.getScale(), 0.1);
		for (int i = 64; i < 745; i += 64) {
			p = mouseWheelMoved(e2);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		p = mouseWheelMoved(e2);
		assertEquals(744, p.x);
		for (int i = 744-64; i >= 0; i -= 64) {
			p = mouseWheelMoved(e1);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		assertEquals(40, p.x);
		p = mouseWheelMoved(e1);
		assertEquals(0, p.x);

		pianoRollScaler.expandPianoViewWide();
		pianoRollScaler.expandPianoViewWide();
		assertEquals(4.0, pianoRollScaler.getScale(), 0.1);
		for (int i = 96; i < 1384; i += 96) {
			p = mouseWheelMoved(e2);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		for (int i = 1344-96; i >= 0; i -= 96) {
			p = mouseWheelMoved(e1);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}

		pianoRollScaler.reducePianoViewWide();
		assertEquals(5.0, pianoRollScaler.getScale(), 0.1);
		for (int i = 76; i < 1000; i += 76) {
			p = mouseWheelMoved(e2);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		for (int i = 988-76; i >= 0; i -= 76) {
			p = mouseWheelMoved(e1);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		pianoRollScaler.expandPianoViewWide();
		pianoRollScaler.expandPianoViewWide();
		pianoRollScaler.expandPianoViewWide();
		pianoRollScaler.expandPianoViewWide();
		assertEquals(1.5, pianoRollScaler.getScale(), 0.1);
		for (int i = 256; i < 3841; i += 256) {
			p = mouseWheelMoved(e2);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		for (int i = 3840-256; i >= 0; i -= 256) {
			p = mouseWheelMoved(e1);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}

		// 拍単位
		pianoRollScaler.expandPianoViewWide();
		assertEquals(1.0, pianoRollScaler.getScale(), 0.1);
		for (int i = 96; i < 7144; i += 96) {
			p = mouseWheelMoved(e2);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		for (int i = 7104-96; i >= 0; i -= 96) {
			p = mouseWheelMoved(e1);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}

		// 最小単位 (6px)
		pianoRollScaler.expandPianoViewWide();
		pianoRollScaler.expandPianoViewWide();
		assertEquals(0.5, pianoRollScaler.getScale(), 0.1);
		for (int i = 6; i <= 600; i += 6) {
			p = mouseWheelMoved(e2);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		for (int i = 600-6; i >= 0; i -= 6) {
			p = mouseWheelMoved(e1);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}

		pianoRollScaler.expandPianoViewWide();
		pianoRollScaler.expandPianoViewWide();
		pianoRollScaler.expandPianoViewWide();
		pianoRollScaler.expandPianoViewWide();
		assertEquals(0.1, pianoRollScaler.getScale(), 0.1);
		for (int i = 6; i <= 600; i += 6) {
			p = mouseWheelMoved(e2);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
		for (int i = 600-6; i >= 0; i -= 6) {
			p = mouseWheelMoved(e1);
			System.out.println(p.x);
			assertEquals(i, p.x);
		}
	}

	@Test
	public void test_expand() throws Exception {
		Point p = viewport.getViewPosition();
		var e1 = new MouseWheelEvent(viewport, 0, 0, InputEvent.CTRL_DOWN_MASK, 400, 30, 0, false, 0, 0, -1);
		var e2 = new MouseWheelEvent(viewport, 0, 0, InputEvent.CTRL_DOWN_MASK, 200, 30, 0, false, 0, 0, 1);

		assertEquals(6.0, pianoRollScaler.getScale(), 0.1);
		assertEquals(0, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(5.0, pianoRollScaler.getScale(), 0.1);
		assertEquals(80, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(4.0, pianoRollScaler.getScale(), 0.1);
		assertEquals(180, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(3.0, pianoRollScaler.getScale(), 0.1);
		assertEquals(313, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(2.0, pianoRollScaler.getScale(), 0.1);
		assertEquals(513, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(1.5, pianoRollScaler.getScale(), 0.1);
		assertEquals(646, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(1.0, pianoRollScaler.getScale(), 0.1);
		assertEquals(846, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(0.75, pianoRollScaler.getScale(), 0.1);
		assertEquals(979, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(0.5, pianoRollScaler.getScale(), 0.1);
		assertEquals(1179, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(0.375, pianoRollScaler.getScale(), 0.1);
		assertEquals(1312, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(0.25, pianoRollScaler.getScale(), 0.1);
		assertEquals(1512, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(0.1, pianoRollScaler.getScale(), 0.1);
		assertEquals(2112, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e1);
		assertEquals(0.1, pianoRollScaler.getScale(), 0.1);
		assertEquals(2112, p.x);
		assertEquals(436, p.y);

		p = mouseWheelMoved(e2);
		assertEquals(0.25, pianoRollScaler.getScale(), 0.1);
		assertEquals(1992, p.x);
		assertEquals(436, p.y);
	}
}

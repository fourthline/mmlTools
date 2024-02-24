/*
 * Copyright (C) 2023-2024 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.MabiIccoProperties;
import jp.fourthline.mabiicco.ui.MMLSeqView;
import jp.fourthline.mabiicco.ui.MainFrame;
import jp.fourthline.mabiicco.ui.PianoRollView.NoteHeight;
import jp.fourthline.mabiicco.ui.editor.VelocityEditor.EditMode;
import jp.fourthline.mabiicco.ui.editor.VelocityEditor.RangeMode;
import jp.fourthline.mabiicco.ui.editor.VelocityEditor.VelocityWidth;
import jp.fourthline.mmlTools.MMLNoteEvent;
import jp.fourthline.mmlTools.MMLTrack;
import jp.fourthline.mmlTools.core.MMLText;

public final class VelocityEditorTest extends UseLoadingDLS {

	private MainFrame frame;
	private MMLSeqView obj;
	private MMLEditor editor;
	private VelocityEditor velocityEditor;
	private boolean setting;
	private NoteHeight noteHeight;
	private VelocityWidth vw;

	private Object getField(String fieldName) throws Exception {
		Field f = MMLSeqView.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(obj);
	}

	@Before
	public void initilizeObj() throws Exception {
		setting = MabiIccoProperties.getInstance().velocityEditor.get();
		noteHeight = MabiIccoProperties.getInstance().pianoRollNoteHeight.get();
		vw = MabiIccoProperties.getInstance().velocityWidth.get();
		MabiIccoProperties.getInstance().velocityEditor.set(true);
		MabiIccoProperties.getInstance().pianoRollNoteHeight.set(NoteHeight.H10);
		MabiIccoProperties.getInstance().velocityWidth.set(VelocityWidth.W10);

		ActionDispatcher dispatcher = ActionDispatcher.getInstance();
		frame = new MainFrame(dispatcher, dispatcher);
		dispatcher.setMainFrame(frame).initialize();
		frame.setSize(800, 600);
		obj = frame.getMMLSeqView();
		editor = (MMLEditor) getField("editor");
		velocityEditor = (VelocityEditor) getField("velocityEditor");

		// 別のトラック
		obj.addMMLTrack(new MMLTrack().setMML("MML@,rv0<c;"));
		obj.addMMLTrack(new MMLTrack());

		var mmlpart = obj.getActiveMMLPart();
		mmlpart.addMMLNoteEvent(new MMLNoteEvent(50, 48, 96, 7));
		mmlpart.addMMLNoteEvent(new MMLNoteEvent(51, 48, 96+48, 8));
		mmlpart.addMMLNoteEvent(new MMLNoteEvent(52, 48, 96+96, 9));
		mmlpart.addMMLNoteEvent(new MMLNoteEvent(53, 48, 96+96+48, 10));
		mmlpart.addMMLNoteEvent(new MMLNoteEvent(54, 48, 96+96+96, 11));

		// 別のパート
		obj.getActiveTrack().getMMLEventAtIndex(2).addMMLNoteEvent(new MMLNoteEvent(48, 48, 96, 1));


		frame.setVisible(true);
	}

	@After
	public void cleanup() {
		frame.setVisible(false);
		MabiIccoProperties.getInstance().velocityEditor.set(setting);
		MabiIccoProperties.getInstance().pianoRollNoteHeight.set(noteHeight);
		MabiIccoProperties.getInstance().velocityWidth.set(vw);
	}

	private MouseEvent createMouseEvent(int x, int y, boolean isLeft) {
		int modifiers = isLeft ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON3_DOWN_MASK;
		return new MouseEvent(velocityEditor, 0, 0, modifiers, x, y, 1, false);
	}

	private enum MouseAction {
		PRESS((v, e) -> v.mousePressed(e)),
		DRAG((v, e) -> v.mouseDragged(e)),
		RELEASE((v, e) -> v.mouseReleased(e)),
		ENTER((v, e) -> v.mouseEntered(e)),
		EXIT((v, e) -> v.mouseExited(e));
		private BiConsumer<VelocityEditor, MouseEvent> func;
		private MouseAction(BiConsumer<VelocityEditor, MouseEvent> func) {
			this.func = func;
		}
		private void accpect(VelocityEditor v, MouseEvent e) {
			func.accept(v, e);
		}
	}

	private void testAction(VelocityEditor velocityEditor, MouseAction action, MouseEvent event, boolean isEdit, String expect) {
		action.accpect(velocityEditor, event);
		assertEquals(isEdit, velocityEditor.isEdit());
		if (expect != null) {
			assertEquals(expect, velocityEditor.getEditMode().getEditNoteMap().toString());
		} else {
			System.out.println(velocityEditor.getEditMode().getEditNoteMap().toString());
		}
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {}
	}

	private void testC(String[] expects, String expectMML1, String expectMML2, String expectMML3) throws InterruptedException {
		var mmlpart = obj.getActiveMMLPart();
		mmlpart.addMMLNoteEvent(new MMLNoteEvent(54, 48, 0, 11));
		frame.repaint();

		testAction(velocityEditor, MouseAction.PRESS, createMouseEvent(300, 100, true), true, "{}");
		testAction(velocityEditor, MouseAction.PRESS, createMouseEvent(300, 100, false), false, "{}");
		testAction(velocityEditor, MouseAction.RELEASE, createMouseEvent(300, 100, false), false, "{}");
		testAction(velocityEditor, MouseAction.RELEASE, createMouseEvent(300, 100, true), false, "{}");
		testAction(velocityEditor, MouseAction.PRESS, createMouseEvent(300, 100, false), false, "{}");
		testAction(velocityEditor, MouseAction.RELEASE, createMouseEvent(300, 100, false), false, "{}");

		assertEquals(expects[0], velocityEditor.getEditMode().getEditNoteMap().toString());

		// 右方向
		testAction(velocityEditor, MouseAction.PRESS, createMouseEvent(2, 30, true), true, expects[1]);
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(20, 30, true), true, expects[2]);  // 横移動
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(20, 60, true), true, expects[3]);  // 縦移動
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(20, 100, true), true, expects[4]); // 縦移動
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(40, 100, true), true, expects[5]); // 横移動
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(60, 100, true), true, expects[6]); // 横移動
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(60, -20, true), true, expects[7]); // 縦移動 (out)
		testAction(velocityEditor, MouseAction.EXIT, createMouseEvent(60, -20, true), true, expects[7]); // 縦移動 (exit)
		testAction(velocityEditor, MouseAction.ENTER, createMouseEvent(60, 0, true), true, null); // 縦移動 (enter)
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(60, 100, true), true, expects[6]); // 元の場所
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(60, 400, true), true, expects[8]); // 縦移動 (out)
		testAction(velocityEditor, MouseAction.EXIT, createMouseEvent(60, 400, true), true, expects[8]); // 縦移動 (exit)
		testAction(velocityEditor, MouseAction.ENTER, createMouseEvent(60, 100, true), true, null); // 縦移動 (enter)
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(60, 100, true), true, expects[6]); // 元の場所
		testAction(velocityEditor, MouseAction.RELEASE, createMouseEvent(60, 100, true), false, expects[9]);
		assertEquals(expectMML1, obj.getActiveTrack().getMabiMML());

		mmlpart.getMMLNoteEventList().forEach(t -> t.setVelocity(15));

		// 左方向
		testAction(velocityEditor, MouseAction.PRESS, createMouseEvent(60, 100, true), true, expects[10]);
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(2, 30, true), true, expects[11]);    // 横移動
		testAction(velocityEditor, MouseAction.RELEASE, createMouseEvent(2, 30, true), false, expects[12]);
		assertEquals(expectMML2, obj.getActiveTrack().getMabiMML());

		mmlpart.getMMLNoteEventList().forEach(t -> t.setVelocity(15));

		// 元の場所に戻るケース
		testAction(velocityEditor, MouseAction.PRESS, createMouseEvent(2, 100, true), true, expects[13]);
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(60, 30, true), true, expects[14]);    // 横移動
		testAction(velocityEditor, MouseAction.DRAG, createMouseEvent(2, 100, true), true, expects[15]);    // 横移動
		testAction(velocityEditor, MouseAction.RELEASE, createMouseEvent(2, 100, true), false, expects[16]);
		assertEquals(expectMML3, obj.getActiveTrack().getMabiMML());
	}

	private List<MMLNoteEvent> pData1 = List.of(new MMLNoteEvent(54, 48, 0, 11));
	private List<MMLNoteEvent> pData2 = List.of(new MMLNoteEvent(54, 48, 0, 11), new MMLNoteEvent(50, 48, 48, 11));
	private String[] pencelModeExpects = {
			"{}",
			"{0=12}",
			"{0=12, 96=12}",
			"{0=12, 96=8}",
			"{0=12, 96=2}",
			"{0=12, 96=2, 144=2, 192=2, 240=2}",
			"{0=12, 96=2, 144=2, 192=2, 240=2, 288=2}",
			"{0=12, 96=2, 144=2, 192=2, 240=2, 288=2}",
			"{0=12, 96=2, 144=2, 192=2, 240=2, 288=2}",
			"{}",
			"{}",
			"{0=12, 96=9, 144=9, 192=7, 240=6, 288=4}",
			"{}",
			"{0=2}",
			"{0=3, 96=6, 144=8, 192=9, 240=10, 288=11}",
			"{0=2, 96=6, 144=6, 192=8, 240=9, 288=11}",
			"{}"
	};
	private String[] pencelModeSelectedExpects = {
			"{}",
			"{}",
			"{96=12}",
			"{96=8}",
			"{96=2}",
			"{96=2, 144=2, 192=2, 240=2}",
			"{96=2, 144=2, 192=2, 240=2, 288=2}",
			"{96=2, 144=2, 192=2, 240=2, 288=2}",
			"{96=2, 144=2, 192=2, 240=2, 288=2}",
			"{}",
			"{}",
			"{96=9, 144=9, 192=7, 240=6, 288=4}",
			"{}",
			"{}",
			"{96=6, 144=8, 192=9, 240=10, 288=11}",
			"{96=6, 144=6, 192=8, 240=9, 288=11}",
			"{}"
	};
	private String[] lineModeExpects = {
			"{}",
			"{}",
			"{0=12, 48=12}",
			"{0=12, 48=10}",
			"{0=11, 48=7}",
			"{0=12, 48=10, 96=8, 144=6, 192=4}",
			"{0=12, 48=11, 96=9, 144=8, 192=7, 240=5, 288=4}",
			"{0=13, 48=14, 96=15, 144=15, 192=15, 240=15, 288=15}",
			"{0=11, 48=3, 96=0, 144=0, 192=0, 240=0, 288=0}",
			"{}",
			"{}",
			"{0=12, 48=11, 96=9, 144=8, 192=7, 240=5, 288=4}",
			"{}",
			"{}",
			"{0=3, 48=4, 96=6, 144=7, 192=8, 240=10, 288=11}",
			"{}",
			"{}"
	};
	private String[] lineModeSelectedExpects = {
			"{}",
			"{}",
			"{}",
			"{}",
			"{}",
			"{96=8, 144=6, 192=4}",
			"{96=9, 144=8, 192=7, 240=5, 288=4}",
			"{96=15, 144=15, 192=15, 240=15, 288=15}",
			"{96=0, 144=0, 192=0, 240=0, 288=0}",
			"{}",
			"{}",
			"{96=9, 144=8, 192=7, 240=5, 288=4}",
			"{}",
			"{}",
			"{96=6, 144=7, 192=8, 240=10, 288=11}",
			"{}",
			"{}"
	};

	private void testMode(EditMode editMode, RangeMode rangeMode, boolean select, List<MMLNoteEvent> addNotes, String[] expects, DataSet dataSet) throws InterruptedException {
		assertEquals(false, velocityEditor.isEdit());
		velocityEditor.setMode(editMode);
		velocityEditor.setRangeMode(rangeMode);

		if (select) {
			editor.selectAll();
		}

		var mmlpart = obj.getActiveMMLPart();
		addNotes.forEach(t -> mmlpart.addMMLNoteEvent(t.clone()));
		frame.repaint();

		testC(expects, dataSet.e1(rangeMode), dataSet.e2(rangeMode), dataSet.e3(rangeMode));
		assertEquals(dataSet.o(rangeMode), obj.getMMLScore().getTrack(1).getMabiMML());
	}

	private static record DataSet(String m1, String m2, String m3, String c0, String c1, String c2, String c3, String o0, String o3) {
		private String e1(RangeMode mode) {
			String s = (mode == RangeMode.SELECTED_PART) ? c0 : c1;
			return new MMLText().setMMLText( new String[] { m1, "", s } ).getMML();
		}
		private String e2(RangeMode mode) {
			String s = (mode == RangeMode.SELECTED_PART) ? c0 : c2;
			return new MMLText().setMMLText( new String[] { m2, "", s } ).getMML();
		}
		private String e3(RangeMode mode) {
			String s = (mode == RangeMode.SELECTED_PART) ? c0 : c3;
			return new MMLText().setMMLText( new String[] { m3, "", s } ).getMML();
		}
		private String o(RangeMode mode) {
			String s = (mode != RangeMode.ALL_TRACK) ? o0 : o3;
			return new MMLText().setMMLText( new String[] { "<>", s, "" } ).getMML();
		}
	}

	private static String PENCIL_M1 = "v12l8f+rv2dd+eff+";
	private static String PENCIL_M2 = "v12l8f+rv9dd+v7ev6fv4f+";
	private static String PENCIL_M3 = "v2l8f+rv6dd+v8ev9fv11f+";
	private static String PENCIL_C0 = "rv1c8";
	private static String PENCIL_C1 = "rv2c8";
	private static String PENCIL_C2 = "rv9c8";
	private static String PENCIL_C3 = "rv6c8";
	private static String PENCIL_O0 = "rv0<c";
	private static String PENCIL_O3 = "rv6<c";
	private static DataSet PENCIL_DATA = new DataSet(PENCIL_M1, PENCIL_M2, PENCIL_M3, PENCIL_C0, PENCIL_C1, PENCIL_C2, PENCIL_C3, PENCIL_O0, PENCIL_O3);
	private static DataSet PENCIL_DATA2 = new DataSet(PENCIL_M1, PENCIL_M2, PENCIL_M3, PENCIL_C0, PENCIL_C0, PENCIL_C0, PENCIL_C0, PENCIL_O0, PENCIL_O0);

	@Test
	public void test_pencilMode_1() throws InterruptedException {
		testMode(EditMode.PENCIL_MODE, RangeMode.SELECTED_PART, false, pData1, pencelModeExpects, PENCIL_DATA);
	}

	@Test
	public void test_pencilMode_2() throws InterruptedException {
		testMode(EditMode.PENCIL_MODE, RangeMode.SELECTED_TRACK, false, pData1, pencelModeExpects, PENCIL_DATA);
	}

	@Test
	public void test_pencilMode_3() throws InterruptedException {
		testMode(EditMode.PENCIL_MODE, RangeMode.ALL_TRACK, false, pData1, pencelModeExpects, PENCIL_DATA);
	}

	@Test
	public void test_pencilModeTrackVisible_1() throws InterruptedException {
		obj.getMMLScore().getTrackList().forEach(t -> t.setVisible(false));
		testMode(EditMode.PENCIL_MODE, RangeMode.SELECTED_PART, false, pData1, pencelModeExpects, PENCIL_DATA2);
	}

	@Test
	public void test_pencilModeTrackVisible_2() throws InterruptedException {
		obj.getMMLScore().getTrackList().forEach(t -> t.setVisible(false));
		testMode(EditMode.PENCIL_MODE, RangeMode.SELECTED_TRACK, false, pData1, pencelModeExpects, PENCIL_DATA2);
	}

	@Test
	public void test_pencilModeTrackVisible_3() throws InterruptedException {
		obj.getMMLScore().getTrackList().forEach(t -> t.setVisible(false));
		testMode(EditMode.PENCIL_MODE, RangeMode.ALL_TRACK, false, pData1, pencelModeExpects, PENCIL_DATA2);
	}

	private static String PENCIL_S_M1 = "v11l8f+rv2dd+eff+";
	private static String PENCIL_S_M2 = "v15l8f+rv9dd+v7ev6fv4f+";
	private static String PENCIL_S_M3 = "v15l8f+rv6dd+v8ev9fv11f+";
	private static DataSet PENCIL_S_DATA = new DataSet(PENCIL_S_M1, PENCIL_S_M2, PENCIL_S_M3, PENCIL_C0, PENCIL_C1, PENCIL_C2, PENCIL_C3, PENCIL_O0, PENCIL_O3);


	@Test
	public void test_pencilMode_selected_1() throws InterruptedException {
		testMode(EditMode.PENCIL_MODE, RangeMode.SELECTED_PART, true, pData1, pencelModeSelectedExpects, PENCIL_S_DATA);
	}

	@Test
	public void test_pencilMode_selected_2() throws InterruptedException {
		testMode(EditMode.PENCIL_MODE, RangeMode.SELECTED_TRACK, true, pData1, pencelModeSelectedExpects, PENCIL_S_DATA);
	}

	@Test
	public void test_pencilMode_selected_3() throws InterruptedException {
		testMode(EditMode.PENCIL_MODE, RangeMode.ALL_TRACK, true, pData1, pencelModeSelectedExpects, PENCIL_S_DATA);
	}

	private static String LINE_M1 = "v12l8f+v11dv9dv8d+v7ev5fv4f+";
	private static String LINE_M2 = "v12l8f+v11dv9dv8d+v7ev5fv4f+";
	private static String LINE_M3 = "v15l8f+ddd+eff+";
	private static String LINE_C0 = PENCIL_C0;
	private static String LINE_CN = PENCIL_C2;
	private static String LINE_O0 = PENCIL_O0;
	private static String LINE_O3 = "rv9<c";
	private static DataSet LINE_DATA = new DataSet(LINE_M1, LINE_M2, LINE_M3, LINE_C0, LINE_CN, LINE_CN, LINE_CN, LINE_O0, LINE_O3);

	@Test
	public void test_lineMode_1() throws InterruptedException {
		testMode(EditMode.LINE_MODE, RangeMode.SELECTED_PART, false, pData2, lineModeExpects, LINE_DATA);
	}

	@Test
	public void test_lineMode_2() throws InterruptedException {
		testMode(EditMode.LINE_MODE, RangeMode.SELECTED_TRACK, false, pData2, lineModeExpects, LINE_DATA);
	}

	@Test
	public void test_lineMode_3() throws InterruptedException {
		testMode(EditMode.LINE_MODE, RangeMode.ALL_TRACK, false, pData2, lineModeExpects, LINE_DATA);
	}

	private static String LINE_S_M1 = "v11l8f+dv9dv8d+v7ev5fv4f+";
	private static String LINE_S_M2 = "v15l8f+dv9dv8d+v7ev5fv4f+";
	private static String LINE_S_M3 = "v15l8f+ddd+eff+";
	private static DataSet LINE_S_DATA = new DataSet(LINE_S_M1, LINE_S_M2, LINE_S_M3, LINE_C0, LINE_CN, LINE_CN, LINE_CN, LINE_O0, LINE_O3);

	@Test
	public void test_lineMode_selected_1() throws InterruptedException {
		testMode(EditMode.LINE_MODE, RangeMode.SELECTED_PART, true, pData2, lineModeSelectedExpects, LINE_S_DATA);
	}
	@Test
	public void test_lineMode_selected_2() throws InterruptedException {
		testMode(EditMode.LINE_MODE, RangeMode.SELECTED_TRACK, true, pData2, lineModeSelectedExpects, LINE_S_DATA);
	}
	@Test
	public void test_lineMode_selected_3() throws InterruptedException {
		testMode(EditMode.LINE_MODE, RangeMode.ALL_TRACK, true, pData2, lineModeSelectedExpects, LINE_S_DATA);
	}
}

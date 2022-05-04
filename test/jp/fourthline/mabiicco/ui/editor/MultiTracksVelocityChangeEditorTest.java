/*
 * Copyright (C) 2018 たんらる
 */

package jp.fourthline.mabiicco.ui.editor;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import javax.swing.JDialog;
import javax.swing.JSpinner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.fourthline.UseLoadingDLS;
import jp.fourthline.mabiicco.ui.IMMLManager;
import jp.fourthline.mabiicco.ui.MMLSeqView;
import jp.fourthline.mabiicco.ui.editor.MultiTracksVelocityChangeEditor;
import jp.fourthline.mabiicco.ui.mml.TrackListTable;
import jp.fourthline.mmlTools.MMLScore;
import jp.fourthline.mmlTools.MMLTrack;

public final class MultiTracksVelocityChangeEditorTest extends UseLoadingDLS {

	private MultiTracksVelocityChangeEditor obj;
	private TrackListTable table;
	private JSpinner spinner;
	private JDialog dialog;

	private IMMLManager mmlManager;

	private Object getField(String name) throws Exception {
		Field f = MultiTracksVelocityChangeEditor.class.getDeclaredField(name);
		f.setAccessible(true);
		return f.get(obj);
	}

	@Before
	public void setUp() {
		mmlManager = new MMLSeqView(null);
	}

	private void createObj(MMLScore mmlScore) {
		try {
			mmlManager.setMMLScore(mmlScore);
			obj = new MultiTracksVelocityChangeEditor(null, mmlManager);
			table = (TrackListTable) getField("table");
			spinner = (JSpinner) getField("spinner");
			dialog = (JDialog) getField("dialog");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@After
	public void tearDown() throws Exception {
		dialog.setVisible(false);
	}

	@Test
	public final void testMultiTracksVelocityChangeEditor() throws InterruptedException {
		MMLScore mmlScore = new MMLScore();
		mmlScore.addTrack(new MMLTrack().setMML("MML@aaa"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@bbb"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@ccc"));
		createObj(mmlScore);
		new Thread(() -> obj.showDialog()).start();
		Thread.sleep(500);

		assertEquals(3, table.getCheckList().length);
		assertEquals("MML@aaa,,;", mmlScore.getTrack(0).getMabiMML());
		assertEquals("MML@bbb,,;", mmlScore.getTrack(1).getMabiMML());
		assertEquals("MML@ccc,,;", mmlScore.getTrack(2).getMabiMML());

		// 単一
		table.getCheckList()[1] = true;
		spinner.setValue(5);
		obj.apply();
		assertEquals("MML@aaa,,;", mmlScore.getTrack(0).getMabiMML());
		assertEquals("MML@v13bbb,,;", mmlScore.getTrack(1).getMabiMML());
		assertEquals("MML@ccc,,;", mmlScore.getTrack(2).getMabiMML());

		// 複数
		table.getCheckList()[0] = true;
		table.getCheckList()[1] = false;
		table.getCheckList()[2] = true;
		spinner.setValue(-6);
		obj.apply();
		assertEquals("MML@v2aaa,,;", mmlScore.getTrack(0).getMabiMML());
		assertEquals("MML@v13bbb,,;", mmlScore.getTrack(1).getMabiMML());
		assertEquals("MML@v2ccc,,;", mmlScore.getTrack(2).getMabiMML());
	}

}

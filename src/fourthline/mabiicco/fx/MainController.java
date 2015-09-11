/*
　* Copyright (C) 2015 たんらる
　*/

package fourthline.mabiicco.fx;

import java.net.URL;
import java.util.ResourceBundle;

import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;

/**
 * 
 */
public final class MainController implements Initializable, IMMLManager {
	@FXML private Canvas canvas;

	private PianoRollView pianoRollView;

	@Override
	public void initialize(URL url, ResourceBundle resouceBundle) {
		System.out.println(this.getClass().getName()+"["+resouceBundle.getLocale()+"]: "+url);
		this.pianoRollView = new PianoRollView(canvas, this);
	}

	private MMLScore mmlScore = new MMLScore();

	public MainController() {
		mmlScore.addTrack(new MMLTrack().setMML("MML@o0cccc,o0eeee,o0gggg,o0bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o1cccc,o1eeee,o1gggg,o1bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o2cccc,o2eeee,o2gggg,o2bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o3cccc,o3eeee,o3gggg,o3bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o4cccc,o4eeee,o4gggg,o4bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o5cccc,o5eeee,o5gggg,o5bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o6cccc,o6eeee,o6gggg,o6bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o7cccc,o7eeee,o7gggg,o7bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o8cccc,o8eeee,o8gggg,o8bbbb;"));
	}
	
	@Override
	public MMLScore getMMLScore() {
		return mmlScore;
	}

	@Override
	public int getActiveTrackIndex() {
		return 0;
	}

	@Override
	public MMLEventList getActiveMMLPart() {
		return mmlScore.getTrack(0).getMMLEventAtIndex(0);
	}

	@Override
	public void updateActivePart(boolean generate) {
	}

	@Override
	public void updateActiveTrackProgram(int trackIndex, int program,
			int songProgram) {
	}

	@Override
	public int getActivePartProgram() {
		return 0;
	}

	@Override
	public boolean selectTrackOnExistNote(int note, int tickOffset) {
		return false;
	}

	@Override
	public void setMMLselectedTrack(MMLTrack track) {}

	@Override
	public void addMMLTrack(MMLTrack track) {}

	@Override
	public void moveTrack(int toIndex) {}

}

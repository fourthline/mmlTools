/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.fx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.ui.IMMLManager;
import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.parser.IMMLFileParser;
import fourthline.mmlTools.parser.MMLParseException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 
 */
public final class MainController implements Initializable {
	@FXML private Canvas canvas;
	
	@FXML private MenuItem newFileMenu;

	private Stage stage;
	private PianoRollView pianoRollView;
	private final IMMLManager mmlContents = new MMLContents();

	@Override
	public void initialize(URL url, ResourceBundle resouceBundle) {
		System.out.println(this.getClass().getName()+"["+resouceBundle.getLocale()+"]: "+url);
		this.pianoRollView = new PianoRollView(canvas, mmlContents);
	}

	public MainController() {
		MMLScore mmlScore = mmlContents.getMMLScore();
		mmlScore.addTrack(new MMLTrack().setMML("MML@o0v7ccccr1o4rc64rc32rc16rc8rc4rc2rc1,o0eeee,o0gggg,o0bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o1cccc,o1eeee,o1gggg,o1bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o2cccc,o2eeee,o2gggg,o2bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o3cccc,o3eeee,o3gggg,o3bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o4cccc,o4eeee,o4gggg,o4bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o5cccc,o5eeee,o5gggg,o5bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o6cccc,o6eeee,o6gggg,o6bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o7cccc,o7eeee,o7gggg,o7bbbb;"));
		mmlScore.addTrack(new MMLTrack().setMML("MML@o8cccc,o8eeee,o8gggg,o8bbbb;"));
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void newFileAction(ActionEvent e) {
		System.out.println("newFileAction");
	}

	@FXML
	private void openFileAction(ActionEvent e) {
		System.out.println("openFileAction");
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(AppResource.appText("file.mmi"), "*.mmi"));
		fileChooser.setInitialDirectory(null);
		File file = fileChooser.showOpenDialog(stage);
		System.out.println(file);
		if (file != null) {
			mmlContents.setMMLScore( fileParse(file) );
			pianoRollView.paint();
		}
	}

	private MMLScore fileParse(File file) {
		MMLScore score = null;
		try {
			IMMLFileParser fileParser = IMMLFileParser.getParser(file);
			score = fileParser.parse(new FileInputStream(file));
		} catch (FileNotFoundException e) {
		} catch (MMLParseException e) {
		}

		// mabiicco由来のファイルであれば, generateされたものにする.
		if (score != null) {
			score = score.toGeneratedScore();
		}
		return score;
	}

	@FXML
	private void reloadFileAction(ActionEvent e) {
		System.out.println("reloadFileAction");
	}
}

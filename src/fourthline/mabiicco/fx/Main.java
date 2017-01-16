/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.fx;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.LineUnavailableException;

import fourthline.mabiicco.AppResource;
import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public final class Main extends Application {

	private Scene createScene(Stage stage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Main.fxml"), AppResource.getResourceBundle());
			Parent root = fxmlLoader.load();
			MainController controller = fxmlLoader.getController();
			controller.setStage(stage);
			return new Scene(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new AssertionError();
	}

	@Override
	public void start(Stage stage) {
		stage.setScene(createScene(stage));
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});
		stage.show();
	}

	private static void loadDLS() {
		try {
			MabiDLS midi = MabiDLS.getInstance();
			if (midi.getAvailableInstByInstType(InstType.MAIN_INST_LIST).length == 0) {
				midi.initializeMIDI();
				midi.loadingDLSFile(new File(MabiDLS.DEFALUT_DLS_PATH));
			}
		} catch (IOException | MidiUnavailableException | InvalidMidiDataException | LineUnavailableException e) {
			throw new AssertionError();
		}
	}

	public static void main(String[] args) {
		loadDLS();
		launch(args);
	}
}

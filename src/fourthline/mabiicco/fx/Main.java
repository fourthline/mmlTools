/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.fx;

import java.io.IOException;

import fourthline.mabiicco.AppResource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public final class Main extends Application {

	private Scene createScene() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Main.fxml"), AppResource.getResourceBundle());
			Parent root = fxmlLoader.load();
			return new Scene(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new AssertionError();
	}

	@Override
	public void start(Stage stage) {
		stage.setScene(createScene());
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}

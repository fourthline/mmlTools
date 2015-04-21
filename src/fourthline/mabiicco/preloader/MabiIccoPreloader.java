/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.preloader;

import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.io.IOException;

import fourthline.mabiicco.AppResource;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class MabiIccoPreloader extends Preloader {

	private StartupController controller;
	private Stage stage;

	private Scene createPreloaderScene() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Startup.fxml"));
			Parent root = (Parent) fxmlLoader.load();
			controller = fxmlLoader.getController();
			controller.setVersionText(AppResource.getVersionText());
			return new Scene(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new AssertionError();
	}

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setScene(createPreloaderScene());
		SplashScreen splashScreen = SplashScreen.getSplashScreen();
		if (splashScreen != null) {
			Rectangle rect = splashScreen.getBounds();
			stage.setX(rect.x);
			stage.setY(rect.y);
		}
		stage.show();
	}

	@Override
	public void handleApplicationNotification(PreloaderNotification pn) {
		if (pn instanceof MabiIccoPreloaderNotification) {
			MabiIccoPreloaderNotification notify = (MabiIccoPreloaderNotification) pn;
			String message = notify.getMessage();
			double progress = notify.getProgress();
			controller.addStatusText(message);
			controller.updateProgress(progress);
		} else if (pn instanceof StateChangeNotification) {
			stage.hide();
		}
	}
}

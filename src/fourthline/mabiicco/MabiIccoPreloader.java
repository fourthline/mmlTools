/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MabiIccoPreloader extends Preloader {

	private StartupController controller;
	private Stage stage;

	private Scene createPreloaderScene() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Startup.fxml"));
		Parent root = (Parent) fxmlLoader.load();
		controller = fxmlLoader.getController();
		controller.versionLabel.setText(getManifestValue("Implementation-Version"));
		return new Scene(root);
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setScene(createPreloaderScene());
		stage.show();
	}

	@Override
	public void handleApplicationNotification(PreloaderNotification pn) {
		if (pn instanceof MabiIccoPreloaderNotification) {
			MabiIccoPreloaderNotification notify = (MabiIccoPreloaderNotification) pn;
			String message = notify.getMessage();
			double progress = notify.getProgress();
			controller.text.setText( controller.text.getText() + message );
			controller.bar.setProgress(progress/100.0);
		} else if (pn instanceof StateChangeNotification) {
			stage.hide();
		}
	}

	private String getManifestValue(String key) {
		try {
			InputStream is = this.getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest mf;
			mf = new Manifest(is);
			Attributes a = mf.getMainAttributes();
			String val = a.getValue(key);
			return val;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
}

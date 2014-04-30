/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.preloader;

import fourthline.mabiicco.AppResource;
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
		controller.versionLabel.setText(AppResource.getManifestValue("Implementation-Version"));
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

}

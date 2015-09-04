/*
 * Copyright (C) 2014-2015 たんらる
 */

package fourthline.mabiicco.preloader;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public final class StartupController implements Initializable {
	@FXML private TextArea text;
	@FXML private ProgressBar bar;
	@FXML private Label versionLabel;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	public void setVersionText(String s) {
		versionLabel.setText("Version " + s);
	}

	public void addStatusText(String s) {
		text.setText( text.getText() + s );
	}

	public void updateProgress(double progress) {
		bar.setProgress(progress/100.0);
	}
}

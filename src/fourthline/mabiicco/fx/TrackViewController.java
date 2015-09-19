/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco.fx;

import java.net.URL;
import java.util.OptionalInt;
import java.util.ResourceBundle;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mabiicco.midi.InstType;
import fourthline.mabiicco.midi.MabiDLS;
import fourthline.mmlTools.MMLTrack;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import static fourthline.mabiicco.AppResource.appText;

/**
 * 
 */
public class TrackViewController implements Initializable {

	@FXML private ComboBox<InstClass> instComboBox;
	@FXML private ComboBox<InstClass> songComboBox;
	@FXML private TextField melodyText;
	@FXML private TextField chord1Text;
	@FXML private TextField chord2Text;
	@FXML private TextField songText;
	@FXML private Label rankText;

	@FXML private Button muteButton;
	@FXML private ImageView muteOnImage;
	@FXML private ImageView muteOffImage;

	private OptionalInt trackIndex = OptionalInt.empty();
	private final MabiDLS dls = MabiDLS.getInstance();

	private final InstClass noUseSongEx = new InstClass(appText("instrument.nouse_chorus"), -1, -1, null);

	public void setMMLTrack(MMLTrack mmlTrack, int trackIndex) {
		this.trackIndex = OptionalInt.of(trackIndex);

		String mml[] = mmlTrack.getMabiMMLArray();
		TextField field[] = { melodyText, chord1Text, chord2Text, songText };
		for (int i = 0; i < mml.length; i++) {
			field[i].setText( mml[i] );
		}
		rankText.setText( mmlTrack.mmlRankFormat() );

		InstClass inst = dls.getInstByProgram( mmlTrack.getProgram() );
		if ( !instComboBox.getItems().contains(inst) ) {
			inst = instComboBox.getItems().get(0);
		}
		instComboBox.setValue( inst );

		InstClass songInst = dls.getInstByProgram( mmlTrack.getSongProgram() );
		if ( !songComboBox.getItems().contains(songInst) ) {
			songInst = noUseSongEx;
		}
		songComboBox.setValue( songInst );
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		InstClass instList[] = dls.getAvailableInstByInstType(InstType.MAIN_INST_LIST);
		instComboBox.getItems().addAll( instList );
		instComboBox.setValue( instList[0] );

		songComboBox.getItems().addAll( dls.getAvailableInstByInstType(InstType.SUB_INST_LIST) );
		songComboBox.getItems().add( noUseSongEx );
		songComboBox.setValue( noUseSongEx );
	}

	private void updateMuteButton() {
		if (trackIndex.isPresent()) {
			if (dls.getMute( trackIndex.getAsInt() )) {
				muteButton.setGraphic( muteOnImage );
			} else {
				muteButton.setGraphic( muteOffImage );
			}
		}
	}

	@FXML
	private void muteAction(ActionEvent e) {
		if (trackIndex.isPresent()) {
			dls.toggleMute( trackIndex.getAsInt() );
			updateMuteButton();
		}
	}

	@FXML
	private void soloAction(ActionEvent e) {
		if (trackIndex.isPresent()) {
			dls.solo( trackIndex.getAsInt() );
			updateMuteButton();
		}
	}

	@FXML
	private void allAction(ActionEvent e) {
		dls.all();
		updateMuteButton();
	}
}

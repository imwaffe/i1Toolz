package com.armellinluca.i1Toolz.Controllers;

import com.armellinluca.i1Toolz.EyeOne.Instrument;
import com.armellinluca.i1Toolz.EyeOne.InstrumentSingleton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class CalibrationTransmissionWizardController implements Initializable {
    @FXML
    private final BorderPane mainPane;
    @FXML
    private Text instructionsText, successCalibrationText, failedCalibrationText, calibrationInProgressText;

    private final Instrument instrument = InstrumentSingleton.getInstrument();

    public CalibrationTransmissionWizardController(){
        mainPane = new BorderPane();
    }

    public void cancel() throws IOException {
        mainPane.setCenter(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/calibrationWizard.fxml"))));
        instrument.buttonPressedHandlers().remove(this::calibrate);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instrument.buttonPressedHandlers().clearAll();
        instrument.buttonPressedHandlers().add(this::calibrate);
    }

    private void calibrate(){
        successCalibrationText.setVisible(false);
        failedCalibrationText.setVisible(false);
        calibrationInProgressText.setVisible(true);
        if(instrument.calibrateTransmission() == Instrument.DEVICE_STATE_OK) {
            successCalibrationText.setVisible(true);
            failedCalibrationText.setVisible(false);
        } else {
            successCalibrationText.setVisible(false);
            failedCalibrationText.setVisible(true);
        }
        calibrationInProgressText.setVisible(false);
    }
}

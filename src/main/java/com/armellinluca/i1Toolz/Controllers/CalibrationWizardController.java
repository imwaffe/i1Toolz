package com.armellinluca.i1Toolz.Controllers;

import com.armellinluca.i1Toolz.EyeOne.Instrument;
import com.armellinluca.i1Toolz.EyeOne.InstrumentSingleton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class CalibrationWizardController implements Initializable {
    @FXML
    private BorderPane mainPane;
    @FXML
    private Text selectedModeText, lastCalibrationText, instructionsTextReflectance, instructionTextEmissive, successCalibrationText, failedCalibrationText, calibrationInProgressText;
    @FXML
    private Button calibrateTransmissionButton;

    private final Instrument instrument = InstrumentSingleton.getInstrument();

    public CalibrationWizardController(){
        mainPane = new BorderPane();
    }

    public void cancel() throws IOException {
        mainPane.setCenter(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/instrumentPane.fxml"))));
        instrument.buttonPressedHandlers().remove(this::calibrate);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(InstrumentSingleton.getInstrument().getOption(Instrument.MEASUREMENT_MODE));
        String mode = instrument.getOption(Instrument.MEASUREMENT_MODE);
        selectedModeText.setText(mode);
        lastCalibrationText.setText(instrument.getLastCalibrationDate());
        if(mode.equals(Instrument.MODE_SCANNING_REFLECTANCE) || mode.equals(Instrument.MODE_SINGLE_REFLECTANCE)) {
            instructionTextEmissive.setVisible(false);
            instructionsTextReflectance.setVisible(true);
        } else {
            instructionTextEmissive.setVisible(true);
            instructionsTextReflectance.setVisible(false);
        }
        instrument.buttonPressedHandlers().add(this::calibrate);
    }

    private void calibrate(){
        successCalibrationText.setVisible(false);
        failedCalibrationText.setVisible(false);
        calibrationInProgressText.setVisible(true);
        if(instrument.triggerCalibration() == Instrument.DEVICE_STATE_OK) {
            successCalibrationText.setVisible(true);
            failedCalibrationText.setVisible(false);
            if(instrument.isTransmissiveMode()){
                calibrateTransmissionButton.setVisible(true);
            }
        } else {
            successCalibrationText.setVisible(false);
            failedCalibrationText.setVisible(true);
            calibrateTransmissionButton.setVisible(false);
        }
        calibrationInProgressText.setVisible(false);
    }

    @FXML
    private void calibrateTransmission() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/calibrationTransmissionWizard.fxml"));
        mainPane.setCenter(loader.load());
    }
}

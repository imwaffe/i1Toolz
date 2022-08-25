package Controllers;

import EyeOne.Instrument;
import EyeOne.InstrumentSingleton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
    private Text selectedModeText, lastCalibrationText, instructionsTextRefectance, instructionTextEmissive, successCalibrationText, failedCalibrationText, calibrationInProgressText;

    private Instrument instrument = InstrumentSingleton.getInstrument();

    public CalibrationWizardController(){
        mainPane = new BorderPane();
    }

    public void cancel() throws IOException {
        mainPane.setCenter(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/instrumentPane.fxml"))));
        instrument.buttonPressedHandlers().remove(this::calibrate);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String mode = instrument.getOption(Instrument.MEASUREMENT_MODE);
        selectedModeText.setText(mode);
        lastCalibrationText.setText(instrument.getLastCalibrationDate());
        if(mode.equals(Instrument.MODE_SCANNING_REFLECTANCE) || mode.equals(Instrument.MODE_SINGLE_REFLECTANCE)) {
            instructionTextEmissive.setVisible(false);
            instructionsTextRefectance.setVisible(true);
        } else {
            instructionTextEmissive.setVisible(true);
            instructionsTextRefectance.setVisible(false);
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
        } else {
            successCalibrationText.setVisible(false);
            failedCalibrationText.setVisible(true);
        }
        calibrationInProgressText.setVisible(false);
    }
}

package com.armellinluca.i1Toolz.Controllers;

import com.armellinluca.i1Toolz.EyeOne.EyeOne;
import com.armellinluca.i1Toolz.EyeOne.ColorMunki;
import com.armellinluca.i1Toolz.EyeOne.Instrument;
import com.armellinluca.i1Toolz.EyeOne.InstrumentSingleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class InstrumentSettingsController implements Initializable {

    @FXML
    private BorderPane mainPane;
    @FXML
    private TextField integrationTime;
    @FXML
    private ComboBox<String> measurementMode, instrumentFamily;
    @FXML
    private ListView instrumentInfo;
    @FXML
    private Button connectButton, calibrateButton, saveButton, restoreButton;
    @FXML
    private Text deviceNotCalibratedText;

    private Instrument instrument;
    private final Map<String,Class<?>> instrumentClass = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instrumentClass.put("EyeOne Pro", EyeOne.class);
        instrumentClass.put("ColorMunki", ColorMunki.class);

        instrumentFamily.setItems(FXCollections.observableArrayList(new ArrayList<>(instrumentClass.keySet())));

        if(InstrumentSingleton.getInstrument() != null) {
            instrument = InstrumentSingleton.getInstrument();
            if (instrument.isConnected())
                setView();
            else
                connectButton.setDisable(false);
        }
    }

    public InstrumentSettingsController(){
        mainPane = new BorderPane();
    }

    @FXML
    public void connectInstrument(){
        connectButton.setDisable(true);
        if(instrument.isConnected())
            setView();
        else
            connectButton.setDisable(false);
    }

    private void setView(){
        String calTime = instrument.getLastCalibrationDate();
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("Device type: "+ instrument.getOption(Instrument.DEVICE_TYPE));
        items.add("Serial number: "+ instrument.getOption(Instrument.SERIAL_NUMBER));
        items.add(instrument.getOption(Instrument.VERSION)+" "+ instrument.getOption(Instrument.REVISION_VERSION));
        items.add("Major version: "+ instrument.getOption(Instrument.MAJOR_VERSION));
        items.add("Minor version: "+ instrument.getOption(Instrument.MINOR_VERSION));
        items.add("Last calibrated: "+calTime);
        instrumentInfo.setItems(items);

        integrationTime.setText(Float.toString(Float.parseFloat(instrument.getOption(Instrument.INTEGRATION_TIME).equals("") ?"-1":instrument.getOption(Instrument.INTEGRATION_TIME))*1000));
        String measModes = "Transmission;"+instrument.getOption(Instrument.AVAILABLE_MEASUREMENT_MODES);
        measurementMode.setItems(FXCollections.observableArrayList(new ArrayList<>(Arrays.asList(measModes.split(";")))));
        measurementMode.getSelectionModel().select(instrument.getOption(Instrument.MEASUREMENT_MODE));

        calibrateButton.setDisable(false);
        saveButton.setDisable(false);
        restoreButton.setDisable(false);
        integrationTime.setDisable(false);
        measurementMode.setDisable(false);

        deviceNotCalibratedText.setVisible(!instrument.isCalibrated());
    }

    @FXML
    public void saveSettings(){
        instrument.setOption(Instrument.INTEGRATION_TIME,Float.toString(Float.parseFloat(integrationTime.getText())/1000));
        String measMode = measurementMode.getSelectionModel().getSelectedItem();
        if(measMode.equals("Transmission")) {
            instrument.setTransmissionMode(true);
        } else {
            instrument.setTransmissionMode(false);
            instrument.setOption(Instrument.MEASUREMENT_MODE, measMode);
        }
        setView();
    }

    @FXML
    public void restoreSettings(){
        setView();
    }

    @FXML
    public void measurementModeChanged(){
        String tmpMode = instrument.getOption(Instrument.MEASUREMENT_MODE);
        String tmpIntTime = instrument.getOption(Instrument.INTEGRATION_TIME);
        boolean wasTransmissive = instrument.isTransmissiveMode();
        instrument.setOption(Instrument.MEASUREMENT_MODE,measurementMode.getSelectionModel().getSelectedItem());
        try {
            integrationTime.setText(Float.toString(Float.parseFloat(instrument.getOption(Instrument.INTEGRATION_TIME)) * 1000));
        }catch(NumberFormatException ignored){}
        if(wasTransmissive)
            instrument.setTransmissionMode(true);
        else
            instrument.setOption(Instrument.MEASUREMENT_MODE,tmpMode);
        instrument.setOption(Instrument.INTEGRATION_TIME,tmpIntTime);
    }

    @FXML
    public void instrumentFamilyChanged(){
        instrument = InstrumentSingleton.setInstrument(instrumentClass.get(instrumentFamily.getSelectionModel().getSelectedItem()));
        connectInstrument();
    }

    @FXML
    public void calibrate() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/calibrationWizard.fxml"));
        mainPane.setCenter(loader.load());
    }

}

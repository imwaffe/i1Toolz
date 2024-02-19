package com.armellinluca.i1Toolz.Controllers;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;
import com.armellinluca.i1Toolz.ColorUtils.StandardIlluminant;
import com.armellinluca.i1Toolz.EyeOne.Instrument;
import com.armellinluca.i1Toolz.EyeOne.InstrumentSingleton;
import com.armellinluca.i1Toolz.FileSystem.ProjectFile;
import com.armellinluca.i1Toolz.Helpers.Measurements;
import com.armellinluca.i1Toolz.Helpers.ResizeHover;
import com.armellinluca.i1Toolz.Helpers.SerializableMeasurements;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    private static File currentProject = null;
    private Alert saveDialog;
    public static final ButtonType BUTTON_SAVE = new ButtonType("Save");
    public static final ButtonType BUTTON_DONT_SAVE = new ButtonType("Don't save");

    private Timer automationTimer = null;

    @FXML
    private BorderPane windowPane;
    @FXML
    private BorderPane mainPane;
    @FXML
    private VBox menuPane;
    @FXML
    private Text projectName, AutomateRemainingAcquisitions;
    @FXML
    private Button automateStartButton, automateStopButton, automatePauseButton;
    @FXML
    private TextField automateInterval, automateAcquisitions;
    @FXML
    private CheckBox automateAcquisitionsInfiniteCB;

    public void checkIfProjectSavedBeforeExecuting(Runnable r){
        if(ProjectFile.hasChanged(currentProject, Measurements.getSerializable()))
            saveDialog.showAndWait().ifPresent((btnType) -> {
                if (btnType == BUTTON_SAVE) {
                    saveProject();
                    r.run();
                } else if (btnType == BUTTON_DONT_SAVE){
                    r.run();
                }
            });
        else
            r.run();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.setProperty("prism.allowhidpi", "true");

        double shrinkMenuPaneWidth = 47;
        ResizeHover r = new ResizeHover(menuPane, menuPane.prefWidthProperty(), shrinkMenuPaneWidth, 100);

        windowPane.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            if(newWidth.intValue()/Screen.getPrimary().getDpi() < 13) {
                r.enable();
            }
            else {
                r.disable();
            }
        });

        automateAcquisitionsInfiniteCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                automateAcquisitions.setDisable(true);
            } else {
                automateAcquisitions.setDisable(false);
            }
        });

        saveDialog = new Alert(Alert.AlertType.CONFIRMATION);
        saveDialog.setTitle("Changes not saved");
        saveDialog.setHeaderText("Project was not saved");
        saveDialog.setContentText("Changes to the current project have not been saved and will be lost if you continue without saving.");
        saveDialog.getButtonTypes().setAll(BUTTON_SAVE, ButtonType.CANCEL, BUTTON_DONT_SAVE);
    }

    @FXML
    public void menuButtonInstrumentSettings() throws IOException {
        if(InstrumentSingleton.getInstrument() != null)
            InstrumentSingleton.getInstrument().buttonPressedHandlers().clearAll();
        mainPane.setCenter(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/instrumentPane.fxml"))));
    }

    @FXML
    public void menuButtonSpectrum() throws IOException {
        if(InstrumentSingleton.getInstrument() != null)
            InstrumentSingleton.getInstrument().buttonPressedHandlers().clearAll();
        mainPane.setCenter(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/spectrumPane.fxml"))));
    }

    @FXML
    public void menuButtonRTA() throws IOException {
        if(InstrumentSingleton.getInstrument() != null)
            InstrumentSingleton.getInstrument().buttonPressedHandlers().clearAll();
        mainPane.setCenter(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/rtaPane.fxml"))));
    }

    @FXML
    public void menuButtonCRI() throws IOException {
        if(InstrumentSingleton.getInstrument() != null)
            InstrumentSingleton.getInstrument().buttonPressedHandlers().clearAll();
        mainPane.setCenter(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/criPane.fxml"))));
    }

    @FXML
    public void menuButtonColor() throws IOException {
        if(InstrumentSingleton.getInstrument() != null)
            InstrumentSingleton.getInstrument().buttonPressedHandlers().clearAll();
        mainPane.setCenter(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/colorPane.fxml"))));
    }

    @FXML
    public void automateStart(){
        final Instrument instrument = InstrumentSingleton.getInstrument();
        Integer interval;
        final Integer[] reps = {null};
        try {
            interval = Integer.parseInt(automateInterval.getText());
            if(!automateAcquisitionsInfiniteCB.isSelected())
                reps[0] = Integer.parseInt(automateAcquisitions.getText());
        } catch (NumberFormatException ignored) {
            return;
        }

        automateStartButton.setDisable(true);
        automateStopButton.setDisable(false);

        automationTimer = new Timer();
        automationTimer.schedule(new TimerTask() {
            public void run() {

                assert instrument != null;
                int state = instrument.getDeviceState(instrument.triggerMeasurement());
                if(state != Instrument.DEVICE_STATE_OK){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    if(state == Instrument.DEVICE_STATE_NOT_CALIBRATED){
                        alert.setTitle("Not calibrated");
                        alert.setContentText("Device is not calibrated. Please perform calibration from 'Instrument Settings' panel.");
                        automationTimer.cancel();
                    } else if(state == Instrument.DEVICE_STATE_SATURATED){
                        alert.setTitle("Device saturated");
                        alert.setContentText("Device is saturated. Please use the ambient light diffuser if present or reduce the luminous flux reaching the instrument.");
                        automationTimer.cancel(); // PAUSE INSTEAD
                    } else if(state == Instrument.DEVICE_STATE_UNKNOWN_ERROR){
                        alert.setTitle("Instrument error");
                        alert.setHeaderText("The instrument returned an unknown error. Please save the project and try closing and reopening the app.");
                        automationTimer.cancel();
                    }
                    alert.show();
                    return;
                }
                TreeMap<Integer, Float> spectrum = instrument.getSpectrum();
                Spectrum sp;
                if(instrument.isEmissiveMode())
                    sp = new Spectrum(spectrum, Spectrum.NORMALIZE_EMISSIVE);
                else
                    sp = new Spectrum(spectrum, Spectrum.NORMALIZE_REFLECTANCE, StandardIlluminant.getD65());
                SpectralMeasurement measurement = new SpectralMeasurement(sp,StandardIlluminant.getD65());
                Measurements.add(measurement);

                if(reps[0] != null) {
                    reps[0]--;
                    if(reps[0] <= 0) {
                        automationTimer.cancel();
                        automateStartButton.setDisable(false);
                        automateStopButton.setDisable(true);
                    }
                }
            }
        }, 0, interval);
    }

    @FXML
    public void automateStop(){
        automationTimer.cancel();
        automateStartButton.setDisable(false);
        automateStopButton.setDisable(true);
    }

    @FXML
    public void automatePause(){}

    @FXML
    public void closeApp(){
        checkIfProjectSavedBeforeExecuting(Platform::exit);
    }

    @FXML
    public void minimizeWindow(){
        ((Stage) mainPane.getScene().getWindow()).setIconified(true);
    }

    @FXML
    public void maximizeWindow(){
        Stage stage = ((Stage) mainPane.getScene().getWindow());
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    public void openProject(){
        checkIfProjectSavedBeforeExecuting(() -> {
            Stage stage = ((Stage) mainPane.getScene().getWindow());
            FileChooser choose = new FileChooser();
            choose.setTitle("Open project...");
            choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("i1Toolz Project (*.i1tz)", "*.i1tz"));
            choose.setInitialFileName("*.i1tz");
            File file = choose.showOpenDialog(stage);
            openProject(file);
            Platform.runLater(() -> stage.getScene().setCursor(Cursor.DEFAULT));
        });
    }

    public void openProject(File file){
        if (file != null) {
            SerializableMeasurements m = ProjectFile.loadFromFile(file);
            if(m != null) {
                Measurements.deserialize(m);
                currentProject = file;
                projectName.setText(currentProject.getName());
            }
        }
    }

    @FXML
    public void newProject(){
        checkIfProjectSavedBeforeExecuting(()-> Measurements.deserialize(new SerializableMeasurements()));
        currentProject = null;
        projectName.setText("New project.i1tz");
    }

    @FXML
    public void saveProject(){
        Stage stage = ((Stage) mainPane.getScene().getWindow());
        if(currentProject == null){
            FileChooser choose = new FileChooser();
            choose.setTitle("Save project...");
            choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("i1Toolz Project (*.i1tz)", "*.i1tz"));
            choose.setInitialFileName("New project.i1tz");
            File file = choose.showSaveDialog(stage);
            if (file != null) {
                if (file.getName().endsWith(".i1tz")) {
                    currentProject = file;
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(currentProject, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(Measurements.getSerializable().get());
            oos.flush();
            oos.close();
            fos.flush();
            projectName.setText(currentProject.getName());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

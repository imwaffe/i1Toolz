package com.armellinluca.i1Toolz.Controllers;

import com.armellinluca.i1Toolz.EyeOne.InstrumentSingleton;
import com.armellinluca.i1Toolz.FileSystem.ProjectFile;
import com.armellinluca.i1Toolz.Helpers.Measurements;
import com.armellinluca.i1Toolz.Helpers.ResizeHover;
import com.armellinluca.i1Toolz.Helpers.SerializableMeasurements;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static File currentProject = null;
    private Alert saveDialog;
    public static final ButtonType BUTTON_SAVE = new ButtonType("Save");
    public static final ButtonType BUTTON_DONT_SAVE = new ButtonType("Don't save");

    @FXML
    private BorderPane windowPane;
    @FXML
    private BorderPane mainPane;
    @FXML
    private VBox menuPane;
    @FXML
    private Text projectName;

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

        double originalMenuPaneWidth = menuPane.getPrefWidth();
        double shrinkMenuPaneWidth = 47;
        ResizeHover r = new ResizeHover(menuPane, menuPane.prefWidthProperty(), shrinkMenuPaneWidth, 100);

        if(Screen.getPrimary().getDpi() < 120){
            menuPane.setPrefWidth(shrinkMenuPaneWidth);
            r.enable();
        } else {
            windowPane.widthProperty().addListener((observable, oldWidth, newWidth) -> {
                if(newWidth.intValue() < 1400) {
                    menuPane.setPrefWidth(shrinkMenuPaneWidth);
                    r.enable();
                }
                else {
                    r.disable();
                    menuPane.setPrefWidth(originalMenuPaneWidth);
                }
            });
        }

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

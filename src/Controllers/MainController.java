package Controllers;

import EyeOne.InstrumentSingleton;
import Helpers.Measurements;
import Helpers.SerializableMeasurements;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static File currentProject = null;
    private Alert saveDialog;
    public static final ButtonType BUTTON_SAVE = new ButtonType("Save");
    public static final ButtonType BUTTON_DONT_SAVE = new ButtonType("Don't save");

    @FXML
    private BorderPane mainPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
    public void closeApp(){
        Platform.exit();
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
        Stage stage = ((Stage) mainPane.getScene().getWindow());
        FileChooser choose = new FileChooser();
        choose.setTitle("Open project...");
        choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("i1Toolz Project (*.i1tz)", "*.i1tz"));
        choose.setInitialFileName("*.i1tz");
        File file = choose.showOpenDialog(stage);
        if (file != null) {
            if (file.getName().endsWith(".i1tz")) {
                currentProject = file;
            }
        }

        try (FileInputStream fis = new FileInputStream(currentProject);
            ObjectInputStream ois = new ObjectInputStream(fis)){
            SerializableMeasurements serializableMeasurements = (SerializableMeasurements)ois.readObject();
            serializableMeasurements.load();
            Measurements.deserialize(serializableMeasurements);
        } catch (IOException | ClassNotFoundException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    public void newProject(){
        saveDialog.showAndWait().ifPresent((btnType) -> {
            if (btnType == BUTTON_SAVE) {
                saveProject();
                currentProject = null;
                Measurements.deserialize(new SerializableMeasurements());
            } else if (btnType == BUTTON_DONT_SAVE){
                currentProject = null;
                Measurements.deserialize(new SerializableMeasurements());
            }
        });
    }

    @FXML
    public void saveProject(){
        Stage stage = ((Stage) mainPane.getScene().getWindow());
        if(currentProject == null){
            FileChooser choose = new FileChooser();
            choose.setTitle("Save project...");
            choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("i1Toolz Project (*.i1tz)", "*.i1tz"));
            choose.setInitialFileName("*.i1tz");
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
            fos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

package Controllers;

import Color.Spectrum.SpectralMeasurement;
import Color.Spectrum.Spectrum;
import Color.SpectrumMath.*;
import Color.SpectrumMath.SpectrumMath;
import Color.StandardIlluminant;
import EyeOne.Instrument;
import EyeOne.InstrumentSingleton;
import Helpers.Measurements;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class SpectrumController implements Initializable {
    private Instrument instrument = InstrumentSingleton.getInstrument();
    @FXML
    private BorderPane mainPane;
    @FXML
    private TableView<SpectralMeasurement> table;
    @FXML
    private LineChart<String,Number> chart;
    @FXML
    private TableColumn<SpectralMeasurement,Float> X, Y, Z;
    @FXML
    private TableColumn<SpectralMeasurement,Double> L, A, B;
    @FXML
    private TableColumn<SpectralMeasurement,Integer> id;
    @FXML
    private TableColumn<SpectralMeasurement,String> label;
    @FXML
    private TableColumn<SpectralMeasurement,Character> flag;
    @FXML
    private Text measurementId;
    @FXML
    private ListView<String> measurementDetails;
    @FXML
    private ToggleGroup referenceMathRadioGroup;
    @FXML
    private VBox referenceMathBox;
    @FXML
    private Button computeAverageSpectrumButton, takeMeasurementButton;

    public final Spectrum REFERENCE_WHITE = StandardIlluminant.getD65();

    private int measId = 1;
    transient ObservableList<SpectralMeasurement> measurements = null;
    transient ObjectProperty<SpectralMeasurement> currentMeasurement = new SimpleObjectProperty<>();

    static transient ObjectProperty<SpectralMeasurement> referenceSpectrum = new SimpleObjectProperty<>();
    static int checkedMathRadioIndex = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        measurements = Measurements.get();

        chart.setAnimated(false);
        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        X.setCellValueFactory(cellData -> cellData.getValue().getX());
        Y.setCellValueFactory(cellData -> cellData.getValue().getY());
        Z.setCellValueFactory(cellData -> cellData.getValue().getZ());
        L.setCellValueFactory(cellData -> cellData.getValue().getL());
        A.setCellValueFactory(cellData -> cellData.getValue().getA());
        B.setCellValueFactory(cellData -> cellData.getValue().getB());
        id.setCellValueFactory(cellData -> cellData.getValue().getId());
        flag.setCellValueFactory(cellData -> cellData.getValue().getFlag());
        label.setCellFactory(TextFieldTableCell.forTableColumn());
        label.setEditable(true);
        label.setCellValueFactory(cellData -> cellData.getValue().getLabel());

        label.setOnEditCommit(event -> event.getRowValue().setLabel(event.getNewValue()));

        //Measurements.setSpectrumMath(new IdentitySpectrum());
        if(referenceSpectrum.get() != null){
            referenceMathBox.setDisable(false);
            if(checkedMathRadioIndex >= 0)
                referenceMathRadioGroup.getToggles().get(checkedMathRadioIndex).setSelected(true);
        }

        table.getItems().clear();
        table.getItems().addAll(measurements);

        measurements.addListener((ListChangeListener<SpectralMeasurement>) c -> {
            while(c.next()) {
                if (c.wasRemoved()) {
                    if(Measurements.get().stream().findFirst().isPresent()) {
                        renderAll(Measurements.get().stream().findFirst().get());
                    }
                    else {
                        measurementDetails.setItems(FXCollections.observableArrayList());
                        chart.getData().clear();
                    }
                }
            }
            table.getItems().clear();
            table.getItems().addAll(measurements);
        });
        currentMeasurement.addListener((observable, oldValue, newValue) -> renderAll(measurements));

        if(Measurements.get().size() > 0){
            renderAll(Measurements.get().stream().findFirst().get());
        }

        referenceMathRadioGroup.selectedToggleProperty().addListener((ov, t, t1) -> {
            RadioButton chk = (RadioButton)t1.getToggleGroup().getSelectedToggle();
            checkedMathRadioIndex = referenceMathRadioGroup.getToggles().indexOf(chk);
            try {
                SpectrumMath spectrumMath = (SpectrumMath) GetOperatorByString.get("Color.SpectrumMath.ReferenceSpectrumMath."+chk.getId()).getDeclaredConstructor(ObjectProperty.class).newInstance(referenceSpectrum);
                Measurements.setSpectrumMath(spectrumMath);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e){
                Measurements.setSpectrumMath(new IdentitySpectrum());
            }
            renderAll(currentMeasurement.get());
        });

        if(instrument != null) {
            instrument.buttonPressedHandlers().add(this::run);
            takeMeasurementButton.setDisable(false);
        }
    }

    @FXML
    public void selectRow(){
        if(table.getSelectionModel().getSelectedItems().size() == 1) {
            renderAll(table.getSelectionModel().getSelectedItem());
            computeAverageSpectrumButton.setDisable(true);
        }
        else if (table.getSelectionModel().getSelectedItems().size() > 1){
            renderAll(table.getSelectionModel().getSelectedItems());
            computeAverageSpectrumButton.setDisable(false);
        }
    }

    @FXML
    public void deleteMeasurement(){
        ArrayList<SpectralMeasurement> toDelete = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        toDelete.forEach(m -> Measurements.get().remove(m));
    }

    @FXML
    public void saveMeasurement(){
        DirectoryChooser directory = new DirectoryChooser();
        directory.setTitle("Save");
        File saveDirectory = directory.showDialog(mainPane.getScene().getWindow());
        Measurements.saveMeasurements(saveDirectory);
        Measurements.get().addListener((ListChangeListener<SpectralMeasurement>) c -> {
            while(c.next())
                c.getRemoved().forEach(Measurements::deleteFile);
        });
    }

    @FXML
    public void setReferenceMeasurement(){
        referenceSpectrum.set(table.getSelectionModel().selectedItemProperty().get());
        referenceMathBox.setDisable(false);
        renderAll(currentMeasurement.get());
    }

    @FXML
    public void cancelReferenceMeasurement(){
        referenceSpectrum.set(null);
        checkedMathRadioIndex = -1;
        referenceMathRadioGroup.getSelectedToggle().setSelected(false);
        Measurements.setSpectrumMath(new IdentitySpectrum());
        referenceMathBox.setDisable(true);
        renderAll(currentMeasurement.get());
    }

    @FXML
    public void addMathSpectrumToMeasurementList(){
        try {
            String label = Measurements.getMathOperatorName(currentMeasurement.get().getId().getValue().toString());
            SpectralMeasurement newMathMeas = new SpectralMeasurement(currentMeasurement.get().getSpectrum().clone(),label,0);
            Measurements.add(newMathMeas);
            currentMeasurement.set(newMathMeas);
            renderAll(newMathMeas);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void computeAverageSpectrum(){
        SpectralMeasurement avg = AverageSpectrums.averageSpectrums(table.getSelectionModel().getSelectedItems());
        Measurements.add(avg);
        renderAll(avg);
    }

    @FXML
    public void takeMeasurement(){
        run();
    }

    private void run() {
        Platform.runLater(()-> {
            instrument.triggerMeasurement();
            TreeMap<Integer, Float> spectrum = instrument.getSpectrum();
            Spectrum sp;
            if(instrument.isEmissiveMode())
                sp = new Spectrum(spectrum, Spectrum.NORMALIZE_EMISSIVE);
            else
                sp = new Spectrum(spectrum, Spectrum.NORMALIZE_REFLECTANCE, REFERENCE_WHITE);
            SpectralMeasurement measurement = new SpectralMeasurement(sp,REFERENCE_WHITE);
            Measurements.add(measurement);
            currentMeasurement.set(measurement);
            renderAll(measurement);
        });
    }


    private void drawPlot(){
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(currentMeasurement.get().getLabel().getValue());
        for (Map.Entry<Integer, Float> entry : currentMeasurement.get().getSpectrum().getSpectralData().entrySet()) {
            series.getData().add(new XYChart.Data<>(Integer.toString(entry.getKey()), entry.getValue()));
        }
        chart.getData().clear();
        chart.getData().add(series);
    }

    private void fillMeasurementDetailsList(SpectralMeasurement measurement){
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("Label: " + measurement.getLabel().getValue());
        items.add("Mode: " + measurement.getMeasurementMode());
        items.add("Device: " + measurement.getInstrumentTypeAndSerialNumber());
        items.add("XYZ: [" + measurement.getX().getValue().toString() + ", "+ measurement.getY().getValue().toString() + ", " + measurement.getZ().getValue().toString() + "]");
        items.add("L*a*b*: [" + measurement.getL().getValue().toString() + ", "+ measurement.getA().getValue().toString() + ", " + measurement.getB().getValue().toString() + "]");
        measurementDetails.setItems(items);
    }

    private void renderAll(SpectralMeasurement measurement){
        currentMeasurement.set(measurement);
        drawPlot();
        fillMeasurementDetailsList(measurement);
        measurementId.setText(measurement.getId().getValue().toString());
    }

    private void renderAll(ObservableList<SpectralMeasurement> measurements){
        chart.getData().clear();
        measurements.forEach(s -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(s.getId().getValue().toString());
            for (Map.Entry<Integer, Float> entry : s.getSpectrum().getSpectralData().entrySet()) {
                series.getData().add(new XYChart.Data<>(Integer.toString(entry.getKey()), entry.getValue()));
            }
            chart.getData().add(series);
        });
    }
}

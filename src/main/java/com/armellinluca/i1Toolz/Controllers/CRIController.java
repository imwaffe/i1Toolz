package com.armellinluca.i1Toolz.Controllers;

import com.armellinluca.i1Toolz.Clipboard.CRIMeasurementsClipboard;
import com.armellinluca.i1Toolz.ColorUtils.CRI.CRIMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;
import com.armellinluca.i1Toolz.ColorUtils.SpectrumMath.IdentitySpectrum;
import com.armellinluca.i1Toolz.ColorUtils.StandardIlluminant;
import com.armellinluca.i1Toolz.EyeOne.Instrument;
import com.armellinluca.i1Toolz.EyeOne.InstrumentSingleton;
import com.armellinluca.i1Toolz.Helpers.CRIMeasurements;
import com.armellinluca.i1Toolz.Helpers.Measurements;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class CRIController implements Initializable {
    private final Instrument instrument = InstrumentSingleton.getInstrument();
    private final CRIMeasurements measurements = new CRIMeasurements();

    @FXML
    private BorderPane mainPane;
    @FXML
    private TableView<CRIMeasurement> table;
    @FXML
    private BarChart<String,Number> criChart;
    @FXML
    private ScatterChart<Number, Number> uvChart;
    @FXML
    private TableColumn<CRIMeasurement,Double> Ra, U, V, X, Y;
    @FXML
    private TableColumn<CRIMeasurement,Integer> id, CCT;
    @FXML
    private TableColumn<CRIMeasurement,String> label;
    @FXML
    private TableColumn<CRIMeasurement,Character> flag;
    @FXML
    private Text measurementId;
    @FXML
    private ListView<String> measurementDetails;
    @FXML
    private Button takeMeasurementButton;

    transient ObjectProperty<CRIMeasurement> currentMeasurement = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        criChart.setAnimated(false);
        uvChart.setAnimated(false);

        new CRIMeasurementsClipboard(table);
        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        X.setCellValueFactory(cellData -> cellData.getValue().getX());
        Y.setCellValueFactory(cellData -> cellData.getValue().getY());
        Ra.setCellValueFactory(cellData -> cellData.getValue().getRa());
        U.setCellValueFactory(cellData -> cellData.getValue().getU());
        V.setCellValueFactory(cellData -> cellData.getValue().getV());
        id.setCellValueFactory(cellData -> cellData.getValue().getId());
        CCT.setCellValueFactory(cellData -> cellData.getValue().getCCT());
        flag.setCellValueFactory(cellData -> cellData.getValue().getFlag());

        label.setCellFactory(TextFieldTableCell.forTableColumn());
        label.setEditable(true);
        label.setCellValueFactory(cellData -> cellData.getValue().getLabel());
        label.setOnEditCommit(event -> event.getRowValue().setLabel(event.getNewValue()));

        Measurements.setSpectrumMath(new IdentitySpectrum());

        Measurements.addListener(c -> {
            renderAll(currentMeasurement.get());
            //table.getItems().clear();
            //table.getItems().addAll(measurements.getCriMeasurements());
            //table.getSortOrder().add(id);
        });
        currentMeasurement.addListener((observable, oldValue, newValue) -> {
            renderAll(currentMeasurement.get());
        });

        if(measurements.getCriMeasurements().size() > 0){
            table.getItems().addAll(measurements.getCriMeasurements());
            renderAll(measurements.getCriMeasurements().stream().findFirst().get());
            table.getSortOrder().add(id);
        }

        table.getItems().addListener((ListChangeListener<CRIMeasurement>) change -> {
            while (change.next()){
                if(change.wasRemoved()) {
                    change.getRemoved().forEach(r -> Measurements.get().remove(r.getSpectralMeasurement()));
                    //change.getRemoved().forEach(r -> measurements.remove(r));
                    if(table.getSelectionModel().getSelectedItem() != null) {
                        renderAll(table.getSelectionModel().getSelectedItem());
                    }
                    else if(table.getItems().stream().findFirst().isPresent()){
                        renderAll(table.getItems().stream().findFirst().get());
                    }
                    else {
                        measurementDetails.setItems(FXCollections.observableArrayList());
                        criChart.getData().clear();
                        uvChart.getData().clear();
                    }
                }
            }
        });
        Measurements.get().addListener((ListChangeListener<SpectralMeasurement>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(a -> table.getItems().add(measurements.getCriMeasurement(a)));
                }
            }
        });
        /*Measurements.get().addListener((ListChangeListener<SpectralMeasurement>) c -> {
            while(c.next()){
                if(c.wasRemoved()){
                    if(measurements.getCriMeasurements().stream().findFirst().isPresent()) {
                        renderAll(measurements.getCriMeasurements().stream().findFirst().get());
                    }
                    else {
                        measurementDetails.setItems(FXCollections.observableArrayList());
                        criChart.getData().clear();
                        uvChart.getData().clear();
                    }
                }
            }
        });*/

        if(instrument != null) {
            if (instrument.isEmissiveMode()) {
                instrument.buttonPressedHandlers().add(this::run);
                takeMeasurementButton.setDisable(false);
            }
        }
    }

    @FXML
    public void selectRow(){
        renderAll(table.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void deleteMeasurement(){
        //Measurements.get().remove(currentMeasurement.get().getSpectralMeasurement());
        ArrayList<CRIMeasurement> toDelete = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        toDelete.forEach(m -> table.getItems().remove(m));
    }

    @FXML
    public void saveMeasurement(){}

    @FXML
    public void takeMeasurement(){
        run();
    }

    private void run() {
        Platform.runLater(()-> {
            Spectrum sp = null;
            assert instrument != null;
            int state = instrument.getDeviceState(instrument.triggerMeasurement());
            if(state != Instrument.DEVICE_STATE_OK){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                if(state == Instrument.DEVICE_STATE_NOT_CALIBRATED){
                    alert.setTitle("Not calibrated");
                    alert.setContentText("Device is not calibrated. Please perform calibration from 'Instrument Settings' panel.");
                } else if(state == Instrument.DEVICE_STATE_SATURATED){
                    alert.setTitle("Device saturated");
                    alert.setContentText("Device is saturated. Please use the ambient light diffuser if present or reduce the luminous flux reaching the instrument.");
                } else if(state == Instrument.DEVICE_STATE_UNKNOWN_ERROR){
                    alert.setTitle("Instrument error");
                    alert.setHeaderText("The instrument returned an unknown error. Please save the project and try closing and reopening the app.");
                }
                alert.show();
                return;
            }
            TreeMap<Integer, Float> spectrum = instrument.getSpectrum();

            char normalize = Spectrum.NORMALIZE_DONT_NORMALIZE;
            if(instrument.isEmissiveMode())
                normalize = Spectrum.NORMALIZE_EMISSIVE;
            sp = new Spectrum(spectrum,normalize);
            SpectralMeasurement measurement = new SpectralMeasurement(sp, StandardIlluminant.getD50());
            Measurements.add(measurement);
            currentMeasurement.set(measurements.getCriMeasurement(measurement));
            renderAll(measurements.getCriMeasurement(measurement));
        });
    }

    private void fillMeasurementDetailsList(CRIMeasurement measurement){
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("Label: " + measurement.getLabel().getValue());
        items.add("Mode: " + measurement.getSpectralMeasurement().getMeasurementMode());
        items.add("Device: " + measurement.getSpectralMeasurement().getInstrumentTypeAndSerialNumber());
        items.add("uv: [" + measurement.getU().getValue().toString() + ", "+ measurement.getV().getValue().toString() + "]");
        items.add("xy: [" + measurement.getX().getValue().toString() + ", " + measurement.getY().getValue().toString() + "]");
        measurementDetails.setItems(items);
    }

    private void renderAll(CRIMeasurement measurement){
        if(measurement != null) {
            currentMeasurement.set(measurement);
            drawPlot();
            fillMeasurementDetailsList(measurement);
            measurementId.setText(measurement.getId().getValue().toString());
        }
    }

    private void drawPlot(){
        XYChart.Series<String, Number> barchartSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> uvChartReference = new XYChart.Series<>();
        XYChart.Series<Number, Number> uvChartTest = new XYChart.Series<>();

        barchartSeries.setName(currentMeasurement.get().getLabel().getValue());
        uvChartReference.setName("Reference");
        uvChartTest.setName("Test");

        for(int i=1; i<=14; i++){
            barchartSeries.getData().add(new XYChart.Data<>("R"+i, currentMeasurement.get().getIndex(i)));
            uvChartReference.getData().add(new XYChart.Data<>(currentMeasurement.get().getYuvPatchReference(i)[1],currentMeasurement.get().getYuvPatchReference(i)[2]));
            uvChartTest.getData().add(new XYChart.Data<>(currentMeasurement.get().getYuvPatchTest(i)[1],currentMeasurement.get().getYuvPatchTest(i)[2]));
        }
        barchartSeries.getData().add(new XYChart.Data<>("Ra", currentMeasurement.get().getRa().getValue()));

        uvChart.getData().clear();
        uvChart.getData().add(uvChartReference);
        uvChart.getData().add(uvChartTest);
        criChart.getData().clear();
        criChart.getData().add(barchartSeries);
    }
}

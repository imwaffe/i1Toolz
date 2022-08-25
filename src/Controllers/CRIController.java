package Controllers;

import Color.CRI.CRIMeasurement;
import Color.Spectrum.SpectralMeasurement;
import Color.Spectrum.Spectrum;
import Color.SpectrumMath.IdentitySpectrum;
import Color.StandardIlluminant;
import EyeOne.Instrument;
import EyeOne.InstrumentSingleton;
import Helpers.CRIMeasurements;
import Helpers.Measurements;
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
import java.util.ResourceBundle;
import java.util.TreeMap;

public class CRIController implements Initializable {
    private Instrument instrument = InstrumentSingleton.getInstrument();
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
            table.getItems().clear();
            table.getItems().addAll(measurements.getCriMeasurements());
            table.getSortOrder().add(id);
        });
        currentMeasurement.addListener((observable, oldValue, newValue) -> {
            renderAll(currentMeasurement.get());
        });

        if(measurements.getCriMeasurements().size() > 0){
            table.getItems().addAll(measurements.getCriMeasurements());
            renderAll(measurements.getCriMeasurements().stream().findFirst().get());
            table.getSortOrder().add(id);
        }

        Measurements.get().addListener((ListChangeListener<SpectralMeasurement>) c -> {
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
        });

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
        Measurements.get().remove(currentMeasurement.get().getSpectralMeasurement());
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
            instrument.triggerMeasurement();
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

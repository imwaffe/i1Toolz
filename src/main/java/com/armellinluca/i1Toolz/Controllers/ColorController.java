package com.armellinluca.i1Toolz.Controllers;

import com.armellinluca.i1Toolz.ColorUtils.ColorMath.DeltaE;
import com.armellinluca.i1Toolz.ColorUtils.ColorMath.XYZ2RGB;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectrumConverter;
import com.armellinluca.i1Toolz.ColorUtils.StandardIlluminant;
import com.armellinluca.i1Toolz.EyeOne.Instrument;
import com.armellinluca.i1Toolz.EyeOne.InstrumentSingleton;
import com.armellinluca.i1Toolz.Helpers.Measurements;
import com.armellinluca.i1Toolz.Helpers.TextNumber;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class ColorController implements Initializable {
    @FXML
    private BorderPane mainPane;
    @FXML
    private TableView<SpectralMeasurement> table;
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
    private Button takeMeasurementButton;
    @FXML
    private TextNumber measurementId, refValRed, refValGreen, refValBlue, refValX, refValY, refValZ, refVal_x, refVal_y,
            refValL, refVal_a, refVal_b, refValL1, refVal_u, refVal_v, sampleValRed, sampleValGreen, sampleValBlue,
            sampleValX, sampleValY, sampleValZ, sampleVal_x, sampleVal_y, sampleValL, sampleVal_a, sampleVal_b,
            sampleValL1, sampleVal_u, sampleVal_v, deVal;
    @FXML
    private Pane refColorSingle, refColorPair, sampleColorPair, sampleColorSingle;
    @FXML
    private TextField refValHex, refValRGB, refValXYZ, refValLab, refValLuv,
            sampleValHex, sampleValRGB, sampleValXYZ, sampleValLab, sampleValLuv;
    @FXML
    private ComboBox<String> rgbSpace;
    @FXML
    private LineChart<String,Number> spectrumChart;

    private final Instrument instrument = InstrumentSingleton.getInstrument();
    private final double[][] srgbMatrix = {{0.4124564, 0.3575761, 0.1804375},{0.2126729, 0.7151522, 0.0721750},{0.0193339, 0.1191920, 0.9503041}};
    private final XYZ2RGB xyz2rgb = new XYZ2RGB(srgbMatrix, 2.2);
    transient ObservableList<SpectralMeasurement> measurements = null;
    static transient ObjectProperty<SpectralMeasurement> currentMeasurement = new SimpleObjectProperty<>();
    static transient ObjectProperty<SpectralMeasurement> referenceSpectrum = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        measurements = Measurements.get();
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
        
        table.getItems().clear();
        table.getItems().addAll(measurements);

        spectrumChart.setAnimated(false);

        if(instrument != null) {
            if (instrument.isReflectiveMode()) {
                instrument.buttonPressedHandlers().add(this::run);
                takeMeasurementButton.setDisable(false);
            }
        }

        measurements.addListener((ListChangeListener<SpectralMeasurement>) c -> {
            while(c.next()) {
                if (c.wasRemoved()) {
                    if(Measurements.get().stream().findFirst().isPresent()) {
                        renderAll(Measurements.get().stream().findFirst().get());
                    }
                    else {

                    }
                }
            }
            table.getItems().clear();
            table.getItems().addAll(measurements);
        });
        //currentMeasurement.addListener((observable, oldValue, newValue) -> renderAll(measurements));

        currentMeasurement.addListener((observable -> setSampleValues()));
        referenceSpectrum.addListener((observable -> setReferenceValues()));

        if(measurements.size() > 0){
            if(currentMeasurement.get() != null)
                renderAll(currentMeasurement.get());
            else
                renderAll(Measurements.get().stream().findFirst().get());
        }
    }

    private void run() {
        Platform.runLater(()-> {
            Spectrum sp = null;
            instrument.triggerMeasurement();
            TreeMap<Integer, Float> spectrum = instrument.getSpectrum();

            char normalize = Spectrum.NORMALIZE_REFLECTANCE;
            if(instrument.isEmissiveMode())
                normalize = Spectrum.NORMALIZE_EMISSIVE;
            sp = new Spectrum(spectrum,Spectrum.NORMALIZE_REFLECTANCE,StandardIlluminant.getD65());
            SpectralMeasurement measurement = new SpectralMeasurement(sp, StandardIlluminant.getD65());
            Measurements.add(measurement);
            currentMeasurement.set(measurement);
            renderAll(measurement);
        });
    }

    private void renderAll(SpectralMeasurement measurement){
        if(measurement != null) {
            currentMeasurement.set(measurement);
            spectrumChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(measurement.getId().getValue().toString());
            for (Map.Entry<Integer, Float> entry : measurement.getSpectrum().getSpectralData().entrySet()) {
                series.getData().add(new XYChart.Data<>(Integer.toString(entry.getKey()), entry.getValue()));
            }
            spectrumChart.getData().add(series);

            if(referenceSpectrum.get() != null) {
                deVal.setText(DeltaE.DE76(currentMeasurement.get().getSpectrumConverter().getLab(), referenceSpectrum.get().getSpectrumConverter().getLab()));
                series = new XYChart.Series<>();
                series.setName("ref");
                for (Map.Entry<Integer, Float> entry : referenceSpectrum.get().getSpectrum().getSpectralData().entrySet()) {
                    series.getData().add(new XYChart.Data<>(Integer.toString(entry.getKey()), entry.getValue()));
                }
                spectrumChart.getData().add(series);
            }

            setSampleValues();
            setReferenceValues();
            //drawPlot();
            //measurementId.setText(measurement.getId().getValue().toString());
        }
    }

    @FXML
    public void selectRow(){
        if(table.getSelectionModel().getSelectedItems().size() == 1) {
            renderAll(table.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    public void deleteMeasurement(){
        ArrayList<SpectralMeasurement> toDelete = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        toDelete.forEach(m -> Measurements.get().remove(m));
    }

    @FXML
    public void setReferenceMeasurement(){
        referenceSpectrum.set(table.getSelectionModel().selectedItemProperty().get());
        renderAll(currentMeasurement.get());
    }

    @FXML
    public void cancelReferenceMeasurement(){
        referenceSpectrum.set(null);
        renderAll(currentMeasurement.get());
    }

    private void setReferenceValues(){
        if(referenceSpectrum.get() == null)
            return;

        SpectrumConverter conv = referenceSpectrum.get().getSpectrumConverter();
        int[] RGB = xyz2rgb.xyz2rgb(conv.getXYZ());
        refValRed.setText(RGB[0]);
        refValGreen.setText(RGB[1]);
        refValBlue.setText(RGB[2]);
        refValX.setText((conv.getX()));
        refValY.setText((conv.getY()));
        refValZ.setText((conv.getZ()));
        refVal_x.setText((conv.get_x()));
        refVal_y.setText((conv.get_y()));
        refValL.setText((conv.getL()));
        refVal_a.setText((conv.getLab()[1]));
        refVal_b.setText((conv.getLab()[2]));
        refValL1.setText((conv.getL()));
        refVal_u.setText((conv.get_u()));
        refVal_v.setText((conv.get_v()));
        refValXYZ.setText(Arrays.toString(conv.getXYZ()));
        refValLab.setText(Arrays.toString(conv.getLab()));
        refValLuv.setText(conv.getL()+","+conv.get_u()+","+ conv.get_v());
        refValRGB.setText(Arrays.toString(RGB));
        refValHex.setText(String.format("#%02x%02x%02x", RGB[0], RGB[1], RGB[2]).toUpperCase());

        int[] boundedRGB = XYZ2RGB.boundRGBValues(RGB);
        refColorPair.setBackground(new Background(new BackgroundFill(Color.rgb(boundedRGB[0],boundedRGB[1],boundedRGB[2]), CornerRadii.EMPTY, Insets.EMPTY)));
        refColorSingle.setBackground(new Background(new BackgroundFill(Color.rgb(boundedRGB[0],boundedRGB[1],boundedRGB[2]), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void setSampleValues(){
        if(currentMeasurement.get() == null)
            return;

        SpectrumConverter conv = currentMeasurement.get().getSpectrumConverter();
        int[] RGB = xyz2rgb.xyz2rgb(conv.getXYZ());
        sampleValRed.setText(RGB[0]);
        sampleValGreen.setText(RGB[1]);
        sampleValBlue.setText(RGB[2]);
        sampleValX.setText((conv.getX()));
        sampleValY.setText((conv.getY()));
        sampleValZ.setText((conv.getZ()));
        sampleVal_x.setText((conv.get_x()));
        sampleVal_y.setText((conv.get_y()));
        sampleValL.setText((conv.getL()));
        sampleVal_a.setText((conv.getLab()[1]));
        sampleVal_b.setText((conv.getLab()[2]));
        sampleValL1.setText((conv.getL()));
        sampleVal_u.setText((conv.get_u()));
        sampleVal_v.setText((conv.get_v()));
        sampleValXYZ.setText(Arrays.toString(conv.getXYZ()));
        sampleValLab.setText(Arrays.toString(conv.getLab()));
        sampleValLuv.setText(conv.getL()+","+conv.get_u()+","+ conv.get_v());
        sampleValRGB.setText(Arrays.toString(RGB));
        sampleValHex.setText(String.format("#%02x%02x%02x", RGB[0], RGB[1], RGB[2]).toUpperCase());

        int[] boundedRGB = XYZ2RGB.boundRGBValues(RGB);
        sampleColorPair.setBackground(new Background(new BackgroundFill(Color.rgb(boundedRGB[0],boundedRGB[1],boundedRGB[2]), CornerRadii.EMPTY, Insets.EMPTY)));
        sampleColorSingle.setBackground(new Background(new BackgroundFill(Color.rgb(boundedRGB[0],boundedRGB[1],boundedRGB[2]), CornerRadii.EMPTY, Insets.EMPTY)));
    }
}

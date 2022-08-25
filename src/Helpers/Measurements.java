package Helpers;

import Color.SpectrumMath.SpectrumMath;
import Color.Spectrum.SpectralMeasurement;
import FileSystem.SpectralMeasurementCSV;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Measurements {
    private static SerializableMeasurements serializableMeasurements = new SerializableMeasurements();
    protected static ObservableList<SpectralMeasurement> measurements = null;

    public static ObservableList<SpectralMeasurement> get(){
        if(measurements == null){
            measurements = FXCollections.observableArrayList(measurements ->  new Observable[] {
                    measurements.getId(), measurements.originalSpectrumProperty(), measurements.spectrumProperty()
            });
            measurements.addListener((ListChangeListener<SpectralMeasurement>) c -> {
                while(c.next()){
                    if(c.wasAdded())
                        serializableMeasurements.measurements.addAll(c.getAddedSubList());
                    if(c.wasRemoved())
                        c.getRemoved().forEach(m->serializableMeasurements.measurements.remove(m));
                }
            });
        }
        return measurements;
    }

    public static SerializableMeasurements getSerializable(){
        return serializableMeasurements;
    }

    public static List<SpectralMeasurement> add(SpectralMeasurement measurement){
        measurement.setId(serializableMeasurements.lastId++);
        if(serializableMeasurements.spectrumMath != null){
            measurement.applySpectrumMath(serializableMeasurements.spectrumMath);
        }
        get().add(measurement);
        return measurements;
    }

    private static void applySpectrumMath(SpectrumMath spectrumMath){
        serializableMeasurements.spectrumMath = spectrumMath;

        serializableMeasurements.spectrumMath.addChangedListener(()->{
            measurements.forEach(measurement -> measurement.applySpectrumMath(serializableMeasurements.spectrumMath));
        });

        if(serializableMeasurements.spectrumMath == null) {
            measurements.forEach(SpectralMeasurement::resetSpectrumMath);
        }
        else
            measurements.forEach(measurement -> measurement.applySpectrumMath(serializableMeasurements.spectrumMath));
    }

    public static void setSpectrumMath(SpectrumMath spectrumMath){
        try {
            serializableMeasurements.spectrumMath.removeAllListeners();
        } catch (NullPointerException ignore) {}
        serializableMeasurements.spectrumMath = spectrumMath;
        applySpectrumMath(serializableMeasurements.spectrumMath);
    }

    public static void removeSpectrumMath(){
        serializableMeasurements.spectrumMath = null;
        applySpectrumMath(serializableMeasurements.spectrumMath);
    }

    public static void resetSpectrumMath(){
        try {
            serializableMeasurements.spectrumMath.removeAllListeners();
        } catch (NullPointerException ignore) {}
        serializableMeasurements.spectrumMath = null;
        measurements.forEach(SpectralMeasurement::resetSpectrumMath);
    }

    public static String getMathOperatorName(String spectrumId){
        return serializableMeasurements.spectrumMath.toString(spectrumId);
    }

    public int nextId(){
        return serializableMeasurements.lastId++;
    }

    public static void addListener(ListChangeListener<? super SpectralMeasurement> listener){
        measurements.addListener(listener);
    }

    public static void removeListener(ListChangeListener<? super SpectralMeasurement> listener){
        measurements.removeListener(listener);
    }

    public static void saveMeasurements(File directory){
        measurements.forEach(m -> {
            serializableMeasurements.measurementsFile.put(m, new SpectralMeasurementCSV(m, directory));
        });
        measurements.addListener((ListChangeListener<SpectralMeasurement>) c -> {
            while (c.next())
                c.getAddedSubList().forEach(m->serializableMeasurements.measurementsFile.put(m,new SpectralMeasurementCSV(m,directory)));
        });
    }

    public static void deleteFile(SpectralMeasurement spectralMeasurement){
        serializableMeasurements.measurementsFile.get(spectralMeasurement).delete();
        serializableMeasurements.measurementsFile.remove(spectralMeasurement);
    }

    public static void deserialize(SerializableMeasurements serializableMeasurements){
        get().clear();
        get().addAll(serializableMeasurements.measurements);
        Measurements.serializableMeasurements = serializableMeasurements;
    }
}

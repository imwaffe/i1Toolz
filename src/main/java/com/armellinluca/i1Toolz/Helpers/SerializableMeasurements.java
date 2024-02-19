package com.armellinluca.i1Toolz.Helpers;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;
import com.armellinluca.i1Toolz.ColorUtils.SpectrumMath.SpectrumMath;
import com.armellinluca.i1Toolz.FileSystem.SpectralMeasurementCSV;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializableMeasurements implements Serializable {
    private static final long serialVersionUID = 9129283098306633232L;

    protected ArrayList<SpectralMeasurement> measurements = new ArrayList<>();
    protected transient HashMap<SpectralMeasurement, SpectralMeasurementCSV> measurementsFile = new HashMap<>();
    protected SpectrumMath spectrumMath = null;
    protected Integer lastId = 1;

    protected final HashMap<SpectralMeasurement, Spectrum> spectrums = new HashMap<>();
    protected final HashMap<SpectralMeasurement, Spectrum> originalSpectrums = new HashMap<>();
    protected final HashMap<SpectralMeasurement, String> labels = new HashMap<>();
    protected final HashMap<SpectralMeasurement, Integer> ids = new HashMap<>();

    private transient Integer startID = null;
    private transient String prependString = "";

    public SerializableMeasurements get(){
        return get(this.measurements);
    }

    public void initMeasurementsFile(){
        measurementsFile = new HashMap<>();
        System.out.println("IS NULL inside: "+(this.measurementsFile == null));
    }

    public SerializableMeasurements get(ArrayList<SpectralMeasurement> measurements){
        this.measurements = measurements;
        spectrums.clear();
        originalSpectrums.clear();
        labels.clear();
        ids.clear();
        measurements.forEach(m -> {
            spectrums.put(m, m.getSpectrum());
            originalSpectrums.put(m, m.getOriginalSpectrum());
            labels.put(m, m.labelProperty().getValue());
            ids.put(m, m.idProperty().getValue());
        });
        return this;
    }

    public void setStartID(Integer startID){
        this.startID = startID;
    }

    public void prependString(String prependString){
        this.prependString = prependString;
    }

    public void load(){
        String prependString = this.prependString==null?"":this.prependString;
        measurements.forEach(m->{
            m.spectrumProperty(spectrums.getOrDefault(m, null));
            m.originalSpectrumProperty(originalSpectrums.getOrDefault(m, null));
            m.labelProperty(prependString+labels.getOrDefault(m, null));
            if(startID == null)
                m.idProperty(ids.getOrDefault(m, null));
            else
                m.idProperty(startID++);
        });
    }

    public SerializableMeasurements append(SerializableMeasurements m){
        measurements.addAll(m.measurements);
        return this;
    }
}

package com.armellinluca.i1Toolz.Helpers;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;
import com.armellinluca.i1Toolz.ColorUtils.SpectrumMath.SpectrumMath;
import com.armellinluca.i1Toolz.FileSystem.SpectralMeasurementCSV;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializableMeasurements implements Serializable {
    protected ArrayList<SpectralMeasurement> measurements = new ArrayList<>();
    protected transient final HashMap<SpectralMeasurement, SpectralMeasurementCSV> measurementsFile = new HashMap<>();
    protected SpectrumMath spectrumMath = null;
    protected Integer lastId = 1;

    private final HashMap<SpectralMeasurement, Spectrum> spectrums = new HashMap<>();
    private final HashMap<SpectralMeasurement, Spectrum> originalSpectrums = new HashMap<>();
    private final HashMap<SpectralMeasurement, String> labels = new HashMap<>();
    private final HashMap<SpectralMeasurement, Integer> ids = new HashMap<>();

    public SerializableMeasurements get(){
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

    public void load(){
        measurements.forEach(m->{
            m.spectrumProperty(spectrums.getOrDefault(m, null));
            m.originalSpectrumProperty(originalSpectrums.getOrDefault(m, null));
            m.labelProperty(labels.getOrDefault(m, null));
            m.idProperty(ids.getOrDefault(m, null));
        });
    }
}

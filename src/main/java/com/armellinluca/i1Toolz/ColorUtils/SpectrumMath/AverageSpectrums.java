package com.armellinluca.i1Toolz.ColorUtils.SpectrumMath;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;
import com.armellinluca.i1Toolz.ColorUtils.StandardIlluminant;
import com.armellinluca.i1Toolz.EyeOne.InstrumentSingleton;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AverageSpectrums {
    public static SpectralMeasurement averageSpectrums(List<SpectralMeasurement> measurements, int newId){
        AtomicBoolean isEmissive = new AtomicBoolean(true);
        TreeMap<Integer, Float> spectrumData = new TreeMap<>();
        int size = measurements.size();
        StringBuilder label = new StringBuilder("Avg(");
        measurements.forEach(m -> {
            if(InstrumentSingleton.getInstrument().isReflectiveMode(m.getMeasurementMode()))
                isEmissive.set(false);
            label.append(m.getId().getValue()).append(",");
            m.getOriginalSpectrum().getSpectralData().forEach((wavelength, value) -> {
                Float newValue = spectrumData.getOrDefault(wavelength, 0F) + value / size;
                spectrumData.put(wavelength, newValue);
            });
        });
        label.append(")");
        SpectralMeasurement spm = new SpectralMeasurement(new Spectrum(spectrumData, isEmissive.get()?Spectrum.NORMALIZE_EMISSIVE:Spectrum.NORMALIZE_REFLECTANCE, isEmissive.get()?null: StandardIlluminant.getD65()), label.toString(), newId);
        return spm;
    }

    public static SpectralMeasurement averageSpectrums(List<SpectralMeasurement> measurements){
        return averageSpectrums(measurements, 0);
    }
}

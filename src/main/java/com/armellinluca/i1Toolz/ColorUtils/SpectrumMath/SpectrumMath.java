package com.armellinluca.i1Toolz.ColorUtils.SpectrumMath;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class SpectrumMath implements Serializable {
    private transient final List<Runnable> changeListeners = new ArrayList<>();

    public abstract Float computeValue(Integer wavelength, Float value);
    protected abstract String getOperatorString(String spectrumId);

    public String toString(String spectrumId){
        return "MathSpectrum_"+getOperatorString(spectrumId);
    }

    public Spectrum computeSpectrum(Spectrum measurement) {
        TreeMap<Integer, Float> outputSpectrumData = new TreeMap<>();

        for(Map.Entry<Integer, Float> entry : measurement.getSpectralData().entrySet()){
            Float newValue = computeValue(entry.getKey(), entry.getValue());
            outputSpectrumData.put(entry.getKey(), newValue);
        }

        Spectrum sp = new Spectrum(outputSpectrumData, measurement.getNormalize());
        if(sp.getY() > 100)
            return new Spectrum(outputSpectrumData, Spectrum.NORMALIZE_Y);
        return sp;
    }

    public void addChangedListener(Runnable r){
        changeListeners.add(r);
    }
    public void removeChangedListener(Runnable r){
        changeListeners.remove(r);
    }
    public void removeAllListeners(){ changeListeners.clear(); }
    protected void changed(){
        changeListeners.forEach(Runnable::run);
    }
}
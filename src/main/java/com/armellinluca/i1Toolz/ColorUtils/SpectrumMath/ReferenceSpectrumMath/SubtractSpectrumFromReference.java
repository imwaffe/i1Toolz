package com.armellinluca.i1Toolz.ColorUtils.SpectrumMath.ReferenceSpectrumMath;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import javafx.beans.property.ObjectProperty;

public class SubtractSpectrumFromReference extends ReferenceSpectrumMath{

    public SubtractSpectrumFromReference(ObjectProperty<SpectralMeasurement> reference) {
        super(reference);
    }

    @Override
    public Float computeValue(Integer wavelength, Float value) {
        return reference.getValue().getOriginalSpectrum().getSpectralData().getOrDefault(wavelength,0F)-value;
    }

    @Override
    public String getOperatorString(String spectrumId) {
        return reference.getValue().getId().getValue().toString()+"-"+spectrumId;
    }
}

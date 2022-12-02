package com.armellinluca.i1Toolz.ColorUtils.SpectrumMath.ReferenceSpectrumMath;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import javafx.beans.property.ObjectProperty;

public class SubtractReferenceFromSpectrum extends ReferenceSpectrumMath {

    public SubtractReferenceFromSpectrum(ObjectProperty<SpectralMeasurement> reference) {
        super(reference);
    }

    @Override
    public Float computeValue(Integer wavelength, Float value) {
        return value-reference.getValue().getOriginalSpectrum().getSpectralData().getOrDefault(wavelength,0F);
    }

    @Override
    public String getOperatorString(String spectrumId) {
        return spectrumId+"-"+reference.getValue().getId().getValue().toString();
    }
}

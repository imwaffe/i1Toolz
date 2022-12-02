package com.armellinluca.i1Toolz.ColorUtils.SpectrumMath.ReferenceSpectrumMath;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.SpectrumMath.SpectrumMath;
import javafx.beans.property.ObjectProperty;

public abstract class ReferenceSpectrumMath extends SpectrumMath {
    protected ObjectProperty<SpectralMeasurement> reference;

    public ReferenceSpectrumMath(ObjectProperty<SpectralMeasurement> reference){
        this.reference = reference;
        reference.addListener((observable, oldValue, newValue) -> super.changed());
    }
}

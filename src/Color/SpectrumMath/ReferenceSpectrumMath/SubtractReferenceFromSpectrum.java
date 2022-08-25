package Color.SpectrumMath.ReferenceSpectrumMath;

import Color.Spectrum.SpectralMeasurement;
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

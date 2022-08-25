package Color.SpectrumMath.ReferenceSpectrumMath;

import Color.Spectrum.SpectralMeasurement;
import javafx.beans.property.ObjectProperty;

public class SpectrumsAbsoluteDifference extends ReferenceSpectrumMath{

    public SpectrumsAbsoluteDifference(ObjectProperty<SpectralMeasurement> reference) {
        super(reference);
    }

    @Override
    public Float computeValue(Integer wavelength, Float value) {
        return Math.abs(reference.getValue().getOriginalSpectrum().getSpectralData().getOrDefault(wavelength,0F)-value);
    }

    @Override
    public String getOperatorString(String spectrumId) {
        return "abs("+reference.getValue().getId().getValue().toString()+"-"+spectrumId+")";
    }
}

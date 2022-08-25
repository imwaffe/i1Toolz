package Color.SpectrumMath.ReferenceSpectrumMath;

import Color.Spectrum.SpectralMeasurement;
import javafx.beans.property.ObjectProperty;

public class DivideReferenceBySpectrum extends ReferenceSpectrumMath{
    public DivideReferenceBySpectrum(ObjectProperty<SpectralMeasurement> reference) {
        super(reference);
    }

    @Override
    public Float computeValue(Integer wavelength, Float value) {
        if(value == 0F)
            return Float.POSITIVE_INFINITY;
        return reference.getValue().getOriginalSpectrum().getSpectralData().getOrDefault(wavelength, 0F)/value;
    }

    @Override
    public String getOperatorString(String spectrumId) {
        return reference.getValue().getId().getValue().toString()+"/"+spectrumId;
    }

}

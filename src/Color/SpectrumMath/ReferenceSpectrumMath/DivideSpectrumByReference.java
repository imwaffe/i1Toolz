package Color.SpectrumMath.ReferenceSpectrumMath;

import Color.Spectrum.SpectralMeasurement;
import javafx.beans.property.ObjectProperty;

public class DivideSpectrumByReference extends ReferenceSpectrumMath{

    public DivideSpectrumByReference(ObjectProperty<SpectralMeasurement> reference) {
        super(reference);
    }

    @Override
    public Float computeValue(Integer wavelength, Float value) {
        Float refValue = reference.getValue().getOriginalSpectrum().getSpectralData().getOrDefault(wavelength, 0F);
        if(refValue == 0F)
            return Float.POSITIVE_INFINITY;
        return value/refValue;
    }

    @Override
    public String getOperatorString(String spectrumId) {
        return spectrumId+"/"+reference.getValue().getId().getValue().toString();
    }
}

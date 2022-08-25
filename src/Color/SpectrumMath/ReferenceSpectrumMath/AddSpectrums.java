package Color.SpectrumMath.ReferenceSpectrumMath;

import Color.Spectrum.SpectralMeasurement;
import javafx.beans.property.ObjectProperty;

public class AddSpectrums extends ReferenceSpectrumMath {

    public AddSpectrums(ObjectProperty<SpectralMeasurement> reference) {
        super(reference);
    }

    @Override
    public Float computeValue(Integer wavelength, Float value) {
        return reference.getValue().getOriginalSpectrum().getSpectralData().getOrDefault(wavelength,0F)+value;
    }

    @Override
    public String getOperatorString(String spectrumId) {
        return reference.getValue().getId().getValue().toString()+"+"+spectrumId;
    }

}

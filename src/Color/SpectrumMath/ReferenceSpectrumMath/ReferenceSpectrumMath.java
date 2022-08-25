package Color.SpectrumMath.ReferenceSpectrumMath;

import Color.Spectrum.SpectralMeasurement;
import Color.SpectrumMath.SpectrumMath;
import javafx.beans.property.ObjectProperty;

public abstract class ReferenceSpectrumMath extends SpectrumMath {
    protected ObjectProperty<SpectralMeasurement> reference;

    public ReferenceSpectrumMath(ObjectProperty<SpectralMeasurement> reference){
        this.reference = reference;
        reference.addListener((observable, oldValue, newValue) -> super.changed());
    }
}

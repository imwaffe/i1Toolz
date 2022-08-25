package Color.SpectrumMath;

public class IdentitySpectrum extends SpectrumMath {
    @Override
    public Float computeValue(Integer wavelength, Float value) {
        return value;
    }

    @Override
    public String getOperatorString(String spectrumId) {
        return "identity("+spectrumId+")";
    }
}

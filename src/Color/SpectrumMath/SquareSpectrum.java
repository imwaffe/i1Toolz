package Color.SpectrumMath;

public class SquareSpectrum extends SpectrumMath {
    @Override
    public Float computeValue(Integer wavelength, Float value) {
        return (float)Math.pow(value,2);
    }

    @Override
    public String getOperatorString(String spectrumId) {
        return "square("+spectrumId+")";
    }
}

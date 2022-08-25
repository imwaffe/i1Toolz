package Color.Spectrum;

public abstract class SpectralMeasurementDecorator extends SpectralMeasurement{
    protected SpectralMeasurement spectralMeasurement;


    public SpectralMeasurementDecorator(Spectrum spectrum, int id) {
        super(spectrum, id);
    }
}

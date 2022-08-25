package Color.CRI;

import Color.ColorMath.CCTCalc;
import Color.Spectrum.SpectralMeasurement;
import Color.Spectrum.Spectrum;
import Color.StandardIlluminant;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.util.HashMap;

public class CRIMeasurement{

    private final SpectralMeasurement measurement;
    private final HashMap<Integer, Spectrum> testSpectrums = new HashMap<>();
    private final HashMap<Integer, Spectrum> referenceSpectrums;
    private final int CCT;
    private final Spectrum referenceIlluminant;
    private final Spectrum testSpectrum;

    private final Double[] uvReference;
    private final Double[] uvTest;

    private static final Spectrum WHITE_REF = StandardIlluminant.getD50();

    private final HashMap<Integer, Double> indices = new HashMap<>();
    private Double Ra = null;

    public CRIMeasurement(SpectralMeasurement measurement){
        this.measurement = measurement;
        CCT = CCTCalc.McCamy(measurement.getSpectrumConverter().get_xyY());
        if(CCT < 5000)
            referenceIlluminant = StandardIlluminant.getBlackbodyRadiator(CCT);
        else
            referenceIlluminant = StandardIlluminant.getIlluminantD(CCT);

        testSpectrum = measurement.getOriginalSpectrum();

        uvReference = referenceIlluminant.getConverter(WHITE_REF).get_uv();
        uvTest = testSpectrum.getConverter(WHITE_REF).get_uv();

        CRIPatches.getPatches().forEach((id, patchSpectrum) -> testSpectrums.put(id, new Spectrum(Spectrum.dotProduct(testSpectrum.getNormalizedSpectrum(Spectrum.NORMALIZE_Y), patchSpectrum.getNormalizedSpectrum(Spectrum.NORMALIZE_DONT_NORMALIZE)), Spectrum.NORMALIZE_DONT_NORMALIZE)));
        referenceSpectrums = CRIPatches.getReferenceSpectrums(referenceIlluminant);
    }

    public double getIndex(int id){
        if(!indices.containsKey(id)) {
            Double[] referenceUVW = getUVW(referenceSpectrums.get(id).getConverter(WHITE_REF).getYuv(), referenceIlluminant.getY());
            Double[] testYUV = new Double[]{(double)testSpectrums.get(id).getConverter(WHITE_REF).getY(), u_TCS_adaptation(id), v_TCS_adaptation(id)};
            Double[] testUVW = getUVW(testYUV, testSpectrum.getY());
            indices.put(id, specialCRI(norm2(referenceUVW, testUVW)));
        }
        return indices.get(id);
    }

    public Double[] getYuvPatchReference(int id){
        return referenceSpectrums.get(id).getConverter(WHITE_REF).getYuv();
    }

    public Double[] getYuvPatchTest(int id){
        return new Double[]{(double)testSpectrums.get(id).getConverter(WHITE_REF).getY(), u_TCS_adaptation(id), v_TCS_adaptation(id)};
    }

    public double computeRa(){
        if(Ra == null){
            Ra = 0D;
            for(int i=1; i<=8; i++)
                Ra+=getIndex(i);
            Ra/=8;
        }
        return Ra;
    }

    private double u_TCS_adaptation(int id){
        Double[] uvPatch = testSpectrums.get(id).getConverter(WHITE_REF).get_uv();
        double cConst = c(uvReference)*c(uvPatch)/c(uvTest);
        double dConst = d(uvReference)*d(uvPatch)/d(uvTest);
        return (10.872 + 0.404*cConst - 4*dConst) / (16.518 + 1.481*cConst - dConst);
    }

    private double v_TCS_adaptation(int id){
        Double[] uvPatch = testSpectrums.get(id).getConverter(WHITE_REF).get_uv();
        double cConst = c(uvReference)*c(uvPatch)/c(uvTest);
        double dConst = d(uvReference)*d(uvPatch)/d(uvTest);
        return 5.52/(16.518 + 1.481*cConst - dConst);
    }

    private Double[] getUVW(Double[] yuv){
        return getUVW(yuv, 100);
    }

    private Double[] getUVW(Double[] yuv, double refY){
        double w = 25*Math.cbrt(100*yuv[0]/refY)-17;
        double u = 13*w*(yuv[1] - WHITE_REF.getConverter(WHITE_REF).get_u());
        double v = 13*w*(yuv[2] - WHITE_REF.getConverter(WHITE_REF).get_v());
        return new Double[]{u,v,w};
    }

    private static Double specialCRI(Double dE){
        return 100-4.6*dE;
    }

    private static Double norm2(Double[] v1, Double[] v2){
        double acc = 0;
        for(int i=0; i<Math.min(v1.length, v2.length); i++)
            acc += Math.abs(v1[i]-v2[i]);
        return acc;
    }

    private static double c(Double[] uv){
        return c(uv[0],uv[1]);
    }
    private static double c(double u, double v){
        return (4-u-10*v)/v;
    }

    private static double d(Double[] uv){
        return d(uv[0],uv[1]);
    }
    private static double d(double u, double v){
        return (1.708*v+0.404-1.481*u)/v;
    }


    public SpectralMeasurement getSpectralMeasurement(){
        return measurement;
    }



    /* GUI METHODS */

    public void setLabel(String label) {
        measurement.setLabel(label);
    }

    public ObservableValue<String> getLabel(){
        return measurement.getLabel();
    }

    public ObservableValue<Integer> getId(){
        return measurement.getId();
    }

    public ObservableValue<Character> getFlag(){
        return measurement.getFlag();
    }

    public ObservableValue<Double> getRa(){
        return new SimpleObjectProperty<>(computeRa());
    }

    public ObservableValue<Integer> getCCT(){
        return new SimpleObjectProperty<>(CCT);
    }

    public ObservableValue<Double> getU(){
        return new SimpleObjectProperty<>(testSpectrum.getConverter(WHITE_REF).get_u());
    }

    public ObservableValue<Double> getV(){
        return new SimpleObjectProperty<>(testSpectrum.getConverter(WHITE_REF).get_v());
    }

    public ObservableValue<Double> getX(){
        return new SimpleObjectProperty<>(testSpectrum.getConverter(WHITE_REF).get_x());
    }

    public ObservableValue<Double> getY(){
        return new SimpleObjectProperty<>(testSpectrum.getConverter(WHITE_REF).get_y());
    }
}

package Color.Spectrum;

import Color.CMF;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Spectrum implements Cloneable, Serializable {

    public static final char NORMALIZE_DONT_NORMALIZE = 0;
    public static final char NORMALIZE_Y = 1;
    public static final char NORMALIZE_REFLECTANCE = NORMALIZE_DONT_NORMALIZE;
    public static final char NORMALIZE_EMISSIVE = NORMALIZE_Y;

    private final TreeMap<Integer,Float> spectralData;
    private final char normalize;
    private final int wavelengthStep;

    private CMF cmfSet = new CMF(CMF.CMF_CIE_XYZ_2006);
    private Spectrum referenceWhite = null;

    private final HashMap<Spectrum, SpectrumConverter> converters = new HashMap<>();
    private final HashMap<Character, Float> precomputedXYZ = new HashMap<>();

    public Spectrum(TreeMap<Integer, Float> spectralData, char normalize){
        this.spectralData = spectralData;
        this.normalize = normalize;
        wavelengthStep = (Integer)(spectralData.keySet().toArray())[0]-(Integer)(spectralData.keySet().toArray())[1];
    }

    public Spectrum(TreeMap<Integer, Float> spectralData, char normalize, Spectrum referenceWhite){
        this(spectralData, normalize);
        this.referenceWhite = referenceWhite;
    }

    public Spectrum(String csvFileName, char normalize){
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(Objects.requireNonNull(getClass().getResource(csvFileName)).getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        TreeMap<Integer,Float> spectrum = new TreeMap<>();
        List<String[]> rows = null;
        try {
            rows = reader.readAll();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        rows.forEach(r -> spectrum.put((int)Float.parseFloat(r[0]), Float.parseFloat(r[1])));
        this.spectralData = spectrum;
        this.normalize = normalize;
        wavelengthStep = (Integer)(spectrum.keySet().toArray())[0]-(Integer)(spectrum.keySet().toArray())[1];
    }

    public Spectrum(String csvFileName, char normalize, Spectrum referenceWhite){
        this(csvFileName, normalize);
        this.referenceWhite = referenceWhite;
    }

    public Spectrum clone() throws CloneNotSupportedException {
        return (Spectrum) super.clone();
    }

    public SpectrumConverter getConverter(Spectrum standardIlluminant){
        if(!converters.containsKey(standardIlluminant))
            converters.put(standardIlluminant, new SpectrumConverter(this, standardIlluminant));
        return converters.get(standardIlluminant);
    }

    public char getNormalize(){
        return this.normalize;
    }

    public TreeMap<Integer,Float> getSpectralData(){
        return spectralData;
    }

    public TreeMap<Integer,Float> getNormalizedSpectrum(){
        return getNormalizedSpectrum(normalize);
    }
    public TreeMap<Integer,Float> getNormalizedSpectrum(char normalize) {
        TreeMap<Integer, Float> normalized = new TreeMap<>();
        Float normCoeff = getNormCoeff(normalize);
        //System.out.println("normCoeff:"+normCoeff);
        //System.out.println("non normalized: "+ spectralData);
        spectralData.forEach((wavelength, value) -> normalized.put(wavelength, value*normCoeff));
        return normalized;
    }

    public Float[] toArray(){
        return getSpectralData().values().toArray(new Float[0]);
    }

    public int getWavelengthStep(){
        return wavelengthStep;
    }

    public static TreeMap<Integer,Float> dotProduct(TreeMap<Integer,Float> leftOperandMap, TreeMap<Integer,Float> rightOperandMap){
        TreeMap<Integer,Float> result = new TreeMap<>();
        for(Map.Entry<Integer,Float> entry : leftOperandMap.entrySet()){
            if(rightOperandMap.containsKey(entry.getKey()))
                result.put(entry.getKey(), entry.getValue()*rightOperandMap.getOrDefault(entry.getKey(),0F));
        }
        return result;
    }

    public static TreeMap<Integer,Float> dotProduct(Spectrum leftOperand, Spectrum rightOperand){
        return dotProduct(leftOperand.getSpectralData(), rightOperand.getSpectralData());
    }
    public TreeMap<Integer,Float> dotProduct(TreeMap<Integer,Float> rightOperandMap){
        return dotProduct(getSpectralData(), rightOperandMap);
    }

    public TreeMap<Integer,Float> dotProduct(Spectrum rightOperand){
        return dotProduct(rightOperand.getSpectralData());
    }

    public static Float getArea(TreeMap<Integer,Float> spectrum, Float normCoeff){
        int wavelengthStep = Math.abs((int) (spectrum.keySet().toArray())[0] - (int) (spectrum.keySet().toArray())[1]);
        AtomicReference<Float> area = new AtomicReference<>(0F);
        spectrum.forEach((wavelength, value) -> area.updateAndGet(v -> v + value));
        return normCoeff*wavelengthStep*area.get();
    }

    public void setCMFset(String csvFileName) {
        cmfSet = new CMF(csvFileName);
    }

    public Float getX(){
        if(!precomputedXYZ.containsKey('X')) {
            float val;
            if (referenceWhite == null)
                val = 100 * getArea(dotProduct(cmfSet.getX()), getNormCoeff(normalize));
            else
                val = 100 * getArea(dotProduct(dotProduct(getSpectralData(), cmfSet.getX()), referenceWhite.getNormalizedSpectrum(NORMALIZE_Y)), getNormCoeff(normalize));
            precomputedXYZ.put('X', val);
        }
        return precomputedXYZ.get('X');
    }
    public Float getY(){
        if(!precomputedXYZ.containsKey('Y')) {
            float val;
            if (referenceWhite == null)
                val = 100*getArea(dotProduct(cmfSet.getY()), getNormCoeff(normalize));
            else
                val = 100*getArea(dotProduct(dotProduct(getSpectralData(),cmfSet.getY()),referenceWhite.getNormalizedSpectrum(NORMALIZE_Y)), getNormCoeff(normalize));
            precomputedXYZ.put('Y', val);
        }
        return precomputedXYZ.get('Y');
    }
    public Float getZ(){
        if(!precomputedXYZ.containsKey('Z')) {
            float val;
            if (referenceWhite == null)
                val = 100*getArea(dotProduct(cmfSet.getZ()), getNormCoeff(normalize));
            else
                val = 100*getArea(dotProduct(dotProduct(getSpectralData(),cmfSet.getZ()),referenceWhite.getNormalizedSpectrum(NORMALIZE_Y)), getNormCoeff(normalize));
            precomputedXYZ.put('Z', val);
        }
        return precomputedXYZ.get('Z');
    }
    private Float getBaseY() {
        if(!precomputedXYZ.containsKey('b')){
            precomputedXYZ.put('b', getArea(dotProduct(cmfSet.getY()), getNormCoeff(NORMALIZE_DONT_NORMALIZE)));
        }
        return precomputedXYZ.get('b');
    }

    public Float[] getXYZ(){
        return new Float[]{getX(), getY(), getZ()};
    }
    public Float getNormCoeff(char normalize){
        switch (normalize){
            case NORMALIZE_DONT_NORMALIZE: return 1F;
            case NORMALIZE_Y: return 1/getBaseY();
        }
        return 1F;
    }
}

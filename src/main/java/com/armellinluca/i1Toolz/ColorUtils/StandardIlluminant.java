package com.armellinluca.i1Toolz.ColorUtils;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;

public class StandardIlluminant {
    private static Spectrum d65 = null;
    private static Spectrum d50 = null;
    private static Spectrum a = null;
    private static final HashMap<Integer, Spectrum> blackbodies = new HashMap<>();
    private static final HashMap<Integer, Spectrum> illuminantsD = new HashMap<>();

    public static final double c = 3*Math.pow(10,8);   // speed of light in vacuum
    public static final double h = 6.625*Math.pow(10,-34);     // Planck constant
    public static final double k = 1.38*Math.pow(10,-23);  // Boltzmann constant

    public static final String STANDARD_ILLUMINANTS_ROOT = "/StandardIlluminants/";
    public static final String STANDARD_ILLUMINANTS_EXTENSION = ".csv";

    public static final char NORMALIZE = Spectrum.NORMALIZE_Y;

    public static final String ILLUMINANT_D_COMPONENT_S0_FILENAME = STANDARD_ILLUMINANTS_ROOT+"illuminant_d_component_s0"+STANDARD_ILLUMINANTS_EXTENSION;
    public static final String ILLUMINANT_D_COMPONENT_S1_FILENAME = STANDARD_ILLUMINANTS_ROOT+"illuminant_d_component_s1"+STANDARD_ILLUMINANTS_EXTENSION;
    public static final String ILLUMINANT_D_COMPONENT_S2_FILENAME = STANDARD_ILLUMINANTS_ROOT+"illuminant_d_component_s2"+STANDARD_ILLUMINANTS_EXTENSION;

    private static TreeMap<Integer, Float> IlluminantD_S0 = null;
    private static TreeMap<Integer, Float> IlluminantD_S1 = null;
    private static TreeMap<Integer, Float> IlluminantD_S2 = null;

    public static Spectrum getA(){
        if(a == null) {
            a = new Spectrum(STANDARD_ILLUMINANTS_ROOT+"a"+STANDARD_ILLUMINANTS_EXTENSION, NORMALIZE);
        }
        return a;
    }

    public static Spectrum getD50(){
        if(d50 == null) {
            d50 = new Spectrum(STANDARD_ILLUMINANTS_ROOT+"d50"+STANDARD_ILLUMINANTS_EXTENSION, NORMALIZE);
        }
        return d50;
    }

    public static Spectrum getD65(){
        if(d65 == null) {
            d65 = new Spectrum(STANDARD_ILLUMINANTS_ROOT+"d65"+STANDARD_ILLUMINANTS_EXTENSION, NORMALIZE);
        }
        return d65;
    }

    public static Spectrum getBlackbodyRadiator(int CCT, int startWavelength, int stopWavelength, int nmSteps){
        if(!blackbodies.containsKey(CCT)){
            TreeMap<Integer, Float> spectralData = new TreeMap<>();
            for (int i=startWavelength; i<=stopWavelength; i+=nmSteps){
                double wl = i*Math.pow(10,-9);
                spectralData.put(i, (float)( ( (2*h*c*c)/Math.pow(wl,5) ) * ( Math.exp( -(h*c)/(wl*k*CCT) ) ) ) );
            }
            blackbodies.put(CCT, new Spectrum(spectralData, NORMALIZE));
        }
        return blackbodies.get(CCT);
    }

    public static Spectrum getBlackbodyRadiator(int CCT, int startWavelength, int stopWavelength){
        return getBlackbodyRadiator(CCT, startWavelength, stopWavelength, 1);
    }

    public static Spectrum getBlackbodyRadiator(int CCT){
        return getBlackbodyRadiator(CCT, 360, 780);
    }

    public static Spectrum getIlluminantD(int CCT){
        if(!illuminantsD.containsKey(CCT)){
            if(IlluminantD_S0 == null)
                IlluminantD_S0 = readSpectrumFromCSV(ILLUMINANT_D_COMPONENT_S0_FILENAME, 0, 1);
            if(IlluminantD_S1 == null)
                IlluminantD_S1 = readSpectrumFromCSV(ILLUMINANT_D_COMPONENT_S1_FILENAME, 0, 1);
            if(IlluminantD_S2 == null)
                IlluminantD_S2 = readSpectrumFromCSV(ILLUMINANT_D_COMPONENT_S2_FILENAME, 0, 1);

            Double[] xy = BlackbodyLocus.get_xy(CCT);

            Double M1 = (-1.3515-1.7703*xy[0]+5.9114*xy[1])/(0.0241+0.2562*xy[0]-0.7341*xy[1]);
            Double M2 = (0.03-31.4424*xy[0]+30.0717*xy[1])/(0.0241+0.2562*xy[0]-0.7341*xy[1]);

            TreeMap<Integer, Float> spectralData = new TreeMap<>();

            IlluminantD_S0.forEach((wavelength, s0val)->{
                spectralData.put(wavelength, (float)(s0val + M1*IlluminantD_S1.get(wavelength) + M2*IlluminantD_S2.get(wavelength)));
            });

            illuminantsD.put(CCT, new Spectrum(spectralData, NORMALIZE));
        }
        return illuminantsD.get(CCT);
    }

    public static TreeMap<Integer, Float> readSpectrumFromCSV(String filename, int wavelengthColumns, int valueColumn){
        TreeMap<Integer, Float> spectralData = new TreeMap<>();
        try {
            CSVReader csvReader = new CSVReader(new InputStreamReader(StandardIlluminant.class.getResourceAsStream(filename)));;
            csvReader.readAll().forEach(r -> {
                spectralData.put(Integer.parseInt(r[wavelengthColumns]), Float.parseFloat(r[valueColumn]));
            });
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        return spectralData;
    }
}
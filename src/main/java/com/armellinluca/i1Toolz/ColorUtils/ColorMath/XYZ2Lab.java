package com.armellinluca.i1Toolz.ColorUtils.ColorMath;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;

import java.io.Serializable;

public class XYZ2Lab implements Serializable {
    public static final Double e = 0.008856;
    public static final Double k = 903.3;
    public static final Double kl_1 = 116D;
    public static final Double kl_2 = 16D;
    public static final Double ka = 500D;
    public static final Double kb = 200D;

    private final Float[] referenceWhiteXYZ;
    Spectrum referenceWhite;

    public XYZ2Lab(Spectrum referenceWhite){
        this.referenceWhite = referenceWhite;
        this.referenceWhiteXYZ = this.referenceWhite.getXYZ();
        /*for (int i=0;i<3;i++)
            this.referenceWhite[i] = 100*this.referenceWhite[i]/this.referenceWhite[1];*/
    }

    public Double getL(Spectrum spectrum){
        Double yR = (double) spectrum.getY()/(double) referenceWhiteXYZ[1];
        return kl_1*getThresholdedValue(yR)-kl_2;
    }

    public Double getLuvL(Spectrum spectrum){
        double yR = (double) spectrum.getY()/(double) referenceWhiteXYZ[1];
        if(yR > e)
            return kl_1*Math.cbrt(yR)-kl_2;
        return k*yR;
    }

    public Double getA(Spectrum spectrum){
        Double xR = (double) spectrum.getX()/(double) referenceWhiteXYZ[0];
        Double yR = (double) spectrum.getY()/(double) referenceWhiteXYZ[1];
        return ka*(getThresholdedValue(xR)-getThresholdedValue(yR));
    }

    public Double getB(Spectrum spectrum){
        Double yR = (double) spectrum.getY()/(double) referenceWhiteXYZ[1];
        Double zR = (double) spectrum.getZ()/(double) referenceWhiteXYZ[2];
        return kb*(getThresholdedValue(yR)-getThresholdedValue(zR));
    }

    public Double getU(Spectrum spectrum){
        return 4D*spectrum.getX()/(spectrum.getX()+15D*spectrum.getY()+3D*spectrum.getZ());
    }

    public Double getV(Spectrum spectrum){
        return 4D*spectrum.getY()/(spectrum.getX()+15D*spectrum.getY()+3D*spectrum.getZ());
    }

    public Double[] getLab(Spectrum spectrum){
        return new Double[]{
            getL(spectrum), getA(spectrum), getB(spectrum)
        };
    }

    public Double getThresholdedValue(Double inputValue){
        if(inputValue > e)
            return Math.cbrt(inputValue);
        return (k*inputValue+ kl_2)/ kl_1;
    }
}

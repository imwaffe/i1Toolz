package com.armellinluca.i1Toolz.ColorUtils.Spectrum;

import com.armellinluca.i1Toolz.ColorUtils.ColorMath.XYZ2Lab;

import java.io.Serializable;

public class SpectrumConverter implements Serializable {
    private final Spectrum spectrum;
    private final Spectrum referenceSpectrum;
    private final XYZ2Lab xyz2Lab;

    public SpectrumConverter(Spectrum spectrum, Spectrum referenceSpectrum){
        this.spectrum = spectrum;
        this.referenceSpectrum = referenceSpectrum;
        xyz2Lab = new XYZ2Lab(referenceSpectrum);
    }

    public Double getL(){
        return xyz2Lab.getL(spectrum);
    }
    public Double getA(){
        return xyz2Lab.getA(spectrum);
    }
    public Double getB(){
        return xyz2Lab.getB(spectrum);
    }
    public Double[] getLab(){
        return new Double[]{getL(), getA(), getB()};
    }

    public Float getX(){
        return spectrum.getX();
    }
    public Float getY(){
        return spectrum.getY();
    }
    public Float getZ(){
        return spectrum.getZ();
    }
    public Float[] getXYZ(){
        return new Float[]{getX(), getY(), getZ()};
    }

    public Double get_x(){
        return (double)getX()/(double)(getX()+getY()+getZ());
    }
    public Double get_y(){
        return (double)getY()/(double)(getX()+getY()+getZ());
    }

    public Double[] get_xyY(){
        return new Double[]{get_x(), get_y(), (double)getY()};
    }

    public Float getRelativeX(){
        return getX()/referenceSpectrum.getX();
    }
    public Float getRelativeY(){
        return getY()/referenceSpectrum.getY();
    }
    public Float getRelativeZ(){
        return getZ()/referenceSpectrum.getZ();
    }
    public Float[] getRelativeXYZ(){
        return new Float[]{getRelativeX(), getRelativeY(), getRelativeZ()};
    }

    public Double getRelative_x(){
        return (double)getRelativeX()/(double)(getRelativeX()+getRelativeY()+getRelativeZ());
    }
    public Double getRelative_y(){
        return (double)getRelativeY()/(double)(getRelativeX()+getRelativeY()+getRelativeZ());
    }

    public Double[] getRelative_xyY(){
        return new Double[]{getRelative_x(),getRelative_y(),(double)getRelativeY()};
    }

    public Double get_u(){
        return xyz2Lab.getU(spectrum);
    }
    public Double get_v(){
        return xyz2Lab.getV(spectrum);
    }
    public Double[] get_uv(){
        return new Double[]{get_u(), get_v()};
    }
    public Double[] getYuv() { return new Double[]{(double)getY(), get_u(), get_v()}; }
}

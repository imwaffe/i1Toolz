package com.armellinluca.i1Toolz.ColorUtils.SpectrumMath;

public class GetOperatorByString {
    public static Class get(String operator){
        try {
            return Class.forName(operator);
        } catch (ClassNotFoundException e) {
            return IdentitySpectrum.class;
        }
    }
}

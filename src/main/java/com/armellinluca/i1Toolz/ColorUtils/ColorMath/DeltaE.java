package com.armellinluca.i1Toolz.ColorUtils.ColorMath;

public class DeltaE {

    public static Double DE76(Double[] lab1, Double[] lab2){
        return Math.abs(lab1[0]-lab2[0])+Math.abs(lab1[1]-lab2[1])+Math.abs(lab1[2]-lab2[2]);
    }

}

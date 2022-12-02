package com.armellinluca.i1Toolz.ColorUtils.CRI;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.Spectrum;

import java.util.HashMap;

public class CRIPatches {
    private static final String CRI_PATCHES_ROOT = "/CRIPatches/";
    private static final String CRI_PATCHES_BASE_NAME = "TCS";
    private static final String CRI_PATCHES_EXTENSION = ".csv";

    private static final HashMap<Integer, Spectrum> patches = new HashMap<>();
    private static final HashMap<Spectrum, HashMap<Integer, Spectrum>> referenceSpectrums = new HashMap<>();

    public static final int MIN_PATCH_ID = 1;
    public static final int MAX_PATCH_ID = 14;

    private static void loadPatch(Integer tcsId){
        if(tcsId < MIN_PATCH_ID || tcsId > MAX_PATCH_ID)
            return;
        if(!patches.containsKey(tcsId))
            patches.put(tcsId, new Spectrum(CRI_PATCHES_ROOT+CRI_PATCHES_BASE_NAME+tcsId.toString()+CRI_PATCHES_EXTENSION, Spectrum.NORMALIZE_DONT_NORMALIZE));
    }

    public static Spectrum getPatch(Integer tcsId){
        loadPatch(tcsId);
        return patches.get(tcsId);
    }

    public static HashMap<Integer, Spectrum> getPatches(){
        for(int i=MIN_PATCH_ID; i<= MAX_PATCH_ID; i++)
            loadPatch(i);
        return patches;
    }

    public static HashMap<Integer, Spectrum> getReferenceSpectrums(Spectrum illuminant){
        if(!referenceSpectrums.containsKey(illuminant)) {
            HashMap<Integer, Spectrum> newRef = new HashMap<>();
            getPatches().forEach((id, patchSpectrum)->{
                newRef.put(id, new Spectrum(Spectrum.dotProduct(illuminant.getNormalizedSpectrum(Spectrum.NORMALIZE_Y), patchSpectrum.getNormalizedSpectrum(Spectrum.NORMALIZE_DONT_NORMALIZE)), Spectrum.NORMALIZE_DONT_NORMALIZE));
            });
            referenceSpectrums.put(illuminant, newRef);
        }
        return referenceSpectrums.get(illuminant);
    }
}

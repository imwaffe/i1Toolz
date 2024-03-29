package com.armellinluca.i1Toolz.FileSystem;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

public class SpectralMeasurementCSV implements Serializable {
    private transient File csvFile;
    private final transient File directory;
    private final transient SpectralMeasurement spectralMeasurement;

    public SpectralMeasurementCSV(SpectralMeasurement spectralMeasurement, File directory) {
        this.spectralMeasurement = spectralMeasurement;
        this.directory = directory;
        try {
            write();
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean delete(){
        return csvFile.delete();
    }

    private void init(){
        spectralMeasurement.getLabel().addListener((observable, oldValue, newValue) -> {
            csvFile.delete();
            try {
                write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void write() throws IOException {
        String fileName = spectralMeasurement.getLabel().getValue();
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        csvFile = new File(directory, spectralMeasurement.getId().getValue().toString()+"_"+fileName+".csv");
        csvFile.createNewFile();
        try (PrintWriter pw = new PrintWriter(csvFile)) {
            spectralMeasurement.getOriginalSpectrum().getSpectralData().forEach((wavelength, value)-> pw.println(wavelength.toString()+","+value.toString()));
        }
    }
}

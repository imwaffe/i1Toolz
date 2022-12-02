package com.armellinluca.i1Toolz.Helpers;

import com.armellinluca.i1Toolz.ColorUtils.CRI.CRIMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;

import java.util.Collection;

public class CRIMeasurements extends Measurements{
    private static transient final ObservableMap<SpectralMeasurement, CRIMeasurement> criMeasurements = FXCollections.observableHashMap();

    public CRIMeasurements(){
        get().forEach(m -> {
            if(m.isEmissive() &&
            ! criMeasurements.containsKey(m))
                criMeasurements.put(m, new CRIMeasurement(m));
        });
        get().addListener((ListChangeListener<SpectralMeasurement>) c -> {
            while (c.next()){
                if(c.wasAdded())
                    c.getAddedSubList().forEach(m -> {
                        if(m.isEmissive() &&
                        ! criMeasurements.containsKey(m))
                            criMeasurements.put(m, new CRIMeasurement(m));
                    });
                else if(c.wasRemoved())
                    c.getRemoved().forEach(criMeasurements::remove);
            }
        });
    }

    public Collection<CRIMeasurement> getCriMeasurements(){
        return criMeasurements.values();
    }

    public CRIMeasurement getCriMeasurement(SpectralMeasurement spectralMeasurement){
        return criMeasurements.get(spectralMeasurement);
    }
}

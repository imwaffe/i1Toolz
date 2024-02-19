package com.armellinluca.i1Toolz.Clipboard;

import com.armellinluca.i1Toolz.ColorUtils.CRI.CRIMeasurement;
import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.Helpers.Measurements;
import com.armellinluca.i1Toolz.Helpers.SerializableMeasurements;
import javafx.scene.control.TableView;
import javafx.scene.input.DataFormat;

import java.util.ArrayList;

public class CRIMeasurementsClipboard extends TableClipboard<CRIMeasurement, SerializableMeasurements> {

    public CRIMeasurementsClipboard(TableView<CRIMeasurement> table) {
        super(table);
    }

    @Override
    protected DataFormat getDataFormat() {
        return SpectralMeasurementsClipboard.dataFormat;
    }

    @Override
    protected SerializableMeasurements putElements(ArrayList<CRIMeasurement> selectedElements) {
        SerializableMeasurements measurements = new SerializableMeasurements();
        ArrayList<SpectralMeasurement> sm = new ArrayList<>();
        selectedElements.forEach(l -> {
            sm.add(l.getSpectralMeasurement());
        });
        return measurements.get(sm);
    }

    @Override
    protected void retrieveElements(SerializableMeasurements clipboardContent) {
        clipboardContent.setStartID(Measurements.getLastID());
        clipboardContent.prependString("copyOf_");
        clipboardContent.load();
        Measurements.deserialize(clipboardContent, true);
    }
}

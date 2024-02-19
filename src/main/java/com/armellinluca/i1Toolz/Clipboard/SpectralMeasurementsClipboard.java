package com.armellinluca.i1Toolz.Clipboard;

import com.armellinluca.i1Toolz.ColorUtils.Spectrum.SpectralMeasurement;
import com.armellinluca.i1Toolz.Helpers.Measurements;
import com.armellinluca.i1Toolz.Helpers.SerializableMeasurements;
import javafx.scene.control.TableView;
import javafx.scene.input.DataFormat;

import java.util.ArrayList;

public class SpectralMeasurementsClipboard extends TableClipboard<SpectralMeasurement, SerializableMeasurements> {
    public static final DataFormat dataFormat = new DataFormat("i1TzSMC");

    public SpectralMeasurementsClipboard(TableView<SpectralMeasurement> table) {
        super(table);
    }

    @Override
    public DataFormat getDataFormat() {
        return dataFormat;
    }

    @Override
    protected SerializableMeasurements putElements(ArrayList<SpectralMeasurement> selectedElements) {
        SerializableMeasurements measurements = new SerializableMeasurements();
        return measurements.get(selectedElements);
    }

    @Override
    protected void retrieveElements(SerializableMeasurements clipboardContent) {
        clipboardContent.setStartID(Measurements.getLastID());
        clipboardContent.prependString("copyOf_");
        clipboardContent.load();
        Measurements.deserialize(clipboardContent, true);
    }
}

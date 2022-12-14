package com.armellinluca.i1Toolz.Helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SerializableMeasurementsDecorator {
    private final SerializableMeasurements serializableMeasurements;

    public SerializableMeasurementsDecorator(SerializableMeasurements serializableMeasurements){
        this.serializableMeasurements = serializableMeasurements;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof SerializableMeasurements))
            return false;
        SerializableMeasurements test = ((SerializableMeasurements) obj).get();
        SerializableMeasurements current = serializableMeasurements.get();

        System.out.println(mapByteEquals(current.spectrums, test.spectrums));
        System.out.println(mapByteEquals(current.labels, test.labels));
        System.out.println(mapByteEquals(current.originalSpectrums, test.originalSpectrums));
        System.out.println(mapByteEquals(current.ids, test.ids));

        return mapByteEquals(current.spectrums, test.spectrums) && mapByteEquals(current.labels, test.labels) &&
                mapByteEquals(current.originalSpectrums, test.originalSpectrums) && mapByteEquals(current.ids, test.ids);
    }

    public boolean isEmpty(){
        return serializableMeasurements.measurements.size() == 0;
    }

    private static boolean mapByteEquals(Map<?,?> map1, Map<?,?> map2){
        return Arrays.equals(mapToByteArray(map1), mapToByteArray(map2));
    }

    private static byte[] mapToByteArray(Map<?,?> map){
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteOut.toByteArray();
    }
}

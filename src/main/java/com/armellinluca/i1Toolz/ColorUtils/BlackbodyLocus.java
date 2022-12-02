package com.armellinluca.i1Toolz.ColorUtils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BlackbodyLocus {
    public static final int CSV_WAVELENGTH_COLUMN = 0;
    public static final int CSV_x_COLUMN = 1;
    public static final int CSV_y_COLUMN = 2;
    public static final int CSV_u_COLUMN = 3;
    public static final int CSV_v_COLUMN = 4;
    public static final int CSV_y_DAILIGHT_COLUMN = 5;
    public static final int CSV_y_TM30_COLUMN = 6;
    public static final String CSV_FILE = "/StandardIlluminants/blackbody_locus.csv";

    private static List<String[]> rows = null;
    private static final HashMap<Integer, Double[]> values = new HashMap<>();

    public static Double[] get_xy(int CCT){
        Double[] allVals = getRow(CCT);
        return new Double[]{allVals[CSV_x_COLUMN-1], allVals[CSV_y_COLUMN-1]};
    }

    public static Double[] get_uv(int CCT){
        Double[] allVals = getRow(CCT);
        return new Double[]{allVals[CSV_u_COLUMN-1], allVals[CSV_v_COLUMN-1]};
    }

    private static Double[] getRow(int CCT){
        if(rows == null) {
            rows = new ArrayList<>();
            try {
                CSVReader csvReader = new CSVReader(new FileReader(Objects.requireNonNull(BlackbodyLocus.class.getResource(CSV_FILE)).getFile()));
                rows = csvReader.readAll();
            } catch (IOException | CsvException e) {
                e.printStackTrace();
            }
        }
        if(!values.containsKey(CCT)){
            values.put(CCT, new Double[]{0D,0D,0D,0D,0D,0D});
            rows.forEach(r -> {
                if(Integer.parseInt(r[CSV_WAVELENGTH_COLUMN]) == CCT){
                    values.put(CCT, new Double[]{
                            Double.parseDouble(r[CSV_x_COLUMN]),
                            Double.parseDouble(r[CSV_y_COLUMN]),
                            Double.parseDouble(r[CSV_u_COLUMN]),
                            Double.parseDouble(r[CSV_v_COLUMN]),
                            Double.parseDouble(r[CSV_y_DAILIGHT_COLUMN]),
                            Double.parseDouble(r[CSV_y_TM30_COLUMN]),
                    });
                }
            });
        }
        return values.get(CCT);
    }
}

package Color;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class CMF implements Serializable {
    public static final String CMFs_ROOT = "/resources/CMFs/";
    public static final String CMF_EXTENSION = ".csv";
    public static final String CMF_CIE_XYZ_1964 = "ciexyz64";
    public static final String CMF_CIE_XYZ_1931 = "ciexyz31";
    public static final String CMF_CIE_XYZ_2006 = "ciexyz06";
    public static final int CSV_CMF_WAVELENGTH_COLUMN = 0;
    public static final int CSV_CMF_X_COLUMN = 1;
    public static final int CSV_CMF_Y_COLUMN = 2;
    public static final int CSV_CMF_Z_COLUMN = 3;

    protected final TreeMap<Integer,Float> X = new TreeMap<>();
    protected final TreeMap<Integer,Float> Y = new TreeMap<>();
    protected final TreeMap<Integer,Float> Z = new TreeMap<>();

    public CMF(String csvFile){
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(Objects.requireNonNull(getClass().getResource(CMFs_ROOT+csvFile+CMF_EXTENSION)).getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String[]> rows = null;
        try {
            rows = reader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }
        rows.forEach(r -> {
            X.put((int)Float.parseFloat(r[CSV_CMF_WAVELENGTH_COLUMN]), Float.parseFloat(r[CSV_CMF_X_COLUMN]));
            Y.put((int)Float.parseFloat(r[CSV_CMF_WAVELENGTH_COLUMN]), Float.parseFloat(r[CSV_CMF_Y_COLUMN]));
            Z.put((int)Float.parseFloat(r[CSV_CMF_WAVELENGTH_COLUMN]), Float.parseFloat(r[CSV_CMF_Z_COLUMN]));
        });
    }

    public TreeMap<Integer,Float> getX(){
        return X;
    }

    public TreeMap<Integer,Float> getY(){
        return Y;
    }

    public TreeMap<Integer,Float> getZ(){
        return Z;
    }
}

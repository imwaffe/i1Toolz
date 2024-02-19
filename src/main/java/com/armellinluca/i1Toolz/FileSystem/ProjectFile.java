package com.armellinluca.i1Toolz.FileSystem; 

import com.armellinluca.i1Toolz.Helpers.BackwardCompatibleInputStream;
import com.armellinluca.i1Toolz.Helpers.SerializableMeasurements;
import com.armellinluca.i1Toolz.Helpers.SerializableMeasurementsDecorator;
import javafx.scene.control.Alert;

import java.io.*;

public class ProjectFile {

    public static SerializableMeasurements loadFromFile(File projectFile){
        try (FileInputStream fis = new FileInputStream(projectFile);
            ObjectInputStream ois = new BackwardCompatibleInputStream(fis)){
            SerializableMeasurements serializableMeasurements = (SerializableMeasurements)ois.readObject();
            serializableMeasurements.load();
            return serializableMeasurements;
        } catch (InvalidClassException ex){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Project not valid");
            alert.setHeaderText("Project file is not valid");
            alert.setContentText("The project file is damaged or from a previous version of this software and can't be opened.");
            alert.show();
            ex.printStackTrace();
        } catch (IOException | ClassNotFoundException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean hasChanged(File projectFile, SerializableMeasurements currentMeasurements){
        if(currentMeasurements == null) {
            return false;
        }
        SerializableMeasurementsDecorator decoratedMeasurements = new SerializableMeasurementsDecorator(currentMeasurements.get());
        if(decoratedMeasurements.isEmpty())
            return false;
        if(projectFile == null) {
            return true;
        }
        SerializableMeasurements savedMeasurements = loadFromFile(projectFile).get();
        assert savedMeasurements != null;
        return !decoratedMeasurements.equals(savedMeasurements);
    }

}

package Color.Spectrum;

import Color.SpectrumMath.SpectrumMath;
import Color.StandardIlluminant;
import EyeOne.Instrument;
import EyeOne.InstrumentSingleton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class SpectralMeasurement implements Serializable {
    private transient SimpleObjectProperty<Spectrum> spectrum;
    private transient SimpleObjectProperty<Spectrum> originalSpectrum;

    private Spectrum ref = null;
    private transient SimpleStringProperty label = new SimpleStringProperty();
    private transient IntegerProperty id = new SimpleIntegerProperty();
    private final SpectrumConverter spectrumConverter;

    private String measurementMode = null;
    private String instrumentType = null;
    private String instrumentSerialNumber = null;
    private boolean isEmissive = false;

    public SimpleObjectProperty<Spectrum> spectrumProperty() {
        return spectrum;
    }
    public ObservableValue<Spectrum> originalSpectrumProperty() {
        return originalSpectrum;
    }
    public void spectrumProperty(Spectrum spectrum) {
        if(this.spectrum == null)
            this.spectrum = new SimpleObjectProperty<>();
        this.spectrum.set(spectrum);
    }
    public void originalSpectrumProperty(Spectrum originalSpectrum) {
        if(this.originalSpectrum == null)
            this.originalSpectrum = new SimpleObjectProperty<>();
        this.originalSpectrum.set(originalSpectrum);
    }
    public SimpleStringProperty labelProperty() {
        return label;
    }
    public IntegerProperty idProperty() {
        return id;
    }
    public void labelProperty(String label) {
        if(this.label == null)
            this.label = new SimpleStringProperty();
        this.label.setValue(label);
    }
    public void idProperty(Integer id) {
        if(this.id == null)
            this.id = new SimpleIntegerProperty();
        this.id.setValue(id);
    }

    public SpectralMeasurement(Spectrum spectrum, String label, int id){
        this.spectrum = new SimpleObjectProperty<>();
        this.originalSpectrum = new SimpleObjectProperty<>();
        try {
            this.spectrum.setValue(spectrum.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        originalSpectrum.setValue(spectrum);
        this.label.setValue(label);
        this.id.setValue(id);
        try {
            measurementMode = InstrumentSingleton.getInstrument().getOption(Instrument.MEASUREMENT_MODE);
            isEmissive = InstrumentSingleton.getInstrument().isEmissiveMode();
            instrumentType = InstrumentSingleton.getInstrument().getOption(Instrument.DEVICE_TYPE);
            instrumentSerialNumber = InstrumentSingleton.getInstrument().getOption(Instrument.SERIAL_NUMBER);
        }catch(NullPointerException ignored){}
        spectrumConverter = new SpectrumConverter(this.spectrum.getValue(), StandardIlluminant.getD65());
    }
    public SpectralMeasurement(Spectrum spectrum, int id){
        this(spectrum,new Date(System.currentTimeMillis()).toString()+"_"+ InstrumentSingleton.getInstrument().getOption(Instrument.MEASUREMENT_MODE),id);
    }
    public SpectralMeasurement(Spectrum spectrum, Spectrum ref, String label, int id){
        this(spectrum,label,id);
        this.ref = ref;
    }
    public SpectralMeasurement(Spectrum spectrum, Spectrum ref, int id){
        this(spectrum,id);
        this.ref = ref;
    }
    public SpectralMeasurement(Spectrum spectrum, Spectrum ref){
        this(spectrum,ref,1);
    }

    public void resetSpectrumMath(){
        try {
            spectrum.setValue(originalSpectrum.getValue().clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void applySpectrumMath(SpectrumMath spectrumMath){
        if(spectrumMath == null)
            resetSpectrumMath();
        else
            spectrum.setValue(spectrumMath.computeSpectrum(originalSpectrum.getValue()));
    }

    public void setLabel(String label) {
        this.label.setValue(label);
    }
    public void setId(int id){
        this.id.setValue(id);
    }
    public ObservableValue<Integer> getId() {
        return new SimpleObjectProperty<Integer>(id.getValue());
    }
    public Spectrum getSpectrum(){
        return spectrum.getValue();
    }
    public Spectrum getOriginalSpectrum(){
        return originalSpectrum.getValue();
    }
    public ObservableValue<String> getLabel(){
        return label;
    }
    public String getMeasurementMode() {
        return measurementMode;
    }
    public boolean isEmissive(){
        return isEmissive;
    }
    public void setMeasurementMode(String measurementMode){ this.measurementMode = measurementMode; }

    public String getInstrumentType() {
        return instrumentType;
    }

    public String getInstrumentSerialNumber() {
        return instrumentSerialNumber;
    }
    public String getInstrumentTypeAndSerialNumber(){
        return getInstrumentType()+" ("+getInstrumentSerialNumber()+")";
    }
    public ObservableValue<Float> getX(){

        return new SimpleObjectProperty<Float>(getSpectrumConverter().getX());
    }
    public ObservableValue<Float> getY(){
        return new SimpleObjectProperty<Float>(getSpectrumConverter().getY());
    }
    public ObservableValue<Float> getZ(){
        return new SimpleObjectProperty<Float>(getSpectrumConverter().getZ());
    }
    public ObservableValue<Double> getL(){
        return new SimpleObjectProperty<Double>(getSpectrumConverter().getL());
    }
    public ObservableValue<Double> getA(){
        return new SimpleObjectProperty<Double>(getSpectrumConverter().getA());
    }
    public ObservableValue<Double> getB(){
        return new SimpleObjectProperty<Double>(getSpectrumConverter().getB());
    }
    public ObservableValue<Character> getFlag(){
        return new SimpleObjectProperty<Character>(' ');
    }

    public SpectrumConverter getSpectrumConverter(){
        return spectrumConverter;
    }
}

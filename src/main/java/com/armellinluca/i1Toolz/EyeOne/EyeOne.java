package com.armellinluca.i1Toolz.EyeOne;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.sql.Timestamp;
import java.util.TreeMap;

public class EyeOne extends Instrument{
    public static final String LAST_CALIBRATION_TIME = "LastCalibrationTime";
    public static final String LAST_CALIBRATION_COUNTER = "LastCalibrationCounter";
    public static final String IS_CONNECTED = "Connection";
    public static final String RECOGNITION_ENABLED = "Recognition";
    public static final String IS_KEY_PRESSED = "IsKeyPressed";

    private static final int MESSAGE_BUTTON_PRESSED = 0;
    private static final int MESSAGE_DEVICE_DISCONNECTED = 2;

    private static final int TRIGGER_STATE_NO_ERRORS = 0;
    private static final int TRIGGER_STATE_NOT_CALIBRATED = 3;
    private static final int TRIGGER_STATE_SATURATED = 3;

    private static final int CALIBRATE_STATE_NO_ERRORS = 0;
    private static final int CALIBRATE_STATE_CALIBRATION_FAILED = 3;

    private static final int OPTIONS_STATE_UNKNOWN_OPTION = 10;

    private static final String ERROR_CALIBRATION_FAILED = "Dark calculation: Data is not valid";
    private static final String ERROR_EMISSION_NOT_CALIBRATED = "Sample calculation: Data not valid";
    private static final String ERROR_EMISSION_SENSOR_SATURATED = "Sample calculation: Sensor is saturated";
    private static final String ERROR_REFLECTANCE_CALIBRATION_FAILED = "New integration time: No light";
    private static final String ERROR_REFLECTANCE_NOT_CALIBRATED = "Sample calculation: White data not valid";

    public static final String LAST_ERROR = "LastError";

    public static final String MODE_SINGLE_REFLECTANCE = "SingleReflectance";
    public static final String MODE_SINGLE_EMISSION = "SingleEmission";
    public static final String MODE_SCANNING_REFLECTANCE = "ScanningReflectance";
    public static final String MODE_SINGLE_AMBIENT_LIGHT = "SingleAmbientLight";
    public static final String MODE_SCANNING_AMBIENT_LIGHT = "ScanningAmbientLight";

    private final EyeOneDLL eyeOne;

    public EyeOne(){
        super();
        System.setProperty("jna.platform.library.path", "C:\\Windows\\system32");
        eyeOne = Native.load("eyeone.dll", EyeOneDLL.class);
        initHandlers();
    }

    public synchronized boolean isConnected(){
        return eyeOne.I1_IsConnected()==0;
    }

    public synchronized boolean keyPressed(){
        return eyeOne.I1_KeyPressed()==0;
    }

    protected synchronized int calibrate(){
        return eyeOne.I1_Calibrate();
    }

    protected synchronized int measure(){
        return eyeOne.I1_TriggerMeasurement();
    }

    public synchronized String getRawOption(String option){
        return eyeOne.I1_GetOption(option);
    }

    public synchronized int setRawOption(String option, String value){
        return eyeOne.I1_SetOption(option, value);
    }

    public synchronized TreeMap<Integer, Float> getRawSpectrum(){
        TreeMap<Integer, Float> spectra = new TreeMap<>();
        long size = (MAX_WL-MIN_WL)/10+1;
        Pointer p = new Memory(size*Native.getNativeSize(float.class));
        eyeOne.I1_GetSpectrum(p, 0L);
        for(int i=0;i<size;i++)
            spectra.put(MIN_WL+STEP_WL*i,p.getFloat(i*4L));
        return spectra;
    }

    @Override
    public void disconnect() {}

    @Override
    protected void registerMainDeviceMessageHandler(MainMessageHandlerCallbacks c) {
        eyeOne.I1_RegisterDeviceMessageHandler(c);
    }

    @Override
    protected void registerMainDeviceErrorHandler(MainErrorHandlerCallbacks c) {
        eyeOne.I1_RegisterErrorHandler(c);
    }

    @Override
    protected void removeMainDeviceErrorHandler(MainErrorHandlerCallbacks c) {
        // not supported
    }

    @Override
    protected void removeMainDeviceMessageHandler(MainMessageHandlerCallbacks c) {
        // not supported
    }

    @Override
    public String getLastCalibrationDate(){
        return new Timestamp(System.currentTimeMillis()-Integer.parseInt(getOption(EyeOne.LAST_CALIBRATION_TIME))*1000).toString();
    }

    @Override
    public boolean isReflectiveMode(String mode) {
        return mode.equals(MODE_SINGLE_REFLECTANCE) || mode.equals(MODE_SCANNING_REFLECTANCE);
    }

    @Override
    public boolean isEmissiveMode(String mode) {
        return mode.equals(MODE_SINGLE_AMBIENT_LIGHT) || mode.equals(MODE_SINGLE_EMISSION) || mode.equals(MODE_SCANNING_AMBIENT_LIGHT);
    }

    @Override
    public String getMeasurementMode() {
        return getOption(MEASUREMENT_MODE);
    }

    @Override
    protected boolean MESSAGE_isKeyPressed(int msg) {
        return msg == MESSAGE_BUTTON_PRESSED;
    }

    @Override
    protected boolean MESSAGE_isDeviceDisconnected(int msg) {
        return msg == MESSAGE_DEVICE_DISCONNECTED;
    }

    @Override
    protected boolean ERROR_deviceSaturated(String msg, int e) {
        return e==TRIGGER_STATE_SATURATED && msg.equals(ERROR_EMISSION_SENSOR_SATURATED);
    }

    @Override
    protected boolean ERROR_notCalibrated(String msg, int e) {
        return e==TRIGGER_STATE_NOT_CALIBRATED && (
                msg.equals(ERROR_EMISSION_NOT_CALIBRATED) ||
                        msg.equals(ERROR_REFLECTANCE_NOT_CALIBRATED)
        );
    }

    @Override
    protected boolean ERROR_calibrationFailed(String msg, int e) {
        return e==CALIBRATE_STATE_CALIBRATION_FAILED && (
                msg.equals(ERROR_CALIBRATION_FAILED) ||
                        msg.equals(ERROR_REFLECTANCE_CALIBRATION_FAILED)
        );
    }

    @Override
    public String getLastError() {
        return getOption(LAST_ERROR);
    }
}

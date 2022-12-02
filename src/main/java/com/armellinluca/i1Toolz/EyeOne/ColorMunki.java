package com.armellinluca.i1Toolz.EyeOne;

import com.sun.jna.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ColorMunki extends Instrument{
    public static final String SENSOR_POSITION = "SensorPosition";

    private static final int MESSAGE_BUTTON_PRESSED = 0;
    private static final int MESSAGE_SENSOR_POSITION_CHANGED = 2;
    private static final int MESSAGE_DEVICE_DISCONNECTED = 3;

    private static final int TRIGGER_STATE_NO_ERRORS = 0;
    private static final int TRIGGER_STATE_NOT_CALIBRATED = 6;
    private static final int TRIGGER_STATE_WRONG_POSITION = 7;
    private static final int TRIGGER_STATE_SATURATED = 15;

    private static final int CALIBRATE_STATE_NO_ERRORS = 0;
    private static final int CALIBRATE_STATE_CALIBRATION_FAILED = 7;

    public static final String MODE_SINGLE_REFLECTANCE = "SingleReflectance";
    public static final String MODE_SINGLE_EMISSION = "SingleEmission";
    public static final String MODE_SCANNING_REFLECTANCE = "ScanningReflectance";
    public static final String MODE_SINGLE_AMBIENT_LIGHT = "SingleAmbientLight";

    private final ColorMunkiHelperDLL colorMunki;

    private DeviceMessageHandler sensorPositionChangedCallbacks;

    public ColorMunki(){
        super();

        System.setProperty("jna.platform.library.path", "C:\\Program Files (x86)\\X-Rite\\i1Diagnostics 4\\");
        System.setProperty("jna.platform.library.path", "C:\\Windows\\SysWOW64");
        final Map<String, Object> options = new HashMap<>();
        options.put(Library.OPTION_INVOCATION_MAPPER, (InvocationMapper) (lib, m) -> {
            if (m.getName().equals("dispose")) {
                return (proxy, method, args) -> {
                    lib.dispose();
                    return null;
                };
            }
            return null;
        });
        colorMunki = Native.load("libColorMunkiHelperDLL.dll", ColorMunkiHelperDLL.class, options);

        initHandlers();
    }

    @Override
    protected void initHandlers(){
        super.initHandlers();
        sensorPositionChangedCallbacks = new DeviceMessageHandler((msg)->msg==MESSAGE_SENSOR_POSITION_CHANGED);
        super.addDeviceMessageHandler(sensorPositionChangedCallbacks);
    }

    @Override
    public boolean isConnected() {
        return colorMunki.IsConnected()==0;
    }

    @Override
    public boolean keyPressed() {
        return colorMunki.ButtonPressed()==1000;
    }

    @Override
    protected int calibrate() {
        return colorMunki.Calibrate();
    }

    @Override
    protected int measure() {
        return colorMunki.TriggerMeasurement();
    }

    @Override
    public String getRawOption(String option) {
        return colorMunki.GetOption(option);
    }

    @Override
    public int setRawOption(String option, String value) {
        return colorMunki.SetOption(option, value);
    }

    @Override
    public TreeMap<Integer, Float> getRawSpectrum() {
        TreeMap<Integer, Float> spectra = new TreeMap<>();
        long size = 50;
        Pointer p = new Memory(size*Native.getNativeSize(float.class));
        colorMunki.GetSpectrum(p, 0L);
        for(int i=0;i<36;i++)
            spectra.put(MIN_WL+STEP_WL*i,p.getFloat(i*4L));
        return spectra;
    }

    @Override
    protected void registerMainDeviceMessageHandler(MainMessageHandlerCallbacks c) {
        colorMunki.RegisterDeviceMessageHandler(c);
    }

    @Override
    protected void registerMainDeviceErrorHandler(MainErrorHandlerCallbacks c) {
        colorMunki.RegisterErrorHandler(c);
    }

    @Override
    protected void removeMainDeviceErrorHandler(MainErrorHandlerCallbacks c) {
        colorMunki.RemoveErrorHandler(c);
    }

    @Override
    protected void removeMainDeviceMessageHandler(MainMessageHandlerCallbacks c) {
        colorMunki.RemoveDeviceMessageHandler(c);
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
        return e == TRIGGER_STATE_SATURATED;
    }

    @Override
    protected boolean ERROR_notCalibrated(String msg, int e) {
        return e == TRIGGER_STATE_NOT_CALIBRATED;
    }

    @Override
    protected boolean ERROR_calibrationFailed(String msg, int e) {
        return e == CALIBRATE_STATE_CALIBRATION_FAILED;
    }

    @Override
    public String getLastError() {
        return getOption(LAST_ERROR);
    }

    public DeviceMessageHandler sensorPositionChangedHandlers(){
        return sensorPositionChangedCallbacks;
    }

    @Override
    public void disconnect(){
        colorMunki.dispose();
    }

    @Override
    public String getLastCalibrationDate() {
        return "N/A on Munki devices";
    }

    @Override
    public boolean isReflectiveMode(String mode) {
        return mode.equals(MODE_SINGLE_REFLECTANCE) || mode.equals(MODE_SCANNING_REFLECTANCE);
    }

    @Override
    public boolean isEmissiveMode(String mode) {
        return mode.equals(MODE_SINGLE_AMBIENT_LIGHT) || mode.equals(MODE_SINGLE_EMISSION);
    }

    @Override
    public String getMeasurementMode() {
        return getOption(MEASUREMENT_MODE);
    }

    public ColorMunkiHelperDLL getDll(){
        return colorMunki;
    }
}
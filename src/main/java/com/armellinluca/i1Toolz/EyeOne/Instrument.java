package com.armellinluca.i1Toolz.EyeOne;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.jna.Callback;

public abstract class Instrument {
    public static final int MIN_WL = 380;
    public static final int MAX_WL = 730;
    public static final int STEP_WL = 10;
    public static final String YES = "yes";
    public static final String NO = "no";
    public static final String CALIBRATED = "0";

    public static final String LAST_ERROR = "LastError";
    public static final String EXTENDED_ERROR_INFORMATION = "ExtendedErrorInformation";
    public static final String DEVICE_TYPE = "DeviceType";
    public static final String VERSION = "Version";
    public static final String MAJOR_VERSION = "MajorVersion";
    public static final String MINOR_VERSION = "MinorVersion";
    public static final String BUILD_VERSION = "BuildVersion";
    public static final String REVISION_VERSION = "RevisionVersion";
    public static final String SERIAL_NUMBER = "SerialNumber";
    public static final String AVAILABLE_MEASUREMENT_MODES = "AvailableMeasurementModes";
    public static final String MEASUREMENT_MODE = "MeasurementMode";
    public static final String INTEGRATION_TIME = "IntegrationTime";

    public static final String MODE_SINGLE_EMISSION = "SingleEmission";
    public static final String MODE_SINGLE_REFLECTANCE = "SingleReflectance";
    public static final String MODE_SCANNING_REFLECTANCE = "ScanningReflectance";
    public static final String MODE_SINGLE_AMBIENT_LIGHT = "SingleAmbientLight";
    public static final String MODE_SCANNING_AMBIENT_LIGHT = "ScanningAmbientLight";

    public static final int DEVICE_STATE_OK = 0;
    public static final int DEVICE_STATE_CALIBRATION_FAILED = 1;
    public static final int DEVICE_STATE_NOT_CALIBRATED = 2;
    public static final int DEVICE_STATE_SATURATED = 3;
    public static final int DEVICE_STATE_UNKNOWN_ERROR = 20;

    private static TreeMap<Integer, Float> transmissionRefSpectrum = null;
    private boolean isTransmissive = false;

    private int lastDeviceState = 0;

    boolean calibrated = false;
    private static final boolean stopWaiting = false;

    private final MainMessageHandlerCallbacks mainMessageHandlerCallbacks;
    private final MainErrorHandlerCallbacks mainErrorHandlerCallbacks;

    private final DeviceMessageHandler buttonClickedCallbacks;
    private final DeviceMessageHandler deviceDisconnectedCallbacks;

    public Instrument(){
        buttonClickedCallbacks = new DeviceMessageHandler(this::MESSAGE_isKeyPressed);
        deviceDisconnectedCallbacks = new DeviceMessageHandler(this::MESSAGE_isDeviceDisconnected);
        mainMessageHandlerCallbacks = new MainMessageHandlerCallbacks();
        mainErrorHandlerCallbacks = new MainErrorHandlerCallbacks();

        mainMessageHandlerCallbacks.add(buttonClickedCallbacks);
        mainMessageHandlerCallbacks.add(deviceDisconnectedCallbacks);
    }

    protected void initHandlers(){
        registerMainDeviceMessageHandler(mainMessageHandlerCallbacks);
        registerMainDeviceErrorHandler(mainErrorHandlerCallbacks);
    }

    /**
     * Method must pass the MainMessageHandlerCallbacks c to the DLL's
     * RegisterDeviceMessageHandler procedure
     * */
    protected abstract void registerMainDeviceMessageHandler(MainMessageHandlerCallbacks c);

    /**
     * Method must pass the MainErrorHandlerCallbacks c to the DLL's
     * RegisterErrorHandler procedure
     * */
    protected abstract void registerMainDeviceErrorHandler(MainErrorHandlerCallbacks c);

    /**
     * Method must pass the MainMessageHandlerCallbacks c to the DLL's
     * RegisterDeviceMessageHandler procedure
     * */
    protected abstract void removeMainDeviceMessageHandler(MainMessageHandlerCallbacks c);

    /**
     * Method must pass the MainErrorHandlerCallbacks c to the DLL's
     * RegisterErrorHandler procedure
     * */
    protected abstract void removeMainDeviceErrorHandler(MainErrorHandlerCallbacks c);

    /**
     * Method must return TRUE if the number of the message corresponds to the
     * button on the instrument being pressed and FALSE otherwise
     * */
    protected abstract boolean MESSAGE_isKeyPressed(int msg);
    public DeviceMessageHandler buttonPressedHandlers(){
        return buttonClickedCallbacks;
    }

    /**
     * Method must return TRUE if the number of the message corresponds to the
     * device being disconnected and FALSE otherwise
     * */
    protected abstract boolean MESSAGE_isDeviceDisconnected(int msg);
    public DeviceMessageHandler deviceDisconnectedHandlers(){
        return deviceDisconnectedCallbacks;
    }

    /**
     * Method, given the LastError message msg and the code e returned by the called function,
     * must return TRUE if device was saturated, FALSE otherwise
     * */
    protected abstract boolean ERROR_deviceSaturated(String msg, int e);

    /**
     * Method, given the LastError message msg and the code e returned by the called function,
     * must return TRUE if device was not calibrated, FALSE otherwise
     * */
    protected abstract boolean ERROR_notCalibrated(String msg, int e);

    /**
     * Method, given the LastError message msg and the code e returned by the called function,
     * must return TRUE if device calibration procedure failed, FALSE otherwise
     * */
    protected abstract boolean ERROR_calibrationFailed(String msg, int e);

    /**
     * Adds a new DeviceMessageHandler
     * */
    protected void addDeviceMessageHandler(DeviceMessageHandler deviceMessageHandler){
        mainMessageHandlerCallbacks.add(deviceMessageHandler);
    }

    /**
     * Removes a new DeviceMessageHandler
     * */
    protected void removeDeviceMessageHandler(DeviceMessageHandler deviceMessageHandler){
        mainMessageHandlerCallbacks.remove(deviceMessageHandler);
    }

    /**
     * Adds a new DeviceErrorHandler
     * */
    protected void addErrorHandler(DeviceErrorHandler deviceErrorHandler){
        mainErrorHandlerCallbacks.add(deviceErrorHandler);
    }

    /**
     * Removes a new DeviceErrorHandler
     * */
    protected void removeErrorHandler(DeviceErrorHandler deviceErrorHandler){
        mainErrorHandlerCallbacks.remove(deviceErrorHandler);
    }

    public void removeAllHandlers(){
        removeMainDeviceErrorHandler(mainErrorHandlerCallbacks);
        removeMainDeviceMessageHandler(mainMessageHandlerCallbacks);
    }

    /**
     * Method, taking the LastError message and given the code returned from the called function,
     * returns the DEVICE STATE.
     * */
    public int getDeviceState(int returnCode){
        if(returnCode == 0) {
            lastDeviceState = DEVICE_STATE_OK;
            calibrated = true;
        }
        else if(ERROR_calibrationFailed(getLastError(), returnCode))
            lastDeviceState = DEVICE_STATE_CALIBRATION_FAILED;
        else if(ERROR_notCalibrated(getLastError(), returnCode))
            lastDeviceState = DEVICE_STATE_NOT_CALIBRATED;
        else if(ERROR_deviceSaturated(getLastError(), returnCode))
            lastDeviceState = DEVICE_STATE_SATURATED;
        else
            lastDeviceState = DEVICE_STATE_UNKNOWN_ERROR+returnCode;

        if(lastDeviceState == DEVICE_STATE_CALIBRATION_FAILED || lastDeviceState == DEVICE_STATE_NOT_CALIBRATED)
            calibrated = false;

        return lastDeviceState;
    }

    /**
     * Method returns the device state at the last time the getDeviceState(int) method was
     * invoked.
     * */
    public int getLastDeviceState(){
        return lastDeviceState;
    }

    public boolean isCalibrated(){
        return calibrated;
    }

    public int triggerCalibration(){
        int result = getDeviceState(calibrate());
        if(result == DEVICE_STATE_OK)
            calibrated = true;
        return result;
    }

    public int triggerMeasurement(){
        return getDeviceState(measure());
    }

    public boolean isReflectiveMode(){
        if(isTransmissiveMode())
            return false;
        return isReflectiveMode(getMeasurementMode());
    }
    public boolean isEmissiveMode(){
        if(isTransmissiveMode())
            return false;
        return isEmissiveMode(getMeasurementMode());
    }
    public boolean isTransmissiveMode(){
        return isTransmissive;
    }

    public int calibrateTransmission(){
        int ret = DEVICE_STATE_OK;
        measure();
        TreeMap<Integer, Float> rawSpectrum = getRawSpectrum();
        for (Map.Entry<Integer, Float> entry : rawSpectrum.entrySet())
            if (entry.getValue() <= 0) {
                ret = DEVICE_STATE_CALIBRATION_FAILED;
                break;
            }
        if(ret == DEVICE_STATE_OK) {
            transmissionRefSpectrum = rawSpectrum;
            isTransmissive = true;
        } else {
            transmissionRefSpectrum = null;
            isTransmissive = false;
        }
        return ret;
    }

    public void setTransmissionMode(boolean state){
        isTransmissive = state;
        if(isTransmissive){
            setRawOption(MEASUREMENT_MODE, MODE_SINGLE_EMISSION);
        }
    }

    public TreeMap<Integer, Float> getSpectrum(){
        TreeMap<Integer, Float> rawSpectrum = getRawSpectrum();
        if(!isTransmissive || transmissionRefSpectrum == null)
            return rawSpectrum;
        TreeMap<Integer, Float> transmissionSpectrum = new TreeMap<>();
        for(Map.Entry<Integer, Float> entry : rawSpectrum.entrySet())
            transmissionSpectrum.put(entry.getKey(),
                    transmissionRefSpectrum.get(entry.getKey()) == 0 ?
                            0 : entry.getValue()/transmissionRefSpectrum.get(entry.getKey())
            );
        return transmissionSpectrum;
    }

    public int setOption(String option, String value){
        if(option.equals(MEASUREMENT_MODE)){
            transmissionRefSpectrum = null;
            isTransmissive = false;
        }
        return setRawOption(option, value);
    }

    public String getOption(String option){
        if(option.equals(MEASUREMENT_MODE) && isTransmissive){
            return "Transmission";
        }
        return getRawOption(option);
    }

    public abstract String getLastError();
    public abstract boolean isConnected();
    public abstract boolean keyPressed();
    protected abstract int calibrate();
    protected abstract int measure();
    public abstract String getRawOption(String option);
    public abstract int setRawOption(String option, String value);
    public abstract TreeMap<Integer, Float> getRawSpectrum();
    public abstract void disconnect();
    public abstract String getLastCalibrationDate();
    public abstract boolean isReflectiveMode(String mode);
    public abstract boolean isEmissiveMode(String mode);
    public abstract String getMeasurementMode();
}


/**
 * Since older devices only support one single DeviceMessageHandler, registered
 * via the RegisterDeviceMessageHandler procedure, a single MainMessageHandlerCallbacks
 * object allows for different DeviceMessageHandler objects to be registered
 * and invoked whenever the device generates a message.
 * */
class MainMessageHandlerCallbacks implements Callback {
    protected List<DeviceMessageHandler> callbacks = new ArrayList<>();

    public void callback(int msg){
        callbacks.forEach((callback -> callback.run(msg)));
    }

    public void add(DeviceMessageHandler callback){
        callbacks.add(callback);
    }

    public void remove(DeviceMessageHandler callback){
        callbacks.remove(callback);
    }

    public void clearAll(){
        callbacks.forEach(c -> c.clearAll());
    }
}

/**
 * Since older devices only support one single ErrorHandler, registered
 * via the RegisterErrorHandler procedure, a single MainErrorHandlerCallbacks
 * object allows for different DeviceErrorHandler objects to be registered
 * and invoked whenever the device generates a message.
 * */
class MainErrorHandlerCallbacks implements Callback {
    protected List<DeviceErrorHandler> callbacks = new ArrayList<>();

    public void callback(String extendedMessage, int msg){
        callbacks.forEach((callback -> callback.run(extendedMessage, msg)));
    }

    public void add(DeviceErrorHandler callback){
        callbacks.add(callback);
    }
    public void remove(DeviceErrorHandler callback){
        callbacks.remove(callback);
    }

    public void clearAll(){
        callbacks.forEach(c -> c.clearAll());
    }
}
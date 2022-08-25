package EyeOne;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface ColorMunkiDLL extends Library {
    int MUNKI_IsConnected();
    int MUNKI_ButtonPressed();
    int MUNKI_Calibrate();
    int MUNKI_TriggerMeasurement();
    String MUNKI_GetOption(String option);
    int MUNKI_SetOption(String option, String value);
    int MUNKI_GetSpectrum(Pointer spectrum, long val);
    int MUNKI_GetSensorPosition();
    int MUNKI_RegisterDeviceMessageHandler(Callback func);
    int MUNKI_RegisterErrorHandler(Callback func);
    int MUNKI_RemoveDeviceMessageHandler(Callback func);
    int MUNKI_RemoveErrorHandler(Callback func);
    void dispose();
}

